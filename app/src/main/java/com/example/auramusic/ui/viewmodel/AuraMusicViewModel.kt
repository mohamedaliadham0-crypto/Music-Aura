package com.example.auramusic.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auramusic.data.local.AuraDatabase
import com.example.auramusic.data.local.FavoriteEntity
import com.example.auramusic.data.local.PlaylistEntity
import com.example.auramusic.data.local.PlaylistSongEntity
import com.example.auramusic.data.mediastore.MusicLibrary
import com.example.auramusic.data.mediastore.MusicScanner
import com.example.auramusic.data.model.AuraThemeStyle
import com.example.auramusic.data.model.Playlist
import com.example.auramusic.data.model.Song
import com.example.auramusic.playback.AuraPlayerController
import com.example.auramusic.playback.PlaybackStateManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    HOME,
    NOW_PLAYING,
    SEARCH,
    PLAYLIST_DETAIL,
    SETTINGS
}

class AuraMusicViewModel(
    private val scanner: MusicScanner,
    private val database: AuraDatabase
) : ViewModel() {

    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _selectedPlaylist = MutableStateFlow<Playlist?>(null)
    val selectedPlaylist: StateFlow<Playlist?> = _selectedPlaylist.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _hasStoragePermission = MutableStateFlow(false)
    val hasStoragePermission: StateFlow<Boolean> = _hasStoragePermission.asStateFlow()

    private val _musicLibrary = MutableStateFlow(MusicLibrary())
    val musicLibrary: StateFlow<MusicLibrary> = _musicLibrary.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _themeStyle = MutableStateFlow(AuraThemeStyle.AURORA_VIOLET)
    val themeStyle: StateFlow<AuraThemeStyle> = _themeStyle.asStateFlow()

    private val _isDarkGlass = MutableStateFlow(true)
    val isDarkGlass: StateFlow<Boolean> = _isDarkGlass.asStateFlow()

    // Room flows
    val favoriteSongIds: StateFlow<List<Long>> = database.musicDao().getFavoriteSongIds()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val rawPlaylists = database.musicDao().getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val recentlyPlayedIds = database.musicDao().getRecentlyPlayedSongIds()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Filtered search results
    val searchResults: StateFlow<List<Song>> = combine(_searchQuery, _musicLibrary) { query, lib ->
        if (query.isBlank()) {
            emptyList()
        } else {
            val q = query.trim().lowercase()
            lib.allSongs.filter {
                it.title.lowercase().contains(q) ||
                it.artist.lowercase().contains(q) ||
                it.album.lowercase().contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Favorites songs list
    val favoriteSongs: StateFlow<List<Song>> = combine(favoriteSongIds, _musicLibrary) { favIds, lib ->
        val idSet = favIds.toSet()
        lib.allSongs.filter { idSet.contains(it.id) }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Recently played songs list
    val recentlyPlayedSongs: StateFlow<List<Song>> = combine(recentlyPlayedIds, _musicLibrary) { recIds, lib ->
        val songMap = lib.allSongs.associateBy { it.id }
        recIds.mapNotNull { songMap[it] }.distinctBy { it.id }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        // Observe automatic media store changes (e.g. downloaded music)
        viewModelScope.launch {
            scanner.observeMediaStoreChanges().collect {
                if (_hasStoragePermission.value) {
                    refreshLibrary()
                }
            }
        }
    }

    fun setHasPermission(granted: Boolean, context: Context) {
        _hasStoragePermission.value = granted
        if (granted) {
            refreshLibrary()
            AuraPlayerController.initialize(context)
        }
    }

    fun refreshLibrary() {
        viewModelScope.launch(Dispatchers.IO) {
            _isScanning.value = true
            try {
                val lib = scanner.scanMusic()
                _musicLibrary.value = lib
            } catch (_: Exception) {
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun openPlaylist(playlist: Playlist) {
        _selectedPlaylist.value = playlist
        _currentScreen.value = AppScreen.PLAYLIST_DETAIL
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setThemeStyle(style: AuraThemeStyle) {
        _themeStyle.value = style
    }

    fun toggleDarkGlass() {
        _isDarkGlass.value = !_isDarkGlass.value
    }

    // --- Favorites ---
    fun toggleFavorite(songId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val isFav = favoriteSongIds.value.contains(songId)
            if (isFav) {
                database.musicDao().removeFavorite(songId)
            } else {
                database.musicDao().addFavorite(FavoriteEntity(songId = songId))
            }
        }
    }

    // --- Playlists ---
    fun createPlaylist(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            database.musicDao().insertPlaylist(PlaylistEntity(name = name.trim()))
        }
    }

    fun renamePlaylist(id: Long, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            database.musicDao().updatePlaylist(PlaylistEntity(id = id, name = newName.trim()))
        }
    }

    fun deletePlaylist(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            database.musicDao().deletePlaylist(id)
            database.musicDao().deletePlaylistSongs(id)
            if (_selectedPlaylist.value?.id == id) {
                _currentScreen.value = AppScreen.HOME
                _selectedPlaylist.value = null
            }
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            database.musicDao().addSongToPlaylist(
                PlaylistSongEntity(playlistId = playlistId, songId = songId)
            )
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            database.musicDao().removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun clearHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            database.musicDao().clearHistory()
        }
    }

    // --- Playback Delegates ---
    fun playSong(context: Context, song: Song, queue: List<Song> = _musicLibrary.value.allSongs) {
        AuraPlayerController.playSong(context, song, queue)
    }

    fun togglePlayPause(context: Context) {
        AuraPlayerController.togglePlayPause(context)
    }

    fun skipNext() {
        AuraPlayerController.skipNext()
    }

    fun skipPrevious() {
        AuraPlayerController.skipPrevious()
    }

    fun seekTo(positionMs: Long) {
        AuraPlayerController.seekTo(positionMs)
    }

    fun cyclePlaybackMode(context: Context) {
        AuraPlayerController.cyclePlaybackMode(context)
    }
}
