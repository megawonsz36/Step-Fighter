package com.example.stepfighter.ui.login

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest

class AuthManager {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun registerUser(email: String, password: String, username: String, onResult: (Boolean, String) -> Unit) {
        val cleanEmail = email.trim()

        auth.createUserWithEmailAndPassword(cleanEmail, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                val profileUpdates = userProfileChangeRequest {
                    displayName = username
                }

                user?.updateProfile(profileUpdates)?.addOnCompleteListener {
                    user.sendEmailVerification()
                    onResult(true, "Witaj $username! Potwierdź e-mail.")
                }
            } else {
                onResult(false, task.exception?.message ?: "Błąd rejestracji")
            }
        }
    }

    fun loginUser(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        val cleanEmail = email.trim()
        auth.signInWithEmailAndPassword(cleanEmail, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                if (user != null && user.isEmailVerified) {
                    onResult(true, "Zalogowano")
                } else {
                    auth.signOut()
                    onResult(false, "Potwierdź e-mail!")
                }
            } else {
                onResult(false, task.exception?.message ?: "Błąd logowania")
            }
        }
    }
}