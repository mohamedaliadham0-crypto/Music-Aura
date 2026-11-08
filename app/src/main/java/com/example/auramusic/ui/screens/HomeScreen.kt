package com.example.auramusic.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import com.example.R
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auramusic.data.local.PlaylistEntity
import com.example.auramusic.data.model.Album
import com.example.auramusic.data.model.Artist
import com.example.auramusic.data.model.Playlist
import com.example.auramusic.data.model.Song
import com.example.auramusic.playback.PlaybackStateManager
import com.example.auramusic.ui.components.AlbumArtworkView
import com.example.auramusic.ui.components.AnimatedEqualizerBars
import com.example.auramusic.ui.components.GlassCircleButton
import com.example.auramusic.ui.components.GlassPanel
import com.example.auramusic.ui.components.MiniPlayerBar
import com.example.auramusic.ui.viewmodel.AppScreen
import com.example.auramusic.ui.viewmodel.AuraMusicViewModel
import com.example.ui.theme.AuraBubblegumPink
import com.example.ui.theme.AuraElectricBlue
import com.example.ui.theme.AuraGlassFill
import com.example.ui.theme.AuraLavender
import com.example.ui.theme.AuraPlayCoral
import com.example.ui.theme.AuraPlayPeach
import com.example.ui.theme.AuraSoftPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AuraMusicViewModel,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hasPermission by viewModel.hasStoragePermission.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val library by viewModel.musicLibrary.collectAsState()
    val favoriteIds by viewModel.favoriteSongIds.collectAsState()
    val rawPlaylists by viewModel.rawPlaylists.collectAsState()
    val favoriteSongs by viewModel.favoriteSongs.collectAsState()
    val recentSongs by viewModel.recentlyPlayedSongs.collectAsState()

    val currentSong by PlaybackStateManager.currentSong.collectAsState()
    val isPlaying by PlaybackStateManager.isPlaying.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("All Songs", "Collections", "Albums", "Artists", "Favorites", "Recent")

    var showNewPlaylistDialog by remember { mutableStateOf(false) }
    var selectedSongForMenu by remember { mutableStateOf<Song?>(null) }
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    var songInfoToShow by remember { mutableStateOf<Song?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            HomeTopBar(
                onSearchClick = { viewModel.navigateTo(AppScreen.SEARCH) },
                onSettingsClick = { viewModel.navigateTo(AppScreen.SETTINGS) }
            )

            // Storage permission warning banner if not granted
            if (!hasPermission) {
                PermissionBanner(onRequestPermission = onRequestPermission)
            }

            // Category Tab Bar
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                contentColor = Color.White,
                edgePadding = 16.dp,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier
                            .tabIndicatorOffset(tabPositions[selectedTabIndex])
                            .clip(RoundedCornerShape(3.dp)),
                        height = 3.dp,
                        color = AuraPlayCoral
                    )
                },
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 15.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) Color.White else Color.White.copy(alpha = 0.6f)
                            )
                        },
                        modifier = Modifier.testTag("tab_$title")
                    )
                }
            }

            // Main Content Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedTabIndex) {
                    0 -> AllSongsSection(
                        songs = library.allSongs,
                        currentSong = currentSong,
                        isPlaying = isPlaying,
                        favoriteIds = favoriteIds,
                        onSongClick = { song -> viewModel.playSong(context, song, library.allSongs) },
                        onSongMenuClick = { song -> selectedSongForMenu = song },
                        onRescan = { viewModel.refreshLibrary() }
                    )
                    1 -> CollectionsSection(
                        rawPlaylists = rawPlaylists,
                        allSongs = library.allSongs,
                        onCreatePlaylist = { showNewPlaylistDialog = true },
                        onPlaylistClick = { playlist -> viewModel.openPlaylist(playlist) },
                        onPlaySong = { song, queue -> viewModel.playSong(context, song, queue) }
                    )
                    2 -> AlbumsSection(
                        albums = library.albums,
                        onAlbumClick = { album ->
                            val albumSongs = library.allSongs.filter { it.album == album.title }
                            if (albumSongs.isNotEmpty()) {
                                viewModel.playSong(context, albumSongs.first(), albumSongs)
                            }
                        }
                    )
                    3 -> ArtistsSection(
                        artists = library.artists,
                        onArtistClick = { artist ->
                            val artistSongs = library.allSongs.filter { it.artist == artist.name }
                            if (artistSongs.isNotEmpty()) {
                                viewModel.playSong(context, artistSongs.first(), artistSongs)
                            }
                        }
                    )
                    4 -> FavoritesSection(
                        favoriteSongs = favoriteSongs,
                        currentSong = currentSong,
                        isPlaying = isPlaying,
                        onSongClick = { song -> viewModel.playSong(context, song, favoriteSongs) },
                        onSongMenuClick = { song -> selectedSongForMenu = song }
                    )
                    5 -> RecentlyPlayedSection(
                        recentSongs = recentSongs,
                        currentSong = currentSong,
                        isPlaying = isPlaying,
                        onSongClick = { song -> viewModel.playSong(context, song, recentSongs) },
                        onSongMenuClick = { song -> selectedSongForMenu = song }
                    )
                }
            }

            // Bottom Mini Player
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

        // Dropdown Menu for Song options
        if (selectedSongForMenu != null) {
            val song = selectedSongForMenu!!
            val isFav = favoriteIds.contains(song.id)
            SongActionDialog(
                song = song,
                isFavorite = isFav,
                onDismiss = { selectedSongForMenu = null },
                onToggleFavorite = {
                    viewModel.toggleFavorite(song.id)
                    selectedSongForMenu = null
                },
                onAddToPlaylist = {
                    showAddToPlaylistDialog = true
                },
                onViewInfo = {
                    songInfoToShow = song
                    selectedSongForMenu = null
                }
            )
        }

        // Create Playlist Dialog
        if (showNewPlaylistDialog) {
            CreatePlaylistDialog(
                onDismiss = { showNewPlaylistDialog = false },
                onConfirm = { name ->
                    viewModel.createPlaylist(name)
                    showNewPlaylistDialog = false
                }
            )
        }

        // Add to Playlist Dialog
        if (showAddToPlaylistDialog && selectedSongForMenu != null) {
            val song = selectedSongForMenu!!
            AddToPlaylistDialog(
                song = song,
                playlists = rawPlaylists,
                onDismiss = {
                    showAddToPlaylistDialog = false
                    selectedSongForMenu = null
                },
                onSelectPlaylist = { playlistId ->
                    viewModel.addSongToPlaylist(playlistId, song.id)
                    showAddToPlaylistDialog = false
                    selectedSongForMenu = null
                }
            )
        }

        // Song Information Dialog
        if (songInfoToShow != null) {
            SongInfoDialog(
                song = songInfoToShow!!,
                onDismiss = { songInfoToShow = null }
            )
        }
    }
}

@Composable
private fun HomeTopBar(
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Identity
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(AuraPlayCoral, AuraBubblegumPink))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.MusicNote,
                    contentDescription = "Aura Music Logo",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "AURA MUSIC",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Liquid Atmosphere",
                    color = AuraLavender,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Action Buttons
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GlassCircleButton(
                icon = Icons.Filled.Search,
                onClick = onSearchClick,
                contentDescription = "Search Music",
                tag = "home_search_button"
            )
            GlassCircleButton(
                icon = Icons.Filled.Settings,
                onClick = onSettingsClick,
                contentDescription = "Settings",
                tag = "home_settings_button"
            )
        }
    }
}

@Composable
private fun PermissionBanner(onRequestPermission: () -> Unit) {
    GlassPanel(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        backgroundColor = Color(0x66FF5E62)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Audio Permission Required",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Grant access to discover MP3 files on this phone.",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp
                )
            }
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Grant", color = AuraPlayCoral, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AllSongsSection(
    songs: List<Song>,
    currentSong: Song?,
    isPlaying: Boolean,
    favoriteIds: List<Long>,
    onSongClick: (Song) -> Unit,
    onSongMenuClick: (Song) -> Unit,
    onRescan: () -> Unit
) {
    if (songs.isEmpty()) {
        EmptyMusicView(onRescan = onRescan)
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(songs, key = { it.id }) { song ->
            val isCurrent = currentSong?.id == song.id
            SongItemRow(
                song = song,
                isCurrent = isCurrent,
                isPlaying = isCurrent && isPlaying,
                isFavorite = favoriteIds.contains(song.id),
                onClick = { onSongClick(song) },
                onMenuClick = { onSongMenuClick(song) }
            )
        }
    }
}

@Composable
fun SongItemRow(
    song: Song,
    isCurrent: Boolean,
    isPlaying: Boolean,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    GlassPanel(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("song_row_${song.id}"),
        shape = RoundedCornerShape(18.dp),
        backgroundColor = if (isCurrent) Color(0x5558459E) else AuraGlassFill,
        borderColors = if (isCurrent) {
            listOf(AuraPlayCoral.copy(alpha = 0.8f), AuraPlayPeach.copy(alpha = 0.3f))
        } else {
            listOf(Color.White.copy(alpha = 0.35f), Color.White.copy(alpha = 0.1f))
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Artwork
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                AlbumArtworkView(
                    song = song,
                    cornerRadius = 12.dp,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title & Artist
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = song.title,
                        color = if (isCurrent) AuraPlayCoral else Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (isCurrent) {
                        Spacer(modifier = Modifier.width(6.dp))
                        AnimatedEqualizerBars(isPlaying = isPlaying)
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${song.artist} • ${song.durationFormatted}",
                    color = AuraLavender,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Quick Play Button (as seen in right panel of reference image)
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Play",
                    tint = AuraPlayCoral,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Options menu
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Options",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun CollectionsSection(
    rawPlaylists: List<PlaylistEntity>,
    allSongs: List<Song>,
    onCreatePlaylist: () -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onPlaySong: (Song, List<Song>) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Create Playlist Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Collections",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = onCreatePlaylist,
                    colors = ButtonDefaults.buttonColors(containerColor = AuraPlayCoral),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Create",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "New Playlist", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Horizontal collections carousel (matching reference image)
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Chillout
                item {
                    CollectionCard(
                        title = "Chillout",
                        subtitle = "${allSongs.size} songs",
                        artRes = R.drawable.default_art_chill_1794131094093,
                        onClick = {
                            if (allSongs.isNotEmpty()) onPlaySong(allSongs.first(), allSongs)
                        }
                    )
                }
                // Ambient
                item {
                    CollectionCard(
                        title = "Atmosphere",
                        subtitle = "${allSongs.size} songs",
                        artRes = R.drawable.default_art_ambient_1794131108421,
                        onClick = {
                            if (allSongs.isNotEmpty()) onPlaySong(allSongs.first(), allSongs)
                        }
                    )
                }
                // Custom user playlists
                items(rawPlaylists) { entity ->
                    CollectionCard(
                        title = entity.name,
                        subtitle = "Custom Playlist",
                        artRes = R.drawable.app_icon_fg_1794130955996,
                        onClick = {
                            onPlaylistClick(
                                Playlist(
                                    id = entity.id,
                                    name = entity.name,
                                    songCount = 0,
                                    createdAt = entity.createdAt
                                )
                            )
                        }
                    )
                }
            }
        }

        // Recommended / Top Tracks title
        item {
            Text(
                text = "Recommended",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(allSongs.take(15)) { song ->
            SongItemRow(
                song = song,
                isCurrent = false,
                isPlaying = false,
                isFavorite = false,
                onClick = { onPlaySong(song, allSongs) },
                onMenuClick = {}
            )
        }
    }
}

@Composable
private fun CollectionCard(
    title: String,
    subtitle: String,
    artRes: Int,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(135.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(135.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White.copy(alpha = 0.2f))
        ) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(artRes),
                contentDescription = title,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = subtitle,
            color = AuraLavender,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AlbumsSection(
    albums: List<Album>,
    onAlbumClick: (Album) -> Unit
) {
    if (albums.isEmpty()) {
        EmptyMusicView(onRescan = {})
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(albums) { album ->
            GlassPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAlbumClick(album) },
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MusicNote,
                            contentDescription = album.title,
                            tint = AuraBubblegumPink,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = album.title,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${album.artist} • ${album.songCount} songs",
                            color = AuraLavender,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Play Album",
                        tint = AuraPlayCoral,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ArtistsSection(
    artists: List<Artist>,
    onArtistClick: (Artist) -> Unit
) {
    if (artists.isEmpty()) {
        EmptyMusicView(onRescan = {})
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(artists) { artist ->
            GlassPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onArtistClick(artist) },
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(AuraElectricBlue, AuraSoftPurple))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = artist.name.take(1).uppercase(),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = artist.name,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${artist.songCount} songs • ${artist.albumCount} albums",
                            color = AuraLavender,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoritesSection(
    favoriteSongs: List<Song>,
    currentSong: Song?,
    isPlaying: Boolean,
    onSongClick: (Song) -> Unit,
    onSongMenuClick: (Song) -> Unit
) {
    if (favoriteSongs.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.FavoriteBorder,
                    contentDescription = "No Favorites",
                    tint = AuraBubblegumPink,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No favorite tracks yet",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tap the heart icon or menu on any song to add it here.",
                    color = AuraLavender,
                    fontSize = 13.sp
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(favoriteSongs, key = { it.id }) { song ->
            val isCurrent = currentSong?.id == song.id
            SongItemRow(
                song = song,
                isCurrent = isCurrent,
                isPlaying = isCurrent && isPlaying,
                isFavorite = true,
                onClick = { onSongClick(song) },
                onMenuClick = { onSongMenuClick(song) }
            )
        }
    }
}

@Composable
private fun RecentlyPlayedSection(
    recentSongs: List<Song>,
    currentSong: Song?,
    isPlaying: Boolean,
    onSongClick: (Song) -> Unit,
    onSongMenuClick: (Song) -> Unit
) {
    if (recentSongs.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.MusicNote,
                    contentDescription = "No History",
                    tint = AuraLavender,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No recently played tracks",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tracks you listen to will appear here automatically.",
                    color = AuraLavender,
                    fontSize = 13.sp
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(recentSongs, key = { it.id }) { song ->
            val isCurrent = currentSong?.id == song.id
            SongItemRow(
                song = song,
                isCurrent = isCurrent,
                isPlaying = isCurrent && isPlaying,
                isFavorite = false,
                onClick = { onSongClick(song) },
                onMenuClick = { onSongMenuClick(song) }
            )
        }
    }
}

@Composable
private fun EmptyMusicView(onRescan: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        GlassPanel(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            backgroundColor = Color(0x33282255)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.MusicNote,
                        contentDescription = "Music",
                        tint = AuraLavender,
                        modifier = Modifier.size(34.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Your Music Library is Empty",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Aura Music scans internal storage for MP3 files (Music, Downloads, Bluetooth folders). Copy or download audio files to your device, then tap refresh.",
                    color = AuraLavender,
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onRescan,
                    colors = ButtonDefaults.buttonColors(containerColor = AuraPlayCoral),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Refresh",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Scan for Music", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SongActionDialog(
    song: Song,
    isFavorite: Boolean,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onViewInfo: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = song.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = song.artist,
                    color = AuraLavender,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Favorite option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onToggleFavorite)
                        .padding(vertical = 10.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = AuraBubblegumPink
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isFavorite) "Remove from Favorites" else "Add to Favorites",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }

                // Add to Playlist
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onAddToPlaylist)
                        .padding(vertical = 10.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = "Add to playlist",
                        tint = AuraLavender
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Add to Playlist",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }

                // Track Info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onViewInfo)
                        .padding(vertical = 10.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "Song Info",
                        tint = AuraLavender
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Song Details",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Close", color = AuraPlayCoral)
            }
        },
        containerColor = Color(0xFF1E1A42)
    )
}

@Composable
private fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "New Playlist", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Playlist Name") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = AuraPlayCoral,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                    focusedLabelColor = AuraPlayCoral
                ),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AuraPlayCoral)
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.7f))
            }
        },
        containerColor = Color(0xFF1E1A42)
    )
}

@Composable
private fun AddToPlaylistDialog(
    song: Song,
    playlists: List<PlaylistEntity>,
    onDismiss: () -> Unit,
    onSelectPlaylist: (Long) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Select Playlist", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            if (playlists.isEmpty()) {
                Text(
                    text = "No playlists created yet. Create a playlist first.",
                    color = AuraLavender,
                    fontSize = 13.sp
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(playlists) { pl ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSelectPlaylist(pl.id) }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                                contentDescription = null,
                                tint = AuraBubblegumPink
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = pl.name, color = Color.White, fontSize = 14.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AuraPlayCoral)
            }
        },
        containerColor = Color(0xFF1E1A42)
    )
}

@Composable
private fun SongInfoDialog(
    song: Song,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Song Information", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoRow(label = "Title", value = song.title)
                InfoRow(label = "Artist", value = song.artist)
                InfoRow(label = "Album", value = song.album)
                InfoRow(label = "Duration", value = song.durationFormatted)
                if (song.dataPath.isNotBlank()) {
                    InfoRow(label = "Location", value = song.dataPath)
                }
                if (song.sizeBytes > 0) {
                    val sizeMb = "%.2f MB".format(song.sizeBytes / (1024.0 * 1024.0))
                    InfoRow(label = "Size", value = sizeMb)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = AuraPlayCoral)
            }
        },
        containerColor = Color(0xFF1E1A42)
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column {
        Text(text = label, color = AuraLavender, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(text = value, color = Color.White, fontSize = 13.sp)
    }
}
