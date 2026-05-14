package com.example.stepfighter.ui.login

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stepfighter.R
import com.example.stepfighter.BuildConfig
import com.example.stepfighter.ui.dashboard.DashboardActivity
import com.example.stepfighter.ui.profile.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { RegisterScreen() }
    }
}

@Composable
fun RegisterScreen() {
    val context = LocalContext.current
    val authManager = remember { AuthManager() }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val googleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account?.idToken?.let { token ->
                authManager.loginWithGoogle(token) { success, _ ->
                    if (success) context.startActivity(Intent(context, DashboardActivity::class.java))
                }
            }
        } catch (e: ApiException) {}
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
                    Toast.makeText(context, "Hasło min. 6 znaków", Toast.LENGTH_SHORT).show()
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
                googleLauncher.launch(client.signInIntent)
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
    }
}