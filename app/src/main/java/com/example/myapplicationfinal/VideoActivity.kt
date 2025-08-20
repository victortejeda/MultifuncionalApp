package com.example.myapplicationfinal

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.myapplicationfinal.ui.theme.MyApplicationFinalTheme

@androidx.annotation.OptIn(UnstableApi::class)
class VideoActivity : ComponentActivity() {
    private var exoPlayer: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationFinalTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    VideoPlayerScreen(
                        onBackPressed = { finish() },
                        onPlayerCreated = { player -> exoPlayer = player }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    onBackPressed: () -> Unit,
    onPlayerCreated: (ExoPlayer) -> Unit
) {
    val context = LocalContext.current
    var videoUrl by remember { mutableStateOf("") }
    var isPlaying by remember { mutableStateOf(false) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().also { player ->
            onPlayerCreated(player)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reproductor de Video") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Campo de URL
            OutlinedTextField(
                value = videoUrl,
                onValueChange = { videoUrl = it },
                label = { Text("URL del video") },
                placeholder = { Text("https://www.youtube.com/watch?v=...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Botón de reproducción
            Button(
                onClick = {
                    if (videoUrl.isNotEmpty()) {
                        isPlaying = true
                        Toast.makeText(context, "Reproduciendo video...", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Por favor ingresa una URL", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reproducir")
            }

            // Reproductor de video
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (isPlaying && videoUrl.isNotEmpty()) {
                    // WebView para todos los videos (YouTube y otros)
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = true
                                settings.mediaPlaybackRequiresUserGesture = false
                                settings.domStorageEnabled = true
                                settings.loadWithOverviewMode = true
                                settings.useWideViewPort = true
                                
                                webViewClient = object : WebViewClient() {
                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                    }
                                }
                                
                                // Función para convertir cualquier URL de YouTube a formato embed
                                val embedUrl = if (videoUrl.contains("youtube.com") || videoUrl.contains("youtu.be")) {
                                    // Extraer solo el ID del video de YouTube
                                    val videoId = extractYouTubeVideoId(videoUrl)
                                    if (videoId != null) {
                                        "https://www.youtube.com/embed/$videoId"
                                    } else {
                                        videoUrl
                                    }
                                } else {
                                    videoUrl
                                }
                                
                                loadUrl(embedUrl)
                                
                                layoutParams = FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Ingresa una URL y presiona 'Reproducir'",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// Función para extraer el ID del video de YouTube de cualquier formato de URL
private fun extractYouTubeVideoId(url: String): String? {
    return try {
        when {
            url.contains("youtube.com/watch") -> {
                val regex = Regex("""[?&]v=([^&]+)""")
                regex.find(url)?.groupValues?.get(1)
            }
            url.contains("youtu.be/") -> {
                val regex = Regex("""youtu\.be/([^?&]+)""")
                regex.find(url)?.groupValues?.get(1)
            }
            url.contains("youtube.com/embed/") -> {
                val regex = Regex("""youtube\.com/embed/([^?&]+)""")
                regex.find(url)?.groupValues?.get(1)
            }
            else -> null
        }
    } catch (e: Exception) {
        null
    }
}
