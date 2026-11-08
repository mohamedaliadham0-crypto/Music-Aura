package com.example.auramusic.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auramusic.data.local.AuraDatabase
import com.example.auramusic.data.model.Song
import com.example.auramusic.playback.PlaybackStateManager
import com.example.auramusic.ui.components.GlassCircleButton
import com.example.auramusic.ui.components.GlassPanel
import com.example.auramusic.ui.components.MiniPlayerBar
import com.example.auramusic.ui.viewmodel.AppScreen
import com.example.auramusic.ui.viewmodel.AuraMusicViewModel
import com.example.ui.theme.AuraGlassFill
import com.example.ui.theme.AuraLavender
import com.example.ui.theme.AuraPlayCoral

@Composable
fun PlaylistDetailScreen(
    viewModel: AuraMusicViewModel,
    database: AuraDatabase,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    BackHandler {
        viewModel.navigateTo(AppScreen.HOME)
    }

    val selectedPlaylist by viewModel.selectedPlaylist.collectAsState()
    val library by viewModel.musicLibrary.collectAsState()
    val currentSong by PlaybackStateManager.currentSong.collectAsState()
    val isPlaying by PlaybackStateManager.isPlaying.collectAsState()

    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    if (selectedPlaylist == null) {
        viewModel.navigateTo(AppScreen.HOME)
        return
    }

    val playlist = selectedPlaylist!!
    val playlistSongIds by database.musicDao().getSongIdsForPlaylist(playlist.id).collectAsState(initial = emptyList())
    val songMap = remember(library.allSongs) { library.allSongs.associateBy { it.id } }
    val playlistSongs = remember(playlistSongIds, songMap) {
        playlistSongIds.mapNotNull { songMap[it] }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassCircleButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    onClick = { viewModel.navigateTo(AppScreen.HOME) },
                    contentDescription = "Back"
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassCircleButton(
                        icon = Icons.Filled.Edit,
                        onClick = { showRenameDialog = true },
                        contentDescription = "Rename Playlist"
                    )
                    GlassCircleButton(
                        icon = Icons.Filled.Delete,
                        onClick = { showDeleteConfirmDialog = true },
                        contentDescription = "Delete Playlist",
                        tint = AuraPlayCoral
                    )
                }
            }

            // Playlist Info Card
            GlassPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = playlist.name,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${playlistSongs.size} tracks",
                        color = AuraLavender,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    if (playlistSongs.isNotEmpty()) {
                        Button(
                            onClick = {
                                viewModel.playSong(context, playlistSongs.first(), playlistSongs)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AuraPlayCoral),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Play All",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Play Playlist", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Song list in playlist
            if (playlistSongs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "This playlist is empty.\nAdd tracks from the song list 3-dot menu.",
                        color = AuraLavender,
                        fontSize = 14.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(playlistSongs, key = { it.id }) { song ->
                        val isCurrent = currentSong?.id == song.id
                        GlassPanel(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.playSong(context, song, playlistSongs) },
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = song.title,
                                        color = if (isCurrent) AuraPlayCoral else Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = song.artist,
                                        color = AuraLavender,
                                        fontSize = 12.sp
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        viewModel.removeSongFromPlaylist(playlist.id, song.id)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.RemoveCircleOutline,
                                        contentDescription = "Remove from playlist",
                                        tint = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Mini Player if active
            if (currentSong != null) {
                MiniPlayerBar(
                    currentSong = currentSong,
                    isPlaying = isPlaying,
                    onPlayPause = { viewModel.togglePlayPause(context) },
                    onSkipNext = { viewModel.skipNext() },
                    onSkipPrev = { viewModel.skipPrevious() },
                    onBarClick = { viewModel.navigateTo(AppScreen.NOW_PLAYING) },
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                )
            } else {
                Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars))
            }
        }

        // Rename Dialog
        if (showRenameDialog) {
            var newName by remember { mutableStateOf(playlist.name) }
            AlertDialog(
                onDismissRequest = { showRenameDialog = false },
                title = { Text("Rename Playlist", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AuraPlayCoral
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.renamePlaylist(playlist.id, newName)
                            showRenameDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AuraPlayCoral)
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRenameDialog = false }) {
                        Text("Cancel", color = Color.White)
                    }
                },
                containerColor = Color(0xFF1E1A42)
            )
        }

        // Delete Confirm Dialog
        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { Text("Delete Playlist", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "Are you sure you want to delete \"${playlist.name}\"? Songs will remain in your library.",
                        color = AuraLavender
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deletePlaylist(playlist.id)
                            showDeleteConfirmDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AuraPlayCoral)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) {
                        Text("Cancel", color = Color.White)
                    }
                },
                containerColor = Color(0xFF1E1A42)
            )
        }
    }
}
