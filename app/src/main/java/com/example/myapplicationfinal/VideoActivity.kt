package com.example.myapplicationfinal

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
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
    
    // URLs de ejemplo predefinidas
    val sampleVideos = listOf(
        "Video Local" to "android.resource://${context.packageName}/${R.raw.sample_video}",
        "Big Buck Bunny" to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
        "Elephant Dream" to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
        "For Bigger Blazes" to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
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
            // Título
            Text(
                text = "Reproductor Multifuncional",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Campo de URL personalizada
            OutlinedTextField(
                value = videoUrl,
                onValueChange = { videoUrl = it },
                label = { Text("URL del video (YouTube, MP4, etc.)") },
                placeholder = { Text("https://www.youtube.com/watch?v=...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Botones de videos predefinidos
            Text(
                text = "Videos de ejemplo:",
                fontWeight = FontWeight.Medium
            )

            sampleVideos.forEach { (name, url) ->
                Button(
                    onClick = { videoUrl = url },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(name)
                }
            }

            // Botones de control
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (videoUrl.isNotEmpty()) {
                            try {
                                val uri = if (videoUrl.startsWith("http")) {
                                    // Para URLs web, incluyendo YouTube
                                    if (videoUrl.contains("youtube.com") || videoUrl.contains("youtu.be")) {
                                        // Convertir URL de YouTube a formato directo si es posible
                                        videoUrl.toUri()
                                    } else {
                                        videoUrl.toUri()
                                    }
                                } else {
                                    videoUrl.toUri()
                                }
                                
                                val mediaItem = MediaItem.fromUri(uri)
                                exoPlayer.setMediaItem(mediaItem)
                                exoPlayer.prepare()
                                exoPlayer.play()
                                isPlaying = true
                                Toast.makeText(context, "Reproduciendo video...", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error al reproducir: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(context, "Por favor ingresa una URL", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reproducir")
                }

                Button(
                    onClick = {
                        exoPlayer.pause()
                        isPlaying = false
                        Toast.makeText(context, "Video pausado", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Pausar")
                }

                Button(
                    onClick = {
                        exoPlayer.stop()
                        isPlaying = false
                        Toast.makeText(context, "Video detenido", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Detener")
                }
            }

            // Botón para abrir en YouTube
            if (videoUrl.contains("youtube.com") || videoUrl.contains("youtu.be")) {
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "No se pudo abrir YouTube", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Abrir en YouTube")
                }
            }

            // Reproductor de video
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (isPlaying || exoPlayer.isPlaying) {
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
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Selecciona un video para reproducir",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
