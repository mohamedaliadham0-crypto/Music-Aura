package com.example.auramusic.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auramusic.data.model.AuraThemeStyle
import com.example.auramusic.ui.components.GlassCircleButton
import com.example.auramusic.ui.components.GlassPanel
import com.example.auramusic.ui.viewmodel.AppScreen
import com.example.auramusic.ui.viewmodel.AuraMusicViewModel
import com.example.ui.theme.AuraBubblegumPink
import com.example.ui.theme.AuraElectricBlue
import com.example.ui.theme.AuraLavender
import com.example.ui.theme.AuraPlayCoral
import com.example.ui.theme.AuraPlayPeach
import com.example.ui.theme.AuraSoftPurple

@Composable
fun SettingsScreen(
    viewModel: AuraMusicViewModel,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(AppScreen.HOME)
    }

    val themeStyle by viewModel.themeStyle.collectAsState()
    val isDarkGlass by viewModel.isDarkGlass.collectAsState()
    val hasPermission by viewModel.hasStoragePermission.collectAsState()
    val library by viewModel.musicLibrary.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassCircleButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    onClick = { viewModel.navigateTo(AppScreen.HOME) },
                    contentDescription = "Back",
                    tag = "settings_back_button"
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Settings",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Section: Visual Identity & Atmosphere
            SettingsSectionHeader(title = "Liquid Atmosphere & Themes", icon = Icons.Filled.ColorLens)

            GlassPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Accent Gradient Theme",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AuraThemeStyle.values().forEach { style ->
                            val isSelected = themeStyle == style
                            val colors = when (style) {
                                AuraThemeStyle.AURORA_VIOLET -> listOf(AuraSoftPurple, AuraLavender)
                                AuraThemeStyle.DEEP_SAPPHIRE -> listOf(AuraElectricBlue, Color(0xFF192B72))
                                AuraThemeStyle.NEON_SUNSET -> listOf(AuraPlayCoral, AuraBubblegumPink)
                                AuraThemeStyle.CYBER_BLUSH -> listOf(AuraBubblegumPink, AuraLavender)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Brush.linearGradient(colors))
                                    .border(
                                        width = if (isSelected) 2.5.dp else 0.dp,
                                        color = if (isSelected) Color.White else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.setThemeStyle(style) }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = style.displayName.split(" ").first(),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Deep Glass Mode",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Enforces high-contrast translucent styling",
                                color = AuraLavender,
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = isDarkGlass,
                            onCheckedChange = { viewModel.toggleDarkGlass() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AuraPlayCoral
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section: Music Library & Storage
            SettingsSectionHeader(title = "Local Music Library", icon = Icons.Filled.MusicNote)

            GlassPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Storage Permission",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (hasPermission) "Granted (MediaStore Audio)" else "Permission needed",
                                color = if (hasPermission) Color(0xFF66E2B1) else AuraPlayCoral,
                                fontSize = 12.sp
                            )
                        }
                        if (!hasPermission) {
                            Button(
                                onClick = onRequestPermission,
                                colors = ButtonDefaults.buttonColors(containerColor = AuraPlayCoral),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Grant")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Tracks in Library",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${library.allSongs.size} songs • ${library.albums.size} albums",
                                color = AuraLavender,
                                fontSize = 12.sp
                            )
                        }
                        Button(
                            onClick = { viewModel.refreshLibrary() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Rescan",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Rescan", color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Playback History",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Clear recently played cache",
                                color = AuraLavender,
                                fontSize = 12.sp
                            )
                        }
                        Button(
                            onClick = { viewModel.clearHistory() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CleaningServices,
                                contentDescription = "Clear",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Clear", color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section: Home Screen Widgets
            SettingsSectionHeader(title = "Android Home Screen Widgets", icon = Icons.Filled.Widgets)

            GlassPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3 Native Widget Sizes Available:",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    WidgetInfoItem(name = "Mini Player (2x1)", desc = "Compact glass bar with artwork, title, and play/next buttons.")
                    WidgetInfoItem(name = "Standard Player (4x1)", desc = "Wider glass player with full controls and track progress bar.")
                    WidgetInfoItem(name = "Premium Player (4x2)", desc = "Hero widget with prominent cover, duration counters, and shuffle mode.")
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "How to add: Long-press your home screen, tap Widgets, find \"Aura Music\", and drag your preferred size onto the screen.",
                        color = AuraLavender,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section: About
            SettingsSectionHeader(title = "About Aura Music", icon = Icons.Filled.Info)

            GlassPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AURA MUSIC v1.0",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Premium native Android MP3 player built with Kotlin, Jetpack Compose, Media3 ExoPlayer, and Liquid Glass design.",
                        color = AuraLavender,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• 100% Offline & Private: No accounts or tracking\n• Background Playback with Headphone Auto-pause\n• Scoped Storage & Auto-sync with MediaStore",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AuraPlayCoral,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun WidgetInfoItem(name: String, desc: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = "• $name", color = AuraPlayPeach, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text(text = desc, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
    }
}
