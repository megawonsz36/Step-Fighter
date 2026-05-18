package com.example.stepfighter.ui.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stepfighter.BaseGameActivity
import com.example.stepfighter.R
import com.example.stepfighter.BuildConfig
import com.example.stepfighter.ui.components.BottomNavigationBar
import com.example.stepfighter.ui.components.SideMenuContent
import com.example.stepfighter.ui.components.TopStepFighterBar
import com.example.stepfighter.ui.login.LoginActivity
import com.example.stepfighter.ui.profile.*
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class SettingsActivity : BaseGameActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HandleNetworkOverlay {
                SettingsScreen()
            }
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
                    SideMenuContent(onClose = { scope.launch { drawerState.close() } })
                }
            },
            gesturesEnabled = drawerState.isOpen
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Scaffold(
                    topBar = { TopStepFighterBar(onMenuClick = { scope.launch { drawerState.open() } }) },
                    bottomBar = { BottomNavigationBar(selectedIndex = -1) },
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
                            when (subMenu) {
                                null -> MainSettingsMenu(onNavigate = { currentSubMenu = it })
                                "LANGUAGE" -> LanguageSettingsMenu(onBack = { currentSubMenu = null })
                                "NOTIFICATIONS" -> NotificationSettingsMenu(onBack = { currentSubMenu = null })
                                "HELP_GUIDE" -> HelpGuideMenu(onBack = { currentSubMenu = null })
                                "HELP_RANKS" -> HelpRanksMenu(onBack = { currentSubMenu = null })
                                "HELP_SUPPORT" -> HelpSupportMenu(onBack = { currentSubMenu = null })
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
    val context = LocalContext.current
    val currentUser = remember { FirebaseAuth.getInstance().currentUser }
    val scope = rememberCoroutineScope()
    val tokenClient = remember { Identity.getSignInClient(context) }
    val currentLang = context.getString(R.string.lang_pl)

    var isGoogleLinked by remember {
        mutableStateOf(
            currentUser?.providerData?.any { it.providerId == GoogleAuthProvider.PROVIDER_ID } ?: false
        )
    }

    val linkGoogleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            try {
                val credential = tokenClient.getSignInCredentialFromIntent(result.data)
                val googleIdToken = credential.googleIdToken
                if (googleIdToken != null) {
                    val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    currentUser?.linkWithCredential(firebaseCredential)?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            isGoogleLinked = true
                            Toast.makeText(context, "Pomyślnie połączono z kontem Google!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Błąd łączenia: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Błąd: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                Text(stringResource(R.string.settings_title), color = GoldColor, style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp))
                Text(stringResource(R.string.settings_desc), color = TextGray, fontSize = 11.sp)
            }
        }

        item {
            SettingsSection(stringResource(R.string.settings_general)) {
                SettingsItem(stringResource(R.string.settings_language), Icons.Default.Language, onClick = { onNavigate("LANGUAGE") })
                SettingsItem(stringResource(R.string.settings_notifications), Icons.Default.Notifications, onClick = { onNavigate("NOTIFICATIONS") })
                SettingsItem(stringResource(R.string.settings_sound_effects), Icons.AutoMirrored.Filled.VolumeUp, hasSwitch = true)
            }
        }

        item {
            SettingsSection(stringResource(R.string.settings_help)) {
                val guideLabel = if (currentLang == "Polski") "Poradnik Przetrwania" else "Survival Guide"
                val ranksLabel = if (currentLang == "Polski") "Księga Tytułów Wojownika" else "Warrior Ranks Progression"
                val supportLabel = if (currentLang == "Polski") "Pomoc techniczna i FAQ" else "Technical Support & FAQ"

                SettingsItem(guideLabel, Icons.Default.MenuBook, onClick = { onNavigate("HELP_GUIDE") })
                SettingsItem(ranksLabel, Icons.Default.MilitaryTech, onClick = { onNavigate("HELP_RANKS") })
                SettingsItem(supportLabel, Icons.AutoMirrored.Filled.HelpOutline, onClick = { onNavigate("HELP_SUPPORT") })
                SettingsItem(stringResource(R.string.settings_version), Icons.Default.Info, showChevron = false)
            }
        }

        item {
            SettingsSection("KONTO") {
                if (!isGoogleLinked) {
                    SettingsItem(
                        label = "Połącz z kontem Google",
                        icon = Icons.Default.Link,
                        showChevron = true,
                        onClick = {
                            val signInRequest = BeginSignInRequest.builder()
                                .setGoogleIdTokenRequestOptions(
                                    BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                                        .setSupported(true)
                                        .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
                                        .setFilterByAuthorizedAccounts(false)
                                        .build()
                                ).build()

                            tokenClient.beginSignIn(signInRequest)
                                .addOnSuccessListener { result ->
                                    scope.launch {
                                        linkGoogleLauncher.launch(
                                            androidx.activity.result.IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                                        )
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Błąd Google: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(16.dp))
                        Text("Konto połączone z Google", color = TextGray, fontSize = 14.sp)
                    }
                }

                SettingsItem(
                    label = "Wyloguj się",
                    icon = Icons.Default.Logout,
                    showChevron = false,
                    onClick = {
                        val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
                        val rememberMe = prefs.getBoolean("remember_me", false)

                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(BuildConfig.GOOGLE_CLIENT_ID)
                            .requestEmail()
                            .build()
                        val googleClient = GoogleSignIn.getClient(context, gso)

                        googleClient.signOut().addOnCompleteListener {
                            FirebaseAuth.getInstance().signOut()

                            if (!rememberMe) {
                                prefs.edit().remove("remember_me").apply()
                            }

                            val intent = Intent(context, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            context.startActivity(intent)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun NotificationSettingsMenu(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE) }

    var globalNotifications by remember { mutableStateOf(prefs.getBoolean("global_notifications", true)) }
    var stepNotifications by remember { mutableStateOf(prefs.getBoolean("step_notifications", true)) }
    var energyNotifications by remember { mutableStateOf(prefs.getBoolean("energy_notifications", true)) }
    var inactivityNotifications by remember { mutableStateOf(prefs.getBoolean("inactivity_notifications", true)) }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = GoldColor) }
            Text(stringResource(R.string.settings_notifications), color = GoldColor, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
        }

        SettingsSection("") {
            SettingsItem(
                label = stringResource(R.string.settings_notifications),
                icon = Icons.Default.Notifications,
                hasSwitch = true,
                initialSwitchState = globalNotifications,
                onSwitchChange = {
                    globalNotifications = it
                    prefs.edit().putBoolean("global_notifications", it).apply()
                }
            )
        }

        AnimatedVisibility(
            visible = globalNotifications,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSection("") {
                    SettingsItem(
                        label = stringResource(R.string.settings_notification_steps),
                        icon = Icons.Default.NotificationsActive,
                        hasSwitch = true,
                        initialSwitchState = stepNotifications,
                        onSwitchChange = {
                            stepNotifications = it
                            prefs.edit().putBoolean("step_notifications", it).apply()
                        }
                    )
                    SettingsItem(
                        label = stringResource(R.string.settings_notification_energy),
                        icon = Icons.Default.BatteryChargingFull,
                        hasSwitch = true,
                        initialSwitchState = energyNotifications,
                        onSwitchChange = {
                            energyNotifications = it
                            prefs.edit().putBoolean("energy_notifications", it).apply()
                        }
                    )
                    val inactivityLabel = if (context.getString(R.string.lang_pl) == "Polski") "Przypomnienia o bezczynności" else "Inactivity reminders"
                    SettingsItem(
                        label = inactivityLabel,
                        icon = Icons.Default.HourglassEmpty,
                        hasSwitch = true,
                        initialSwitchState = inactivityNotifications,
                        onSwitchChange = {
                            inactivityNotifications = it
                            prefs.edit().putBoolean("inactivity_notifications", it).apply()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LanguageSettingsMenu(onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val currentLocale = AppCompatDelegate.getApplicationLocales().get(0)?.language ?: "pl"

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = GoldColor) }
            Text(stringResource(R.string.settings_language_select), color = GoldColor, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
        }
        SettingsSection("") {
            LanguageItem(stringResource(R.string.lang_pl), "PL", isSelected = currentLocale.startsWith("pl")) {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("pl"))
                activity?.let {
                    val intent = it.intent
                    it.finish()
                    it.overridePendingTransition(0, 0)
                    it.startActivity(intent)
                    it.overridePendingTransition(0, 0)
                }
            }
            LanguageItem(stringResource(R.string.lang_en), "EN", isSelected = currentLocale.startsWith("en")) {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
                activity?.let {
                    val intent = it.intent
                    it.finish()
                    it.overridePendingTransition(0, 0)
                    it.startActivity(intent)
                    it.overridePendingTransition(0, 0)
                }
            }
        }
    }
}

@Composable
fun HelpGuideMenu(onBack: () -> Unit) {
    val context = LocalContext.current
    val currentLang = context.getString(R.string.lang_pl)
    val title = if (currentLang == "Polski") "Poradnik Przetrwania" else "Survival Guide"

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = GoldColor) }
            Text(title, color = GoldColor, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(bottom = 32.dp)) {
            item {
                HelpSection(title = if (currentLang == "Polski") "1. PĘTLA ROZGRYWKI" else "1. GAMEPLAY LOOP", icon = Icons.Default.DirectionsWalk) {
                    val txt = if (currentLang == "Polski") {
                        "• Spaceruj w świecie rzeczywistym, aby automatycznie zbierać punkty kroków.\n" +
                                "• Każdy zrobiony krok bezpośrednio ładuje pasek doświadczenia (EXP) Twojej postaci.\n" +
                                "• Zgromadzone kroki są główną walutą bojową, służącą jako punkty akcji (AP) do atakowania potworów w lochach."
                    } else {
                        "• Walk in the real world to automatically accumulate step points.\n" +
                                "• Each step you take directly charges your hero's experience bar (EXP).\n" +
                                "• Accumulated steps are the main combat currency, serving as action points (AP) to attack monsters in dungeons."
                    }
                    Text(txt, color = Color.White, fontSize = 13.sp, lineHeight = 20.sp)
                }
            }
            item {
                HelpSection(title = if (currentLang == "Polski") "2. SYSTEM ENERGII" else "2. ENERGY SYSTEM", icon = Icons.Default.BatteryChargingFull) {
                    val txt = if (currentLang == "Polski") {
                        "• Każda próba wejścia do lochu oraz eksploracja mapy wymaga określonej ilości energii (EN).\n" +
                                "• Koszt wejścia skaluje się i wzrasta proporcjonalnio do poziomu trudności wybranego lochu.\n" +
                                "• Energia odnawia się samoistnie w stałym tempie 1 EN co 5 minut (również przy wyłączonej aplikacji)."
                    } else {
                        "• Every single attempt to enter a dungeon and explore the map requires a specific amount of energy (EN).\n" +
                                "• The entry cost scales up proportionally based on the difficulty level of the selected dungeon.\n" +
                                "• Energy restores automatically at a steady rate of 1 EN every 5 minutes (even when the app is completely closed)."
                    }
                    Text(txt, color = Color.White, fontSize = 13.sp, lineHeight = 20.sp)
                }
            }
            item {
                HelpSection(title = if (currentLang == "Polski") "3. SKLEP I WALUTY" else "3. SHOP & CURRENCIES", icon = Icons.Default.Storefront) {
                    val txt = if (currentLang == "Polski") {
                        "• W sklepie możesz nabywać asortyment podzielony na wygodne kategorie rynkowe.\n" +
                                "• Przedmioty użytkowe (Alchemia) kupisz za Kroki zebrane podczas spacerowania.\n" +
                                "• Rynsztunek bojowy (Wyposażenie) zakupisz za Monety (Tokens) zdobywane w lochach.\n" +
                                "• Możesz filtrować asortyment, aby ukryć przedmioty przekraczające Twój aktualny poziom."
                    } else {
                        "• In the shop you can acquire items sorted into comfortable market categories.\n" +
                                "• Usable consumables (Alchemy) are purchased with Steps gathered from real walking.\n" +
                                "• Combat gear (Equipment) is bought using Tokens rewarded from cleared dungeons.\n" +
                                "• You can toggle filters to completely hide products that exceed your current level."
                    }
                    Text(txt, color = Color.White, fontSize = 13.sp, lineHeight = 20.sp)
                }
            }
            item {
                HelpSection(title = if (currentLang == "Polski") "4. ALCHEMIA (PRZEDMIOTY)" else "4. ALCHEMY (CONSUMABLES)", icon = Icons.Default.LocalPharmacy) {
                    val txt = if (currentLang == "Polski") {
                        "• Mikstury HP – przywracają utracone punkty zdrowia. Możesz ich użyć w oknie przerwy w lochu.\n" +
                                "• Mikstury Energii – odnawiają natychmiastowo określoną pulę kroków bojowych i energii.\n" +
                                "• Zwój Ucieczki – natychmiast wyciąga wojownika z niebezpieczeństwa, gwarantując bezpieczny odwrót."
                    } else {
                        "• HP Potions – regenerate lost health points. Can be consumed via the dungeon intermission window.\n" +
                                "• Energy Potions – instantly restore a designated pool of combat steps and exploration energy.\n" +
                                "• Escape Scroll – extracts a warrior from grave danger instantly, guaranteeing a safe retreat."
                    }
                    Text(txt, color = Color.White, fontSize = 13.sp, lineHeight = 20.sp)
                }
            }
            item {
                HelpSection(title = if (currentLang == "Polski") "5. RYNSZTUNEK (EKWIPUNEK)" else "5. GEAR & EQUIPMENT", icon = Icons.Default.Checkroom) {
                    val txt = if (currentLang == "Polski") {
                        "• Wyposażenie dzieli się na kaski, bronie, pancerze, tarcze oraz buty.\n" +
                                "• Każdy przedmiot trvale zwiększa statystyki (Atak, Obrona, Zręczność) lub daje pasywne bonusy.\n" +
                                "• Zakładanie rynsztunku odbywa się poprzez czytelny interfejs kliknięć w plecaku.\n" +
                                "• Pamiętaj: potężniejszy ekwipunek wymaga wyższego poziomu bohatera do założenia!"
                    } else {
                        "• Equipment is divided into distinct slots: helms, weapons, armor, shields, and boots.\n" +
                                "• Each piece permanently increases your combat stats (Attack, Armor, Agility) or grants passives.\n" +
                                "• Gear management is handled through a seamless click-to-equip inventory interface.\n" +
                                "• Note: highly powerful equipment requires an advanced hero level to be successfully equipped!"
                    }
                    Text(txt, color = Color.White, fontSize = 13.sp, lineHeight = 20.sp)
                }
            }
            item {
                HelpSection(title = if (currentLang == "Polski") "6. PRZERWA, MEDYTACJA I UCIECZKA" else "6. INTERMISSION, MEDITATION & ESCAPE", icon = Icons.Default.SelfImprovement) {
                    val txt = if (currentLang == "Polski") {
                        "• Po pokonaniu wroga uruchamia się bezpieczne okno przygotowawcze. Możesz wtedy użyć przedmiotów lub Medytacji.\n" +
                                "• Medytacja przywraca losowo 10-50% maksymalnego HP i odnawia 1 czar. Można z niej skorzystać TYLKO JEDEN RAZ na cały dungeon!\n" +
                                "• Przycisk Ucieczki pozwala na wycofanie się z lochu, lecz szansa na sukces maleje o 20% z każdym pokonanym przeciwnikiem (zaczynasz od 100%, potem 80%, 60% itd.).\n" +
                                "• Jeśli ucieczka się nie powiedzie, zostajesz w lochu i musisz walczyć dalej!\n" +
                                "• Posiadanie przedmiotu Zwój Ucieczki daje gwarantowane 100% szansy na bezpieczny odwrót w dowolnym momencie."
                    } else {
                        "• After a foe is defeated, a safe intermission stage starts. You can use items or Meditate.\n" +
                                "• Meditation restores 10-50% max HP and refunds 1 spell charge. It can ONLY BE USED ONCE per complete dungeon run!\n" +
                                "• The Escape button allows you to leave, but success chance drops by 20% for each defeated enemy (starts at 100%, then 80%, 60% etc.).\n" +
                                "• If the manual escape fails, you remain trapped in the dungeon and must keep fighting!\n" +
                                "• Carrying an Escape Scroll item always guarantees a 100% chance for a safe retreat at any time."
                    }
                    Text(txt, color = Color.White, fontSize = 13.sp, lineHeight = 20.sp)
                }
            }
        }
    }
}

@Composable
fun HelpRanksMenu(onBack: () -> Unit) {
    val context = LocalContext.current
    val currentLang = context.getString(R.string.lang_pl)
    val title = if (currentLang == "Polski") "Księga Tytułów Wojownika" else "Warrior Ranks Progression"

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = GoldColor) }
            Text(title, color = GoldColor, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
        ) {
            item { RankRow("LVL 0 - 1", if (currentLang == "Polski") "Boso-Nogi Start" else "Barefoot Start") }
            item { RankRow("LVL 2 - 3", if (currentLang == "Polski") "Deptacz Trawy" else "Grass Trampler") }
            item { RankRow("LVL 4 - 5", if (currentLang == "Polski") "Spacerowicz" else "Stroller") }
            item { RankRow("LVL 6 - 7", if (currentLang == "Polski") "Biegacz Miejski" else "City Runner") }
            item { RankRow("LVL 8 - 9", if (currentLang == "Polski") "Zwiadowca Ścieżek" else "Path Scout") }
            item { RankRow("LVL 10 - 11", if (currentLang == "Polski") "Wędrowiec" else "Wanderer") }
            item { RankRow("LVL 12 - 13", if (currentLang == "Polski") "Kreślarz Map" else "Map Maker") }
            item { RankRow("LVL 14 - 15", if (currentLang == "Polski") "Piechur Doświadczony" else "Experienced Hiker") }
            item { RankRow("LVL 16 - 17", if (currentLang == "Polski") "Pokonywacz Dystansu" else "Distance Crusher") }
            item { RankRow("LVL 18 - 19", if (currentLang == "Polski") "Pogromca Asfaltu" else "Asphalt Conqueror") }
            item { RankRow("LVL 20 - 21", if (currentLang == "Polski") "Wojownik Ścieżek" else "Path Warrior") }
            item { RankRow("LVL 22 - 23", if (currentLang == "Polski") "Krokobieg" else "Step Runner") }
            item { RankRow("LVL 24 - 25", if (currentLang == "Polski") "Kuryer Traktu" else "Highway Courier") }
            item { RankRow("LVL 26 - 27", if (currentLang == "Polski") "Długodystansowiec" else "Long Distance Walker") }
            item { RankRow("LVL 28 - 29", if (currentLang == "Polski") "Szybki Marszał" else "Speed Marshal") }
            item { RankRow("LVL 30 - 31", if (currentLang == "Polski") "Nocny Trakter" else "Night Hiker") }
            item { RankRow("LVL 32 - 33", if (currentLang == "Polski") "Żelazna Pięta" else "Iron Heel") }
            item { RankRow("LVL 34 - 35", if (currentLang == "Polski") "Biegacz Przełajowy" else "Trail Runner") }
            item { RankRow("LVL 36 - 37", if (currentLang == "Polski") "Poszukiwacz Horyzontu" else "Horizon Seeker") }
            item { RankRow("LVL 38 - 39", if (currentLang == "Polski") "Wędrowny Rycerz" else "Knight Errant") }
            item { RankRow("LVL 40 - 41", if (currentLang == "Polski") "Weteran Marszu" else "March Veteran") }
            item { RankRow("LVL 42 - 43", if (currentLang == "Polski") "Elitarny Piechur" else "Elite Hiker") }
            item { RankRow("LVL 44 - 45", if (currentLang == "Polski") "Zdobywca Szczytów" else "Peak Conqueror") }
            item { RankRow("LVL 46 - 47", if (currentLang == "Polski") "Krokowy Zwid" else "Stepping Phantom") }
            item { RankRow("LVL 48 - 49", if (currentLang == "Polski") "Niestrudzony" else "Tireless Packer") }
            item { RankRow("LVL 50 - 51", if (currentLang == "Polski") "Panek Traktów" else "Lord of the Trails") }
            item { RankRow("LVL 52 - 53", if (currentLang == "Polski") "Mistrz Tempa" else "Pace Master") }
            item { RankRow("LVL 54 - 55", if (currentLang == "Polski") "Lekki Krok" else "Light Stepper") }
            item { RankRow("LVL 56 - 57", if (currentLang == "Polski") "Włóczykij Przeznaczenia" else "Destiny Vagabond") }
            item { RankRow("LVL 58 - 59", if (currentLang == "Polski") "Złoty Marszrut" else "Golden Route Traveller") }
            item { RankRow("LVL 60 - 61", if (currentLang == "Polski") "Mistrz Stalowych Nóg" else "Steel Legs Master") }
            item { RankRow("LVL 62 - 63", if (currentLang == "Polski") "Przewodnik Chmur" else "Cloud Guide") }
            item { RankRow("LVL 64 - 65", if (currentLang == "Polski") "Srebrny Trakter" else "Silver Pathfinder") }
            item { RankRow("LVL 66 - 67", if (currentLang == "Polski") "Lewitator" else "Levitator") }
            item { RankRow("LVL 68 - 69", if (currentLang == "Polski") "Pustynny Wędrowiec" else "Desert Strider") }
            item { RankRow("LVL 70 - 71", if (currentLang == "Polski") "Łowca Kroków" else "Step Hunter") }
            item { RankRow("LVL 72 - 73", if (currentLang == "Polski") "As Stratosfery" else "Stratosphere Ace") }
            item { RankRow("LVL 74 - 75", if (currentLang == "Polski") "Mityczny Piechur" else "Mythical Strider") }
            item { RankRow("LVL 76 - 77", if (currentLang == "Polski") "Wulkaniczny Krok" else "Volcanic Step") }
            item { RankRow("LVL 78 - 79", if (currentLang == "Polski") "Dżentelmen Drogi" else "Road Gentleman") }
            item { RankRow("LVL 80 - 81", if (currentLang == "Polski") "Strażnik Przeznaczenia" else "Guardian of Destiny") }
            item { RankRow("LVL 82 - 83", if (currentLang == "Polski") "Niebieski Biegacz" else "Celestial Runner") }
            item { RankRow("LVL 84 - 85", if (currentLang == "Polski") "Tlenowy Atlas" else "Oxygen Atlas") }
            item { RankRow("LVL 86 - 87", if (currentLang == "Polski") "Władca Horyzontu" else "Ruler of Horizons") }
            item { RankRow("LVL 88 - 89", if (currentLang == "Polski") "Anastazy Marszu" else "March Supremo") }
            item { RankRow("LVL 90 - 91", if (currentLang == "Polski") "Krokowy Monarcha" else "Step Monarch") }
            item { RankRow("LVL 92 - 93", if (currentLang == "Polski") "Tytan Dystansu" else "Distance Titan") }
            item { RankRow("LVL 94 - 95", if (currentLang == "Polski") "Widmo Traktu" else "Highway Spectre") }
            item { RankRow("LVL 96 - 97", if (currentLang == "Polski") "Kosmiczny Piechur" else "Cosmic Strider") }
            item { RankRow("LVL 98 - 99", if (currentLang == "Polski") "Krokowy Absolut" else "Step Absolute") }
            item { RankRow("LVL 100+", if (currentLang == "Polski") "Boski Champion" else "Divine Champion") }
        }
    }
}

@Composable
fun HelpSupportMenu(onBack: () -> Unit) {
    val context = LocalContext.current
    val currentLang = context.getString(R.string.lang_pl)
    val title = if (currentLang == "Polski") "Pomoc techniczna i FAQ" else "Technical Support & FAQ"

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = GoldColor) }
            Text(title, color = GoldColor, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
        ) {
            item {
                HelpSection(title = if (currentLang == "Polski") "KROKI SIĘ NIE NALICZAJĄ?" else "STEPS NOT TRACKING?", icon = Icons.Default.DirectionsRun) {
                    val txt = if (currentLang == "Polski") {
                        "• Upewnij się, że aplikacja ma przyznane uprawnienie systemowe do 'Aktywności Fizycznej'.\n" +
                                "• Wyłącz tryby głębokiego oszczędzania baterii, które mogą usypiać wbudowany czujnik krokomierza."
                    } else {
                        "• Make sure that the application has the 'Physical Activity' system permission granted.\n" +
                                "• Disable deep battery saver modes that might put the built-in step sensor to sleep."
                    }
                    Text(txt, color = Color.White, fontSize = 13.sp, lineHeight = 20.sp)
                }
            }

            item {
                HelpSection(title = if (currentLang == "Polski") "SYSTEM ZGŁOSZEŃ" else "SUPPORT TICKET SYSTEM", icon = Icons.Default.Build) {
                    val txt = if (currentLang == "Polski") {
                        "• Napotkałeś błąd krytyczny uniemożliwiający grę? Wyślij szczegółowe zgłoszenie bezpośrednio na nasz adres mailowy.\n" +
                                "• Adres kontaktowy: support@stepfighter-game.com\n" +
                                "• Dołącz swój unikalny pseudonim (Nick) lub powiązany adres E-mail."
                    } else {
                        "• Encountered a critical bug preventing you from playing? Send a detailed ticket directly to our email.\n" +
                                "• Contact email: support@stepfighter-game.com\n" +
                                "• Please include your unique username (Nick) or registered E-mail address."
                    }
                    Text(txt, color = Color.White, fontSize = 13.sp, lineHeight = 20.sp)
                }
            }
        }
    }
}

@Composable
fun RankRow(lvl: String, name: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(4.dp))
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(4.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(lvl, color = GoldColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text(name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun HelpSection(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(CardBg, RoundedCornerShape(6.dp)).border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(6.dp)).padding(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(bottom = 12.dp)) {
            Icon(icon, contentDescription = null, tint = GoldColor, modifier = Modifier.size(20.dp))
            Text(text = title, color = GoldColor, fontSize = 14.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        }
        HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(bottom = 12.dp))
        content()
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (title.isNotEmpty()) {
            Text(title, color = GoldColor.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 8.dp, start = 4.dp))
        }
        Column(modifier = Modifier.fillMaxWidth().background(CardBg, RoundedCornerShape(4.dp)).border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(4.dp))) {
            content()
        }
    }
}

@Composable
fun SettingsItem(label: String, icon: ImageVector, hasSwitch: Boolean = false, initialSwitchState: Boolean = false, onSwitchChange: (Boolean) -> Unit = {}, showChevron: Boolean = true, onClick: () -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = if (hasSwitch) ({ }) else onClick).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = GoldColor, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Text(label, color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
        if (hasSwitch) {
            Switch(
                checked = initialSwitchState,
                onCheckedChange = onSwitchChange,
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
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(32.dp).background(if (isSelected) GoldColor else Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
            Text(code, color = if (isSelected) BgColor else Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
        }
        Spacer(Modifier.width(16.dp))
        Text(label, color = Color.White, fontSize = 16.sp, modifier = Modifier.weight(1f))
        if (isSelected) Icon(Icons.Default.Check, null, tint = GoldColor)
    }
}