package com.example.auramusic.data.mediastore

import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import com.example.auramusic.data.model.Album
import com.example.auramusic.data.model.Artist
import com.example.auramusic.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import java.io.File

data class MusicLibrary(
    val allSongs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val newlyAdded: List<Song> = emptyList()
)

class MusicScanner(private val context: Context) {

    private val albumArtUriBase = Uri.parse("content://media/external/audio/albumart")

    suspend fun scanMusic(): MusicLibrary = withContext(Dispatchers.IO) {
        val songList = mutableListOf<Song>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATE_ADDED
        )

        // Filter for real music tracks, duration at least 5 seconds
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 5000"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

                val seenIds = mutableSetOf<Long>()

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    if (seenIds.contains(id)) continue
                    seenIds.add(id)

                    val rawTitle = cursor.getString(titleCol) ?: ""
                    val rawArtist = cursor.getString(artistCol) ?: ""
                    val rawAlbum = cursor.getString(albumCol) ?: ""
                    val duration = cursor.getLong(durationCol)
                    val dataPath = if (dataCol != -1) cursor.getString(dataCol) ?: "" else ""
                    val size = cursor.getLong(sizeCol)
                    val albumId = cursor.getLong(albumIdCol)
                    val dateAdded = cursor.getLong(dateAddedCol)

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    val albumArtUri = if (albumId > 0) {
                        ContentUris.withAppendedId(albumArtUriBase, albumId)
                    } else null

                    // Format clean title
                    val cleanTitle = if (rawTitle.isBlank() || rawTitle.equals("<unknown>", ignoreCase = true)) {
                        if (dataPath.isNotBlank()) {
                            File(dataPath).nameWithoutExtension.replace("_", " ")
                        } else {
                            "Track #$id"
                        }
                    } else {
                        rawTitle.trim()
                    }

                    // Format clean artist
                    val cleanArtist = if (rawArtist.isBlank() || rawArtist.equals("<unknown>", ignoreCase = true)) {
                        "Unknown Artist"
                    } else {
                        rawArtist.trim()
                    }

                    // Format clean album
                    val cleanAlbum = if (rawAlbum.isBlank() || rawAlbum.equals("<unknown>", ignoreCase = true)) {
                        "Unknown Album"
                    } else {
                        rawAlbum.trim()
                    }

                    songList.add(
                        Song(
                            id = id,
                            title = cleanTitle,
                            artist = cleanArtist,
                            album = cleanAlbum,
                            durationMs = duration,
                            contentUri = contentUri,
                            albumArtUri = albumArtUri,
                            dataPath = dataPath,
                            sizeBytes = size,
                            dateAdded = dateAdded
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("MusicScanner", "Error querying MediaStore", e)
        }

        // Group into Albums
        val albums = songList
            .groupBy { it.album }
            .entries
            .mapIndexed { index, entry ->
                val albumTitle = entry.key
                val songsInAlbum = entry.value
                val firstWithArt = songsInAlbum.firstOrNull { it.albumArtUri != null }
                Album(
                    id = index.toLong() + 1,
                    title = albumTitle,
                    artist = songsInAlbum.firstOrNull()?.artist ?: "Unknown Artist",
                    songCount = songsInAlbum.size,
                    artworkUri = firstWithArt?.albumArtUri
                )
            }
            .sortedBy { it.title }

        // Group into Artists
        val artists = songList
            .groupBy { it.artist }
            .entries
            .map { entry ->
                val artistName = entry.key
                val songsByArtist = entry.value
                val distinctAlbums = songsByArtist.map { it.album }.distinct().size
                Artist(
                    name = artistName,
                    songCount = songsByArtist.size,
                    albumCount = distinctAlbums
                )
            }
            .sortedBy { it.name }

        // Newly added (sorted by date added DESC)
        val newlyAdded = songList.sortedByDescending { it.dateAdded }.take(20)

        MusicLibrary(
            allSongs = songList,
            albums = albums,
            artists = artists,
            newlyAdded = newlyAdded
        )
    }

    /**
     * Flow that monitors MediaStore changes automatically.
     */
    fun observeMediaStoreChanges(): Flow<Unit> = callbackFlow {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)
                trySend(Unit)
            }
        }

        context.contentResolver.registerContentObserver(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            true,
            observer
        )

        awaitClose {
            context.contentResolver.unregisterContentObserver(observer)
        }
    }
}
