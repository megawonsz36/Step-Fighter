package com.example.stepfighter.ui.dungeon

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.stepfighter.R
import com.example.stepfighter.ui.components.BottomNavigationBar
import com.example.stepfighter.ui.components.SideMenuContent
import com.example.stepfighter.ui.components.TopStepFighterBar
import com.example.stepfighter.ui.profile.*
import kotlinx.coroutines.launch

class DungeonActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dungeonId = intent.getIntExtra("DUNGEON_ID", 1)
        setContent { DungeonScreen(dungeonId) }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DungeonScreen(dungeonId: Int) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val levelData = dungeonLevelsData.find { it.id == dungeonId } ?: dungeonLevelsData[0]

    var currentWaveIndex by remember { mutableIntStateOf(0) }
    val currentEnemy = levelData.enemies[currentWaveIndex]
    var currentEnemyHp by remember { mutableIntStateOf(currentEnemy.maxHp) }
    var playerHp by remember { mutableIntStateOf(100) }
    var availableSteps by remember { mutableIntStateOf(2000) }

    val victoryMsg = stringResource(R.string.victory_message)

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    SideMenuContent(onClose = { scope.launch { drawerState.close() } })
                }
            },
            gesturesEnabled = drawerState.isOpen
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Scaffold(
                    topBar = { TopStepFighterBar(onMenuClick = { scope.launch { drawerState.open() } }) },
                    bottomBar = { BottomNavigationBar(selectedIndex = 1) },
                    containerColor = BgColor
                ) { innerPadding ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
                    ) {
                        item {
                            Text(
                                text = stringResource(R.string.dungeon_status, stringResource(levelData.nameRes).uppercase(), currentWaveIndex + 1, levelData.enemies.size),
                                color = GoldColor.copy(alpha = 0.7f),
                                style = TextStyle(fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 2.sp)
                            )
                        }
                        item {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                                    Text(stringResource(currentEnemy.nameRes).uppercase(), color = GoldColor, style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp))
                                    Text(stringResource(R.string.hp_label, currentEnemyHp, currentEnemy.maxHp), color = TextGray, style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold))
                                }
                                Spacer(Modifier.height(8.dp))
                                Box(modifier = Modifier.fillMaxWidth().height(12.dp).background(Color(0xFF2A2A2A), RoundedCornerShape(2.dp))) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth(currentEnemyHp.toFloat() / currentEnemy.maxHp.toFloat()).fillMaxHeight()
                                            .background(Brush.horizontalGradient(listOf(Color(0xFF8B0000), Color(0xFFFF4D4D))), RoundedCornerShape(2.dp))
                                    )
                                }
                            }
                        }
                        item {
                            Box(modifier = Modifier.size(300.dp).padding(8.dp), contentAlignment = Alignment.Center) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Icon(Icons.Default.ViewInAr, null, tint = GoldColor, modifier = Modifier.align(Alignment.TopStart).size(32.dp))
                                    Icon(Icons.Default.ViewInAr, null, tint = GoldColor, modifier = Modifier.align(Alignment.TopEnd).size(32.dp))
                                    Icon(Icons.Default.ViewInAr, null, tint = GoldColor, modifier = Modifier.align(Alignment.BottomStart).size(32.dp))
                                    Icon(Icons.Default.ViewInAr, null, tint = GoldColor, modifier = Modifier.align(Alignment.BottomEnd).size(32.dp))
                                }
                                Box(
                                    modifier = Modifier.fillMaxSize(0.92f).border(BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))).background(Color(0xFF1A1A1A)).zIndex(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(painter = painterResource(id = currentEnemy.imageRes), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                }
                            }
                        }
                        item {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Column(modifier = Modifier.weight(1f).background(CardBg, RoundedCornerShape(4.dp)).border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(4.dp)).padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(stringResource(R.string.your_health), color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(8.dp))
                                    Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(Color.Black, RoundedCornerShape(2.dp))) {
                                        Box(modifier = Modifier.fillMaxWidth(playerHp / 100f).fillMaxHeight().background(GoldColor, RoundedCornerShape(2.dp)))
                                    }
                                    Text("$playerHp / 100", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                                }
                                Column(modifier = Modifier.weight(1f).background(CardBg, RoundedCornerShape(4.dp)).border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(4.dp)).padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(stringResource(R.string.available_steps), color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.AutoMirrored.Filled.DirectionsRun, null, tint = AccentColor, modifier = Modifier.size(16.dp))
                                        Text(" $availableSteps", color = AccentColor, fontSize = 18.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }
                        item {
                            Button(
                                onClick = {
                                    if (availableSteps >= currentEnemy.costToHit) {
                                        availableSteps -= currentEnemy.costToHit
                                        val dmg = 30
                                        if (currentEnemyHp - dmg <= 0) {
                                            currentEnemyHp = 0
                                            if (currentWaveIndex < levelData.enemies.size - 1) {
                                                currentWaveIndex++
                                                currentEnemyHp = levelData.enemies[currentWaveIndex].maxHp
                                            } else {
                                                val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
                                                val currentMax = prefs.getInt("unlocked_level", 1)
                                                if (dungeonId == currentMax) {
                                                    prefs.edit().putInt("unlocked_level", dungeonId + 1).apply()
                                                }
                                                Toast.makeText(context, victoryMsg, Toast.LENGTH_LONG).show()
                                                (context as? Activity)?.finish()
                                            }
                                        } else { currentEnemyHp -= dmg }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(64.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD48A5F)),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column {
                                        Text(stringResource(R.string.action_strike), color = BgColor, fontWeight = FontWeight.Black, fontSize = 18.sp)
                                        Text(stringResource(R.string.action_strike_desc_steps), color = BgColor.copy(alpha = 0.7f), fontSize = 11.sp)
                                    }
                                    Surface(color = BgColor.copy(alpha = 0.2f), shape = RoundedCornerShape(2.dp)) {
                                        Text(stringResource(R.string.cost_label, currentEnemy.costToHit), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = BgColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        item {
                            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(2.dp)).background(AttributePanelBg).padding(4.dp)) {
                                Column(modifier = Modifier.fillMaxWidth().border(BorderStroke(1.dp, AttributeText.copy(alpha = 0.1f))).padding(20.dp)) {
                                    Text(stringResource(R.string.combat_log_title), color = AttributeText, style = TextStyle(fontWeight = FontWeight.Bold, letterSpacing = 1.sp, fontSize = 13.sp))
                                    Spacer(Modifier.height(12.dp))
                                    HorizontalDivider(color = AttributeText.copy(alpha = 0.1f))
                                    Spacer(Modifier.height(12.dp))
                                    Text(stringResource(R.string.combat_log_location, stringResource(levelData.nameRes)), color = AttributeText.copy(alpha = 0.6f), fontStyle = FontStyle.Italic, fontSize = 12.sp)
                                    Text(stringResource(R.string.combat_log_enemy, stringResource(currentEnemy.nameRes)), color = AttributeText, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}