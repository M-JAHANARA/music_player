package com.jahanmusic.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.jahanmusic.app.navigation.AppNavigation
import com.jahanmusic.app.player.PlayerManager
import com.jahanmusic.app.ui.theme.JahanMusicTheme
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Warm up ExoPlayer so first tap plays instantly
        try { PlayerManager.getOrCreate(this) } catch (_: Exception) {}
        enableEdgeToEdge()
        setContent {
            JahanMusicTheme {
                // Miuix (HyperOS-style) theme — dark, like BakaSU
                val miuixController = remember { ThemeController(ColorSchemeMode.Dark) }
                MiuixTheme(controller = miuixController) {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        val navController = rememberNavController()
                        AppNavigation(navController = navController)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        // Don't release singleton here — Service/Widget may still need it
        super.onDestroy()
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    JahanMusicTheme {
        val navController = rememberNavController()
        AppNavigation(navController = navController)
    }
}
