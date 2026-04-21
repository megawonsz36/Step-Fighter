package com.example.stepfighter.ui.inventory

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stepfighter.R
import com.example.stepfighter.ui.components.BottomNavigationBar
import com.example.stepfighter.ui.components.SideMenuContent
import com.example.stepfighter.ui.components.TopStepFighterBar
import com.example.stepfighter.ui.profile.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class InventoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InventoryScreen()
        }
    }
}

data class InventoryItem(
    val id: Int,
    val icon: ImageVector,
    val color: Color,
    val name: String
)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Stan ekwipunku (sloty stałe) - Wszystko na null (szare)
    var headSlot by remember { mutableStateOf<InventoryItem?>(null) }
    var weaponSlot by remember { mutableStateOf<InventoryItem?>(null) }
    var torsoSlot by remember { mutableStateOf<InventoryItem?>(null) }
    var shieldSlot by remember { mutableStateOf<InventoryItem?>(null) }
    var feetSlot by remember { mutableStateOf<InventoryItem?>(null) }

    // Stan siatki (16 slotów)
    var gridItems by remember {
        mutableStateOf(
            List(16) { index ->
                if (index < 3) InventoryItem(index, Icons.Default.WineBar, Color.Red.copy(alpha = 0.6f), "Mikstura HP")
                else null
            }
        )
    }

    // Stan przeciągania
    var draggedItem by remember { mutableStateOf<InventoryItem?>(null) }
    var dragSourceIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var initialTouchOffset by remember { mutableStateOf(Offset.Zero) }
    
    // Bounds dla detekcji upuszczania
    val gridBounds = remember { mutableStateMapOf<Int, Rect>() }
    val equipBounds = remember { mutableStateMapOf<String, Rect>() }
    
    var currentHoveredIndex by remember { mutableStateOf(-1) }
    var currentHoveredEquipSlot by remember { mutableStateOf<String?>(null) }

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
                    bottomBar = {
                        BottomNavigationBar(selectedIndex = 1)
                    },
                    containerColor = BgColor
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .padding(horizontal = 24.dp),
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
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(8.dp))
                                        .padding(24.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                        EquipmentSlot(
                                            stringResource(R.string.slot_head), Icons.Default.Face, headSlot, isSmall = true, 
                                            isHovered = currentHoveredEquipSlot == "head",
                                            onPositioned = { equipBounds["head"] = it },
                                            onDragStart = { offset, item ->
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
                                                stringResource(R.string.slot_right_hand), Icons.Default.HorizontalRule, weaponSlot,
                                                isHovered = currentHoveredEquipSlot == "weapon",
                                                onPositioned = { equipBounds["weapon"] = it },
                                                onDragStart = { offset, item ->
                                                    draggedItem = item
                                                    dragSourceIndex = -2
                                                    initialTouchOffset = offset
                                                    dragOffset = Offset.Zero
                                                    weaponSlot = null
                                                }
                                            )
                                            EquipmentSlot(
                                                stringResource(R.string.slot_torso), Icons.Default.Checkroom, torsoSlot,
                                                isHovered = currentHoveredEquipSlot == "torso",
                                                onPositioned = { equipBounds["torso"] = it },
                                                onDragStart = { offset, item ->
                                                    draggedItem = item
                                                    dragSourceIndex = -3
                                                    initialTouchOffset = offset
                                                    dragOffset = Offset.Zero
                                                    torsoSlot = null
                                                }
                                            )
                                            EquipmentSlot(
                                                stringResource(R.string.slot_left_hand), Icons.Default.Shield, shieldSlot,
                                                isHovered = currentHoveredEquipSlot == "shield",
                                                onPositioned = { equipBounds["shield"] = it },
                                                onDragStart = { offset, item ->
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
                                            stringResource(R.string.slot_feet), Icons.Default.IceSkating, feetSlot, isSmall = true,
                                            isHovered = currentHoveredEquipSlot == "feet",
                                            onPositioned = { equipBounds["feet"] = it },
                                            onDragStart = { offset, item ->
                                                draggedItem = item
                                                dragSourceIndex = -5
                                                initialTouchOffset = offset
                                                dragOffset = Offset.Zero
                                                feetSlot = null
                                            }
                                        )

                                        Spacer(Modifier.height(24.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                            StatItem("69", stringResource(R.string.stat_attack))
                                            StatItem("69", stringResource(R.string.stat_defense))
                                            StatItem("69", stringResource(R.string.stat_agility))
                                        }
                                    }
                                }
                            }

                            item {
                                InventoryGrid(
                                    items = gridItems,
                                    hoveredIndex = currentHoveredIndex,
                                    onSlotPositioned = { index, rect -> gridBounds[index] = rect },
                                    onDragStart = { index, offset, item ->
                                        draggedItem = item
                                        dragSourceIndex = index
                                        initialTouchOffset = offset
                                        dragOffset = Offset.Zero
                                        val newItems = gridItems.toMutableList()
                                        newItems[index] = null
                                        gridItems = newItems
                                    }
                                )
                            }

                            item {
                                ItemDetailsBox()
                            }

                            item {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Button(
                                        onClick = {},
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentColor),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(stringResource(R.string.use_item), color = BgColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    OutlinedButton(
                                        onClick = {},
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(stringResource(R.string.drop_item), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        // Obsługa przeciągania globalna
                        if (draggedItem != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(Unit) {
                                        detectDragGesturesAfterLongPress(
                                            onDragStart = { },
                                            onDrag = { change, dragAmount ->
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
                                            },
                                            onDragEnd = {
                                                val item = draggedItem!!
                                                var placed = false
                                                
                                                if (currentHoveredIndex != -1 && gridItems[currentHoveredIndex] == null) {
                                                    val newItems = gridItems.toMutableList()
                                                    newItems[currentHoveredIndex] = item
                                                    gridItems = newItems
                                                    placed = true
                                                } else if (currentHoveredEquipSlot != null) {
                                                    when (currentHoveredEquipSlot) {
                                                        "head" -> { headSlot = item; placed = true }
                                                        "weapon" -> { weaponSlot = item; placed = true }
                                                        "torso" -> { torsoSlot = item; placed = true }
                                                        "shield" -> { shieldSlot = item; placed = true }
                                                        "feet" -> { feetSlot = item; placed = true }
                                                    }
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
                                                }
                                                
                                                draggedItem = null
                                                dragSourceIndex = null
                                                currentHoveredIndex = -1
                                                currentHoveredEquipSlot = null
                                            },
                                            onDragCancel = {
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
                                        )
                                    }
                            )

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
    onDragStart: (Int, Offset, InventoryItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(4.dp))
            .padding(12.dp),
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
                            .pointerInput(item) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { offset -> 
                                        if (item != null && layoutCoordinates != null) {
                                            onDragStart(index, layoutCoordinates!!.positionInWindow() + offset, item) 
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
    onDragStart: (Offset, InventoryItem) -> Unit = { _, _ -> }
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
                .pointerInput(item) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = { offset -> 
                            if (item != null && layoutCoordinates != null) {
                                onDragStart(layoutCoordinates!!.positionInWindow() + offset, item) 
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

@Composable
fun ItemDetailsBox() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(2.dp))
            .background(AttributePanelBg)
            .padding(16.dp)
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.item_name_sword), color = AttributeText, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Surface(color = AttributeText.copy(alpha = 0.1f), shape = RoundedCornerShape(2.dp), border = BorderStroke(1.dp, AttributeText.copy(alpha = 0.2f))) {
                    Text(stringResource(R.string.item_rarity_rare), color = AttributeText, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                }
            }
            Text(
                stringResource(R.string.item_desc_sword),
                color = AttributeText.copy(alpha = 0.8f),
                fontStyle = FontStyle.Italic,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.item_stat_attack_bonus), color = AttributeText, fontSize = 13.sp)
                Text("+69", color = AttributeText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.item_stat_agility_bonus), color = AttributeText, fontSize = 13.sp)
                Text("+69", color = AttributeText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.item_req_level, 69), color = AttributeText.copy(alpha = 0.5f), fontSize = 10.sp)
                Text(stringResource(R.string.item_value, 69), color = AttributeText.copy(alpha = 0.5f), fontSize = 10.sp)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF131313)
@Composable
fun InventoryPreview() {
    InventoryScreen()
}
