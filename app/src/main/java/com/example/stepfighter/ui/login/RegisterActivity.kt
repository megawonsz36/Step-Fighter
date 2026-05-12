package com.example.stepfighter.ui.login

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stepfighter.ui.profile.*

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

    Column(
        modifier = Modifier.fillMaxSize().background(BgColor).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.PersonAdd, null, tint = GoldColor, modifier = Modifier.size(64.dp))
        Text("NOWY WOJOWNIK", color = GoldColor, fontSize = 28.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(32.dp))

        LoginInput(username, { username = it }, "NAZWA WOJOWNIKA", Icons.Default.Badge)
        Spacer(Modifier.height(16.dp))
        LoginInput(email, { email = it }, "ADRES E-MAIL", Icons.Default.Email)
        Spacer(Modifier.height(16.dp))
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
                    Toast.makeText(context, "Błędne dane (hasło min. 6 znaków)", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoldColor),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text("ZAREJESTRUJ SIĘ", color = BgColor, fontWeight = FontWeight.Bold)
        }
    }
}