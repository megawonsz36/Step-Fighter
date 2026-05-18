package com.example.stepfighter.ui.dungeon

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.stepfighter.BaseGameActivity
import com.example.stepfighter.R
import com.example.stepfighter.ui.components.TopStepFighterBar
import com.example.stepfighter.ui.login.AuthManager
import com.example.stepfighter.ui.profile.*
import com.example.stepfighter.ui.shop.getFullShopItemsList
import kotlinx.coroutines.delay
import kotlin.random.Random

class DungeonActivity : BaseGameActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dungeonId = intent.getIntExtra("DUNGEON_ID", 1)
        setContent {
            HandleNetworkOverlay {
                DungeonScreen(dungeonId)
            }
        }
    }
}

data class DungeonUsableItem(
    val id: String,
    val name: String,
    val statText: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val isHpPotion: Boolean,
    val isScroll: Boolean,
    val restoreAmount: Int
)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DungeonScreen(dungeonId: Int) {
    val context = LocalContext.current
    val authManager = remember { AuthManager() }
    val levelData = dungeonLevelsData.find { it.id == dungeonId } ?: dungeonLevelsData[0]
    val prefs = remember { context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE) }
    val currentLang = context.getString(R.string.lang_pl)

    var currentWaveIndex by remember { mutableStateOf(0) }
    var currentEnemyHp by remember { mutableStateOf(levelData.enemies[currentWaveIndex].maxHp) }

    var playerHp by remember { mutableStateOf(500) }
    var maxPlayerHp by remember { mutableStateOf(500) }
    var availableSteps by remember { mutableStateOf(0) }

    val combatLogs = remember { mutableStateListOf<Pair<String, Color>>() }
    val currentEnemy = levelData.enemies[currentWaveIndex]
    val victoryMsg = stringResource(R.string.victory_message)

    val reducedCostToHit = (currentEnemy.costToHit / 10).coerceAtLeast(1)

    var isWaveIntermission by remember { mutableStateOf(false) }
    var intermissionTicksLeft by remember { mutableStateOf(10) }
    var isItemMenuOpen by remember { mutableStateOf(false) }
    var usableItemsInInventory by remember { mutableStateOf<List<DungeonUsableItem>>(emptyList()) }
    var hasMeditatedInThisDungeon by remember { mutableStateOf(false) }
    var showNoStepsDefeatDialog by remember { mutableStateOf(false) }

    val escapeChance = remember(currentWaveIndex) { (100 - (currentWaveIndex * 20)).coerceAtLeast(10) }

    fun resetDungeon() {
        currentWaveIndex = 0
        currentEnemyHp = levelData.enemies[0].maxHp
        playerHp = maxPlayerHp
        combatLogs.clear()
        isWaveIntermission = false
        hasMeditatedInThisDungeon = false
    }

    fun loadUsableItemsFromInventory() {
        val allItems = getFullShopItemsList()
        val inventoryString = prefs.getString("inventory_items", "") ?: ""
        val ids = if (inventoryString.isEmpty()) emptyList() else inventoryString.split(",")

        usableItemsInInventory = ids.mapNotNull { id ->
            val shopItem = allItems.find { it.id == id }
            if (shopItem != null && shopItem.type == "USE") {
                val value = shopItem.statPl.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 10
                DungeonUsableItem(
                    id = shopItem.id,
                    name = if (currentLang == "Polski") shopItem.namePl else shopItem.nameEn,
                    statText = if (currentLang == "Polski") shopItem.statPl else shopItem.statEn,
                    icon = shopItem.icon,
                    isHpPotion = shopItem.id.contains("pot_hp"),
                    isScroll = shopItem.id.contains("scroll"),
                    restoreAmount = value
                )
            } else null
        }
    }

    fun handleDungeonEscape(isGuaranteed: Boolean = false) {
        val roll = Random.nextInt(1, 101)
        if (isGuaranteed || roll <= escapeChance) {
            val msg = if (currentLang == "Polski") "Pomyślnie uciekasz z lochu!" else "You successfully escaped the dungeon!"
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            (context as? Activity)?.finish()
        } else {
            isWaveIntermission = false
            isItemMenuOpen = false
            val failMsg = if (currentLang == "Polski") "Ucieczka nieudana! Potwory zablokowały drogę odwrotu!" else "Escape failed! Monsters blocked your retreat!"
            Toast.makeText(context, failMsg, Toast.LENGTH_LONG).show()
            combatLogs.add(failMsg to Color(0xFFFF3D00))
        }
    }

    fun useItemInDungeon(item: DungeonUsableItem) {
        val inventoryString = prefs.getString("inventory_items", "") ?: ""
        val ids = inventoryString.split(",").toMutableList()
        ids.remove(item.id)
        prefs.edit().putString("inventory_items", ids.filter { it.isNotEmpty() }.joinToString(",")).apply()

        if (item.isScroll) {
            handleDungeonEscape(isGuaranteed = true)
        } else if (item.isHpPotion) {
            playerHp = (playerHp + item.restoreAmount).coerceAtMost(maxPlayerHp)
            val logText = if (currentLang == "Polski") "Używasz ${item.name} i odnawiasz ${item.restoreAmount} HP." else "You use ${item.name} and restore ${item.restoreAmount} HP."
            combatLogs.add(logText to Color(0xFF00E676))
        } else {
            availableSteps += item.restoreAmount
            val logText = if (currentLang == "Polski") "Używasz ${item.name} i zyskujesz ${item.restoreAmount} kroków walki." else "You use ${item.name} and gain ${item.restoreAmount} combat steps."
            combatLogs.add(logText to Color(0xFF00B0FF))
        }

        loadUsableItemsFromInventory()
    }

    LaunchedEffect(Unit) {
        authManager.loadDashboardData { success, data, _ ->
            if (success && data != null) {
                availableSteps = (data["combatSteps"] as? Long)?.toInt() ?: (data["steps"] as? Long)?.toInt() ?: 0
                if (availableSteps < reducedCostToHit) {
                    showNoStepsDefeatDialog = true
                }
            }
        }
    }

    LaunchedEffect(isWaveIntermission) {
        if (isWaveIntermission) {
            intermissionTicksLeft = 10
            while (intermissionTicksLeft > 0 && isWaveIntermission) {
                delay(1000)
                intermissionTicksLeft--
            }
            if (isWaveIntermission) {
                isWaveIntermission = false
                isItemMenuOpen = false
            }
        }
    }

    if (showNoStepsDefeatDialog) {
        AlertDialog(
            onDismissRequest = { },
            confirmButton = {
                Button(
                    onClick = {
                        showNoStepsDefeatDialog = false
                        (context as? Activity)?.finish()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B0000), contentColor = Color.White),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(if (currentLang == "Polski") "OPUŚĆ LOCH" else "LEAVE DUNGEON", fontWeight = FontWeight.Black)
                }
            },
            title = {
                Text(
                    text = if (currentLang == "Polski") "KONIEC SIŁY WALKI!" else "OUT OF COMBAT STEPS!",
                    color = Color.Red,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = if (currentLang == "Polski")
                        "Nie miałeś wystarczającej siły, by dalej walczyć! Twoje kroki bojowe spadły poniżej wymaganego kosztu ataku. Zostałeś bezwzględnie pokonany przez grasującego tu potwora!"
                    else "You had no strength left to fight! Your combat steps dropped below the required strike cost. You were brutally defeated by the lurking monster!",
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            // Ramka została poprawnie przeniesiona jako modyfikator do parametru modifier
            modifier = Modifier.border(BorderStroke(1.dp, Color.Red.copy(alpha = 0.4f)), RoundedCornerShape(8.dp)),
            containerColor = CardBg,
            shape = RoundedCornerShape(8.dp)
        )
    }

    Scaffold(
        topBar = { TopStepFighterBar() },
        bottomBar = {
            Spacer(modifier = Modifier.navigationBarsPadding())
        },
        containerColor = BgColor
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
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
                                Box(modifier = Modifier.fillMaxWidth(playerHp / maxPlayerHp.toFloat()).fillMaxHeight().background(GoldColor, RoundedCornerShape(2.dp)))
                            }
                            Text("$playerHp / $maxPlayerHp", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
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
                        enabled = !isWaveIntermission,
                        onClick = {
                            if (availableSteps >= reducedCostToHit && playerHp > 0) {
                                authManager.spendStepsInFight(reducedCostToHit) { success, newStepsTotal, msg ->
                                    if (success) {
                                        availableSteps = newStepsTotal
                                        val playerRoll = Random.nextInt(10, 19)
                                        val logText = if (currentLang == "Polski") "Zadajesz $playerRoll obrażeń." else "You deal $playerRoll damage."

                                        combatLogs.add(logText to Color(0xFF64DD17))

                                        if (currentEnemyHp - playerRoll <= 0) {
                                            currentEnemyHp = 0
                                            if (currentWaveIndex < levelData.enemies.size - 1) {
                                                currentWaveIndex++
                                                currentEnemyHp = levelData.enemies[currentWaveIndex].maxHp
                                                isWaveIntermission = true
                                            } else {
                                                val maxUnlocked = prefs.getInt("unlocked_level", 1)
                                                if (dungeonId == maxUnlocked) {
                                                    prefs.edit().putInt("unlocked_level", dungeonId + 1).apply()
                                                }
                                                Toast.makeText(context, victoryMsg, Toast.LENGTH_LONG).show()
                                                (context as? Activity)?.finish()
                                            }
                                        } else {
                                            currentEnemyHp -= playerRoll
                                            val minDmg = (currentEnemy.power * 0.3).toInt()
                                            val maxDmg = (currentEnemy.power * 0.5).toInt()
                                            val dmgTaken = Random.nextInt(minDmg.coerceAtLeast(1), (maxDmg + 1).coerceAtLeast(2))

                                            playerHp -= dmgTaken
                                            val enemyName = context.getString(currentEnemy.nameRes)
                                            val logEnemy = if (currentLang == "Polski") "$enemyName zadaje $dmgTaken obrażeń." else "$enemyName deals $dmgTaken damage."
                                            combatLogs.add(logEnemy to Color(0xFFFF5252))

                                            if (playerHp <= 0) {
                                                playerHp = 0
                                                Toast.makeText(context, context.getString(R.string.locked), Toast.LENGTH_LONG).show()
                                                resetDungeon()
                                            } else if (availableSteps < reducedCostToHit) {
                                                showNoStepsDefeatDialog = true
                                            }
                                        }
                                    } else {
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else if (availableSteps < reducedCostToHit) {
                                showNoStepsDefeatDialog = true
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
                                Text(stringResource(R.string.cost_label, reducedCostToHit), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = BgColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
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
                            combatLogs.takeLast(4).forEach { (log, color) ->
                                Text(log, color = color, fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
                            }
                        }
                    }
                }
            }

            if (isWaveIntermission) {
                val nextEnemyName = stringResource(levelData.enemies[currentWaveIndex].nameRes).uppercase()
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.85f))
                        .zIndex(10f),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.88f)
                            .padding(16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        border = BorderStroke(1.dp, GoldColor.copy(alpha = 0.25f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = if (currentLang == "Polski") "WRÓG POKONANY!" else "ENEMY DEFEATED!",
                                color = GoldColor,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp
                            )

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = if (currentLang == "Polski")
                                        "Następny wróg ($nextEnemyName) pojawi się za:"
                                    else "Next enemy ($nextEnemyName) approaches in:",
                                    color = TextGray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${intermissionTicksLeft}s",
                                    color = Color.White,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            if (!isItemMenuOpen) {
                                Button(
                                    onClick = { isWaveIntermission = false },
                                    modifier = Modifier.fillMaxWidth().height(46.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldColor, contentColor = BgColor),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(if (currentLang == "Polski") "POMIŃ CZEKANIE" else "SKIP WAITING", fontWeight = FontWeight.Black, fontSize = 13.sp)
                                }

                                Button(
                                    onClick = {
                                        loadUsableItemsFromInventory()
                                        isItemMenuOpen = true
                                    },
                                    modifier = Modifier.fillMaxWidth().height(44.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentColor, contentColor = BgColor),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(if (currentLang == "Polski") "UŻYJ PRZEDMIOTU Z EKWIPUNKU" else "USE ITEM FROM INVENTORY", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(
                                        enabled = !hasMeditatedInThisDungeon,
                                        onClick = {
                                            hasMeditatedInThisDungeon = true
                                            val pct = Random.nextInt(10, 51)
                                            val healAmount = (maxPlayerHp * pct) / 100
                                            playerHp = (playerHp + healAmount).coerceAtMost(maxPlayerHp)

                                            val logText = if (currentLang == "Polski")
                                                "Skupiasz myśli w medytacji. Odnawiasz $pct% ($healAmount HP) oraz jedno użycie czaru!"
                                            else "You focus your mind in meditation. Restored $pct% ($healAmount HP) and one spell charge!"
                                            combatLogs.add(logText to Color(0xFF00B0FF))
                                        },
                                        modifier = Modifier.fillMaxWidth().height(44.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF00838F),
                                            disabledContainerColor = Color(0xFF212121),
                                            disabledContentColor = Color.Gray
                                        ),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(if (currentLang == "Polski") "MEDYTACJA" else "MEDITATE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = if (currentLang == "Polski")
                                            "Przywraca losowo 10-50% HP i odnawia 1 czar (TYLKO RAZ NA CALY DUNGEON)"
                                        else "Restores random 10-50% HP and refunds 1 spell charge (ONLY ONCE PER DUNGEON)",
                                        color = Color.Gray,
                                        fontSize = 9.sp,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 13.sp
                                    )
                                }

                                OutlinedButton(
                                    onClick = { handleDungeonEscape(isGuaranteed = false) },
                                    modifier = Modifier.fillMaxWidth().height(44.dp),
                                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (currentLang == "Polski") "UCIECZKA Z LOCHU ($escapeChance%)" else "ESCAPE DUNGEON ($escapeChance%)",
                                        color = Color.Red,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            } else {
                                Text(
                                    text = if (currentLang == "Polski") "Wybierz przedmiot z plecaka:" else "Select an item from backpack:",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (usableItemsInInventory.isEmpty()) {
                                    Text(
                                        text = if (currentLang == "Polski") "Brak przedmiotów użytkowych w ekwipunku!" else "No usable items in inventory!",
                                        color = Color.Gray, fontSize = 11.sp, fontStyle = FontStyle.Italic,
                                        modifier = Modifier.padding(vertical = 12.dp)
                                    )
                                } else {
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(3),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.height(110.dp)
                                    ) {
                                        items(usableItemsInInventory) { usableItem ->
                                            Box(
                                                modifier = Modifier
                                                    .aspectRatio(1f)
                                                    .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
                                                    .border(BorderStroke(1.dp, GoldColor.copy(alpha = 0.3f)), RoundedCornerShape(4.dp))
                                                    .clickable { useItemInDungeon(usableItem) },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.padding(4.dp)) {
                                                    Icon(
                                                        imageVector = usableItem.icon,
                                                        contentDescription = null,
                                                        tint = if (usableItem.isScroll) GoldColor else if (usableItem.isHpPotion) Color.Red else AccentColor,
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                    Spacer(Modifier.height(2.dp))
                                                    Text(usableItem.name.take(7), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                Button(
                                    onClick = { isItemMenuOpen = false },
                                    modifier = Modifier.fillMaxWidth().height(40.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2A2A), contentColor = Color.White),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(if (currentLang == "Polski") "ANULUJ / WRÓĆ" else "CANCEL / BACK", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}