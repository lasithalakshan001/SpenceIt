package com.example.spenceit.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class FriendRequest(
    val requestId: String,
    val senderUid: String,
    val senderName: String,
    val senderEmail: String
)

class RequestsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _pendingRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
    val pendingRequests: StateFlow<List<FriendRequest>> = _pendingRequests.asStateFlow()

    private val _debugText = MutableStateFlow<String>("Initializing...")
    val debugText: StateFlow<String> = _debugText.asStateFlow()

    // 1. Keep track of the active listener so we can cancel it
    private var snapshotListener: ListenerRegistration? = null
    private var authStateListener: FirebaseAuth.AuthStateListener? = null

    init {
        // 2. React dynamically whenever someone logs in or out
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            // Cancel existing listener and wipe old requests
            snapshotListener?.remove()
            _pendingRequests.value = emptyList()

            if (user == null) {
                _debugText.value = "Signed out. No requests loaded."
            } else {
                listenForRequests(user.uid)
            }
        }
        auth.addAuthStateListener(authStateListener!!)
    }

    private fun listenForRequests(myUid: String) {
        _debugText.value = "Listening for requests sent to UID: ${myUid.take(6)}..."

        snapshotListener = db.collection("friend_requests")
            .whereEqualTo("to", myUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _debugText.value = "FIRESTORE ERROR: ${error.message}"
                    _pendingRequests.value = emptyList() // Wipe list on error
                    return@addSnapshotListener
                }

                if (snapshot == null || snapshot.isEmpty) {
                    _debugText.value = "0 requests found for UID ${myUid.take(6)}"
                    _pendingRequests.value = emptyList()
                    return@addSnapshotListener
                }

                val documents = snapshot.documents
                val requestsList = mutableListOf<FriendRequest>()
                var profilesToLoad = 0

                documents.forEach { doc ->
                    val status = doc.getString("status")
                    if (status == "pending") {
                        profilesToLoad++
                        val reqId = doc.id
                        val fromUid = doc.getString("from") ?: return@forEach

                        db.collection("users").document(fromUid).get()
                            .addOnSuccessListener { userDoc ->
                                val name = userDoc.getString("name") ?: "Unknown User"
                                val email = userDoc.getString("email") ?: "Unknown Email"

                                requestsList.add(FriendRequest(reqId, fromUid, name, email))

                                if (requestsList.size == profilesToLoad) {
                                    _debugText.value = "Loaded ${requestsList.size} request(s)."
                                    _pendingRequests.value = requestsList.toList()
                                }
                            }
                            .addOnFailureListener {
                                _debugText.value = "Failed to load sender profile."
                            }
                    }
                }

                if (profilesToLoad == 0) {
                    _debugText.value = "0 pending requests (all handled)."
                    _pendingRequests.value = emptyList()
                }
            }
    }

    fun respondToRequest(request: FriendRequest, accept: Boolean) {
        val myUid = auth.currentUser?.uid ?: return
        val requestRef = db.collection("friend_requests").document(request.requestId)

        if (accept) {
            db.collection("users").document(myUid)
                .update("friends", FieldValue.arrayUnion(request.senderUid))

            db.collection("users").document(request.senderUid)
                .update("friends", FieldValue.arrayUnion(myUid))

            requestRef.update("status", "accepted")
        } else {
            requestRef.update("status", "rejected")
        }
    }

    // 3. Prevent memory leaks when ViewModel is destroyed
    override fun onCleared() {
        super.onCleared()
        snapshotListener?.remove()
        authStateListener?.let { auth.removeAuthStateListener(it) }
    }
}