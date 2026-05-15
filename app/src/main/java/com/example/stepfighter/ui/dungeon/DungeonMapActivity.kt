package com.example.stepfighter.ui.dungeon

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(top = 24.dp, bottom = 32.dp)
                    ) {
                        item {
                            Text(
                                stringResource(R.string.dungeon_map_title),
                                color = GoldColor,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        val regions = dungeonLevelsData.groupBy { (it.id - 1) / 5 }

                        regions.forEach { (regionIndex, levels) ->
                            item {
                                val regionTitle = when(regionIndex) {
                                    0 -> stringResource(R.string.region_1)
                                    1 -> stringResource(R.string.region_2)
                                    2 -> stringResource(R.string.region_3)
                                    3 -> stringResource(R.string.region_4)
                                    4 -> stringResource(R.string.region_5)
                                    else -> stringResource(R.string.region_final)
                                }
                                RegionHeader(regionTitle)
                            }
                            items(levels) { dungeon ->
                                val isAvailable = dungeon.id <= unlockedLevel
                                DungeonLevelCard(dungeon, isAvailable) {
                                    val intent = Intent(context, DungeonActivity::class.java)
                                    intent.putExtra("DUNGEON_ID", dungeon.id)
                                    context.startActivity(intent)
                                }
                            }
                            item { Spacer(modifier = Modifier.height(16.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RegionHeader(title: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            text = title,
            color = GoldColor.copy(alpha = 0.5f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp), color = GoldColor.copy(alpha = 0.2f), thickness = 1.dp)
    }
}

@Composable
fun DungeonLevelCard(dungeon: DungeonLevel, isAvailable: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(enabled = isAvailable) { onClick() },
        colors = CardDefaults.cardColors(containerColor = if (isAvailable) CardBg else Color.Black.copy(alpha = 0.3f)),
        border = BorderStroke(1.dp, if (isAvailable) GoldColor.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(if (isAvailable) GoldColor else Color.DarkGray, RoundedCornerShape(2.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = dungeon.id.toString(),
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(dungeon.nameRes).uppercase(),
                    color = if (isAvailable) Color.White else Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = stringResource(R.string.dungeon_waves_count, dungeon.enemies.size),
                    color = TextGray,
                    fontSize = 11.sp
                )
            }
            if (isAvailable) {
                Icon(Icons.Default.PlayArrow, null, tint = GoldColor, modifier = Modifier.size(20.dp))
            } else {
                Icon(Icons.Default.Lock, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
            }
        }
    }
}