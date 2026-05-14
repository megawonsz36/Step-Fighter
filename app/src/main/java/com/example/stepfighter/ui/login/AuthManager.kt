package com.example.stepfighter.ui.login

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest

class AuthManager {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null && auth.currentUser!!.isEmailVerified
    }

    fun registerUser(email: String, password: String, username: String, onResult: (Boolean, String) -> Unit) {
        auth.createUserWithEmailAndPassword(email.trim(), password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                val profileUpdates = userProfileChangeRequest { displayName = username }
                user?.updateProfile(profileUpdates)?.addOnCompleteListener {
                    user.sendEmailVerification()
                    onResult(true, "Wysłano link weryfikacyjny na maila!")
                }
            } else {
                onResult(false, task.exception?.message ?: "Błąd rejestracji")
            }
        }
    }

    fun loginUser(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        auth.signInWithEmailAndPassword(email.trim(), password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                if (user != null && user.isEmailVerified) {
                    onResult(true, "Zalogowano")
                } else {
                    auth.signOut()
                    onResult(false, "Najpierw potwierdź swój e-mail!")
                }
            } else {
                onResult(false, task.exception?.message ?: "Błąd logowania")
            }
        }
    }

    fun loginWithGoogle(idToken: String, onResult: (Boolean, String) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onResult(true, "Zalogowano przez Google")
            } else {
                onResult(false, task.exception?.message ?: "Błąd autoryzacji Google")
            }
        }
    }
}