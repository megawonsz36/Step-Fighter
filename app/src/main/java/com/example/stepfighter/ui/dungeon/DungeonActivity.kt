package com.example.stepfighter.ui.dungeon

import android.annotation.SuppressLint
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
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.zIndex
import com.example.stepfighter.R
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.stepfighter.ui.components.BottomNavigationBar
import com.example.stepfighter.ui.components.SideMenuContent
import com.example.stepfighter.ui.components.TopStepFighterBar
import com.example.stepfighter.ui.profile.*
import kotlinx.coroutines.launch

class DungeonActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DungeonScreen()
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DungeonScreen() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    SideMenuContent(onClose = {
                        scope.launch { drawerState.close() }
                    })
                }
            },
            gesturesEnabled = drawerState.isOpen
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Scaffold(
                    topBar = {
                        TopStepFighterBar(onMenuClick = {
                            scope.launch { drawerState.open() }
                        })
                    },
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
                                        stringResource(R.string.enemy_name),
                                        color = GoldColor,
                                        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 1.sp)
                                    )
                                    Text(
                                        stringResource(R.string.hp_label, 69, 69),
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
                                            .fillMaxWidth(0.69f)
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
                                    .size(300.dp) // Zwiększono kontener
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                // Ozdobne narożniki (pod obrazkiem, ale widoczne na krawędziach)
                                val cornerColor = GoldColor
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Icon(Icons.Default.ViewInAr, null, tint = cornerColor, modifier = Modifier.align(Alignment.TopStart).size(32.dp).zIndex(0f))
                                    Icon(Icons.Default.ViewInAr, null, tint = cornerColor, modifier = Modifier.align(Alignment.TopEnd).size(32.dp).zIndex(0f))
                                    Icon(Icons.Default.ViewInAr, null, tint = cornerColor, modifier = Modifier.align(Alignment.BottomStart).size(32.dp).zIndex(0f))
                                    Icon(Icons.Default.ViewInAr, null, tint = cornerColor, modifier = Modifier.align(Alignment.BottomEnd).size(32.dp).zIndex(0f))
                                }

                                // Główny obraz przeciwnika (zwiększony, by przykrywać częściowo ikony)
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize(0.92f) // Zwiększono z 0.85f
                                        .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)))
                                        .background(Color(0xFF1A1A1A))
                                        .zIndex(1f), // Wyższy priorytet rysowania
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.enemy_placeholder),
                                        contentDescription = "Przeciwnik",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
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
                                    Text(stringResource(R.string.your_health), color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(8.dp))
                                    Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(Color.Black, RoundedCornerShape(2.dp))) {
                                        Box(modifier = Modifier.fillMaxWidth(0.69f).fillMaxHeight().background(GoldColor, RoundedCornerShape(2.dp)))
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Text(stringResource(R.string.hp_label, 69, 69).replace(" HP", ""), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
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
                                    Text(stringResource(R.string.available_steps), color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.AutoMirrored.Filled.DirectionsRun, null, tint = AccentColor, modifier = Modifier.size(16.dp))
                                        Text(" 69", color = AccentColor, fontSize = 18.sp, fontWeight = FontWeight.Black)
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
                                            Text(stringResource(R.string.action_strike), color = BgColor, fontWeight = FontWeight.Black, fontSize = 18.sp)
                                            Text(stringResource(R.string.action_strike_desc, 69), color = BgColor.copy(alpha = 0.7f), fontSize = 11.sp)
                                        }
                                    }
                                    Surface(
                                        color = BgColor.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(2.dp)
                                    ) {
                                        Text(
                                            stringResource(R.string.cost_label, 69),
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
                                    Text(stringResource(R.string.action_dodge), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 2.sp)
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
                                        stringResource(R.string.combat_log_title),
                                        color = AttributeText,
                                        style = TextStyle(fontWeight = FontWeight.Bold, letterSpacing = 1.sp, fontSize = 13.sp)
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    HorizontalDivider(color = AttributeText.copy(alpha = 0.1f))
                                    Spacer(Modifier.height(12.dp))
                                    
                                    Text(stringResource(R.string.combat_log_start), color = AttributeText.copy(alpha = 0.6f), fontStyle = FontStyle.Italic, fontSize = 12.sp)
                                    Spacer(Modifier.height(8.dp))
                                    Text(stringResource(R.string.combat_log_player_hit, 69), color = AttributeText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Spacer(Modifier.height(8.dp))
                                    Text(stringResource(R.string.combat_log_enemy_action), color = AttributeText, fontSize = 13.sp)
                                    Text(stringResource(R.string.combat_log_player_loss, 69), color = Color(0xFF8B0000), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
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
