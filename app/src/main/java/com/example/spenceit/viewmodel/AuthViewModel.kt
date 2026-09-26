package com.example.spenceit.viewmodel


import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.google.firebase.firestore.FirebaseFirestore

// 1. We define the 4 possible situations (states) our screen can be in
sealed class AuthState {
    object Idle : AuthState() // Waiting for the user to click the button
    object Loading : AuthState() // Showing a spinning wheel while contacting Google
    object Success : AuthState() // Account created!
    data class Error(val message: String) : AuthState() // Something went wrong (e.g., bad email)
}

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance() // 1. Grab the Database engine

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // 2. Add 'name' to the required ingredients
    fun register(name: String, email: String, pass: String) {
        _authState.value = AuthState.Loading

        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = task.result?.user?.uid

                    if (userId != null) {
                        val userProfile = hashMapOf(
                            "uid" to userId,
                            "name" to name,
                            "email" to email
                        )

                        // THIS IS THE CRUCIAL PART FOR FIRESTORE
                        db.collection("users").document(userId)
                            .set(userProfile)
                            .addOnSuccessListener {
                                _authState.value = AuthState.Success
                            }
                            .addOnFailureListener { e ->
                                _authState.value = AuthState.Error(e.message ?: "Database error")
                            }
                    }
                } else {
                    val errorMessage = task.exception?.message ?: "Registration failed"
                    _authState.value = AuthState.Error(errorMessage)
                }
            }
    }

    fun login(email: String, pass: String) {
        // ... keep your existing login code exactly the same ...
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Success
                } else {
                    val errorMessage = task.exception?.message ?: "Login failed"
                    _authState.value = AuthState.Error(errorMessage)
                }
            }
    }
}