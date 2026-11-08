package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.auramusic.data.local.AuraDatabase
import com.example.auramusic.data.mediastore.MusicScanner
import com.example.auramusic.playback.AuraPlayerController
import com.example.auramusic.ui.AuraApp
import com.example.auramusic.ui.viewmodel.AuraMusicViewModel

class MainActivity : ComponentActivity() {

    private val database by lazy { AuraDatabase.getInstance(applicationContext) }
    private val scanner by lazy { MusicScanner(applicationContext) }

    private val viewModel: AuraMusicViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AuraMusicViewModel(scanner, database) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Ensure Player controller is connected
        AuraPlayerController.initialize(applicationContext)

        setContent {
            AuraApp(
                viewModel = viewModel,
                database = database
            )
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh library when user returns (e.g. after downloading a song)
        if (viewModel.hasStoragePermission.value) {
            viewModel.refreshLibrary()
        }
    }
}
