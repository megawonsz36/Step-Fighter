package com.example.stepfighter.ui.profile

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.stepfighter.BaseGameActivity
import com.example.stepfighter.R
import com.example.stepfighter.ui.components.BottomNavigationBar
import com.example.stepfighter.ui.components.SideMenuContent
import com.example.stepfighter.ui.components.TopStepFighterBar
import kotlinx.coroutines.launch

val BgColor = Color(0xFF131313)
val CardBg = Color(0xFF1C1C1C)
val AccentColor = Color(0xFFFFB783)
val GoldColor = Color(0xFFE9C349)
val TextGray = Color(0xFF9A8F80)
val AttributePanelBg = Color(0xFFC39D91)
val AttributeText = Color(0xFF50342C)

class ProfileActivity : BaseGameActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HandleNetworkOverlay {
                StatystykiSpojnyProfil()
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatystykiSpojnyProfil() {
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
                    bottomBar = { BottomNavigationBar(selectedIndex = 2) },
                    containerColor = BgColor
                ) { innerPadding ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(28.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
                    ) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stringResource(R.string.total_steps_label),
                                    color = TextGray,
                                    style = TextStyle(fontSize = 12.sp, letterSpacing = 3.sp)
                                )
                                Text(
                                    text = "0",
                                    color = AccentColor,
                                    style = TextStyle(fontSize = 58.sp, fontWeight = FontWeight.Black, letterSpacing = (-2).sp)
                                )
                                Text(
                                    text = stringResource(R.string.warrior_rank),
                                    color = GoldColor,
                                    fontStyle = FontStyle.Italic,
                                    style = TextStyle(fontSize = 16.sp)
                                )
                            }
                        }

                        item {
                            AttributesBox()
                        }

                        item {
                            SectionTitle(stringResource(R.string.weekly_progress_title))
                            WeeklyProgressChart()
                        }

                        item {
                            SectionTitle(stringResource(R.string.war_achievements))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                AchievementCard(
                                    title = stringResource(R.string.ach_path_slayer),
                                    desc = stringResource(R.string.ach_path_slayer_desc, 0),
                                    icon = Icons.Default.EmojiEvents,
                                    isActive = true,
                                    modifier = Modifier.weight(1f)
                                )
                                AchievementCard(
                                    title = stringResource(R.string.ach_peak_lord),
                                    desc = stringResource(R.string.locked),
                                    icon = Icons.Default.Lock,
                                    isActive = false,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 12.dp)
    ) {
        Icon(
            Icons.Default.Square,
            contentDescription = null,
            tint = GoldColor,
            modifier = Modifier.size(8.dp).rotate(45f)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            color = Color.White,
            style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        )
    }
}

@Composable
fun AttributesBox() {
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(2.dp)).background(AttributePanelBg).padding(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().border(BorderStroke(1.dp, AttributeText.copy(alpha = 0.2f))).padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(R.string.attributes_title),
                color = AttributeText,
                style = TextStyle(fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, fontSize = 14.sp)
            )
            Spacer(Modifier.height(24.dp))

            AttributeRow(stringResource(R.string.attr_strength), "0", "", Icons.Default.FitnessCenter)
            AttributeRow(stringResource(R.string.attr_dexterity), "0", "", Icons.Default.Speed)
            AttributeRow(stringResource(R.string.attr_intelligence), "0", "", Icons.AutoMirrored.Filled.MenuBook)
            AttributeRow(stringResource(R.string.attr_vitality), "0", "", Icons.Default.Favorite)
        }
    }
}

@Composable
fun AttributeRow(name: String, value: String, change: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            name,
            modifier = Modifier.weight(1f),
            color = AttributeText,
            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp)
        )

        Box(
            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(8.dp)).background(AttributeText),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = AttributePanelBg, modifier = Modifier.size(22.dp))
        }

        Spacer(Modifier.width(16.dp))

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                value,
                color = AttributeText,
                style = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Black)
            )
        }
    }
}

@Composable
fun WeeklyProgressChart() {
    Box(
        modifier = Modifier.fillMaxWidth().height(160.dp).background(CardBg, RoundedCornerShape(4.dp)).border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(4.dp)).padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            val days = listOf(
                stringResource(R.string.day_mon),
                stringResource(R.string.day_tue),
                stringResource(R.string.day_wed),
                stringResource(R.string.day_thu),
                stringResource(R.string.day_fri),
                stringResource(R.string.day_sat),
                stringResource(R.string.day_sun)
            )
            val values = listOf(0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f)

            days.forEachIndexed { index, day ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.width(28.dp).fillMaxHeight(values[index] * 0.85f).clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)).background(AccentColor)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        day,
                        color = TextGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun AchievementCard(title: String, desc: String, icon: ImageVector, isActive: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.background(CardBg, RoundedCornerShape(4.dp)).border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(4.dp)).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(56.dp).border(BorderStroke(2.dp, if (isActive) GoldColor else Color.DarkGray), RoundedCornerShape(12.dp)).padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isActive) GoldColor else Color.DarkGray,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            title,
            color = if (isActive) Color.White else Color.DarkGray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )
        Text(
            desc,
            color = Color.Gray,
            fontSize = 9.sp,
            textAlign = TextAlign.Center,
            lineHeight = 12.sp
        )
    }
}