package com.example.stepfighter.ui.dashboard

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import com.example.stepfighter.BaseGameActivity
import com.example.stepfighter.R
import com.example.stepfighter.ui.components.BottomNavigationBar
import com.example.stepfighter.ui.components.SideMenuContent
import com.example.stepfighter.ui.components.TopStepFighterBar
import com.example.stepfighter.ui.dungeon.DungeonMapActivity
import com.example.stepfighter.ui.login.AuthManager
import com.example.stepfighter.ui.profile.*
import com.example.stepfighter.utils.NotificationHelper
import com.example.stepfighter.utils.StepTrackerManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

enum class SaveState {
    IDLE, LOADING, SUCCESS, ERROR
}

class DashboardActivity : BaseGameActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HandleNetworkOverlay {
                DashboardScreen()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            NotificationHelper(this).scheduleInactivityReminder()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun getWarriorTitleRes(level: Int): Int {
    val boundedLevel = level.coerceIn(0, 100)
    val targetLevel = if (boundedLevel == 100) 100 else (boundedLevel / 2) * 2
    return when (targetLevel) {
        100 -> R.string.title_level_100
        98 -> R.string.title_level_98
        96 -> R.string.title_level_96
        94 -> R.string.title_level_94
        92 -> R.string.title_level_92
        90 -> R.string.title_level_90
        88 -> R.string.title_level_88
        86 -> R.string.title_level_86
        84 -> R.string.title_level_84
        82 -> R.string.title_level_82
        80 -> R.string.title_level_80
        78 -> R.string.title_level_78
        76 -> R.string.title_level_76
        74 -> R.string.title_level_74
        72 -> R.string.title_level_72
        70 -> R.string.title_level_70
        68 -> R.string.title_level_68
        66 -> R.string.title_level_66
        64 -> R.string.title_level_64
        62 -> R.string.title_level_62
        60 -> R.string.title_level_60
        58 -> R.string.title_level_58
        56 -> R.string.title_level_56
        54 -> R.string.title_level_54
        52 -> R.string.title_level_52
        50 -> R.string.title_level_50
        48 -> R.string.title_level_48
        46 -> R.string.title_level_46
        44 -> R.string.title_level_44
        46 -> R.string.title_level_46
        44 -> R.string.title_level_44
        42 -> R.string.title_level_42
        40 -> R.string.title_level_40
        38 -> R.string.title_level_38
        36 -> R.string.title_level_36
        34 -> R.string.title_level_34
        32 -> R.string.title_level_32
        30 -> R.string.title_level_30
        28 -> R.string.title_level_28
        26 -> R.string.title_level_26
        24 -> R.string.title_level_24
        22 -> R.string.title_level_22
        20 -> R.string.title_level_20
        18 -> R.string.title_level_18
        16 -> R.string.title_level_16
        14 -> R.string.title_level_14
        12 -> R.string.title_level_12
        10 -> R.string.title_level_10
        8 -> R.string.title_level_8
        6 -> R.string.title_level_6
        4 -> R.string.title_level_4
        2 -> R.string.title_level_2
        else -> R.string.title_level_0
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentUser = remember { FirebaseAuth.getInstance().currentUser }
    val userName = currentUser?.displayName ?: "Wojownik"
    val authManager = remember { AuthManager() }
    val stepTrackerManager = remember { StepTrackerManager(context) }
    val notificationHelper = remember { NotificationHelper(context) }

    var saveState by remember { mutableStateOf(SaveState.IDLE) }
    var showLinkDialog by remember { mutableStateOf(false) }
    var playerLevel by remember { mutableStateOf(0) }
    var showLevelUpDialog by remember { mutableStateOf(false) }

    var baseCombatSteps by remember { mutableStateOf(0L) }
    var baseTotalXpSteps by remember { mutableStateOf(0L) }

    val stepsFromSensor = stepTrackerManager.stepsSinceStart

    val currentCombatSteps = baseCombatSteps + stepsFromSensor
    val currentTotalXpSteps = baseTotalXpSteps + stepsFromSensor

    val stepsForNextLevel = 1000
    val calculatedLevel = (currentTotalXpSteps / stepsForNextLevel).toInt()
    val currentLevelProgress = (currentTotalXpSteps % stepsForNextLevel).toInt()

    var hasNotifiedMilestone by remember { mutableStateOf(false) }
    var isDataLoaded by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            stepTrackerManager.startTracking()
        } else {
            Toast.makeText(context, "Wymagane uprawnienie!", Toast.LENGTH_LONG).show()
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
            } else {
                stepTrackerManager.startTracking()
            }
        } else {
            stepTrackerManager.startTracking()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        authManager.loadDashboardData { success, data, _ ->
            if (success && data != null) {
                playerLevel = (data["level"] as? Long)?.toInt() ?: 0
                baseCombatSteps = (data["combatSteps"] as? Long) ?: (data["steps"] as? Long) ?: 0L
                baseTotalXpSteps = (data["totalXpSteps"] as? Long) ?: (data["steps"] as? Long) ?: 0L
                isDataLoaded = true
            }
        }
    }

    LaunchedEffect(stepsFromSensor) {
        if (stepsFromSensor > 0 && isDataLoaded) {
            authManager.saveTwoWaySteps(currentCombatSteps.toInt(), currentTotalXpSteps.toInt()) {}
            if (currentCombatSteps >= 500 && !hasNotifiedMilestone) {
                notificationHelper.notifyStepsMilestone()
                hasNotifiedMilestone = true
            }
        }
    }

    LaunchedEffect(calculatedLevel) {
        if (isDataLoaded && playerLevel > 0 && calculatedLevel > playerLevel) {
            playerLevel = calculatedLevel
            showLevelUpDialog = true
            notificationHelper.notifyLevelUp(calculatedLevel)
            authManager.updateLevel(calculatedLevel) {}
            authManager.saveTwoWaySteps(currentCombatSteps.toInt(), currentTotalXpSteps.toInt()) {}
        } else if (isDataLoaded && playerLevel == 0 && calculatedLevel > 0) {
            playerLevel = calculatedLevel
        }
    }

    val saveCardBg = when (saveState) {
        SaveState.IDLE -> CardBg
        SaveState.LOADING -> Color(0xFF222222)
        SaveState.SUCCESS -> Color(0xFF1B5E20)
        SaveState.ERROR -> Color(0xFFB71C1C)
    }

    val saveIconBg = when (saveState) {
        SaveState.IDLE -> Color(0xFF8B2020)
        SaveState.LOADING -> Color(0xFF444444)
        SaveState.SUCCESS -> Color(0xFF2E7D32)
        SaveState.ERROR -> Color(0xFFD32F2F)
    }

    LaunchedEffect(saveState) {
        if (saveState == SaveState.SUCCESS || saveState == SaveState.ERROR) {
            kotlinx.coroutines.delay(3000)
            saveState = SaveState.IDLE
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            stepTrackerManager.stopTracking()
        }
    }

    if (showLevelUpDialog) {
        AlertDialog(
            onDismissRequest = { showLevelUpDialog = false },
            containerColor = CardBg,
            title = { Text("NOWY POZIOM!", color = GoldColor, fontWeight = FontWeight.Bold) },
            text = { Text("Gratulacje! Awansowałeś na poziom $playerLevel!", color = Color.White) },
            confirmButton = {
                Button(
                    onClick = { showLevelUpDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldColor)
                ) {
                    Text("SUPER", color = Color.Black)
                }
            }
        )
    }

    if (showLinkDialog) {
        AlertDialog(
            onDismissRequest = { showLinkDialog = false },
            containerColor = CardBg,
            title = { Text("WYMAGANE KONTO GOOGLE", color = GoldColor, fontWeight = FontWeight.Black, fontSize = 18.sp) },
            text = { Text("Twoje konto zostało zarejestrowane tradycyjnie (E-mail). Aby móc synchronizować postępy, przejdź do Ustawień, gdzie w zakładce Konto znajdziesz możliwość połączenia swojego profilu z kontem Google.", color = Color.White, fontSize = 14.sp) },
            confirmButton = {
                Button(onClick = { showLinkDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = GoldColor), shape = RoundedCornerShape(4.dp)) {
                    Text("ROZUMIEM", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
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
                    topBar = {
                        Box(modifier = Modifier.fillMaxWidth().zIndex(1f)) {
                            TopStepFighterBar(onMenuClick = { scope.launch { drawerState.open() } })
                            Column(modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp)) {
                                Text(text = "Witaj,", color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(text = userName, color = GoldColor, fontSize = 12.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    },
                    bottomBar = { BottomNavigationBar(selectedIndex = 0) },
                    containerColor = BgColor
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding())
                        ) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().height(340.dp)) {
                                    Image(painter = painterResource(id = R.drawable.bg_warrior), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = 0.6f)
                                    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color.Transparent, BgColor), startY = 500f)))
                                    Column(modifier = Modifier.align(Alignment.BottomStart).padding(24.dp).padding(bottom = 16.dp)) {
                                        Text(stringResource(R.string.level_label, playerLevel), color = GoldColor, style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 2.sp))
                                        Text(stringResource(getWarriorTitleRes(playerLevel)), color = Color.White, style = TextStyle(fontWeight = FontWeight.Black, fontSize = 32.sp))
                                    }
                                }
                            }

                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp).background(CardBg, RoundedCornerShape(8.dp)).border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(8.dp)).padding(20.dp)) {
                                    Column {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Column {
                                                Text(stringResource(R.string.energy_progress_title), color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                                Text(stringResource(R.string.steps_count, currentLevelProgress.toLong(), stepsForNextLevel.toLong()), color = Color.White, fontSize = 16.sp, fontStyle = FontStyle.Italic)
                                            }
                                            Icon(Icons.Default.FlashOn, null, tint = GoldColor, modifier = Modifier.size(24.dp))
                                        }
                                        Spacer(Modifier.height(16.dp))
                                        Box(modifier = Modifier.fillMaxWidth().height(10.dp).background(Color.Black, RoundedCornerShape(5.dp))) {
                                            Box(modifier = Modifier.fillMaxWidth((currentLevelProgress.toFloat() / stepsForNextLevel.toFloat()).coerceIn(0f, 1f)).fillMaxHeight().background(brush = Brush.horizontalGradient(colors = listOf(Color(0xFFD48A5F), Color(0xFFFFB783))), shape = RoundedCornerShape(5.dp)))
                                        }
                                        Spacer(Modifier.height(12.dp))
                                        Text(stringResource(R.string.energy_missing_desc, (stepsForNextLevel - currentLevelProgress)), color = TextGray, fontSize = 11.sp, fontStyle = FontStyle.Italic, lineHeight = 16.sp)
                                    }
                                }
                            }

                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp).height(140.dp).background(CardBg, RoundedCornerShape(8.dp)).border(BorderStroke(1.dp, GoldColor.copy(alpha = 0.2f)), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(stringResource(R.string.dungeons_ready_title), color = GoldColor, fontWeight = FontWeight.Black, fontSize = 18.sp, letterSpacing = 1.sp)
                                        Text(stringResource(R.string.dungeons_ready_desc), color = TextGray, fontSize = 12.sp, fontStyle = FontStyle.Italic)
                                        Spacer(Modifier.height(4.dp))
                                        Text(stringResource(R.string.combat_steps_available, currentCombatSteps), color = AccentColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.height(12.dp))
                                        Button(
                                            onClick = {
                                                val intent = Intent(context, DungeonMapActivity::class.java)
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                                                context.startActivity(intent)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = GoldColor.copy(alpha = 0.1f)),
                                            border = BorderStroke(1.dp, GoldColor),
                                            shape = RoundedCornerShape(4.dp),
                                            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 8.dp)
                                        ) {
                                            Text(stringResource(R.string.go_into_dungeon_btn), color = GoldColor, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            item {
                                Surface(
                                    onClick = {
                                        if (saveState != SaveState.LOADING) {
                                            val isGoogleUser = currentUser?.providerData?.any { it.providerId == com.google.firebase.auth.GoogleAuthProvider.PROVIDER_ID } ?: false
                                            if (isGoogleUser) {
                                                saveState = SaveState.LOADING
                                                authManager.saveTwoWaySteps(currentCombatSteps.toInt(), currentTotalXpSteps.toInt()) { success ->
                                                    if (success) {
                                                        authManager.updateLevel(playerLevel) { lvlSuccess ->
                                                            saveState = if (lvlSuccess) SaveState.SUCCESS else SaveState.ERROR
                                                        }
                                                    } else {
                                                        saveState = SaveState.ERROR
                                                    }
                                                }
                                            } else {
                                                showLinkDialog = true
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    color = saveCardBg,
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                                ) {
                                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(48.dp).background(saveIconBg, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                                            if (saveState == SaveState.LOADING) {
                                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                                            } else {
                                                Icon(Icons.Default.CloudUpload, null, tint = Color.White)
                                            }
                                        }
                                        Spacer(Modifier.width(16.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            val titleText = when (saveState) {
                                                SaveState.IDLE -> stringResource(R.string.save_progress)
                                                SaveState.LOADING -> "Synchronizacja..."
                                                SaveState.SUCCESS -> "Postęp zapisany!"
                                                SaveState.ERROR -> "Błąd zapisu"
                                            }
                                            val descText = when (saveState) {
                                                SaveState.IDLE -> stringResource(R.string.login_google)
                                                SaveState.LOADING -> "Zapisywanie danych..."
                                                SaveState.SUCCESS -> "Dane są bezpieczne."
                                                SaveState.ERROR -> "Spróbuj ponownie."
                                            }
                                            Text(titleText, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(descText, color = TextGray, fontSize = 11.sp)
                                        }
                                        if (saveState == SaveState.IDLE) {
                                            Icon(Icons.Default.ChevronRight, null, tint = TextGray)
                                        }
                                    }
                                }
                            }

                            item {
                                Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
//                                    Text(stringResource(R.string.recent_finds), color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, modifier = Modifier.padding(bottom = 16.dp))
//                                    FindItem(stringResource(R.string.find_medallion_title), stringResource(R.string.find_medallion_desc, playerLevel), Icons.Default.MilitaryTech)
//                                    Spacer(Modifier.height(12.dp))
//                                    FindItem(stringResource(R.string.find_boots_title), stringResource(R.string.find_boots_desc, playerLevel), Icons.AutoMirrored.Filled.DirectionsWalk)
                                }
                            }
                            item { Spacer(Modifier.height(20.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FindItem(title: String, desc: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth().background(CardBg, RoundedCornerShape(4.dp)).border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(4.dp)).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(40.dp).background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = GoldColor, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(desc, color = TextGray, fontSize = 11.sp)
        }
    }
}