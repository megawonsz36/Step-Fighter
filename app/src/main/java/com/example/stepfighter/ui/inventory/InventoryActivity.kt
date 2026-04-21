package com.example.stepfighter.ui.inventory

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.stepfighter.R
import com.example.stepfighter.ui.components.BottomNavigationBar
import com.example.stepfighter.ui.components.SideMenuContent
import com.example.stepfighter.ui.components.TopStepFighterBar
import com.example.stepfighter.ui.profile.*
import kotlinx.coroutines.launch

class InventoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InventoryScreen()
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen() {
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
                    bottomBar = {
                        BottomNavigationBar(selectedIndex = 1)
                    },
                    containerColor = BgColor
                ) { innerPadding ->
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
                                Text(stringResource(R.string.inventory_title), color = GoldColor, style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp))
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
                                    EquipmentSlot(stringResource(R.string.slot_head), Icons.Default.Face, isSmall = true)
                                    Spacer(Modifier.height(16.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        EquipmentSlot(stringResource(R.string.slot_right_hand), Icons.Default.HorizontalRule, hasItem = true, isSelected = true)
                                        EquipmentSlot(stringResource(R.string.slot_torso), Icons.Default.Checkroom, hasItem = true)
                                        EquipmentSlot(stringResource(R.string.slot_left_hand), Icons.Default.Shield)
                                    }
                                    Spacer(Modifier.height(16.dp))
                                    EquipmentSlot(stringResource(R.string.slot_feet), Icons.Default.IceSkating, isSmall = true)

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
                            InventoryGrid()
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
                }
            }
        }
    }
}

@Composable
fun InventoryGrid() {
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
                    val hasItem = index < 3
                    val isSelected = index == 0
                    Box(modifier = Modifier.weight(1f)) {
                        InventoryGridSlot(hasItem, isSelected)
                    }
                }
            }
        }
    }
}

@Composable
fun EquipmentSlot(label: String, icon: ImageVector, hasItem: Boolean = false, isSelected: Boolean = false, isSmall: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(if (isSmall) 48.dp else 64.dp)
                .border(
                    BorderStroke(
                        if (isSelected) 2.dp else 1.dp,
                        if (isSelected) AccentColor else Color.White.copy(alpha = 0.1f)
                    ),
                    RoundedCornerShape(4.dp)
                )
                .background(if (hasItem) Color.White.copy(alpha = 0.05f) else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (hasItem) Color.Cyan else Color.White.copy(alpha = 0.1f),
                modifier = Modifier.size(if (isSmall) 20.dp else 28.dp)
            )
            if (isSelected) {
                Box(modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(6.dp).clip(RoundedCornerShape(50)).background(AccentColor))
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(label, color = TextGray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun InventoryGridSlot(hasItem: Boolean, isSelected: Boolean) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .border(
                BorderStroke(
                    if (isSelected) 2.dp else 1.dp,
                    if (isSelected) AccentColor else Color.White.copy(alpha = 0.05f)
                ),
                RoundedCornerShape(2.dp)
            )
            .background(if (hasItem) Color.White.copy(alpha = 0.03f) else Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        if (hasItem) {
            Icon(Icons.Default.WineBar, contentDescription = null, tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(24.dp))
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
