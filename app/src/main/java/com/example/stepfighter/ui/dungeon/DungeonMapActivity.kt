package com.example.stepfighter.ui.dungeon

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stepfighter.R
import com.example.stepfighter.ui.components.BottomNavigationBar
import com.example.stepfighter.ui.components.SideMenuContent
import com.example.stepfighter.ui.components.TopStepFighterBar
import com.example.stepfighter.ui.profile.*
import kotlinx.coroutines.launch

class DungeonMapActivity : ComponentActivity() {
    private val unlockedLevel = mutableIntStateOf(1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { DungeonMapScreen(unlockedLevel.value) }
    }

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
        unlockedLevel.value = prefs.getInt("unlocked_level", 1)
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DungeonMapScreen(unlockedLevel: Int) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

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
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(top = 24.dp, bottom = 32.dp)
                    ) {
                        item {
                            Text(
                                stringResource(R.string.dungeon_map_title),
                                color = GoldColor,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        items(dungeonLevelsData) { dungeon ->
                            val isAvailable = dungeon.id <= unlockedLevel
                            DungeonLevelCard(dungeon, isAvailable) {
                                val intent = Intent(context, DungeonActivity::class.java)
                                intent.putExtra("DUNGEON_ID", dungeon.id)
                                context.startActivity(intent)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DungeonLevelCard(dungeon: DungeonLevel, isAvailable: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(enabled = isAvailable) { onClick() },
        colors = CardDefaults.cardColors(containerColor = if (isAvailable) CardBg else Color.Black.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, if (isAvailable) GoldColor.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f))
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(if (isAvailable) GoldColor else Color.DarkGray, RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) {
                Text(dungeon.id.toString(), color = Color.Black, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(dungeon.nameRes).uppercase(), color = if (isAvailable) Color.White else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(stringResource(R.string.dungeon_waves_count, dungeon.enemies.size), color = TextGray, fontSize = 12.sp)
            }
            if (isAvailable) Icon(Icons.Default.PlayArrow, null, tint = GoldColor)
            else Icon(Icons.Default.Lock, null, tint = Color.Gray)
        }
    }
}