package com.example.stepfighter.ui.settings

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stepfighter.R
import com.example.stepfighter.ui.components.BottomNavigationBar
import com.example.stepfighter.ui.components.SideMenuContent
import com.example.stepfighter.ui.components.TopStepFighterBar
import com.example.stepfighter.ui.profile.*
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SettingsScreen()
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentSubMenu by remember { mutableStateOf<String?>(null) }

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
                    bottomBar = { BottomNavigationBar(selectedIndex = -1) }, // Brak zaznaczenia na barze
                    containerColor = BgColor
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        AnimatedContent(
                            targetState = currentSubMenu,
                            transitionSpec = {
                                if (targetState != null) {
                                    slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                                } else {
                                    slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                                }
                            },
                            label = "settings_nav"
                        ) { subMenu ->
                            if (subMenu == null) {
                                MainSettingsMenu(onNavigate = { currentSubMenu = it })
                            } else if (subMenu == "LANGUAGE") {
                                LanguageSettingsMenu(onBack = { currentSubMenu = null })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainSettingsMenu(onNavigate: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                Text(
                    stringResource(R.string.settings_title),
                    color = GoldColor,
                    style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                )
                Text(
                    stringResource(R.string.settings_desc),
                    color = TextGray,
                    fontSize = 11.sp
                )
            }
        }

        item {
            SettingsSection(stringResource(R.string.settings_general)) {
                SettingsItem(stringResource(R.string.settings_language), Icons.Default.Language, onClick = { onNavigate("LANGUAGE") })
                SettingsItem(stringResource(R.string.settings_sound_effects), Icons.AutoMirrored.Filled.VolumeUp, hasSwitch = true)
            }
        }

        item {
            SettingsSection(stringResource(R.string.settings_notifications)) {
                SettingsItem(stringResource(R.string.settings_notification_steps), Icons.Default.NotificationsActive, hasSwitch = true, initialSwitchState = true)
                SettingsItem(stringResource(R.string.settings_notification_energy), Icons.Default.BatteryChargingFull, hasSwitch = true)
            }
        }

        item {
            SettingsSection(stringResource(R.string.settings_help)) {
                SettingsItem(stringResource(R.string.settings_help), Icons.AutoMirrored.Filled.HelpOutline)
                SettingsItem(stringResource(R.string.settings_version), Icons.Default.Info, showChevron = false)
            }
        }
    }
}

@Composable
fun LanguageSettingsMenu(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = GoldColor)
            }
            Text(
                stringResource(R.string.settings_language_select),
                color = GoldColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        SettingsSection("") {
            val currentLocale = AppCompatDelegate.getApplicationLocales().get(0)?.language ?: "pl"
            LanguageItem(stringResource(R.string.lang_pl), "PL", isSelected = currentLocale == "pl") {
                val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags("pl")
                AppCompatDelegate.setApplicationLocales(appLocale)
            }
            LanguageItem(stringResource(R.string.lang_en), "EN", isSelected = currentLocale == "en") {
                val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags("en")
                AppCompatDelegate.setApplicationLocales(appLocale)
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (title.isNotEmpty()) {
            Text(
                title,
                color = GoldColor.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBg, RoundedCornerShape(4.dp))
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(4.dp))
        ) {
            content()
        }
    }
}

@Composable
fun SettingsItem(
    label: String,
    icon: ImageVector,
    hasSwitch: Boolean = false,
    initialSwitchState: Boolean = false,
    showChevron: Boolean = true,
    onClick: () -> Unit = {}
) {
    var switchState by remember { mutableStateOf(initialSwitchState) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = if (hasSwitch) ({ switchState = !switchState }) else onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = GoldColor, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Text(label, color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
        
        if (hasSwitch) {
            Switch(
                checked = switchState,
                onCheckedChange = { switchState = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = GoldColor,
                    checkedTrackColor = GoldColor.copy(alpha = 0.3f),
                    uncheckedThumbColor = TextGray,
                    uncheckedTrackColor = Color.Black.copy(alpha = 0.5f)
                )
            )
        } else if (showChevron) {
            Icon(Icons.Default.ChevronRight, null, tint = TextGray, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun LanguageItem(label: String, code: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(if (isSelected) GoldColor else Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(code, color = if (isSelected) BgColor else Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
        }
        Spacer(Modifier.width(16.dp))
        Text(label, color = Color.White, fontSize = 16.sp, modifier = Modifier.weight(1f))
        if (isSelected) {
            Icon(Icons.Default.Check, null, tint = GoldColor)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF131313)
@Composable
fun SettingsPreview() {
    SettingsScreen()
}
