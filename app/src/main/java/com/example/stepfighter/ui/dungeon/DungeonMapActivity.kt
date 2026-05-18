package com.example.stepfighter.ui.dungeon

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
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
import com.example.stepfighter.BaseGameActivity
import com.example.stepfighter.R
import com.example.stepfighter.ui.components.BottomNavigationBar
import com.example.stepfighter.ui.components.SideMenuContent
import com.example.stepfighter.ui.components.TopStepFighterBar
import com.example.stepfighter.ui.login.AuthManager
import com.example.stepfighter.ui.profile.*
import com.example.stepfighter.utils.NotificationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DungeonMapActivity : BaseGameActivity() {
    private val unlockedLevel = mutableStateOf(1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HandleNetworkOverlay {
                DungeonMapScreen(unlockedLevel.value)
            }
        }
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
    val authManager = remember { AuthManager() }
    val notificationHelper = remember { NotificationHelper(context) }
    val currentLang = context.getString(R.string.lang_pl)

    var currentEnergy by remember { mutableStateOf(100) }
    var maxEnergy by remember { mutableStateOf(100) }
    var lastRefillTime by remember { mutableStateOf(0L) }
    var timeRemainingMs by remember { mutableStateOf(300000L) }

    var hasNotifiedEnergyFull by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        authManager.loadEnergyData { success, energy, max, lastTime ->
            if (success) {
                maxEnergy = max
                val currentTime = System.currentTimeMillis()
                val timePassedMs = currentTime - lastTime
                val minutesPassed = (timePassedMs / 60000).toInt()

                if (minutesPassed > 0 && energy < max) {
                    val energyToGain = minutesPassed
                    val newEnergy = (energy + energyToGain).coerceAtMost(max)
                    val newRefillTime = lastTime + (minutesPassed * 60000L)

                    currentEnergy = newEnergy
                    lastRefillTime = if (newEnergy >= max) currentTime else newRefillTime
                    authManager.saveEnergyData(currentEnergy, lastRefillTime) {}

                    if (newEnergy >= max) {
                        notificationHelper.notifyEnergyFull()
                        hasNotifiedEnergyFull = true
                    }
                    timeRemainingMs = 300000L - (timePassedMs % 300000L)
                } else {
                    currentEnergy = energy
                    lastRefillTime = lastTime
                    timeRemainingMs = if (energy < max) 300000L - (timePassedMs % 300000L) else 0L
                    if (energy >= max) {
                        hasNotifiedEnergyFull = true
                    }
                }
            }
        }
    }

    LaunchedEffect(currentEnergy, timeRemainingMs) {
        while (true) {
            delay(1000L)
            if (currentEnergy < maxEnergy) {
                if (timeRemainingMs <= 1000L) {
                    currentEnergy = (currentEnergy + 1).coerceAtMost(maxEnergy)
                    timeRemainingMs = 300000L
                    lastRefillTime = System.currentTimeMillis()
                    authManager.saveEnergyData(currentEnergy, lastRefillTime) {}

                    if (currentEnergy >= maxEnergy && !hasNotifiedEnergyFull) {
                        notificationHelper.notifyEnergyFull()
                        hasNotifiedEnergyFull = true
                    }
                } else {
                    timeRemainingMs -= 1000L
                }
            } else {
                timeRemainingMs = 0L
            }
        }
    }

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
                    bottomBar = { BottomNavigationBar(selectedIndex = 0) },
                    containerColor = BgColor
                ) { innerPadding ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(top = 24.dp, bottom = 32.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    stringResource(R.string.dungeon_map_title),
                                    color = GoldColor,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black
                                )

                                Surface(
                                    color = CardBg,
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, GoldColor.copy(alpha = 0.2f))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalAlignment = Alignment.End,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.BatteryChargingFull,
                                                contentDescription = null,
                                                tint = AccentColor,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "$currentEnergy / $maxEnergy",
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }

                                        if (currentEnergy < maxEnergy) {
                                            val minutes = (timeRemainingMs / 60000).toInt()
                                            val seconds = ((timeRemainingMs % 60000) / 1000).toInt()

                                            val totalNeeded = maxEnergy - currentEnergy
                                            val totalTimeMs = ((totalNeeded - 1) * 300000L) + timeRemainingMs
                                            val totalMinutes = (totalTimeMs / 60000).toInt()
                                            val totalHours = totalMinutes / 60
                                            val remMinutes = totalMinutes % 60

                                            Text(
                                                text = stringResource(R.string.energy_next_gain, minutes, seconds),
                                                color = TextGray,
                                                fontSize = 10.sp
                                            )
                                            Text(
                                                text = stringResource(R.string.energy_max_duration, totalHours, remMinutes),
                                                color = GoldColor.copy(alpha = 0.8f),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
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
                                val energyCost = 5 + dungeon.id

                                DungeonLevelCard(dungeon, isAvailable, energyCost, currentLang) {
                                    if (currentEnergy >= energyCost) {
                                        currentEnergy -= energyCost
                                        hasNotifiedEnergyFull = false
                                        if (currentEnergy < maxEnergy && lastRefillTime == 0L) {
                                            lastRefillTime = System.currentTimeMillis()
                                        }
                                        authManager.saveEnergyData(currentEnergy, lastRefillTime) {
                                            val intent = Intent(context, DungeonActivity::class.java)
                                            intent.putExtra("DUNGEON_ID", dungeon.id)
                                            context.startActivity(intent)
                                        }
                                    } else {
                                        val errorMsg = if (currentLang == "Polski") "Brak energii!" else "Not enough energy!"
                                        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                                    }
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
fun DungeonLevelCard(dungeon: DungeonLevel, isAvailable: Boolean, energyCost: Int, currentLang: String, onClick: () -> Unit) {
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.dungeon_waves_count, dungeon.enemies.size),
                        color = TextGray,
                        fontSize = 11.sp
                    )
                    if (isAvailable) {
                        val costText = if (currentLang == "Polski") "Koszt: $energyCost EN" else "Cost: $energyCost EN"
                        Text(
                            text = costText,
                            color = AccentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            if (isAvailable) {
                Icon(Icons.Default.PlayArrow, null, tint = GoldColor, modifier = Modifier.size(20.dp))
            } else {
                Icon(Icons.Default.Lock, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
            }
        }
    }
}