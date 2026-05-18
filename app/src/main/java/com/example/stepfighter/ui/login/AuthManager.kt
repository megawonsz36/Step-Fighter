package com.example.stepfighter.ui.login

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase

class AuthManager {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun isUserLoggedIn(): Boolean {
        val user = auth.currentUser
        return user != null && (user.isEmailVerified || user.providerData.any { it.providerId == "google.com" })
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

    fun loginWithGoogle(idToken: String, onResult: (Boolean, String, Boolean) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val currentUid = auth.currentUser?.uid
                val firebaseIsNewUser = task.result?.additionalUserInfo?.isNewUser ?: false

                if (currentUid != null) {
                    FirebaseDatabase.getInstance().getReference("users").child(currentUid).get()
                        .addOnCompleteListener { dbTask ->
                            if (dbTask.isSuccessful && dbTask.result?.exists() == true) {
                                onResult(true, "Zalogowano przez Google", false)
                            } else {
                                onResult(true, "Zalogowano przez Google", firebaseIsNewUser)
                            }
                        }
                } else {
                    onResult(true, "Zalogowano przez Google", firebaseIsNewUser)
                }
            } else {
                onResult(false, task.exception?.message ?: "Błąd autoryzacji Google", false)
            }
        }
    }

    fun updateUsername(username: String, onResult: (Boolean) -> Unit) {
        val user = auth.currentUser
        val profileUpdates = userProfileChangeRequest { displayName = username }
        user?.updateProfile(profileUpdates)?.addOnCompleteListener { task ->
            onResult(task.isSuccessful)
        }
    }

    fun loadDashboardData(onResult: (Boolean, Map<String, Any>?, String) -> Unit) {
        val currentUid = auth.currentUser?.uid
        if (currentUid != null) {
            FirebaseDatabase.getInstance().getReference("users").child(currentUid).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val snapshot = task.result
                        if (snapshot != null && snapshot.exists()) {
                            val data = snapshot.value as? Map<String, Any>
                            onResult(true, data, "Dane zostały pomyślnie wczytane!")
                        } else {
                            onResult(false, null, "Nie znaleziono zapisu w chmurze.")
                        }
                    } else {
                        onResult(false, null, task.exception?.message ?: "Błąd połączenia z bazą.")
                    }
                }
        } else {
            onResult(false, null, "Użytkownik niezalogowany.")
        }
    }

    fun spendStepsInFight(stepsToSpend: Int, onResult: (Boolean, Int, String) -> Unit) {
        val currentUid = auth.currentUser?.uid ?: return onResult(false, 0, "Niezalogowany")
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUid)

        userRef.child("combatSteps").get().addOnCompleteListener { task ->
            if (task.isSuccessful && task.result?.exists() == true) {
                val currentStepsInCloud = (task.result.value as? Long)?.toInt() ?: 0

                if (currentStepsInCloud >= stepsToSpend) {
                    val updatedSteps = currentStepsInCloud - stepsToSpend
                    userRef.child("combatSteps").setValue(updatedSteps).addOnCompleteListener { saveTask ->
                        if (saveTask.isSuccessful) {
                            onResult(true, updatedSteps, "Kroki zostały trwale zużyte w walce!")
                        } else {
                            onResult(false, currentStepsInCloud, "Błąd zapisu kroków")
                        }
                    }
                } else {
                    onResult(false, currentStepsInCloud, "Masz za mało kroków na ten ruch!")
                }
            } else {
                onResult(false, 0, "Nie udało się pobrać kroków z chmury")
            }
        }
    }

    fun updateLevel(newLevel: Int, onResult: (Boolean) -> Unit) {
        val currentUid = auth.currentUser?.uid ?: return onResult(false)
        FirebaseDatabase.getInstance().getReference("users").child(currentUid).child("level")
            .setValue(newLevel)
            .addOnCompleteListener { task ->
                onResult(task.isSuccessful)
            }
    }

    fun saveTwoWaySteps(combatSteps: Int, totalXpSteps: Int, onResult: (Boolean) -> Unit) {
        val currentUid = auth.currentUser?.uid ?: return onResult(false)
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUid)
        val updates = mapOf(
            "combatSteps" to combatSteps,
            "totalXpSteps" to totalXpSteps
        )
        userRef.updateChildren(updates).addOnCompleteListener { task ->
            onResult(task.isSuccessful)
        }
    }

    fun loadEnergyData(onResult: (Boolean, Int, Int, Long) -> Unit) {
        val currentUid = auth.currentUser?.uid ?: return onResult(false, 0, 0, 0L)
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUid)

        userRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful && task.result?.exists() == true) {
                val snapshot = task.result
                val currentEnergy = (snapshot.child("currentEnergy").value as? Long)?.toInt() ?: 100
                val maxEnergy = (snapshot.child("maxEnergy").value as? Long)?.toInt() ?: 100
                val lastTime = (snapshot.child("lastEnergyRefillTime").value as? Long) ?: System.currentTimeMillis()
                onResult(true, currentEnergy, maxEnergy, lastTime)
            } else {
                onResult(false, 100, 100, System.currentTimeMillis())
            }
        }
    }

    fun saveEnergyData(currentEnergy: Int, lastRefillTime: Long, onResult: (Boolean) -> Unit) {
        val currentUid = auth.currentUser?.uid ?: return onResult(false)
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUid)

        val updates = mapOf(
            "currentEnergy" to currentEnergy,
            "lastEnergyRefillTime" to lastRefillTime
        )
        userRef.updateChildren(updates).addOnCompleteListener { task ->
            onResult(task.isSuccessful)
        }
    }
}