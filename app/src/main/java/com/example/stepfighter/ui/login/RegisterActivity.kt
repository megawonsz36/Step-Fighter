package com.example.stepfighter.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stepfighter.BaseGameActivity
import com.example.stepfighter.R
import com.example.stepfighter.BuildConfig
import com.example.stepfighter.ui.dashboard.DashboardActivity
import com.example.stepfighter.ui.profile.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class RegisterActivity : BaseGameActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HandleNetworkOverlay {
                RegisterScreen()
            }
        }
    }
}

@Composable
fun RegisterScreen() {
    val context = LocalContext.current
    val authManager = remember { AuthManager() }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var showNickDialog by remember { mutableStateOf(false) }
    var newUsername by remember { mutableStateOf("") }

    val googleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account?.idToken?.let { token ->
                context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
                    .edit().putBoolean("remember_me", false).apply()

                authManager.loginWithGoogle(token) { success, msg, isNewUser ->
                    if (success) {
                        if (isNewUser) {
                            showNickDialog = true
                        } else {
                            context.startActivity(Intent(context, DashboardActivity::class.java))
                            (context as? ComponentActivity)?.finish()
                        }
                    } else Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: ApiException) {
            Toast.makeText(context, "Błąd Google: ${e.statusCode}", Toast.LENGTH_SHORT).show()
        }
    }

    if (showNickDialog) {
        AlertDialog(
            onDismissRequest = { },
            containerColor = Color(0xFF1A1A1A),
            title = { Text("STWÓRZ PROFIL", color = GoldColor, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Twoje konto Google jest połączone. Podaj nick:", color = Color.White, fontSize = 14.sp)
                    Spacer(Modifier.height(16.dp))
                    LoginInput(newUsername, { newUsername = it }, "NICK", Icons.Default.Badge)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newUsername.length >= 3) {
                            authManager.updateUsername(newUsername) { success ->
                                if (success) {
                                    context.startActivity(Intent(context, DashboardActivity::class.java))
                                    (context as? ComponentActivity)?.finish()
                                }
                            }
                        } else {
                            Toast.makeText(context, "Nick musi mieć min. 3 znaki", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldColor)
                ) {
                    Text("ZATWIERDŹ", color = Color.Black)
                }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0F0F0F)).padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("DOŁĄCZ DO NAS", color = GoldColor, fontSize = 32.sp, fontWeight = FontWeight.Black)
        Text("Stwórz konto bohatera", color = Color.Gray, fontSize = 14.sp)

        Spacer(Modifier.height(40.dp))

        LoginInput(username, { username = it }, "TWÓJ NICK", Icons.Default.Badge)
        Spacer(Modifier.height(12.dp))
        LoginInput(email, { email = it }, "E-MAIL", Icons.Default.Email)
        Spacer(Modifier.height(12.dp))
        LoginInput(password, { password = it }, "HASŁO", Icons.Default.Lock, true)

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                if (username.isNotEmpty() && password.length >= 6) {
                    authManager.registerUser(email, password, username) { success, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        if (success) (context as ComponentActivity).finish()
                    }
                } else {
                    Toast.makeText(context, "Hasło min. 6 znaków i Nick nie może być pusty", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoldColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("STWÓRZ KONTO", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(BuildConfig.GOOGLE_CLIENT_ID)
                    .requestEmail()
                    .build()
                val client = GoogleSignIn.getClient(context, gso)

                client.signOut().addOnCompleteListener {
                    googleLauncher.launch(client.signInIntent)
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFDADCE0))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_google_logo),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = Color.Unspecified
                )
                Spacer(Modifier.width(12.dp))
                Text("Zarejestruj przez Google", color = Color(0xFF3C4043), fontWeight = FontWeight.Medium)
            }
        }

        Spacer(Modifier.height(24.dp))

        TextButton(onClick = { (context as? ComponentActivity)?.finish() }) {
            Text("Masz już konto? Zaloguj się", color = GoldColor, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun LoginInput(value: String, onValueChange: (String) -> Unit, label: String, icon: ImageVector, isPass: Boolean = false) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null, tint = GoldColor) },
        visualTransformation = if (isPass) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GoldColor,
            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedLabelColor = GoldColor,
            unfocusedLabelColor = Color.Gray
        )
    )
}