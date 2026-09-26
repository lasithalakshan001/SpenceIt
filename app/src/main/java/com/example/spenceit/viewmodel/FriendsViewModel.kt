package com.example.spenceit.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class FoundUser(val uid: String, val name: String, val email: String)

// 1. Move the Friend data class here so the ViewModel can use it
data class Friend(val uid: String, val name: String, val email: String)

class FriendsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _searchResultText = MutableStateFlow<String>("")
    val searchResultText: StateFlow<String> = _searchResultText.asStateFlow()

    private val _foundUser = MutableStateFlow<FoundUser?>(null)
    val foundUser: StateFlow<FoundUser?> = _foundUser.asStateFlow()

    // 2. State variable to hold your real friends list
    private val _friendsList = MutableStateFlow<List<Friend>>(emptyList())
    val friendsList: StateFlow<List<Friend>> = _friendsList.asStateFlow()

    private var authStateListener: FirebaseAuth.AuthStateListener? = null
    private var userListener: ListenerRegistration? = null

    init {
        // 3. React instantly when the user logs in
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            userListener?.remove()
            _friendsList.value = emptyList()

            if (user != null) {
                listenToMyFriends(user.uid)
            }
        }
        auth.addAuthStateListener(authStateListener!!)
    }

    private fun listenToMyFriends(myUid: String) {
        // Watch my own user document in Firestore
        userListener = db.collection("users").document(myUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener

                // Grab the "friends" array we created earlier
                val friendUids = snapshot.get("friends") as? List<String> ?: emptyList()

                if (friendUids.isEmpty()) {
                    _friendsList.value = emptyList()
                    return@addSnapshotListener
                }

                val fetchedFriends = mutableListOf<Friend>()
                var profilesLoaded = 0

                // For every UID in the array, fetch their actual Name and Email
                friendUids.forEach { friendUid ->
                    db.collection("users").document(friendUid).get()
                        .addOnSuccessListener { userDoc ->
                            val name = userDoc.getString("name") ?: "Unknown"
                            val email = userDoc.getString("email") ?: "Unknown"
                            fetchedFriends.add(Friend(friendUid, name, email))

                            profilesLoaded++
                            // Only update the UI once all friends are loaded
                            if (profilesLoaded == friendUids.size) {
                                _friendsList.value = fetchedFriends.toList()
                            }
                        }
                }
            }
    }

    fun searchByEmail(email: String) {
        _searchResultText.value = "Searching..."
        _foundUser.value = null

        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    _searchResultText.value = "No user found with that email."
                } else {
                    val userDoc = documents.documents[0]
                    val targetUid = userDoc.id
                    val name = userDoc.getString("name") ?: "Unknown"

                    if (targetUid == auth.currentUser?.uid) {
                        _searchResultText.value = "You can't add yourself as a friend!"
                    } else {
                        _searchResultText.value = ""
                        _foundUser.value = FoundUser(targetUid, name, email)
                    }
                }
            }
            .addOnFailureListener {
                _searchResultText.value = "Error connecting to database."
            }
    }

    fun sendFriendRequest() {
        val targetUser = _foundUser.value ?: return
        val myUid = auth.currentUser?.uid ?: return

        _searchResultText.value = "Sending request..."

        val requestData = hashMapOf(
            "from" to myUid,
            "to" to targetUser.uid,
            "status" to "pending",
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("friend_requests")
            .add(requestData)
            .addOnSuccessListener {
                _searchResultText.value = "Friend request sent to ${targetUser.name}!"
                _foundUser.value = null
            }
            .addOnFailureListener {
                _searchResultText.value = "Failed to send request."
            }
    }

    fun resetSearch() {
        _searchResultText.value = ""
        _foundUser.value = null
    }

    override fun onCleared() {
        super.onCleared()
        userListener?.remove()
        authStateListener?.let { auth.removeAuthStateListener(it) }
    }
}