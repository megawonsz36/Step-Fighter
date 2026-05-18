package com.example.stepfighter.ui.inventory

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stepfighter.BaseGameActivity
import com.example.stepfighter.R
import com.example.stepfighter.ui.components.BottomNavigationBar
import com.example.stepfighter.ui.components.SideMenuContent
import com.example.stepfighter.ui.components.TopStepFighterBar
import com.example.stepfighter.ui.profile.*
import com.example.stepfighter.ui.shop.getFullShopItemsList
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class InventoryActivity : BaseGameActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HandleNetworkOverlay {
                InventoryScreen()
            }
        }
    }
}

data class InventoryItem(
    val id: String,
    val icon: ImageVector,
    val color: Color,
    val name: String,
    val slotType: String,
    val isUseable: Boolean,
    val statText: String,
    val loreText: String,
    val levelReq: Int,
    val isCurrentlyEquipped: Boolean = false
)

class SelectedItemState(
    val item: InventoryItem,
    val sourceGridIndex: Int? = null,
    val sourceEquipSlot: String? = null
)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val currentLang = context.getString(R.string.lang_pl)
    val prefs = remember { context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE) }

    val allShopItems = remember { getFullShopItemsList() }

    var headSlot by remember { mutableStateOf<InventoryItem?>(null) }
    var weaponSlot by remember { mutableStateOf<InventoryItem?>(null) }
    var torsoSlot by remember { mutableStateOf<InventoryItem?>(null) }
    var shieldSlot by remember { mutableStateOf<InventoryItem?>(null) }
    var feetSlot by remember { mutableStateOf<InventoryItem?>(null) }

    var gridItems by remember { mutableStateOf<List<InventoryItem?>>(List(16) { null }) }
    var selectedItemState by remember { mutableStateOf<SelectedItemState?>(null) }

    var attackBonus by remember { mutableStateOf(0) }
    var defenseBonus by remember { mutableStateOf(0) }
    var agilityBonus by remember { mutableStateOf(0) }

    var draggedItem by remember { mutableStateOf<InventoryItem?>(null) }
    var dragSourceIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var initialTouchOffset by remember { mutableStateOf(Offset.Zero) }

    val gridBounds = remember { mutableStateMapOf<Int, Rect>() }
    val equipBounds = remember { mutableStateMapOf<String, Rect>() }

    var currentHoveredIndex by remember { mutableStateOf(-1) }
    var currentHoveredEquipSlot by remember { mutableStateOf<String?>(null) }

    fun refreshEquipmentAndGrid() {
        val inventoryString = prefs.getString("inventory_items", "") ?: ""
        val purchasedIds = if (inventoryString.isEmpty()) emptyList() else inventoryString.split(",")

        val newGrid = MutableList<InventoryItem?>(16) { null }
        purchasedIds.forEachIndexed { idx, id ->
            if (idx < 16) {
                val shopItem = allShopItems.find { it.id == id }
                if (shopItem != null) {
                    newGrid[idx] = InventoryItem(
                        id = shopItem.id,
                        icon = shopItem.icon,
                        color = if (shopItem.currency == "TOKENS") GoldColor else Color.White,
                        name = if (currentLang == "Polski") shopItem.namePl else shopItem.nameEn,
                        slotType = shopItem.slot,
                        isUseable = shopItem.type == "USE",
                        statText = if (currentLang == "Polski") shopItem.statPl else shopItem.statEn,
                        loreText = if (currentLang == "Polski") shopItem.lorePl else shopItem.loreEn,
                        levelReq = shopItem.level,
                        isCurrentlyEquipped = false
                    )
                }
            }
        }
        gridItems = newGrid

        val eqHeadId = prefs.getString("eq_head", "") ?: ""
        val eqRightHandId = prefs.getString("eq_right_hand", "") ?: ""
        val eqLeftHandId = prefs.getString("eq_left_hand", "") ?: ""
        val eqChestId = prefs.getString("eq_chest", "") ?: ""
        val eqBootsId = prefs.getString("eq_boots", "") ?: ""

        headSlot = allShopItems.find { it.id == eqHeadId }?.let { shopItem ->
            InventoryItem(shopItem.id, shopItem.icon, GoldColor, if (currentLang == "Polski") shopItem.namePl else shopItem.nameEn, shopItem.slot, false, if (currentLang == "Polski") shopItem.statPl else shopItem.statEn, if (currentLang == "Polski") shopItem.lorePl else shopItem.loreEn, shopItem.level, true)
        }
        weaponSlot = allShopItems.find { it.id == eqRightHandId }?.let { shopItem ->
            InventoryItem(shopItem.id, shopItem.icon, GoldColor, if (currentLang == "Polski") shopItem.namePl else shopItem.nameEn, shopItem.slot, false, if (currentLang == "Polski") shopItem.statPl else shopItem.statEn, if (currentLang == "Polski") shopItem.lorePl else shopItem.loreEn, shopItem.level, true)
        }
        torsoSlot = allShopItems.find { it.id == eqChestId }?.let { shopItem ->
            InventoryItem(shopItem.id, shopItem.icon, GoldColor, if (currentLang == "Polski") shopItem.namePl else shopItem.nameEn, shopItem.slot, false, if (currentLang == "Polski") shopItem.statPl else shopItem.statEn, if (currentLang == "Polski") shopItem.lorePl else shopItem.loreEn, shopItem.level, true)
        }
        shieldSlot = allShopItems.find { it.id == eqLeftHandId }?.let { shopItem ->
            InventoryItem(shopItem.id, shopItem.icon, GoldColor, if (currentLang == "Polski") shopItem.namePl else shopItem.nameEn, shopItem.slot, false, if (currentLang == "Polski") shopItem.statPl else shopItem.statEn, if (currentLang == "Polski") shopItem.lorePl else shopItem.loreEn, shopItem.level, true)
        }
        feetSlot = allShopItems.find { it.id == eqBootsId }?.let { shopItem ->
            InventoryItem(shopItem.id, shopItem.icon, GoldColor, if (currentLang == "Polski") shopItem.namePl else shopItem.nameEn, shopItem.slot, false, if (currentLang == "Polski") shopItem.statPl else shopItem.statEn, if (currentLang == "Polski") shopItem.lorePl else shopItem.loreEn, shopItem.level, true)
        }

        var atk = 0
        var def = 0
        var agi = 0
        listOfNotNull(headSlot, weaponSlot, torsoSlot, shieldSlot, feetSlot).forEach { eq ->
            val numValue = eq.statText.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
            if (eq.statText.contains("Atak") || eq.statText.contains("Attack")) atk += numValue
            if (eq.statText.contains("Pancerz") || eq.statText.contains("Armor") || eq.statText.contains("Obrony")) def += numValue
            if (eq.statText.contains("EXP") || eq.statText.contains("EN") || eq.statText.contains("Unik") || eq.statText.contains("Evasion")) agi += numValue
        }
        attackBonus = atk
        defenseBonus = def
        agilityBonus = agi
    }

    fun saveChangesToPrefs(updatedGrid: List<InventoryItem?>, h: InventoryItem?, w: InventoryItem?, t: InventoryItem?, s: InventoryItem?, f: InventoryItem?) {
        val ids = updatedGrid.mapNotNull { it?.id }
        prefs.edit().putString("inventory_items", ids.joinToString(",")).apply()
        prefs.edit().putString("eq_head", h?.id ?: "").apply()
        prefs.edit().putString("eq_right_hand", w?.id ?: "").apply()
        prefs.edit().putString("eq_chest", t?.id ?: "").apply()
        prefs.edit().putString("eq_left_hand", s?.id ?: "").apply()
        prefs.edit().putString("eq_boots", f?.id ?: "").apply()
        refreshEquipmentAndGrid()
    }

    LaunchedEffect(Unit) {
        refreshEquipmentAndGrid()
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
                    bottomBar = { BottomNavigationBar(selectedIndex = 2) },
                    containerColor = BgColor
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { },
                                    onDrag = { change, dragAmount ->
                                        if (draggedItem != null) {
                                            change.consume()
                                            dragOffset += dragAmount
                                            val currentPos = change.position

                                            currentHoveredIndex = -1
                                            gridBounds.forEach { (idx, rect) ->
                                                if (rect.contains(currentPos)) currentHoveredIndex = idx
                                            }

                                            currentHoveredEquipSlot = null
                                            equipBounds.forEach { (slot, rect) ->
                                                if (rect.contains(currentPos)) currentHoveredEquipSlot = slot
                                            }
                                        }
                                    },
                                    onDragEnd = {
                                        if (draggedItem != null) {
                                            val item = draggedItem!!
                                            var placed = false
                                            val playerLevel = prefs.getInt("player_level", 1)

                                            if (currentHoveredEquipSlot != null && currentHoveredEquipSlot == item.slotType) {
                                                if (playerLevel < item.levelReq) {
                                                    Toast.makeText(context, if (currentLang == "Polski") "Zbyt niski poziom!" else "Level too low!", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    var oldEquipment: InventoryItem? = null
                                                    when (currentHoveredEquipSlot) {
                                                        "HEAD" -> { oldEquipment = headSlot; headSlot = item; placed = true }
                                                        "RIGHT_HAND" -> { oldEquipment = weaponSlot; weaponSlot = item; placed = true }
                                                        "CHEST" -> { oldEquipment = torsoSlot; torsoSlot = item; placed = true }
                                                        "LEFT_HAND" -> { oldEquipment = shieldSlot; shieldSlot = item; placed = true }
                                                        "BOOTS" -> { oldEquipment = feetSlot; feetSlot = item; placed = true }
                                                    }
                                                    if (oldEquipment != null && dragSourceIndex != null && dragSourceIndex!! >= 0) {
                                                        val nextGrid = gridItems.toMutableList()
                                                        nextGrid[dragSourceIndex!!] = oldEquipment
                                                        gridItems = nextGrid
                                                    }
                                                }
                                            } else if (currentHoveredIndex != -1 && gridItems[currentHoveredIndex] == null) {
                                                val newItems = gridItems.toMutableList()
                                                newItems[currentHoveredIndex] = item
                                                gridItems = newItems
                                                placed = true
                                            }

                                            if (!placed) {
                                                when (dragSourceIndex) {
                                                    -1 -> headSlot = item
                                                    -2 -> weaponSlot = item
                                                    -3 -> torsoSlot = item
                                                    -4 -> shieldSlot = item
                                                    -5 -> feetSlot = item
                                                    else -> {
                                                        val newItems = gridItems.toMutableList()
                                                        newItems[dragSourceIndex!!] = item
                                                        gridItems = newItems
                                                    }
                                                }
                                            } else {
                                                selectedItemState = null
                                                saveChangesToPrefs(gridItems, headSlot, weaponSlot, torsoSlot, shieldSlot, feetSlot)
                                            }

                                            draggedItem = null
                                            dragSourceIndex = null
                                            currentHoveredIndex = -1
                                            currentHoveredEquipSlot = null
                                        }
                                    },
                                    onDragCancel = {
                                        if (draggedItem != null) {
                                            val item = draggedItem!!
                                            when (dragSourceIndex) {
                                                -1 -> headSlot = item
                                                -2 -> weaponSlot = item
                                                -3 -> torsoSlot = item
                                                -4 -> shieldSlot = item
                                                -5 -> feetSlot = item
                                                else -> {
                                                    val newItems = gridItems.toMutableList()
                                                    newItems[dragSourceIndex!!] = item
                                                    gridItems = newItems
                                                }
                                            }
                                            draggedItem = null
                                            dragSourceIndex = null
                                        }
                                    }
                                )
                            }
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(24.dp),
                            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
                        ) {
                            item {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        stringResource(R.string.inventory_title),
                                        color = GoldColor,
                                        style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                                    )
                                    Text(stringResource(R.string.inventory_desc), color = TextGray, fontSize = 11.sp)
                                }
                            }

                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(8.dp)).padding(24.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                        EquipmentSlot(
                                            label = stringResource(R.string.slot_head), icon = Icons.Default.MilitaryTech, item = headSlot, isSmall = true,
                                            isHovered = currentHoveredEquipSlot == "HEAD",
                                            onPositioned = { equipBounds["HEAD"] = it },
                                            onSlotClick = {
                                                headSlot?.let { selectedItemState = SelectedItemState(it, sourceEquipSlot = "HEAD") }
                                            },
                                            onTriggerDrag = { offset, item ->
                                                draggedItem = item
                                                dragSourceIndex = -1
                                                initialTouchOffset = offset
                                                dragOffset = Offset.Zero
                                                headSlot = null
                                            }
                                        )
                                        Spacer(Modifier.height(16.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                            EquipmentSlot(
                                                label = stringResource(R.string.slot_right_hand), icon = Icons.Default.Hardware, item = weaponSlot,
                                                isHovered = currentHoveredEquipSlot == "RIGHT_HAND",
                                                onPositioned = { equipBounds["RIGHT_HAND"] = it },
                                                onSlotClick = {
                                                    weaponSlot?.let { selectedItemState = SelectedItemState(it, sourceEquipSlot = "RIGHT_HAND") }
                                                },
                                                onTriggerDrag = { offset, item ->
                                                    draggedItem = item
                                                    dragSourceIndex = -2
                                                    initialTouchOffset = offset
                                                    dragOffset = Offset.Zero
                                                    weaponSlot = null
                                                }
                                            )
                                            EquipmentSlot(
                                                label = stringResource(R.string.slot_torso), icon = Icons.Default.Checkroom, item = torsoSlot,
                                                isHovered = currentHoveredEquipSlot == "CHEST",
                                                onPositioned = { equipBounds["CHEST"] = it },
                                                onSlotClick = {
                                                    torsoSlot?.let { selectedItemState = SelectedItemState(it, sourceEquipSlot = "CHEST") }
                                                },
                                                onTriggerDrag = { offset, item ->
                                                    draggedItem = item
                                                    dragSourceIndex = -3
                                                    initialTouchOffset = offset
                                                    dragOffset = Offset.Zero
                                                    torsoSlot = null
                                                }
                                            )
                                            EquipmentSlot(
                                                label = stringResource(R.string.slot_left_hand), icon = Icons.Default.Shield, item = shieldSlot,
                                                isHovered = currentHoveredEquipSlot == "LEFT_HAND",
                                                onPositioned = { equipBounds["LEFT_HAND"] = it },
                                                onSlotClick = {
                                                    shieldSlot?.let { selectedItemState = SelectedItemState(it, sourceEquipSlot = "LEFT_HAND") }
                                                },
                                                onTriggerDrag = { offset, item ->
                                                    draggedItem = item
                                                    dragSourceIndex = -4
                                                    initialTouchOffset = offset
                                                    dragOffset = Offset.Zero
                                                    shieldSlot = null
                                                }
                                            )
                                        }
                                        Spacer(Modifier.height(16.dp))
                                        EquipmentSlot(
                                            label = stringResource(R.string.slot_feet), icon = Icons.Default.DirectionsWalk, item = feetSlot, isSmall = true,
                                            isHovered = currentHoveredEquipSlot == "BOOTS",
                                            onPositioned = { equipBounds["BOOTS"] = it },
                                            onSlotClick = {
                                                feetSlot?.let { selectedItemState = SelectedItemState(it, sourceEquipSlot = "BOOTS") }
                                            },
                                            onTriggerDrag = { offset, item ->
                                                draggedItem = item
                                                dragSourceIndex = -5
                                                initialTouchOffset = offset
                                                dragOffset = Offset.Zero
                                                feetSlot = null
                                            }
                                        )

                                        Spacer(Modifier.height(24.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                            StatItem("$attackBonus", stringResource(R.string.stat_attack))
                                            StatItem("$defenseBonus", stringResource(R.string.stat_defense))
                                            StatItem("$agilityBonus", stringResource(R.string.stat_agility))
                                        }
                                    }
                                }
                            }

                            item {
                                InventoryGrid(
                                    items = gridItems,
                                    hoveredIndex = currentHoveredIndex,
                                    onSlotPositioned = { index, rect -> gridBounds[index] = rect },
                                    onSlotTap = { index, item ->
                                        selectedItemState = SelectedItemState(item, sourceGridIndex = index)
                                    },
                                    onActiveDragTrigger = { index, item, globalPos ->
                                        selectedItemState = SelectedItemState(item, sourceGridIndex = index)
                                        draggedItem = item
                                        dragSourceIndex = index
                                        initialTouchOffset = globalPos
                                        dragOffset = Offset.Zero
                                        val newItems = gridItems.toMutableList()
                                        newItems[index] = null
                                        gridItems = newItems
                                    }
                                )
                            }

                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(2.dp)).background(AttributePanelBg).padding(16.dp)
                                ) {
                                    Column {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Text(selectedItemState?.item?.name ?: "---", color = AttributeText, fontWeight = FontWeight.Black, fontSize = 18.sp)
                                            Surface(color = AttributeText.copy(alpha = 0.1f), shape = RoundedCornerShape(2.dp), border = BorderStroke(1.dp, AttributeText.copy(alpha = 0.2f))) {
                                                Text(
                                                    text = if (selectedItemState?.item?.color == GoldColor) "RARE" else "COMMON",
                                                    color = AttributeText, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Text(
                                            selectedItemState?.item?.loreText ?: (if (currentLang == "Polski") "Zaznacz przedmiot z plecaka lub rynsztunku, aby ujrzeć jego właściwości i zarządzać nim." else "Select an item from backpack or gear to view stats and manage it."),
                                            color = AttributeText.copy(alpha = 0.8f),
                                            fontStyle = FontStyle.Italic,
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                        Text(selectedItemState?.item?.statText ?: "", color = AttributeText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Spacer(Modifier.height(12.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(stringResource(R.string.item_req_level, selectedItemState?.item?.levelReq ?: 1), color = AttributeText.copy(alpha = 0.5f), fontSize = 10.sp)
                                        }
                                    }
                                }
                            }

                            item {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Button(
                                        enabled = selectedItemState != null,
                                        onClick = {
                                            selectedItemState?.let { state ->
                                                val playerLevel = prefs.getInt("player_level", 1)
                                                if (state.item.isCurrentlyEquipped) {
                                                    val freeIndex = gridItems.indexOf(null)
                                                    if (freeIndex == -1) {
                                                        Toast.makeText(context, if (currentLang == "Polski") "Brak miejsca w plecaku!" else "No space in backpack!", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        when (state.sourceEquipSlot) {
                                                            "HEAD" -> headSlot = null
                                                            "RIGHT_HAND" -> weaponSlot = null
                                                            "CHEST" -> torsoSlot = null
                                                            "LEFT_HAND" -> shieldSlot = null
                                                            "BOOTS" -> feetSlot = null
                                                        }
                                                        val nextGrid = gridItems.toMutableList()
                                                        nextGrid[freeIndex] = state.item.copy(isCurrentlyEquipped = false)
                                                        gridItems = nextGrid
                                                        saveChangesToPrefs(gridItems, headSlot, weaponSlot, torsoSlot, shieldSlot, feetSlot)
                                                        selectedItemState = null
                                                        Toast.makeText(context, if (currentLang == "Polski") "Zdjmowano rynsztunek!" else "Equipment unequipped!", Toast.LENGTH_SHORT).show()
                                                    }
                                                } else if (state.item.isUseable) {
                                                    val cleanGrid = gridItems.toMutableList()
                                                    if (state.sourceGridIndex != null) {
                                                        cleanGrid[state.sourceGridIndex] = null
                                                    } else {
                                                        cleanGrid.remove(state.item)
                                                    }
                                                    saveChangesToPrefs(cleanGrid, headSlot, weaponSlot, torsoSlot, shieldSlot, feetSlot)
                                                    selectedItemState = null
                                                    Toast.makeText(context, if (currentLang == "Polski") "Użyto przedmiotu!" else "Item consumed!", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    if (playerLevel < state.item.levelReq) {
                                                        Toast.makeText(context, if (currentLang == "Polski") "Zbyt niski poziom!" else "Level too low!", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        var oldEquipment: InventoryItem? = null
                                                        when (state.item.slotType) {
                                                            "HEAD" -> { oldEquipment = headSlot; headSlot = state.item.copy(isCurrentlyEquipped = true) }
                                                            "RIGHT_HAND" -> { oldEquipment = weaponSlot; weaponSlot = state.item.copy(isCurrentlyEquipped = true) }
                                                            "CHEST" -> { oldEquipment = torsoSlot; torsoSlot = state.item.copy(isCurrentlyEquipped = true) }
                                                            "LEFT_HAND" -> { oldEquipment = shieldSlot; shieldSlot = state.item.copy(isCurrentlyEquipped = true) }
                                                            "BOOTS" -> { oldEquipment = feetSlot; feetSlot = state.item.copy(isCurrentlyEquipped = true) }
                                                        }
                                                        val nextGrid = gridItems.toMutableList()
                                                        if (state.sourceGridIndex != null) {
                                                            nextGrid[state.sourceGridIndex] = oldEquipment?.copy(isCurrentlyEquipped = false)
                                                        }
                                                        gridItems = nextGrid
                                                        saveChangesToPrefs(gridItems, headSlot, weaponSlot, torsoSlot, shieldSlot, feetSlot)
                                                        selectedItemState = null
                                                        Toast.makeText(context, if (currentLang == "Polski") "Założono przedmiot!" else "Gear equipped!", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentColor),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        val btnText = if (selectedItemState?.item?.isCurrentlyEquipped == true) {
                                            if (currentLang == "Polski") "ZDEJMIJ RYNSZTUNEK" else "UNEQUIP GEAR"
                                        } else if (selectedItemState?.item?.isUseable == true) {
                                            if (currentLang == "Polski") "UŻYJ PRZEDMIOTU" else "USE ITEM"
                                        } else {
                                            if (currentLang == "Polski") "ZAŁÓŻ RYNSZTUNEK" else "EQUIP GEAR"
                                        }
                                        Text(btnText, color = BgColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    OutlinedButton(
                                        enabled = selectedItemState != null,
                                        onClick = {
                                            selectedItemState?.let { state ->
                                                val cleanGrid = gridItems.toMutableList()
                                                if (state.item.isCurrentlyEquipped) {
                                                    var h = headSlot; var w = weaponSlot; var t = torsoSlot; var s = shieldSlot; var f = feetSlot
                                                    when (state.sourceEquipSlot) {
                                                        "HEAD" -> h = null
                                                        "RIGHT_HAND" -> w = null
                                                        "CHEST" -> t = null
                                                        "LEFT_HAND" -> s = null
                                                        "BOOTS" -> f = null
                                                    }
                                                    saveChangesToPrefs(cleanGrid, h, w, t, s, f)
                                                } else {
                                                    if (state.sourceGridIndex != null) {
                                                        cleanGrid[state.sourceGridIndex] = null
                                                    }
                                                    saveChangesToPrefs(cleanGrid, headSlot, weaponSlot, torsoSlot, shieldSlot, feetSlot)
                                                }
                                                selectedItemState = null
                                            }
                                        },
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(stringResource(R.string.drop_item), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        if (draggedItem != null) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .offset {
                                        IntOffset(
                                            (initialTouchOffset.x + dragOffset.x - 32.dp.toPx()).roundToInt(),
                                            (initialTouchOffset.y + dragOffset.y - 32.dp.toPx()).roundToInt()
                                        )
                                    }
                                    .background(draggedItem!!.color.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                    .border(BorderStroke(2.dp, AccentColor), RoundedCornerShape(4.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(draggedItem!!.icon, null, tint = draggedItem!!.color, modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

fun LayoutCoordinates.getBoundsInWindow(): Rect {
    val pos = positionInWindow()
    return Rect(pos.x, pos.y, pos.x + size.width, pos.y + size.height)
}

@Composable
fun InventoryGrid(
    items: List<InventoryItem?>,
    hoveredIndex: Int,
    onSlotPositioned: (Int, Rect) -> Unit,
    onSlotTap: (Int, InventoryItem) -> Unit,
    onActiveDragTrigger: (Int, InventoryItem, Offset) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(4.dp)).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(4) { rowIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(4) { colIndex ->
                    val index = rowIndex * 4 + colIndex
                    val item = items[index]
                    var layoutCoordinates: LayoutCoordinates? by remember { mutableStateOf(null) }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .onGloballyPositioned {
                                layoutCoordinates = it
                                onSlotPositioned(index, it.getBoundsInWindow())
                            }
                            .clickable(enabled = item != null) {
                                if (item != null) onSlotTap(index, item)
                            }
                            .pointerInput(item) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { offset ->
                                        if (item != null && layoutCoordinates != null) {
                                            onActiveDragTrigger(index, item, layoutCoordinates!!.positionInWindow() + offset)
                                        }
                                    },
                                    onDrag = { _, _ -> },
                                    onDragEnd = { },
                                    onDragCancel = { }
                                )
                            }
                    ) {
                        InventoryGridSlot(item != null, item, isHovered = index == hoveredIndex)
                    }
                }
            }
        }
    }
}

@Composable
fun EquipmentSlot(
    label: String,
    icon: ImageVector,
    item: InventoryItem? = null,
    isSmall: Boolean = false,
    isHovered: Boolean = false,
    onPositioned: (Rect) -> Unit = {},
    onSlotClick: () -> Unit = {},
    onTriggerDrag: (Offset, InventoryItem) -> Unit = { _, _ -> }
) {
    var layoutCoordinates: LayoutCoordinates? by remember { mutableStateOf(null) }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(if (isSmall) 48.dp else 64.dp)
                .onGloballyPositioned {
                    layoutCoordinates = it
                    onPositioned(it.getBoundsInWindow())
                }
                .border(
                    BorderStroke(
                        if (isHovered) 2.dp else 1.dp,
                        if (isHovered) AccentColor else Color.White.copy(alpha = 0.1f)
                    ),
                    RoundedCornerShape(4.dp)
                )
                .background(if (item != null) Color.White.copy(alpha = 0.05f) else Color.Transparent)
                .clickable(enabled = item != null) {
                    onSlotClick()
                }
                .pointerInput(item) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = { offset ->
                            if (item != null && layoutCoordinates != null) {
                                onTriggerDrag(layoutCoordinates!!.positionInWindow() + offset, item)
                            }
                        },
                        onDrag = { _, _ -> },
                        onDragEnd = { },
                        onDragCancel = { }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                item?.icon ?: icon,
                contentDescription = null,
                tint = if (item != null) item.color else Color.White.copy(alpha = 0.1f),
                modifier = Modifier.size(if (isSmall) 20.dp else 28.dp)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(label, color = TextGray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun InventoryGridSlot(hasItem: Boolean, item: InventoryItem? = null, isHovered: Boolean = false) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .border(
                BorderStroke(
                    if (isHovered) 2.dp else 1.dp,
                    if (isHovered) AccentColor else Color.White.copy(alpha = 0.05f)
                ),
                RoundedCornerShape(2.dp)
            )
            .background(if (hasItem) Color.White.copy(alpha = 0.03f) else Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        if (item != null) {
            Icon(item.icon, contentDescription = null, tint = item.color, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = GoldColor, fontSize = 18.sp, fontWeight = FontWeight.Black)
        Text(label, color = TextGray, fontSize = 10.sp)
    }
}