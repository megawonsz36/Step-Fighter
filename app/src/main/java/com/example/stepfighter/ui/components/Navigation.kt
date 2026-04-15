package com.example.stepfighter.ui.components

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import com.example.stepfighter.ui.profile.GoldColor
import com.example.stepfighter.ui.profile.BgColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stepfighter.ui.dashboard.DashboardActivity
import com.example.stepfighter.ui.dungeon.DungeonActivity
import com.example.stepfighter.ui.inventory.InventoryActivity
import com.example.stepfighter.ui.profile.ProfileActivity
import com.example.stepfighter.ui.profile.AccentColor
import com.example.stepfighter.ui.profile.CardBg
import com.example.stepfighter.ui.profile.TextGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopStepFighterBar() {
    CenterAlignedTopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Shield,
                    contentDescription = null,
                    tint = GoldColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "STEP FIGHTER",
                    style = TextStyle(
                        color = GoldColor,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        fontSize = 16.sp
                    )
                )
            }
        },
        actions = {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
                Text(
                    "LEVEL 14",
                    color = TextGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Menu, contentDescription = null, tint = TextGray)
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BgColor)
    )
}

@Composable
fun BottomNavigationBar(selectedIndex: Int) {
    val context = LocalContext.current
    
    Surface(
        color = CardBg,
        modifier = Modifier.fillMaxWidth().height(72.dp),
        shadowElevation = 16.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem("Lochy", Icons.Default.Explore, selectedIndex == 0) {
                if (selectedIndex != 0) {
                    navigateTo(context as Activity, DashboardActivity::class.java)
                }
            }
            NavItem("Ekwipunek", Icons.Default.Inventory, selectedIndex == 1) {
                if (selectedIndex != 1) {
                    navigateTo(context as Activity, InventoryActivity::class.java)
                }
            }
            NavItem("Statystyki", Icons.Default.Person, selectedIndex == 2) {
                if (selectedIndex != 2) {
                    navigateTo(context as Activity, ProfileActivity::class.java)
                }
            }
        }
    }
}

private fun navigateTo(activity: Activity, targetClass: Class<*>) {
    val intent = Intent(activity, targetClass)
    // Flagi zapobiegają tworzeniu stosu wielu tych samych aktywności
    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
    activity.startActivity(intent)
    activity.finish() // Zamykamy poprzednią aktywność, żeby nie wracać do niej przyciskiem wstecz
    // Wyłączamy animacje systemowe
    activity.overridePendingTransition(0, 0)
}

@Composable
fun NavItem(label: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxHeight()
            .clickable { onClick() }
            .padding(horizontal = 12.dp)
    ) {
        Icon(
            icon, 
            contentDescription = null, 
            tint = if (isSelected) AccentColor else TextGray,
            modifier = Modifier.size(24.dp)
        )
        Text(
            label, 
            color = if (isSelected) AccentColor else TextGray, 
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
