package com.example.stepfighter.ui.shop

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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stepfighter.BaseGameActivity
import com.example.stepfighter.ui.components.BottomNavigationBar
import com.example.stepfighter.ui.components.SideMenuContent
import com.example.stepfighter.ui.components.TopStepFighterBar
import com.example.stepfighter.ui.profile.*
import kotlinx.coroutines.launch

data class ShopItemData(
    val id: String,
    val namePl: String,
    val nameEn: String,
    val lorePl: String,
    val loreEn: String,
    val statPl: String,
    val statEn: String,
    val type: String,
    val slot: String,
    val price: Int,
    val currency: String,
    val level: Int,
    val icon: ImageVector
)

class ShopActivity : BaseGameActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HandleNetworkOverlay {
                ShopScreen()
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val currentLang = context.getString(com.example.stepfighter.R.string.lang_pl)
    val prefs = remember { context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE) }

    var playerLevel by remember { mutableStateOf(prefs.getInt("player_level", 1)) }
    var playerSteps by remember { mutableStateOf(prefs.getInt("player_steps", 50000)) }
    var playerTokens by remember { mutableStateOf(prefs.getInt("player_tokens", 100)) }

    var hideLocked by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("ALL") }

    val allItems = remember { getFullShopItemsList() }

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
                    bottomBar = { BottomNavigationBar(selectedIndex = 3) },
                    containerColor = BgColor
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(horizontal = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .background(CardBg, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (currentLang == "Polski") "Kroki: $playerSteps" else "Steps: $playerSteps",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (currentLang == "Polski") "Monety: $playerTokens" else "Tokens: $playerTokens",
                                color = GoldColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CategoryTab(
                                label = if (currentLang == "Polski") "Wszystko" else "All",
                                isSelected = selectedCategory == "ALL",
                                modifier = Modifier.weight(1f)
                            ) { selectedCategory = "ALL" }

                            CategoryTab(
                                label = if (currentLang == "Polski") "Alchemia" else "Alchemy",
                                isSelected = selectedCategory == "POTIONS",
                                modifier = Modifier.weight(1f)
                            ) { selectedCategory = "POTIONS" }

                            CategoryTab(
                                label = if (currentLang == "Polski") "Rynsztunek" else "Gear",
                                isSelected = selectedCategory == "GEAR",
                                modifier = Modifier.weight(1f)
                            ) { selectedCategory = "GEAR" }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (currentLang == "Polski") "Ukryj zablokowane" else "Hide locked items",
                                color = TextGray,
                                fontSize = 13.sp
                            )
                            Checkbox(
                                checked = hideLocked,
                                onCheckedChange = { hideLocked = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = GoldColor,
                                    uncheckedColor = TextGray,
                                    checkmarkColor = BgColor
                                )
                            )
                        }

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 24.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (selectedCategory == "ALL" || selectedCategory == "POTIONS") {
                                val alchemyItems = allItems.filter { it.type == "USE" && (!hideLocked || playerLevel >= it.level) }
                                if (alchemyItems.isNotEmpty()) {
                                    item {
                                        ShopSectionHeader(if (currentLang == "Polski") "ALCHEMIA I ZWOJE" else "ALCHEMY & SCROLLS")
                                    }
                                    items(alchemyItems) { item ->
                                        ShopItemRow(item, currentLang, playerLevel, playerSteps, playerTokens) { steps, tokens ->
                                            playerSteps = steps
                                            playerTokens = tokens
                                            prefs.edit().putInt("player_steps", steps).putInt("player_tokens", tokens).apply()
                                            val currentInv = prefs.getString("inventory_items", "") ?: ""
                                            val updatedInv = if (currentInv.isEmpty()) item.id else "$currentInv,${item.id}"
                                            prefs.edit().putString("inventory_items", updatedInv).apply()
                                        }
                                    }
                                }
                            }

                            if (selectedCategory == "ALL" || selectedCategory == "GEAR") {
                                val gearSlots = listOf(
                                    Pair("HEAD", if (currentLang == "Polski") "RYNSZTUNEK GŁOWY" else "HEAD GEAR"),
                                    Pair("RIGHT_HAND", if (currentLang == "Polski") "ORĘŻ (BROŃ)" else "WEAPONS"),
                                    Pair("LEFT_HAND", if (currentLang == "Polski") "TARCZE OBRONNE" else "SHIELDS"),
                                    Pair("CHEST", if (currentLang == "Polski") "PANCERZE KORPUSU" else "CHEST ARMOR"),
                                    Pair("BOOTS", if (currentLang == "Polski") "OBUWIE I GREAWY" else "BOOTS & GREAVES")
                                )

                                gearSlots.forEach { (slotType, title) ->
                                    val slotItems = allItems.filter { it.type == "EQUIP" && it.slot == slotType && (!hideLocked || playerLevel >= it.level) }
                                    if (slotItems.isNotEmpty()) {
                                        item {
                                            ShopSectionHeader(title)
                                        }
                                        items(slotItems) { item ->
                                            ShopItemRow(item, currentLang, playerLevel, playerSteps, playerTokens) { steps, tokens ->
                                                playerSteps = steps
                                                playerTokens = tokens
                                                prefs.edit().putInt("player_steps", steps).putInt("player_tokens", tokens).apply()
                                                val currentInv = prefs.getString("inventory_items", "") ?: ""
                                                val updatedInv = if (currentInv.isEmpty()) item.id else "$currentInv,${item.id}"
                                                prefs.edit().putString("inventory_items", updatedInv).apply()
                                            }
                                        }
                                    }
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
fun ShopSectionHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp)
    ) {
        Text(
            text = title,
            color = GoldColor.copy(alpha = 0.8f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp,
            modifier = Modifier.background(Color.Transparent)
        )
    }
}

@Composable
fun CategoryTab(
    label: String,
    isSelected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(36.dp)
            .background(if (isSelected) GoldColor else Color(0xFF1E1E1E), RoundedCornerShape(4.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) BgColor else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
fun ShopItemRow(
    item: ShopItemData,
    currentLang: String,
    playerLevel: Int,
    playerSteps: Int,
    playerTokens: Int,
    onBuySuccess: (Int, Int) -> Unit
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }
    val isLevelLocked = playerLevel < item.level

    val name = if (currentLang == "Polski") item.namePl else item.nameEn
    val lore = if (currentLang == "Polski") item.lorePl else item.loreEn
    val stat = if (currentLang == "Polski") item.statPl else item.statEn
    val currencyName = if (item.currency == "STEPS") {
        if (currentLang == "Polski") "Kroków" else "Steps"
    } else {
        if (currentLang == "Polski") "Monet" else "Tokens"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isLevelLocked) Color(0xFF1E1E1E) else CardBg, RoundedCornerShape(6.dp))
            .border(
                BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                RoundedCornerShape(6.dp)
            )
            .clickable { isExpanded = !isExpanded }
            .padding(14.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isLevelLocked) Icons.Default.Lock else item.icon,
                        contentDescription = null,
                        tint = if (isLevelLocked) Color.Gray else GoldColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = name,
                            color = if (isLevelLocked) Color.Gray else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        if (isLevelLocked) {
                            Text(
                                text = if (currentLang == "Polski") "Wymagany poziom: ${item.level}" else "Requires Level: ${item.level}",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Text(
                    text = "${item.price} $currencyName",
                    color = if (isLevelLocked) Color.Gray else (if (item.currency == "STEPS") Color.White else GoldColor),
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(bottom = 8.dp))
                    Text(
                        text = lore,
                        color = TextGray,
                        fontSize = 12.sp,
                        fontStyle = FontStyle.Italic,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Text(
                        text = stat,
                        color = if (isLevelLocked) Color.Gray else AccentColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Button(
                        enabled = !isLevelLocked,
                        onClick = {
                            if (item.currency == "STEPS" && playerSteps < item.price) {
                                val msg = if (currentLang == "Polski") "Brak kroków!" else "Not enough steps!"
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (item.currency == "TOKENS" && playerTokens < item.price) {
                                val msg = if (currentLang == "Polski") "Brak monet!" else "Not enough tokens!"
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val nextSteps = if (item.currency == "STEPS") playerSteps - item.price else playerSteps
                            val nextTokens = if (item.currency == "TOKENS") playerTokens - item.price else playerTokens
                            onBuySuccess(nextSteps, nextTokens)

                            val completeMsg = if (currentLang == "Polski") "Zakupiono pomyślnie!" else "Purchase successful!"
                            Toast.makeText(context, completeMsg, Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldColor,
                            contentColor = BgColor,
                            disabledContainerColor = Color(0xFF2A2A2A),
                            disabledContentColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth().height(36.dp)
                    ) {
                        Text(
                            text = if (isLevelLocked) (if (currentLang == "Polski") "ZABLOKOWANE" else "LOCKED") else (if (currentLang == "Polski") "KUP PRZEDMIOT" else "BUY ITEM"),
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

fun getFullShopItemsList(): List<ShopItemData> {
    val list = mutableListOf<ShopItemData>()

    list.add(ShopItemData("pot_hp_1", "Mała Mikstura Zdrowia", "Minor Health Potion", "Szkarłatny wywar z pospolitych ziół leczniczych.", "A crimson decoction made from common medicinal herbs.", "Przywraca 50 HP", "Restores 50 HP", "USE", "NONE", 400, "STEPS", 1, Icons.Default.LocalPharmacy))
    list.add(ShopItemData("pot_hp_2", "Średnia Mikstura Zdrowia", "Medium Health Potion", "Zagęszczony eliksir silnie regenerujący uszkodzone tkanki.", "A concentrated elixir that repairs body tissues.", "Przywraca 150 HP", "Restores 150 HP", "USE", "NONE", 1500, "STEPS", 15, Icons.Default.LocalPharmacy))
    list.add(ShopItemData("pot_hp_3", "Duża Mikstura Zdrowia", "Major Health Potion", "Legendarny napój kapłanów, przywracający wolę walki.", "A legendary drink that restores combat will.", "Przywraca 400 HP", "Restores 400 HP", "USE", "NONE", 5000, "STEPS", 40, Icons.Default.LocalPharmacy))
    list.add(ShopItemData("pot_hp_4", "Boski Eliksir Życia", "Divine Life Elixir", "Płynne światło, które natychmiastowo zamyka nawet najgłębsze rany.", "Liquid light that instantly seals even the deepest wounds.", "Przywraca 1000 HP", "Restores 1000 HP", "USE", "NONE", 12000, "STEPS", 60, Icons.Default.LocalPharmacy))

    list.add(ShopItemData("pot_en_1", "Mała Mikstura Energii", "Minor Energy Potion", "Lekki napar ze skrzydeł imba dodający wigoru stóp.", "A light brew providing vigor to your feet.", "Odnawia 10 EN", "Restores 10 EN", "USE", "NONE", 2000, "STEPS", 1, Icons.Default.ElectricBolt))
    list.add(ShopItemData("pot_en_2", "Średnia Mikstura Energii", "Medium Energy Potion", "Destylat ze skrystalizowanej esencji kinetycznej.", "A distillate from crystallized kinetic essence.", "Odnawia 30 EN", "Restores 30 EN", "USE", "NONE", 7000, "STEPS", 25, Icons.Default.ElectricBolt))
    list.add(ShopItemData("pot_en_3", "Duża Mikstura Energii", "Major Energy Potion", "Esencja czystej, nieposkromionej i dzikiej determinacji.", "The essence of pure and untamed determination.", "Odnawia 100 EN", "Restores 100 EN", "USE", "NONE", 18000, "STEPS", 50, Icons.Default.ElectricBolt))

    list.add(ShopItemData("pot_regen_1", "Mały Eliksir Regeneracji", "Minor Regeneration Elixir", "Słodki sok z paproci, który powoli stabilizuje puls.", "Sweet fern juice that slowly stabilizes the pulse.", "Odnawia 15 HP na turę", "Restores 15 HP per turn", "USE", "NONE", 600, "STEPS", 1, Icons.Default.Favorite))
    list.add(ShopItemData("pot_regen_2", "Wielki Eliksir Regeneracji", "Major Regeneration Elixir", "Wyciąg z serca trolla, gwarantujący potężną odnowę.", "Extract from a troll's heart, guaranteeing massive renewal.", "Odnawia 50 HP na turę", "Restores 50 HP per turn", "USE", "NONE", 3500, "STEPS", 30, Icons.Default.Favorite))
    list.add(ShopItemData("pot_fury_1", "Nektar Berserkera", "Berserker Nectar", "Gęsty, wrzący płyn, nasycający wojownika czystą furią.", "A thick, boiling liquid infusing the warrior with pure fury.", "+5 Ataku w lochu", "+5 Attack in dungeon", "USE", "NONE", 1200, "STEPS", 10, Icons.Default.LocalFireDepartment))
    list.add(ShopItemData("pot_greed_1", "Olej Szczęścia Chciwca", "Greed Fortune Oil", "Pachnie starym złotem. Przyciąga bogactwo w lochach.", "Smells like old gold. Attracts wealth in dungeons.", "+25% Monet z walki", "+25% Tokens from combat", "USE", "NONE", 2500, "STEPS", 20, Icons.Default.MonetizationOn))
    list.add(ShopItemData("pot_anti_1", "Uniwersalne Antidotum", "Universal Antidote", "Neutralizuje wszelkie jady gniazdujące w mroku.", "Neutralizes all venoms nesting in the dark.", "Usuwa trucizny", "Removes poisons", "USE", "NONE", 500, "STEPS", 1, Icons.Default.Medication))

    list.add(ShopItemData("scroll_esc", "Zwój Ucieczki", "Escape Scroll", "Natychmiastowo wyciąga wojownika z walki, gwarantując bezpieczny odwrót.", "Instantly extracts a warrior from combat, guaranteeing a safe retreat.", "Bezpieczny odwrót (100%)", "Safe retreat (100%)", "USE", "NONE", 300, "STEPS", 1, Icons.Default.InsertDriveFile))
    list.add(ShopItemData("scroll_tp", "Zwój Teleportacji", "Teleportation Scroll", "Pozwala przenieść się bezpiecznie na początek obecnego piętra lochów.", "Allows you to teleport safely to the start of the current dungeon floor.", "Powrót na start piętra", "Return to floor start", "USE", "NONE", 800, "STEPS", 5, Icons.Default.InsertDriveFile))
    list.add(ShopItemData("scroll_id", "Zwój Identyfikacji", "Identification Scroll", "Inkantacja ujawniająca ukryte statystyki i potęgę następnego potwora.", "Incantation revealing hidden stats and power of the next monster.", "Odkrywa cechy wroga", "Reveals enemy traits", "USE", "NONE", 450, "STEPS", 1, Icons.Default.InsertDriveFile))
    list.add(ShopItemData("scroll_bless", "Zwój Błogosławieństwa", "Blessing Scroll", "Przepełnia rynsztunek świętą energią, osłabiając nadchodzące ciosy.", "Infuses equipment with holy energy, weakening incoming strikes.", "Blokuje 15% obrażeń wroga", "Blocks 15% enemy damage", "USE", "NONE", 2000, "STEPS", 15, Icons.Default.InsertDriveFile))
    list.add(ShopItemData("scroll_fire", "Zwój Kuli Ognia", "Fireball Scroll", "Spala całą pierwszą linię wroga potężną eksplozją magii.", "Burns the entire enemy front line with a powerful magic explosion.", "Zadaje 100 dmg na start", "Deals 100 dmg at start", "USE", "NONE", 4000, "STEPS", 25, Icons.Default.InsertDriveFile))
    list.add(ShopItemData("scroll_doom", "Zwój Zagłady", "Doom Scroll", "Przeklina loch. Niszczy 30% obecnego zdrowia każdego wroga w strefie.", "Curses the dungeon. Destroys 30% current HP of every enemy in the zone.", "-30% HP wszystkim wrogom", "-30% HP to all enemies", "USE", "NONE", 9000, "STEPS", 50, Icons.Default.InsertDriveFile))

    list.add(ShopItemData("helm_tier1", "Opaska Nowicjusza", "Novice Bandana", "Zwykły kawałek materiału chroniący oczy przed potem.", "A simple piece of cloth protecting eyes from sweat.", "+10 HP, +2 Obrony", "+10 HP, +2 Armor", "EQUIP", "HEAD", 3, "TOKENS", 1, Icons.Default.Face))
    list.add(ShopItemData("helm_tier2", "Płócienny Kaptur Zwiadowcy", "Scout Cloth Hood", "Lekki kaptur, idealny do wtapiania się w cienie krzaków.", "A light hood, ideal for blending into the shadows.", "+25 HP, +5 Obrony", "+25 HP, +5 Armor", "EQUIP", "HEAD", 12, "TOKENS", 5, Icons.Default.Face))
    list.add(ShopItemData("helm_tier3", "Skórzany Hełm Ćwiekowany", "Studded Leather Helm", "Wzmocniona skóra chroniąca przed niespodziewanymi cięciami potworów.", "Reinforced leather protective against unexpected slashes.", "+60 HP, +12 Obrony", "+60 HP, +12 Armor", "EQUIP", "HEAD", 35, "TOKENS", 15, Icons.Default.Face))
    list.add(ShopItemData("helm_tier4", "Żelazny Szyszak Rekruta", "Iron Recruit Helmet", "Masywny garnczkowy hełm, standardowe wyposażenie armii miejskiej.", "A massive pot helmet, standard city vanguard issue.", "+120 HP, +24 Obrony", "+120 HP, +24 Armor", "EQUIP", "HEAD", 80, "TOKENS", 30, Icons.Default.Face))
    list.add(ShopItemData("helm_tier5", "Stalowy Przyłbica Gwardzisty", "Sturdy Steel Visor", "Wytworny hełm królewskiej gwardii. Wykuty z hartowanej stali.", "An exquisite royal guard helmet forged from hardened steel.", "+250 HP, +50 Obrony", "+250 HP, +50 Armor", "EQUIP", "HEAD", 180, "TOKENS", 50, Icons.Default.Face))
    list.add(ShopItemData("helm_tier6", "Mityczna Korona Kroku", "Mythic Crown of Pacing", "Zwieńczenie rynsztunku prawdziwego championa. Emanuje złotym blaskiem.", "The crown of a true pacing champion. It radiates a golden glow.", "+600 HP, +120 Obrony", "+600 HP, +120 Armor", "EQUIP", "HEAD", 500, "TOKENS", 75, Icons.Default.Face))

    list.add(ShopItemData("wpn_tier1", "Zardzewiały Sztylet", "Rusty Dagger", "Znoszone i wyszczerbione ostrze znalezione w stercie złomu.", "An old, chipped blade found in a scrap pile.", "+3 Atak", "+3 Attack", "EQUIP", "RIGHT_HAND", 4, "TOKENS", 1, Icons.Default.HorizontalRule))
    list.add(ShopItemData("wpn_tier2", "Myśliwski Kordelas", "Hunter Cutlass", "Dobrze wyważony nóż używany przez leśnych traperów.", "A well-balanced knife used by wilderness trappers.", "+8 Atak", "+8 Attack", "EQUIP", "RIGHT_HAND", 15, "TOKENS", 5, Icons.Default.HorizontalRule))
    list.add(ShopItemData("wpn_tier3", "Żelazny Miecz Krótki", "Iron Shortsword", "Solidny żelazny miecz, wykuty z myślą o prostych żołnierzach.", "A sturdy iron sword forged for common soldiers.", "+18 Atak", "+18 Attack", "EQUIP", "RIGHT_HAND", 45, "TOKENS", 15, Icons.Default.HorizontalRule))
    list.add(ShopItemData("wpn_tier4", "Stalowy Tasak Bojowy", "Steel Cleaver", "Ciężka broń zdolna gładko rąbać najgrubsze pancerze wrogów.", "A heavy weapon capable of chopping through thick armor.", "+40 Atak", "+40 Attack", "EQUIP", "RIGHT_HAND", 110, "TOKENS", 30, Icons.Default.HorizontalRule))
    list.add(ShopItemData("wpn_tier5", "Runiczne Ostrze Strażnika", "Runic Sentry Blade", "Starożytna stal runiczna, potęgująca siłę fizycznych uderzeń.", "Ancient runic steel amplifying physical strike force.", "+90 Atak", "+90 Attack", "EQUIP", "RIGHT_HAND", 280, "TOKENS", 50, Icons.Default.HorizontalRule))
    list.add(ShopItemData("wpn_tier6", "Demoniczny Pożeracz Ścieżek", "Demonic Path Eater", "Broń wykuta w głębinach otchłani. Pochłania esencję pokonanych.", "A weapon forged in the deep abyss. Absorbs the essence of the fallen.", "+220 Atak", "+220 Attack", "EQUIP", "RIGHT_HAND", 750, "TOKENS", 75, Icons.Default.HorizontalRule))

    list.add(ShopItemData("shd_tier1", "Pęknięty Puklerz", "Buckler", "Mała metalowa tarcza, która ledwo trzyma się w całości.", "A small metal shield barely holding together.", "+1 Obrony", "+1 Armor", "EQUIP", "LEFT_HAND", 3, "TOKENS", 1, Icons.Default.Shield))
    list.add(ShopItemData("shd_tier2", "Drewniana Tarcza Traktowa", "Wooden Trail Shield", "Wyciosana z grubych dębowych desek zebranych przy drodze.", "Carved from thick oak planks gathered by the road.", "+4 Obrony", "+4 Armor", "EQUIP", "LEFT_HAND", 10, "TOKENS", 5, Icons.Default.Shield))
    list.add(ShopItemData("shd_tier3", "Wzmocniona Tarcza Skórzana", "Reinforced Targe", "Lekka, obciągnięta grubą skórą tarcza z metalowym umbem.", "Light, leather-bound shield with a sturdy metal boss.", "+10 Obrony", "+10 Armor", "EQUIP", "LEFT_HAND", 30, "TOKENS", 15, Icons.Default.Shield))
    list.add(ShopItemData("shd_tier4", "Stalowa Tarcza Piechoty", "Steel Kite Shield", "Trójkątna tarcza zapewniająca doskonałą osłonę całego ciała.", "A triangular shield offering excellent full-body protection.", "+22 Obrony", "+22 Armor", "EQUIP", "LEFT_HAND", 90, "TOKENS", 30, Icons.Default.Shield))
    list.add(ShopItemData("shd_tier5", "Egida Wiecznego Marszu", "Aegis of Eternal March", "Legendarna bariera strażników traktu. Nie do przebicia.", "Legendary barrier of pathkeepers. Absolutely impenetrable.", "+50 Obrony", "+50 Armor", "EQUIP", "LEFT_HAND", 240, "TOKENS", 50, Icons.Default.Shield))
    list.add(ShopItemData("shd_tier6", "Boskie Zwierciadło Prawdy", "Divine Truth Mirror", "Aura tej tarczy oślepia przeciwników, odbijając ich nienawiść.", "The aura of this shield blinds enemies, reflecting their animosity.", "+110 Obrony", "+110 Armor", "EQUIP", "LEFT_HAND", 650, "TOKENS", 75, Icons.Default.Shield))

    list.add(ShopItemData("chst_tier1", "Szmaciana Tunika", "Ragged Tunica", "Zwykłe płótno dające minimalną ochronę przed zadrapaniami.", "Basic canvas offering minimal scratch protection.", "+15 HP, +2 Obrony", "+15 HP, +2 Armor", "EQUIP", "CHEST", 4, "TOKENS", 1, Icons.Default.Checkroom))
    list.add(ShopItemData("chst_tier2", "Przeszywanica Myśliwego", "Hunter Gambeson", "Pikowany kaftan wytłumiający lżejsze uderzenia obuchowe.", "Quilted jacket designed to dampen lighter blunt impacts.", "+40 HP, +6 Obrony", "+40 HP, +6 Armor", "EQUIP", "CHEST", 18, "TOKENS", 5, Icons.Default.Checkroom))
    list.add(ShopItemData("chst_tier3", "Skórzana Kurta Ćwiekowana", "Studded Leather Jerkin", "Warstwowa skóra wzmocniona nitami. Bardzo elastyczna.", "Layered leather reinforced with rivets. Very flexible.", "+90 HP, +14 Obrony", "+90 HP, +14 Armor", "EQUIP", "CHEST", 50, "TOKENS", 15, Icons.Default.Checkroom))
    list.add(ShopItemData("chst_tier4", "Żelazna Kolczuga Poborowego", "Iron Recruit Mail", "Splot stalowych kółek chroniący klatkę piersiową i ramiona.", "Interlinked iron rings protecting the chest and shoulders.", "+200 HP, +30 Obrony", "+200 HP, +30 Armor", "EQUIP", "CHEST", 140, "TOKENS", 30, Icons.Default.Checkroom))
    list.add(ShopItemData("chst_tier5", "Płytowy Pancerz Rycerski", "Steel Knight Plate", "Hartowane stalowe płyty chroniące przed najcięższymi ciosami.", "Hardened steel plates guarding against the heaviest blows.", "+450 HP, +65 Obrony", "+450 HP, +65 Armor", "EQUIP", "CHEST", 380, "TOKENS", 50, Icons.Default.Checkroom))
    list.add(ShopItemData("chst_tier6", "Zbroja Świętego Championa", "Divine Champion Regalia", "Eteryczny pancerz wykuty z czystej woli zwycięstwa.", "Ethereal armor forged from the pure spirit of victory.", "+1100 HP, +150 Obrony", "+1100 HP, +150 Armor", "EQUIP", "CHEST", 900, "TOKENS", 75, Icons.Default.Checkroom))

    list.add(ShopItemData("bts_tier1", "Rozpadające się Łapcie", "Tattered Wraps", "Znoszone owijki płócienne, ledwo chroniące bose stopo.", "Worn cloth wraps barely protecting bare soles.", "+2 Zręczność", "+2 Zręczność", "EQUIP", "BOOTS", 3, "TOKENS", 1, Icons.Default.DirectionsWalk))
    list.add(ShopItemData("bts_tier2", "Skórzane Buty Podróżne", "Leather Trail Boots", "Wygodne obuwie, idealnie dopasowane do długich wędrówek.", "Comfortable footwear perfectly broken-in for long treks.", "+5 Zręczność", "+5 Zręczność", "EQUIP", "BOOTS", 12, "TOKENS", 5, Icons.Default.DirectionsWalk))
    list.add(ShopItemData("bts_tier3", "Okute Buty Piechura", "Reinforced Hiker Boots", "Gruba podeszwa ze stalowym czubkiem, niezastąpiona w lochach.", "Thick sole with a steel toe, indispensable in dungeons.", "+12 Zręczność", "+12 Zręczność", "EQUIP", "BOOTS", 35, "TOKENS", 15, Icons.Default.DirectionsWalk))
    list.add(ShopItemData("bts_tier4", "Buty Siedmiomilowe", "Seven-League Boots", "Magiczne obuwie, które drastycznie przyspiesza ruch nóg.", "Magical footwear that drastically accelerates leg speed.", "+25 Zręczność", "+25 Zręczność", "EQUIP", "BOOTS", 100, "TOKENS", 30, Icons.Default.DirectionsWalk))
    list.add(ShopItemData("bts_tier5", "Pancerne Buty Strażnika", "Steel Vanguard Greaves", "Ciężkie metalowe nagolenniki połączone z profilowanym butem.", "Heavy metal greaves integrated with a contoured boot.", "+55 Zręczność", "+55 Zręczność", "EQUIP", "BOOTS", 290, "TOKENS", 50, Icons.Default.DirectionsWalk))
    list.add(ShopItemData("bts_tier6", "Skrzydlate Buty Hermesa", "Winged Astral Striders", "Pozwalają niemal lewitować nad pułapkami. Absolutna zwinność.", "Allows near-levitation over traps. Absolute agility.", "+130 Zręczność", "+130 Zręczność", "EQUIP", "BOOTS", 800, "TOKENS", 75, Icons.Default.DirectionsWalk))

    return list
}