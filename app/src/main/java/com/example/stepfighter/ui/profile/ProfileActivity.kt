package com.example.stepfighter.ui.profile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.stepfighter.R // Zastąp to rzeczywistym pakietem Twojej aplikacji!

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Tutaj możesz dodać swój motyw aplikacji, np. StepFighterTheme { ... }
            StatystykiSpojnyProfil()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatystykiSpojnyProfil(modifier: Modifier = Modifier) {
    // Uwaga: Usunąłem nieistniejącą zmienną 'badgeNumber' i zastąpiłem statycznym tekstem "1".
    // Będziesz musiał dostarczyć prawdziwe dane lub stany w docelowej implementacji.

    Box(
        modifier = modifier
            .fillMaxSize() // Zmieniono z required na fillMaxSize dla lepszego responsywnego zachowania
            .background(color = Color(0xff131313))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.Top),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 96.dp,
                        bottom = 128.dp
                    )
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .requiredHeight(height = 184.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "ŁĄCZNA LICZBA KROKÓW",
                                    color = Color(0xff9a8f80),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 1.33.em,
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        letterSpacing = 3.6.sp
                                    ),
                                    modifier = Modifier.wrapContentHeight(align = Alignment.CenterVertically)
                                )
                            }
                            CenterAlignedTopAppBar(
                                title = {
                                    Text(
                                        text = "142,850",
                                        color = Color(0xffffb783),
                                        textAlign = TextAlign.Center,
                                        lineHeight = 1.em,
                                        style = TextStyle(
                                            fontSize = 60.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = (-3).sp
                                        ),
                                        modifier = Modifier.wrapContentHeight(align = Alignment.CenterVertically)
                                    )
                                },
                                modifier = Modifier.shadow(elevation = 15.dp)
                            )
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Legenda w drodze",
                                    color = Color(0xffe9c349),
                                    fontStyle = FontStyle.Italic,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 1.56.em,
                                    style = TextStyle(
                                        fontSize = 18.sp
                                    ),
                                    modifier = Modifier.wrapContentHeight(align = Alignment.CenterVertically)
                                )
                            }
                        }
                    }
                }
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .requiredHeight(height = 409.dp)
                            .clip(shape = RoundedCornerShape(2.dp))
                            .background(color = Color(0xffc39d91))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(all = 4.dp)
                        ) {
                            Surface(
                                border = BorderStroke(12.dp, Color(0xff50342c).copy(alpha = 0.2f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(all = 24.dp)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 8.dp)
                                    ) {
                                        Text(
                                            text = "ATRYBUTY WOJOWNIKA",
                                            color = Color(0xff50342c),
                                            textAlign = TextAlign.Center,
                                            lineHeight = 1.5.em,
                                            style = TextStyle(
                                                fontSize = 16.sp,
                                                letterSpacing = 1.6.sp
                                            ),
                                            modifier = Modifier.wrapContentHeight(align = Alignment.CenterVertically)
                                        )
                                    }
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.Top),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        // Sekcja SIŁA
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "74",
                                                    color = Color(0xff50342c),
                                                    style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Black)
                                                )
                                                // Usunięto nieznane odniesienie do badgeNumber
                                            }
                                            Text(
                                                text = "SIŁA",
                                                color = Color(0xff50342c),
                                                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                            )
                                        }

                                        // Sekcja ZRĘCZNOŚĆ
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "91",
                                                    color = Color(0xff50342c),
                                                    style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Black)
                                                )
                                            }
                                            Text(
                                                text = "ZRĘCZNOŚĆ",
                                                color = Color(0xff50342c),
                                                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                            )
                                        }

                                        // Sekcja INTELIGENCJA
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "45",
                                                    color = Color(0xff50342c),
                                                    style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Black)
                                                )
                                            }
                                            Text(
                                                text = "INTELIGENCJA",
                                                color = Color(0xff50342c),
                                                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                            )
                                        }

                                        // Sekcja WITALNOŚĆ
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "88",
                                                    color = Color(0xff50342c),
                                                    style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Black)
                                                )
                                            }
                                            Text(
                                                text = "WITALNOŚĆ",
                                                color = Color(0xff50342c),
                                                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    }
                                }
                            }
                            // Usunięto błędy związane z powtarzającym się checkedState
                        }
                    }
                }
                // Dalsza część elementów (Mapa postępu, Zasługi) została uproszczona,
                // aby kod był czytelniejszy. Będziesz je stopniowo dodawał.
            }
        }

        // Dolny pasek nawigacyjny
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(alignment = Alignment.BottomCenter)
                .fillMaxWidth()
                .requiredHeight(height = 80.dp)
                .clip(shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                .background(color = Color(0xff1c1c1c))
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Lochy", color = Color(0xffd1c5b4), fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Ekwipunek", color = Color(0xffd1c5b4), fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.background(Color(0xffffb783).copy(alpha = 0.1f)).padding(4.dp)) {
                Text("Statystyki", color = Color(0xffffb783), fontSize = 12.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StatystykiSpojnyProfilPreview() {
    StatystykiSpojnyProfil()
}