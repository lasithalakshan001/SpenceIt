package com.example.spenceit.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

data class ExpenseGroup(
    val groupId: String,
    val name: String,
    val memberUids: List<String>
)

// Helper class for the settlement algorithm
class MemberBalance(val uid: String, var amount: Double)

class GroupsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _myGroups = MutableStateFlow<List<ExpenseGroup>>(emptyList())
    val myGroups: StateFlow<List<ExpenseGroup>> = _myGroups.asStateFlow()

    private var authStateListener: FirebaseAuth.AuthStateListener? = null
    private var snapshotListener: ListenerRegistration? = null

    init {
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            snapshotListener?.remove()
            _myGroups.value = emptyList()

            if (user != null) {
                listenForMyGroups(user.uid)
            }
        }
        auth.addAuthStateListener(authStateListener!!)
    }

    private fun listenForMyGroups(myUid: String) {
        snapshotListener = db.collection("groups")
            .whereArrayContains("members", myUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val groupsList = mutableListOf<ExpenseGroup>()
                snapshot.documents.forEach { doc ->
                    val name = doc.getString("name") ?: "Unnamed Group"
                    val members = doc.get("members") as? List<String> ?: emptyList()
                    groupsList.add(ExpenseGroup(doc.id, name, members))
                }
                _myGroups.value = groupsList.toList()
            }
    }

    fun createGroup(name: String, selectedFriends: Set<String>) {
        val myUid = auth.currentUser?.uid ?: return
        val allMembers = selectedFriends.toMutableList()
        allMembers.add(myUid)

        val groupData = hashMapOf(
            "name" to name,
            "members" to allMembers,
            "createdBy" to myUid,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("groups").add(groupData)
    }

    fun getGroupById(groupId: String): ExpenseGroup? {
        return _myGroups.value.find { it.groupId == groupId }
    }

    // --- NEW: Multi-Payer Complex Algorithm ---
    fun addComplexSharedExpense(group: ExpenseGroup, contributions: Map<String, Double>, description: String) {
        val totalAmount = contributions.values.sum()
        if (totalAmount <= 0) return

        val membersCount = group.memberUids.size
        if (membersCount == 0) return

        // 1. Determine the equal share everyone is supposed to pay
        val equalShare = totalAmount / membersCount

        // 2. Calculate Net Balances
        val balances = mutableMapOf<String, Double>()
        for (uid in group.memberUids) {
            val paid = contributions[uid] ?: 0.0
            balances[uid] = paid - equalShare // Positive = Owed money. Negative = Owes money.
        }

        // 3. Separate into Debtors and Creditors
        val debtors = balances.filter { it.value < -0.01 }.map { MemberBalance(it.key, it.value) }.toMutableList()
        val creditors = balances.filter { it.value > 0.01 }.map { MemberBalance(it.key, it.value) }.toMutableList()

        // 4. Greedy Matching Algorithm
        var i = 0 // debtor index
        var j = 0 // creditor index

        while (i < debtors.size && j < creditors.size) {
            val debtor = debtors[i]
            val creditor = creditors[j]

            val oweAmount = abs(debtor.amount)
            val receiveAmount = creditor.amount

            // The amount to transfer is the smaller of the two balances
            val settledAmount = minOf(oweAmount, receiveAmount)

            // Generate the official contract and save to the central Debts collection
            val debtRecord = hashMapOf(
                "lenderUid" to creditor.uid,
                "borrowerUid" to debtor.uid,
                "amount" to (Math.round(settledAmount * 100.0) / 100.0), // Round to 2 decimals
                "description" to "$description (Group: ${group.name})",
                "status" to "active",
                "timestamp" to System.currentTimeMillis()
            )
            db.collection("debts").add(debtRecord)

            // Adjust balances
            debtor.amount += settledAmount
            creditor.amount -= settledAmount

            // Move to the next person if they are fully settled
            if (abs(debtor.amount) < 0.01) i++
            if (creditor.amount < 0.01) j++
        }
    }

    // --- NEW: Group Management Functions ---

    fun deleteGroup(groupId: String) {
        db.collection("groups").document(groupId).delete()
    }

    fun addMembersToGroup(groupId: String, newMemberUids: List<String>) {
        if (newMemberUids.isEmpty()) return
        // FieldValue.arrayUnion adds elements to the array without duplicating existing ones
        db.collection("groups").document(groupId)
            .update("members", com.google.firebase.firestore.FieldValue.arrayUnion(*newMemberUids.toTypedArray()))
    }

    fun removeMemberFromGroup(groupId: String, memberUidToRemove: String) {
        // FieldValue.arrayRemove pulls a specific element out of the array
        db.collection("groups").document(groupId)
            .update("members", com.google.firebase.firestore.FieldValue.arrayRemove(memberUidToRemove))
    }

    override fun onCleared() {
        super.onCleared()
        snapshotListener?.remove()
        authStateListener?.let { auth.removeAuthStateListener(it) }
    }
}