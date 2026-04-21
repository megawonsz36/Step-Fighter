package com.example.stepfighter.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.stepfighter.R
import com.example.stepfighter.ui.profile.BgColor
import com.example.stepfighter.ui.profile.GoldColor
import com.example.stepfighter.ui.profile.TextGray
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import com.example.stepfighter.ui.settings.SettingsActivity

@Composable
fun SideMenuContent(onClose: () -> Unit) {
    val context = LocalContext.current
    ModalDrawerSheet(
        drawerContainerColor = BgColor,
        drawerShape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
        modifier = Modifier.fillMaxHeight().width(300.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Nagłówek menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.menu_title),
                    color = GoldColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = GoldColor)
                }
            }

            Spacer(Modifier.height(32.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    SideMenuItem(stringResource(R.string.menu_rankings), Icons.Default.EmojiEvents) {
                        onClose()
                    }
                }
                item {
                    SideMenuItem(stringResource(R.string.menu_quest_book), Icons.Default.AutoStories) {
                        onClose()
                    }
                }
                item {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = Color.White.copy(alpha = 0.1f)
                    )
                }
                item {
                    SideMenuItem(stringResource(R.string.menu_settings), Icons.Default.Settings) {
                        val intent = Intent(context, SettingsActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        context.startActivity(intent)
                        onClose()
                    }
                }
                item {
                    SideMenuItem(stringResource(R.string.menu_logout), Icons.AutoMirrored.Filled.Logout, color = Color(0xFFB94A4A)) {
                        onClose()
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Text(
                "v1.0.4",
                modifier = Modifier.fillMaxWidth(),
                color = TextGray.copy(alpha = 0.5f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SideMenuItem(
    label: String,
    icon: ImageVector,
    color: Color = Color.White,
    isSmall: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (color == Color.White) GoldColor else color,
                modifier = Modifier.size(if (isSmall) 20.dp else 24.dp)
            )
            Spacer(Modifier.width(16.dp))
            Text(
                label,
                color = if (isSmall && color == Color.White) TextGray else color,
                fontSize = if (isSmall) 14.sp else 16.sp,
                fontWeight = if (isSmall) FontWeight.Normal else FontWeight.Bold
            )
        }
    }
}
