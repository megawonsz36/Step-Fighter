package com.example.stepfighter.ui.dashboard

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stepfighter.R
import com.example.stepfighter.ui.components.BottomNavigationBar
import com.example.stepfighter.ui.components.TopStepFighterBar
import com.example.stepfighter.ui.dungeon.DungeonActivity
import com.example.stepfighter.ui.profile.*

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DashboardScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    val context = LocalContext.current
    Scaffold(
        topBar = { TopStepFighterBar() },
        bottomBar = { BottomNavigationBar(selectedIndex = 0) },
        containerColor = BgColor
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- NAGŁÓWEK Z OBRAZKIEM ---
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    // Tło - tutaj użytkownik wstawi swój obrazek
                    Image(
                        painter = painterResource(id = R.drawable.bg_warrior), // Placeholder, trzeba dodać plik
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = 0.6f
                    )
                    
                    // Gradient nakładany na dół obrazka dla płynnego przejścia
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, BgColor),
                                    startY = 400f
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(24.dp)
                    ) {
                        Text(
                            "POZIOM 42",
                            color = GoldColor,
                            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 2.sp)
                        )
                        Text(
                            "Wojownik Ścieżek",
                            color = Color.White,
                            style = TextStyle(fontWeight = FontWeight.Black, fontSize = 32.sp)
                        )
                    }
                }
            }

            // --- POSTĘP ENERGII ---
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .background(CardBg, RoundedCornerShape(8.dp))
                        .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(8.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("POSTĘP ENERGII", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                Text("8,432 / 10,000 Kroków", color = Color.White, fontSize = 16.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                            }
                            Icon(Icons.Default.FlashOn, null, tint = GoldColor, modifier = Modifier.size(24.dp))
                        }
                        Spacer(Modifier.height(16.dp))
                        // Pasek Energii
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .background(Color.Black, RoundedCornerShape(5.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.84f)
                                    .fillMaxHeight()
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(Color(0xFFD48A5F), Color(0xFFFFB783))
                                        ),
                                        shape = RoundedCornerShape(5.dp)
                                    )
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Brakuje ci 1,568 kroków do pełnego naładowania.\nNastępny poziom.",
                            color = TextGray,
                            fontSize = 11.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // --- BLOK WALKI (Zmieniony z Synchronizacji) ---
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .height(140.dp)
                        .background(CardBg, RoundedCornerShape(8.dp))
                        .border(BorderStroke(1.dp, GoldColor.copy(alpha = 0.2f)), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "BRAK PRZECIWNIKÓW",
                            color = GoldColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            "Poruszaj się aby jakichś namierzyć",
                            color = TextGray,
                            fontSize = 12.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = {
                                val intent = Intent(context, DungeonActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                                context.startActivity(intent)
                                (context as Activity).overridePendingTransition(0, 0)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldColor.copy(alpha = 0.1f)),
                            border = BorderStroke(1.dp, GoldColor),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 8.dp)
                        ) {
                            Text("WALCZ", color = GoldColor, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // --- ZAPISZ POSTĘP ---
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .background(CardBg, RoundedCornerShape(8.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFF8B2020), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CloudUpload, null, tint = Color.White)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("ZAPISZ POSTĘP", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Zaloguj przez Google", color = TextGray, fontSize = 11.sp)
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = TextGray)
                }
            }

            // --- OSTATNIE ZNALEZISKA ---
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        "OSTATNIE ZNALEZISKA",
                        color = TextGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    FindItem("Medalion Wytrwałości", "-5% do regeneracji energii z kroków", Icons.Default.MilitaryTech)
                    Spacer(Modifier.height(12.dp))
                    FindItem("Buty Siedmiomilowe", "Zaliczono 50,000 kroków w tym tygodniu", Icons.Default.DirectionsWalk)
                }
            }
            
            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}

@Composable
fun FindItem(title: String, desc: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(4.dp))
            .border(
                BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), 
                RoundedCornerShape(4.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = GoldColor, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(desc, color = TextGray, fontSize = 11.sp)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF131313)
@Composable
fun DashboardPreview() {
    DashboardScreen()
}
