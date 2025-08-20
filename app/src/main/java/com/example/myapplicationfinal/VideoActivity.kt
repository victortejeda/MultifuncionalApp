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
    var isYouTubeVideo by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    
    // URLs de ejemplo predefinidas
    val sampleVideos = listOf(
        "Video Local" to "android.resource://${context.packageName}/${R.raw.sample_video}",
        "Big Buck Bunny (MP4)" to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
        "Elephant Dream (MP4)" to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
    )

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
                onValueChange = { 
                    videoUrl = it
                    isYouTubeVideo = it.contains("youtube.com") || it.contains("youtu.be")
                },
                label = { Text("URL del video (YouTube, MP4, etc.)") },
                placeholder = { Text("https://www.youtube.com/watch?v=... o https://ejemplo.com/video.mp4") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Botones de videos de ejemplo
            Text(
                text = "Videos de ejemplo:",
                fontWeight = FontWeight.Medium
            )

            sampleVideos.forEach { (name, url) ->
                Button(
                    onClick = { 
                        videoUrl = url
                        isYouTubeVideo = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(name)
                }
            }

            // Botón de reproducción
            Button(
                onClick = {
                    if (videoUrl.isNotEmpty()) {
                        if (isYouTubeVideo) {
                            // Para YouTube, usar WebView integrado
                            isPlaying = true
                            Toast.makeText(context, "Reproduciendo video de YouTube...", Toast.LENGTH_SHORT).show()
                        } else {
                            // Para videos directos, usar ExoPlayer
                            try {
                                val uri = videoUrl.toUri()
                                val mediaItem = MediaItem.fromUri(uri)
                                exoPlayer.setMediaItem(mediaItem)
                                exoPlayer.prepare()
                                exoPlayer.play()
                                isPlaying = true
                                Toast.makeText(context, "Reproduciendo video...", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error al reproducir: ${e.message}", Toast.LENGTH_LONG).show()
                                Log.e("VideoActivity", "Error al reproducir video", e)
                            }
                        }
                    } else {
                        Toast.makeText(context, "Por favor ingresa una URL", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reproducir Video")
            }

            // Reproductor de video
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (isPlaying) {
                    if (isYouTubeVideo) {
                        // WebView para YouTube
                        AndroidView(
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    settings.javaScriptEnabled = true
                                    settings.mediaPlaybackRequiresUserGesture = false
                                    settings.domStorageEnabled = true
                                    webViewClient = object : WebViewClient() {
                                        override fun onPageFinished(view: WebView?, url: String?) {
                                            super.onPageFinished(view, url)
                                        }
                                    }
                                    
                                    // Convertir URL de YouTube a formato embed
                                    val embedUrl = videoUrl.replace("watch?v=", "embed/")
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
                        // ExoPlayer para videos directos
                        DisposableEffect(Unit) {
                            onDispose {
                                // El exoPlayer se libera en onDestroy de la Activity
                            }
                        }
                        AndroidView(
                            factory = { ctx ->
                                PlayerView(ctx).apply {
                                    player = exoPlayer
                                    layoutParams = FrameLayout.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                    useController = true
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Ingresa una URL y presiona 'Reproducir Video'",
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
