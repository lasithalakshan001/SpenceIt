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

// 1. Added settlementNote to the data model
data class ActiveDebt(
    val debtId: String,
    val otherPersonUid: String,
    val otherPersonName: String,
    val amount: Double,
    val description: String,
    val iOweThem: Boolean,
    val status: String,
    val settlementNote: String? = null
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

                val docs = snapshot.documents
                val activeDocs = docs.filter { it.getString("status") != "settled" }

                if (activeDocs.isEmpty()) {
                    _activeDebts.value = emptyList()
                    return@addSnapshotListener
                }

                val debtsList = mutableListOf<ActiveDebt>()
                var profilesToLoad = 0

                activeDocs.forEach { doc ->
                    val lender = doc.getString("lenderUid") ?: ""
                    val borrower = doc.getString("borrowerUid") ?: ""
                    val amount = doc.getDouble("amount") ?: 0.0
                    val desc = doc.getString("description") ?: ""
                    val status = doc.getString("status") ?: "active"
                    val note = doc.getString("settlementNote") // 2. Extract the note

                    val iOweThem = myUid == borrower
                    val otherUid = if (iOweThem) lender else borrower

                    db.collection("users").document(otherUid).get()
                        .addOnSuccessListener { userDoc ->
                            val otherName = userDoc.getString("name") ?: "Unknown User"

                            // 3. Pass the note into the UI object
                            // 2. Pass the otherUid into the object
                            debtsList.add(ActiveDebt(doc.id, otherUid, otherName, amount, desc, iOweThem, status, note))
                            profilesToLoad++

                            if (profilesToLoad == activeDocs.size) {
                                _activeDebts.value = debtsList.toList()
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

        val debtRecord = hashMapOf(
            "lenderUid" to lender,
            "borrowerUid" to borrower,
            "amount" to amount,
            "description" to description,
            "status" to "active",
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("debts").add(debtRecord)
            .addOnSuccessListener { _saveStatus.value = "Success! Debt saved." }
            .addOnFailureListener { e -> _saveStatus.value = "Failed to save: ${e.message}" }
    }

    // 4. Upgraded status function to handle the note and clearing the note if rejected
    fun updateDebtStatus(debtId: String, newStatus: String, note: String? = null) {
        val updates = mutableMapOf<String, Any>("status" to newStatus)

        if (note != null) {
            updates["settlementNote"] = note
        } else if (newStatus == "active") {
            // If the lender rejects the payment, we delete the old note
            updates["settlementNote"] = FieldValue.delete()
        }

        db.collection("debts").document(debtId).update(updates)
    }
    fun consolidateAndPay(friendUid: String, amountPaidText: String, note: String) {
        val myUid = auth.currentUser?.uid ?: return
        val amountPaid = amountPaidText.toDoubleOrNull() ?: 0.0

        // 1. Grab all the old messy debts between you and this friend
        val sharedDebts = _activeDebts.value.filter { it.otherPersonUid == friendUid && it.status == "active" }
        if (sharedDebts.isEmpty()) return

        // 2. Calculate what the real Net Balance was
        var netBalance = 0.0
        sharedDebts.forEach {
            if (it.iOweThem) netBalance -= it.amount
            else netBalance += it.amount
        }

        // 3. CLEANUP: Archive all old debts so they disappear from the screen!
        sharedDebts.forEach { debt ->
            db.collection("debts").document(debt.debtId).update("status", "settled")
        }

        // 4. Create the custom payment request that your friend has to approve
        if (amountPaid > 0) {
            val paymentRecord = hashMapOf(
                "lenderUid" to myUid, // You are handing them money
                "borrowerUid" to friendUid,
                "amount" to amountPaid,
                "description" to "Payment: $note",
                "status" to "pending_approval", // Waits for them to accept it
                "timestamp" to System.currentTimeMillis()
            )
            db.collection("debts").add(paymentRecord)
        }

        // 5. Generate exactly ONE new record for whatever balance is left over
        // (e.g. You owed 1000, you paid 400. You still owe 600).
        val newBalance = netBalance + amountPaid

        // Only create a remaining record if the math didn't perfectly hit zero
        if (Math.abs(newBalance) > 0.01) {
            val iOweThemNew = newBalance < 0
            val remainingRecord = hashMapOf(
                "lenderUid" to if (iOweThemNew) friendUid else myUid,
                "borrowerUid" to if (iOweThemNew) myUid else friendUid,
                "amount" to (Math.round(Math.abs(newBalance) * 100.0) / 100.0), // Round to 2 decimals
                "description" to "Consolidated Remaining Balance",
                "status" to "active",
                "timestamp" to System.currentTimeMillis() + 1000 // Add 1 second so it sorts correctly
            )
            db.collection("debts").add(remainingRecord)
        }
    }
    // 1. Send the payment request WITHOUT touching the old debts yet
    fun sendPaymentRequest(friendUid: String, amountPaidText: String, note: String) {
        val myUid = auth.currentUser?.uid ?: return
        val amountPaid = amountPaidText.toDoubleOrNull() ?: 0.0

        if (amountPaid <= 0) return

        // Because YOU owe the money, your friend is the Lender.
        // Setting them as the Lender guarantees they will see the Accept/Reject buttons on their screen!
        val paymentRecord = hashMapOf(
            "lenderUid" to friendUid,
            "borrowerUid" to myUid,
            "amount" to amountPaid,
            "description" to "[PAYMENT] $note", // We tag it so the system recognizes it later
            "status" to "pending_approval",
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("debts").add(paymentRecord)
    }

    // 2. Intercept the Accept button to run the sweep
    fun acceptDebt(debt: ActiveDebt) {
        val myUid = auth.currentUser?.uid ?: return

        // Check if this is a custom payment that needs math, or just a standard debt
        if (debt.description.startsWith("[PAYMENT]")) {
            val friendUid = debt.otherPersonUid

            // Grab all active debts on the screen
            val sharedDebts = _activeDebts.value.filter { it.otherPersonUid == friendUid && it.status == "active" }

            // Calculate the true balance before this payment
            var netBalance = 0.0
            sharedDebts.forEach {
                if (it.iOweThem) netBalance -= it.amount
                else netBalance += it.amount
            }

            // Sweep! Archive all the old messy debts
            sharedDebts.forEach { oldDebt ->
                db.collection("debts").document(oldDebt.debtId).update("status", "settled")
            }

            // Archive the payment request itself
            db.collection("debts").document(debt.debtId).update("status", "settled")

            // Calculate what is left over. (e.g., they owed you 1200, but only paid 500. Remainder = 700)
            val remainder = Math.abs(netBalance) - debt.amount

            // If they didn't pay exactly the full amount, generate a clean remainder record
            if (Math.abs(remainder) > 0.01) {
                val iOweThemNew = remainder < 0
                val remainingRecord = hashMapOf(
                    "lenderUid" to if (iOweThemNew) friendUid else myUid,
                    "borrowerUid" to if (iOweThemNew) myUid else friendUid,
                    "amount" to (Math.round(Math.abs(remainder) * 100.0) / 100.0),
                    "description" to "Remaining Balance",
                    "status" to "active",
                    "timestamp" to System.currentTimeMillis()
                )
                db.collection("debts").add(remainingRecord)
            }
        } else {
            // If it's just a normal debt, accept it normally
            updateDebtStatus(debt.debtId, "settled")
        }
    }
    // Intercept the Reject button
    fun rejectDebt(debt: ActiveDebt) {
        // Check if this was a custom payment proposal
        if (debt.description.startsWith("[PAYMENT]")) {
            // If the friend denies receiving the payment, we delete the proposal entirely.
            // We do NOT set it to "active", otherwise it adds fake debt to the ledger.
            db.collection("debts").document(debt.debtId).delete()
        } else {
            // If it's a normal active debt that they rejected a "Mark as Paid" for,
            // we just flip it back to active so it returns to the main list.
            updateDebtStatus(debt.debtId, "active")
        }
    }

    fun resetStatus() { _saveStatus.value = "" }

    override fun onCleared() {
        super.onCleared()
        snapshotListener?.remove()
        authStateListener?.let { auth.removeAuthStateListener(it) }
    }
}