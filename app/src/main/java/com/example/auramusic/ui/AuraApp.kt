package com.example.auramusic.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.content.ContextCompat
import com.example.R
import com.example.auramusic.data.local.AuraDatabase
import com.example.auramusic.data.model.AuraThemeStyle
import com.example.auramusic.ui.screens.HomeScreen
import com.example.auramusic.ui.screens.NowPlayingScreen
import com.example.auramusic.ui.screens.PlaylistDetailScreen
import com.example.auramusic.ui.screens.SearchScreen
import com.example.auramusic.ui.screens.SettingsScreen
import com.example.auramusic.ui.viewmodel.AppScreen
import com.example.auramusic.ui.viewmodel.AuraMusicViewModel
import com.example.ui.theme.AuraBubblegumPink
import com.example.ui.theme.AuraElectricBlue
import com.example.ui.theme.AuraMusicTheme
import com.example.ui.theme.AuraPlayCoral
import com.example.ui.theme.AuraSoftPurple

@Composable
fun AuraApp(
    viewModel: AuraMusicViewModel,
    database: AuraDatabase
) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isDarkGlass by viewModel.isDarkGlass.collectAsState()
    val themeStyle by viewModel.themeStyle.collectAsState()

    // Permissions check
    val requiredPermissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val audioGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            results[Manifest.permission.READ_MEDIA_AUDIO] == true
        } else {
            results[Manifest.permission.READ_EXTERNAL_STORAGE] == true
        }
        viewModel.setHasPermission(audioGranted, context)
    }

    LaunchedEffect(Unit) {
        val allGranted = requiredPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (allGranted) {
            viewModel.setHasPermission(true, context)
        } else {
            permissionLauncher.launch(requiredPermissions)
        }
    }

    AuraMusicTheme(
        darkTheme = isDarkGlass,
        themeStyle = themeStyle
    ) {
        // Atmospheric Ambient Liquid Glass Background
        Box(modifier = Modifier.fillMaxSize()) {
            // Ethereal ambient 3D glass background artwork
            Image(
                painter = painterResource(R.drawable.aura_bg_ambient_1794131080463),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(if (isDarkGlass) 0.55f else 0.35f)
            )

            // Dynamic thematic gradient tint overlay
            val overlayColors = when (themeStyle) {
                AuraThemeStyle.AURORA_VIOLET -> listOf(
                    Color(0xE6141334),
                    Color(0xB3271D58),
                    Color(0xF20F0D24)
                )
                AuraThemeStyle.DEEP_SAPPHIRE -> listOf(
                    Color(0xE60D193E),
                    Color(0xB3152F6E),
                    Color(0xF2091028)
                )
                AuraThemeStyle.NEON_SUNSET -> listOf(
                    Color(0xE6261230),
                    Color(0xB3481845),
                    Color(0xF215091D)
                )
                AuraThemeStyle.CYBER_BLUSH -> listOf(
                    Color(0xE6251433),
                    Color(0xB33A184A),
                    Color(0xF212081B)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(overlayColors))
            )

            // Main Screen Router
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_switch"
            ) { screen ->
                when (screen) {
                    AppScreen.HOME -> HomeScreen(
                        viewModel = viewModel,
                        onRequestPermission = { permissionLauncher.launch(requiredPermissions) }
                    )
                    AppScreen.NOW_PLAYING -> NowPlayingScreen(
                        viewModel = viewModel
                    )
                    AppScreen.SEARCH -> SearchScreen(
                        viewModel = viewModel
                    )
                    AppScreen.PLAYLIST_DETAIL -> PlaylistDetailScreen(
                        viewModel = viewModel,
                        database = database
                    )
                    AppScreen.SETTINGS -> SettingsScreen(
                        viewModel = viewModel,
                        onRequestPermission = { permissionLauncher.launch(requiredPermissions) }
                    )
                }
            }
        }
    }
}
