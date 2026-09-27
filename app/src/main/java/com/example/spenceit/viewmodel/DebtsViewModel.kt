package com.example.spenceit.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow



data class ActiveDebt(
    val debtId: String,
    val otherPersonUid: String,
    val otherPersonName: String,
    val amount: Double,
    val description: String,
    val iOweThem: Boolean,
    val status: String,
    val settlementNote: String? = null,
    val timestamp: Long = 0L // <-- ADD THIS LINE
)

class DebtsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _saveStatus = MutableStateFlow<String>("")
    val saveStatus: StateFlow<String> = _saveStatus.asStateFlow()

    private val _activeDebts = MutableStateFlow<List<ActiveDebt>>(emptyList())
    val activeDebts: StateFlow<List<ActiveDebt>> = _activeDebts.asStateFlow()

    private var authStateListener: FirebaseAuth.AuthStateListener? = null
    private var snapshotListener: ListenerRegistration? = null

    // 1. THIS PREVENTS THE GHOST UI RACE CONDITION
    private var listenerCallCount = 0

    init {
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            snapshotListener?.remove()
            _activeDebts.value = emptyList()

            if (user != null) {
                listenForMyDebts(user.uid)
            }
        }
        auth.addAuthStateListener(authStateListener!!)
    }

    private fun listenForMyDebts(myUid: String) {
        snapshotListener = db.collection("debts")
            .where(Filter.or(
                Filter.equalTo("lenderUid", myUid),
                Filter.equalTo("borrowerUid", myUid)
            ))
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                // Track the exact call ID of this snapshot
                listenerCallCount++
                val currentCallId = listenerCallCount

                val docs = snapshot.documents


                if (docs.isEmpty()) {
                    if (currentCallId == listenerCallCount) _activeDebts.value = emptyList()
                    return@addSnapshotListener
                }

                val debtsList = mutableListOf<ActiveDebt>()
                var profilesToLoad = 0

                docs.forEach { doc ->
                    val status = doc.getString("status") ?: "active"

                    if (status == "settled") {
                        // Check if the 3-day timer has expired
                        val expireDate = doc.getDate("expireAt")
                        if (expireDate != null && expireDate.time < System.currentTimeMillis()) {
                            // The 3 days are up! Delete it permanently from Firestore.
                            db.collection("debts").document(doc.id).delete()

                            // Skip processing this document so it doesn't show up in the UI
                            return@forEach
                        }
                    }

                    val lender = doc.getString("lenderUid") ?: ""
                    val borrower = doc.getString("borrowerUid") ?: ""
                    val amount = doc.getDouble("amount") ?: 0.0
                    val desc = doc.getString("description") ?: ""
                    val note = doc.getString("settlementNote")
                    val timestamp = doc.getLong("timestamp") ?: 0L



                    val iOweThem = myUid == borrower
                    val otherUid = if (iOweThem) lender else borrower

                    db.collection("users").document(otherUid).get()
                        .addOnSuccessListener { userDoc ->
                            if (currentCallId == listenerCallCount) {
                                val otherName = userDoc.getString("name") ?: "Unknown User"
                                debtsList.add(ActiveDebt(doc.id, otherUid, otherName, amount, desc, iOweThem, status, note, timestamp))
                                profilesToLoad++

                                if (profilesToLoad == docs.size) {
                                    _activeDebts.value = debtsList.toList()
                                }
                            }
                        }
                }
            }
    }

    fun createDebt(targetFriendUid: String, amountText: String, description: String, iAmLender: Boolean) {
        val myUid = auth.currentUser?.uid ?: return
        val amount = amountText.toDoubleOrNull() ?: return
        _saveStatus.value = "Saving..."
        val lender = if (iAmLender) myUid else targetFriendUid
        val borrower = if (iAmLender) targetFriendUid else myUid
        val debtRecord = hashMapOf("lenderUid" to lender, "borrowerUid" to borrower, "amount" to amount, "description" to description, "status" to "active", "timestamp" to System.currentTimeMillis())
        db.collection("debts").add(debtRecord)
            .addOnSuccessListener { _saveStatus.value = "Success! Debt saved." }
            .addOnFailureListener { e -> _saveStatus.value = "Failed to save: ${e.message}" }
    }

    fun updateDebtStatus(debtId: String, newStatus: String, note: String? = null) {
        val updates = mutableMapOf<String, Any>("status" to newStatus)
        if (note != null) updates["settlementNote"] = note
        else if (newStatus == "active") updates["settlementNote"] = FieldValue.delete()

        // NEW: Stamp the execution date for Firestore auto-deletion (3 days = 3 * 24 * 60 * 60 * 1000 ms)
        if (newStatus == "settled") {
            updates["expireAt"] = java.util.Date(System.currentTimeMillis() + (3L * 24 * 60 * 60 * 1000))
        }

        db.collection("debts").document(debtId).update(updates)
    }

    fun sendPaymentRequest(friendUid: String, amountPaidText: String, note: String) {
        val myUid = auth.currentUser?.uid ?: return
        val amountPaid = amountPaidText.toDoubleOrNull() ?: 0.0
        if (amountPaid <= 0) return

        val paymentRecord = hashMapOf(
            "lenderUid" to friendUid,
            "borrowerUid" to myUid,
            "amount" to amountPaid,
            "description" to "[PAYMENT] $note",
            "status" to "pending_approval",
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("debts").add(paymentRecord)
    }

    // 3. Upgraded Pure Signed Math
    fun acceptDebt(debt: ActiveDebt) {
        val myUid = auth.currentUser?.uid ?: return

        if (debt.description.startsWith("[PAYMENT]")) {
            val friendUid = debt.otherPersonUid

            // Grab ALL active AND pending debts, EXCEPT the payment currently being accepted
            val sharedDebts = _activeDebts.value.filter {
                it.otherPersonUid == friendUid &&
                        it.debtId != debt.debtId &&
                        (it.status == "active" || it.status == "pending_approval")
            }

            var netBalance = 0.0
            sharedDebts.forEach { oldDebt ->
                if (oldDebt.description.startsWith("[PAYMENT]")) {
                    if (oldDebt.iOweThem) netBalance += oldDebt.amount else netBalance -= oldDebt.amount
                } else {
                    if (oldDebt.iOweThem) netBalance -= oldDebt.amount else netBalance += oldDebt.amount
                }
            }

            val expirationTime = java.util.Date(System.currentTimeMillis() + (3L * 24 * 60 * 60 * 1000))
            sharedDebts.forEach { oldDebt ->
                db.collection("debts").document(oldDebt.debtId).update(
                    "status", "settled",
                    "expireAt", expirationTime
                )
            }
            db.collection("debts").document(debt.debtId).update(
                "status", "settled",
                "expireAt", expirationTime
            )

            // Pure signed math. Drops the balance cleanly without inverting.
            val newBalance = netBalance - debt.amount

            if (Math.abs(newBalance) > 0.01) {
                val iOweThemNew = newBalance < 0
                val remainingRecord = hashMapOf(
                    "lenderUid" to if (iOweThemNew) friendUid else myUid,
                    "borrowerUid" to if (iOweThemNew) myUid else friendUid,
                    "amount" to (Math.round(Math.abs(newBalance) * 100.0) / 100.0),
                    "description" to "Remaining Balance",
                    "status" to "active",
                    "timestamp" to System.currentTimeMillis()
                )
                db.collection("debts").add(remainingRecord)
            }
        } else {
            updateDebtStatus(debt.debtId, "settled")
        }
    }

    fun rejectDebt(debt: ActiveDebt) {
        if (debt.description.startsWith("[PAYMENT]")) db.collection("debts").document(debt.debtId).delete()
        else updateDebtStatus(debt.debtId, "active")
    }

    fun resetStatus() { _saveStatus.value = "" }

    override fun onCleared() {
        super.onCleared()
        snapshotListener?.remove()
        authStateListener?.let { auth.removeAuthStateListener(it) }
    }
}