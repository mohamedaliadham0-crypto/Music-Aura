package com.example.auramusic.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auramusic.data.model.PlaybackMode
import com.example.auramusic.data.model.Song
import com.example.auramusic.playback.PlaybackStateManager
import com.example.auramusic.ui.components.AlbumArtworkView
import com.example.auramusic.ui.components.GlassCircleButton
import com.example.auramusic.ui.components.GlassPanel
import com.example.auramusic.ui.components.GlassProgressBar
import com.example.auramusic.ui.components.GlowingPlayButton
import com.example.auramusic.ui.viewmodel.AppScreen
import com.example.auramusic.ui.viewmodel.AuraMusicViewModel
import com.example.ui.theme.AuraBubblegumPink
import com.example.ui.theme.AuraElectricBlue
import com.example.ui.theme.AuraGlassFill
import com.example.ui.theme.AuraGlassHighlight
import com.example.ui.theme.AuraGlassStroke
import com.example.ui.theme.AuraLavender
import com.example.ui.theme.AuraPlayCoral
import com.example.ui.theme.AuraPlayPeach
import com.example.ui.theme.AuraSoftPurple
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    viewModel: AuraMusicViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    BackHandler {
        viewModel.navigateTo(AppScreen.HOME)
    }

    val currentSong by PlaybackStateManager.currentSong.collectAsState()
    val isPlaying by PlaybackStateManager.isPlaying.collectAsState()
    val positionMs by PlaybackStateManager.playbackPositionMs.collectAsState()
    val durationMs by PlaybackStateManager.durationMs.collectAsState()
    val playbackMode by PlaybackStateManager.playbackMode.collectAsState()
    val queue by PlaybackStateManager.queue.collectAsState()
    val currentIndex by PlaybackStateManager.currentIndex.collectAsState()
    val favoriteIds by viewModel.favoriteSongIds.collectAsState()

    val isFavorite = currentSong != null && favoriteIds.contains(currentSong!!.id)
    var showQueueSheet by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = if (isPlaying) 0.85f else 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Back Arrow, "Listen now", 3-dots
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassCircleButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    onClick = { viewModel.navigateTo(AppScreen.HOME) },
                    contentDescription = "Back to Library",
                    tag = "now_playing_back"
                )

                Text(
                    text = "Listen now",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                GlassCircleButton(
                    icon = Icons.AutoMirrored.Filled.QueueMusic,
                    onClick = { showQueueSheet = true },
                    contentDescription = "Playback Queue",
                    tag = "now_playing_queue"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Large Centered Artwork inside Glowing Liquid Glass Frame (Matching reference image)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(1f)
                    .shadow(
                        elevation = 24.dp,
                        shape = RoundedCornerShape(36.dp),
                        ambientColor = AuraSoftPurple.copy(alpha = pulseGlow),
                        spotColor = AuraPlayCoral.copy(alpha = pulseGlow)
                    )
                    .clip(RoundedCornerShape(36.dp))
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color.White.copy(alpha = 0.25f),
                                Color(0x332E2764)
                            )
                        )
                    )
                    .border(
                        BorderStroke(
                            2.dp,
                            Brush.linearGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.75f),
                                    AuraLavender.copy(alpha = 0.3f),
                                    Color.White.copy(alpha = 0.15f)
                                )
                            )
                        ),
                        RoundedCornerShape(36.dp)
                    )
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                AlbumArtworkView(
                    song = currentSong,
                    cornerRadius = 28.dp,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Title & Artist
            AnimatedContent(
                targetState = currentSong,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                label = "title_change"
            ) { targetSong ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = targetSong?.title ?: "Select a song",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = targetSong?.artist ?: "Aura Music Player",
                        color = AuraLavender,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Liquid Glass Progress Bar
            GlassProgressBar(
                positionMs = positionMs,
                durationMs = durationMs,
                onSeek = { seekPos -> viewModel.seekTo(seekPos) },
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Main Playback Controls (Previous, Large Play/Pause, Next)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous Button
                GlassCircleButton(
                    icon = Icons.Filled.SkipPrevious,
                    onClick = { viewModel.skipPrevious() },
                    size = 54.dp,
                    iconSize = 28.dp,
                    contentDescription = "Previous Song",
                    tag = "player_prev_button"
                )

                // Large Glowing Play/Pause Button
                GlowingPlayButton(
                    isPlaying = isPlaying,
                    onClick = { viewModel.togglePlayPause(context) },
                    size = 76.dp,
                    iconSize = 38.dp,
                    tag = "player_play_pause_button"
                )

                // Next Button
                GlassCircleButton(
                    icon = Icons.Filled.SkipNext,
                    onClick = { viewModel.skipNext() },
                    size = 54.dp,
                    iconSize = 28.dp,
                    contentDescription = "Next Song",
                    tag = "player_next_button"
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom Auxiliary Controls: Playback Mode (Shuffle/Normal/Repeat 1), Favorite Heart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Playback Mode pill button
                GlassPanel(
                    modifier = Modifier
                        .clickable { viewModel.cyclePlaybackMode(context) }
                        .testTag("mode_toggle_button"),
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = Color(0x33FFFFFF)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val (modeIcon, modeText) = when (playbackMode) {
                            PlaybackMode.NORMAL -> Icons.Filled.Repeat to "Sequential"
                            PlaybackMode.SHUFFLE -> Icons.Filled.Shuffle to "Shuffle"
                            PlaybackMode.REPEAT_ONE -> Icons.Filled.RepeatOne to "Repeat 1"
                        }

                        Icon(
                            imageVector = modeIcon,
                            contentDescription = modeText,
                            tint = if (playbackMode != PlaybackMode.NORMAL) AuraPlayCoral else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = modeText,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Favorite Heart Button
                GlassCircleButton(
                    icon = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    onClick = {
                        currentSong?.let { viewModel.toggleFavorite(it.id) }
                    },
                    tint = if (isFavorite) AuraBubblegumPink else Color.White,
                    size = 46.dp,
                    iconSize = 22.dp,
                    contentDescription = "Toggle Favorite",
                    tag = "player_favorite_button"
                )
            }
        }

        // Queue Bottom Sheet
        if (showQueueSheet) {
            val sheetState = rememberModalBottomSheetState()
            val scope = rememberCoroutineScope()

            ModalBottomSheet(
                onDismissRequest = { showQueueSheet = false },
                sheetState = sheetState,
                containerColor = Color(0xFF1B173B)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Current Queue (${queue.size} songs)",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(queue) { index, song ->
                            val isCurrentInQueue = index == currentIndex
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isCurrentInQueue) Color(0x44FF5E62) else Color.Transparent
                                    )
                                    .clickable {
                                        viewModel.playSong(context, song, queue)
                                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                                            showQueueSheet = false
                                        }
                                    }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    color = AuraLavender,
                                    fontSize = 12.sp,
                                    modifier = Modifier.width(28.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = song.title,
                                        color = if (isCurrentInQueue) AuraPlayCoral else Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = if (isCurrentInQueue) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = song.artist,
                                        color = AuraLavender,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = song.durationFormatted,
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
