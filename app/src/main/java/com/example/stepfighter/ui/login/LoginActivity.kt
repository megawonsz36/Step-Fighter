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
import com.example.stepfighter.R
import com.example.stepfighter.BuildConfig
import com.example.stepfighter.ui.dashboard.DashboardActivity
import com.example.stepfighter.ui.profile.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authManager = AuthManager()
        val prefs = getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
        val rememberMe = prefs.getBoolean("remember_me", false)


        if (!rememberMe) {
            FirebaseAuth.getInstance().signOut()
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            GoogleSignIn.getClient(this, gso).signOut()
        }


        if (authManager.isUserLoggedIn()) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }

        setContent { LoginScreen() }
    }
}

@Composable
fun LoginScreen() {
    val context = LocalContext.current
    val authManager = remember { AuthManager() }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }


    val prefs = remember { context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE) }
    var rememberMe by remember { mutableStateOf(prefs.getBoolean("remember_me", true)) }

    var showNickDialog by remember { mutableStateOf(false) }
    var newUsername by remember { mutableStateOf("") }

    val googleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account?.idToken?.let { token ->

                prefs.edit().putBoolean("remember_me", rememberMe).apply()

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
            title = { Text("WITAJ BOHATERZE!", color = GoldColor, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Podaj swój unikalny pseudonim:", color = Color.White, fontSize = 14.sp)
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
                    Text("ZACZYNAMY", color = Color.Black)
                }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0F0F0F)).padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(90.dp),
            color = GoldColor.copy(alpha = 0.1f),
            shape = RoundedCornerShape(20.dp)
        ) {
            Icon(Icons.Default.Shield, null, tint = GoldColor, modifier = Modifier.padding(16.dp))
        }

        Spacer(Modifier.height(16.dp))
        Text("STEP FIGHTER", color = GoldColor, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 4.sp)
        Text("Zaloguj się do swojej przygody", color = Color.Gray, fontSize = 14.sp)

        Spacer(Modifier.height(48.dp))

        LoginInput(email, { email = it }, "E-MAIL", Icons.Default.Email)
        Spacer(Modifier.height(16.dp))
        LoginInput(password, { password = it }, "HASŁO", Icons.Default.Lock, true)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Checkbox(
                checked = rememberMe,
                onCheckedChange = { rememberMe = it },
                colors = CheckboxDefaults.colors(checkedColor = GoldColor, uncheckedColor = Color.Gray)
            )
            Text("Zapamiętaj mnie", color = Color.LightGray, fontSize = 14.sp)
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                prefs.edit().putBoolean("remember_me", rememberMe).apply()
                authManager.loginUser(email, password) { success, msg ->
                    if (success) {
                        context.startActivity(Intent(context, DashboardActivity::class.java))
                        (context as? ComponentActivity)?.finish()
                    } else Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoldColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("ZALOGUJ SIĘ", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                Text("Kontynuuj z Google", color = Color(0xFF3C4043), fontWeight = FontWeight.Medium, fontSize = 16.sp)
            }
        }

        Spacer(Modifier.height(24.dp))

        TextButton(onClick = { context.startActivity(Intent(context, RegisterActivity::class.java)) }) {
            Text("Nie masz konta? Załóż je teraz", color = GoldColor, fontWeight = FontWeight.Bold)
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