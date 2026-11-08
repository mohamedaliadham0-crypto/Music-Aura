package com.example.auramusic.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.auramusic.data.model.Song
import com.example.ui.theme.AuraBubblegumPink
import com.example.ui.theme.AuraGlassFill
import com.example.ui.theme.AuraGlassHighlight
import com.example.ui.theme.AuraGlassStroke
import com.example.ui.theme.AuraLavender
import com.example.ui.theme.AuraPlayCoral
import com.example.ui.theme.AuraPlayPeach
import com.example.ui.theme.AuraSoftPurple

@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    backgroundColor: Color = AuraGlassFill,
    borderColors: List<Color> = listOf(AuraGlassHighlight, AuraGlassStroke.copy(alpha = 0.2f)),
    borderWidth: Dp = 1.2.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(elevation = 16.dp, shape = shape, spotColor = Color(0x66000000))
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        backgroundColor.copy(alpha = 0.45f),
                        backgroundColor.copy(alpha = 0.25f)
                    )
                )
            )
            .border(
                BorderStroke(
                    borderWidth,
                    Brush.linearGradient(borderColors)
                ),
                shape = shape
            )
    ) {
        content()
    }
}

@Composable
fun GlassCircleButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    size: Dp = 46.dp,
    iconSize: Dp = 22.dp,
    tint: Color = Color.White,
    tag: String = "glass_button"
) {
    Box(
        modifier = modifier
            .size(size)
            .minimumInteractiveComponentSize()
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(
                        Color.White.copy(alpha = 0.28f),
                        Color.White.copy(alpha = 0.12f)
                    )
                )
            )
            .border(
                BorderStroke(
                    1.dp,
                    Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.7f),
                            Color.White.copy(alpha = 0.15f)
                        )
                    )
                ),
                CircleShape
            )
            .clickable(onClick = onClick)
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun GlowingPlayButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 68.dp,
    iconSize: Dp = 32.dp,
    tag: String = "play_pause_button"
) {
    Box(
        modifier = modifier
            .size(size)
            .minimumInteractiveComponentSize()
            .shadow(
                elevation = 20.dp,
                shape = CircleShape,
                ambientColor = AuraPlayCoral,
                spotColor = AuraPlayPeach
            )
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(AuraPlayCoral, AuraPlayPeach)
                )
            )
            .border(
                BorderStroke(1.5.dp, Color.White.copy(alpha = 0.8f)),
                CircleShape
            )
            .clickable(onClick = onClick)
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
            contentDescription = if (isPlaying) "Pause" else "Play",
            tint = Color.White,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun AlbumArtworkView(
    song: Song?,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp
) {
    val fallbackArt = if ((song?.id ?: 0L) % 2L == 0L) {
        R.drawable.default_art_chill_1794131094093
    } else {
        R.drawable.default_art_ambient_1794131108421
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(Color(0x33FFFFFF))
            .border(
                BorderStroke(1.2.dp, Color.White.copy(alpha = 0.4f)),
                RoundedCornerShape(cornerRadius)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (song?.albumArtUri != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(song.albumArtUri)
                    .crossfade(true)
                    .build(),
                contentDescription = song.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                error = painterResource(fallbackArt),
                placeholder = painterResource(fallbackArt)
            )
        } else {
            Image(
                painter = painterResource(fallbackArt),
                contentDescription = song?.title ?: "Album Artwork",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun GlassProgressBar(
    positionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }

    val safeDuration = durationMs.coerceAtLeast(1L)
    val progress = if (isDragging) {
        dragProgress
    } else {
        (positionMs.toFloat() / safeDuration).coerceIn(0f, 1f)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Slider(
            value = progress,
            onValueChange = {
                isDragging = true
                dragProgress = it
            },
            onValueChangeFinished = {
                onSeek((dragProgress * safeDuration).toLong())
                isDragging = false
            },
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = AuraPlayCoral,
                inactiveTrackColor = Color.White.copy(alpha = 0.25f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("progress_slider")
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val currentFormatted = formatMs(if (isDragging) (dragProgress * safeDuration).toLong() else positionMs)
            val remainingMs = (safeDuration - (if (isDragging) (dragProgress * safeDuration).toLong() else positionMs)).coerceAtLeast(0)
            val remainingFormatted = "-${formatMs(remainingMs)}"

            Text(
                text = currentFormatted,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = remainingFormatted,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

@Composable
fun AnimatedEqualizerBars(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 4
) {
    val transition = rememberInfiniteTransition(label = "eq")
    val heights = List(barCount) { index ->
        val duration = 400 + (index * 130)
        transition.animateFloat(
            initialValue = 0.2f,
            targetValue = if (isPlaying) 1.0f else 0.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(duration, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar_$index"
        )
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        heights.forEach { animHeight ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height((animHeight.value * 16).dp.coerceAtLeast(4.dp))
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(AuraBubblegumPink)
            )
        }
    }
}

@Composable
fun MiniPlayerBar(
    currentSong: Song?,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrev: () -> Unit,
    onBarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (currentSong == null) return

    GlassPanel(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable(onClick = onBarClick)
            .testTag("mini_player_bar"),
        shape = RoundedCornerShape(22.dp),
        backgroundColor = Color(0x401E1A4A)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Miniature Play Button with gradient ring
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(AuraPlayCoral, AuraPlayPeach))
                    )
                    .clickable(onClick = onPlayPause)
                    .testTag("mini_play_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title & Artist
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = currentSong.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = currentSong.artist,
                    color = AuraLavender,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Controls
            GlassCircleButton(
                icon = Icons.Filled.SkipPrevious,
                onClick = onSkipPrev,
                size = 36.dp,
                iconSize = 18.dp,
                contentDescription = "Previous Track",
                tag = "mini_prev_button"
            )

            Spacer(modifier = Modifier.width(6.dp))

            GlassCircleButton(
                icon = Icons.Filled.SkipNext,
                onClick = onSkipNext,
                size = 36.dp,
                iconSize = 18.dp,
                contentDescription = "Next Track",
                tag = "mini_next_button"
            )
        }
    }
}
