package com.example.stepfighter.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stepfighter.ui.dashboard.DashboardActivity
import com.example.stepfighter.ui.profile.*

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { LoginScreen() }
    }
}

@Composable
fun LoginScreen() {
    val context = LocalContext.current
    val authManager = remember { AuthManager() }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().background(BgColor).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Shield, null, tint = GoldColor, modifier = Modifier.size(80.dp))
        Text("STEP FIGHTER", color = GoldColor, fontSize = 32.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(40.dp))

        LoginInput(email, { email = it }, "E-MAIL", Icons.Default.Person)
        Spacer(Modifier.height(16.dp))
        LoginInput(password, { password = it }, "HASŁO", Icons.Default.Lock, true)

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                authManager.loginUser(email, password) { success, msg ->
                    if (success) {
                        val intent = Intent(context, DashboardActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                    } else {
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoldColor),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text("ZALOGUJ DO WALKI", color = BgColor, fontWeight = FontWeight.Bold)
        }

        TextButton(onClick = { context.startActivity(Intent(context, RegisterActivity::class.java)) }) {
            Text("STWÓRZ NOWE KONTO", color = GoldColor)
        }
    }
}

@Composable
fun LoginInput(value: String, onValueChange: (String) -> Unit, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isPass: Boolean = false) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label, color = TextGray) },
        leadingIcon = { Icon(icon, null, tint = GoldColor) },
        visualTransformation = if (isPass) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldColor, unfocusedBorderColor = Color.White.copy(0.1f), focusedTextColor = Color.White)
    )
}