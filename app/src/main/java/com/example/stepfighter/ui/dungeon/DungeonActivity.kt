package com.example.stepfighter.ui.dungeon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stepfighter.ui.components.BottomNavigationBar
import com.example.stepfighter.ui.components.TopStepFighterBar
import com.example.stepfighter.ui.profile.*

class DungeonActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DungeonScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DungeonScreen() {
    Scaffold(
        topBar = { TopStepFighterBar() },
        bottomBar = { BottomNavigationBar(selectedIndex = 0) },
        containerColor = BgColor
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
        ) {
            // --- PRZECIWNIK I HP ---
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            "PRZEKLĘTY STRAŻNIK",
                            color = GoldColor,
                            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 1.sp)
                        )
                        Text(
                            "840 / 1200 HP",
                            color = TextGray,
                            style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    // Pasek HP Przeciwnika
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .background(Color(0xFF2A2A2A), RoundedCornerShape(2.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .fillMaxHeight()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF8B0000), Color(0xFFFF4D4D))
                                    ),
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                    }
                }
            }

            // --- RAMKA Z PRZECIWNIKIEM ---
            item {
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Ozdobne narożniki (imitacja tych sześcianów ze screena)
                    val cornerColor = GoldColor
                    Box(modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.Default.ViewInAr, null, tint = cornerColor, modifier = Modifier.align(Alignment.TopStart).size(24.dp))
                        Icon(Icons.Default.ViewInAr, null, tint = cornerColor, modifier = Modifier.align(Alignment.TopEnd).size(24.dp))
                        Icon(Icons.Default.ViewInAr, null, tint = cornerColor, modifier = Modifier.align(Alignment.BottomStart).size(24.dp))
                        Icon(Icons.Default.ViewInAr, null, tint = cornerColor, modifier = Modifier.align(Alignment.BottomEnd).size(24.dp))
                    }

                    // Główny obraz/placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxSize(0.85f)
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)))
                            .background(Color(0xFF1A1A1A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "PRZECIWNIK",
                            color = Color.White,
                            style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // --- STATUS GRACZA ---
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Twoje Zdrowie
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(CardBg, RoundedCornerShape(4.dp))
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(4.dp))
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("TWOJE ZDROWIE", color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(Color.Black, RoundedCornerShape(2.dp))) {
                            Box(modifier = Modifier.fillMaxWidth(0.85f).fillMaxHeight().background(GoldColor, RoundedCornerShape(2.dp)))
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("170 / 200", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                    }
                    // Dostępne Kroki
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(CardBg, RoundedCornerShape(4.dp))
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(4.dp))
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("DOSTĘPNE KROKI", color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DirectionsRun, null, tint = AccentColor, modifier = Modifier.size(16.dp))
                            Text(" 4,258", color = AccentColor, fontSize = 18.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            // --- PRZYCISKI AKCJI ---
            item {
                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD48A5F)),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FlashOn, null, tint = BgColor, modifier = Modifier.size(28.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("UDERZ", color = BgColor, fontWeight = FontWeight.Black, fontSize = 18.sp)
                                Text("Zadaj 45-60 obrażeń", color = BgColor.copy(alpha = 0.7f), fontSize = 11.sp)
                            }
                        }
                        Surface(
                            color = BgColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(2.dp)
                        ) {
                            Text(
                                "KOSZT: 1000 KROKÓW",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = BgColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = CardBg.copy(alpha = 0.5f))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessibilityNew, null, tint = TextGray, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("UNIK", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 2.sp)
                    }
                }
            }

            // --- KRONIKA WALKI ---
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(2.dp))
                        .background(AttributePanelBg)
                        .padding(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(BorderStroke(1.dp, AttributeText.copy(alpha = 0.1f)))
                            .padding(20.dp)
                    ) {
                        Text(
                            "KRONIKA WALKI",
                            color = AttributeText,
                            style = TextStyle(fontWeight = FontWeight.Bold, letterSpacing = 1.sp, fontSize = 13.sp)
                        )
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = AttributeText.copy(alpha = 0.1f))
                        Spacer(Modifier.height(12.dp))
                        
                        Text("Zaczynasz pojedynek z Strażnikiem...", color = AttributeText.copy(alpha = 0.6f), fontStyle = FontStyle.Italic, fontSize = 12.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("Ty: Uderzasz za 52 obrażeń.", color = AttributeText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("Strażnik: Wykonuje potężny zamach!", color = AttributeText, fontSize = 13.sp)
                        Text("Tracisz 15 HP.", color = Color(0xFF8B0000), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF131313)
@Composable
fun DungeonPreview() {
    DungeonScreen()
}
