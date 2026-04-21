package com.example.stepfighter.ui.dashboard

import android.annotation.SuppressLint
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
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.stepfighter.R
import com.example.stepfighter.ui.components.BottomNavigationBar
import com.example.stepfighter.ui.components.SideMenuContent
import com.example.stepfighter.ui.components.TopStepFighterBar
import com.example.stepfighter.ui.dungeon.DungeonActivity
import com.example.stepfighter.ui.profile.*
import kotlinx.coroutines.launch

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DashboardScreen()
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    val context = LocalContext.current
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
                        Box(modifier = Modifier.fillMaxWidth().zIndex(1f)) {
                            TopStepFighterBar(onMenuClick = {
                                scope.launch { drawerState.open() }
                            })
                        }
                    },
                    bottomBar = { BottomNavigationBar(selectedIndex = 0) },
                    containerColor = BgColor
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding())
                        ) {
                            // --- NAGŁÓWEK Z OBRAZKIEM ---
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(340.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.bg_warrior),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                        alpha = 0.6f
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(Color.Transparent, BgColor),
                                                    startY = 500f
                                                )
                                            )
                                    )
                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(24.dp)
                                            .padding(bottom = 16.dp)
                                    ) {
                                        Text(
                                            stringResource(R.string.level_label, 69),
                                            color = GoldColor,
                                            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 2.sp)
                                        )
                                        Text(
                                            stringResource(R.string.warrior_title),
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
                                                Text(stringResource(R.string.energy_progress_title), color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                                Text(stringResource(R.string.steps_count, 69, 69), color = Color.White, fontSize = 16.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                            }
                                            Icon(Icons.Default.FlashOn, null, tint = GoldColor, modifier = Modifier.size(24.dp))
                                        }
                                        Spacer(Modifier.height(16.dp))
                                        Box(modifier = Modifier.fillMaxWidth().height(10.dp).background(Color.Black, RoundedCornerShape(5.dp))) {
                                            Box(
                                                modifier = Modifier.fillMaxWidth(0.69f).fillMaxHeight().background(
                                                    brush = Brush.horizontalGradient(colors = listOf(Color(0xFFD48A5F), Color(0xFFFFB783))),
                                                    shape = RoundedCornerShape(5.dp)
                                                )
                                            )
                                        }
                                        Spacer(Modifier.height(12.dp))
                                        Text(
                                            stringResource(R.string.energy_missing_desc, 69),
                                            color = TextGray,
                                            fontSize = 11.sp,
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }

                            // --- BLOK WALKI ---
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
                                        Text(stringResource(R.string.no_enemies), color = GoldColor, fontWeight = FontWeight.Black, fontSize = 18.sp, letterSpacing = 1.sp)
                                        Text(stringResource(R.string.no_enemies_desc), color = TextGray, fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                        Spacer(Modifier.height(16.dp))
                                        Button(
                                            onClick = {
                                                val intent = Intent(context, DungeonActivity::class.java)
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                                                context.startActivity(intent)
                                                @Suppress("DEPRECATION")
                                                (context as Activity).overridePendingTransition(0, 0)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = GoldColor.copy(alpha = 0.1f)),
                                            border = BorderStroke(1.dp, GoldColor),
                                            shape = RoundedCornerShape(4.dp),
                                            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 8.dp)
                                        ) {
                                            Text(stringResource(R.string.fight_btn), color = GoldColor, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // --- ZAPISZ POSTĘP ---
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp).background(CardBg, RoundedCornerShape(8.dp)).padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(modifier = Modifier.size(48.dp).background(Color(0xFF8B2020), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.CloudUpload, null, tint = Color.White)
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(stringResource(R.string.save_progress), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(stringResource(R.string.login_google), color = TextGray, fontSize = 11.sp)
                                    }
                                    Icon(Icons.Default.ChevronRight, null, tint = TextGray)
                                }
                            }

                            // --- OSTATNIE ZNALEZISKA ---
                            item {
                                Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                                    Text(stringResource(R.string.recent_finds), color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, modifier = Modifier.padding(bottom = 16.dp))
                                    FindItem(stringResource(R.string.find_medallion_title), stringResource(R.string.find_medallion_desc, 69), Icons.Default.MilitaryTech)
                                    Spacer(Modifier.height(12.dp))
                                    FindItem(stringResource(R.string.find_boots_title), stringResource(R.string.find_boots_desc, 69), Icons.AutoMirrored.Filled.DirectionsWalk)
                                }
                            }
                            item { Spacer(Modifier.height(20.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FindItem(title: String, desc: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth().background(CardBg, RoundedCornerShape(4.dp)).border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(4.dp)).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(40.dp).background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
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
