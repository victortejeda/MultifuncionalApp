package com.example.myapplicationfinal

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
    
                // URLs de ejemplo predefinidas (videos que funcionan realmente)
            val sampleVideos = listOf(
                "Video Local" to "android.resource://${context.packageName}/${R.raw.sample_video}",
                "Big Buck Bunny (MP4)" to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                "Elephant Dream (MP4)" to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                "For Bigger Blazes (MP4)" to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                "Sintel (MP4)" to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"
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
                label = { Text("URL del video (MP4, WebM, etc.)") },
                placeholder = { Text("https://ejemplo.com/video.mp4") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Información sobre YouTube
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "ℹ️ Información sobre YouTube",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Las URLs de YouTube se abrirán en la app oficial\n" +
                                "• Para videos directos, usa URLs de archivos MP4/WebM\n" +
                                "• Los videos de ejemplo funcionan perfectamente",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

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
                                    // Verificar si es una URL de YouTube
                                    if (videoUrl.contains("youtube.com") || videoUrl.contains("youtu.be")) {
                                        // Para YouTube, mostrar mensaje explicativo y abrir en la app
                                        Toast.makeText(
                                            context, 
                                            "YouTube requiere la app oficial. Abriendo en YouTube...", 
                                            Toast.LENGTH_LONG
                                        ).show()
                                        
                                        // Abrir directamente en YouTube
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
                                        context.startActivity(intent)
                                        return@Button
                                    }
                                    
                                    // Para otras URLs, intentar reproducir
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
                            Toast.makeText(context, "Abriendo en YouTube...", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "No se pudo abrir YouTube", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Text("🎬 Abrir en YouTube")
                }
            }
            
            // Botón de prueba para YouTube
            Button(
                onClick = {
                    val testYouTubeUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(testYouTubeUrl))
                        context.startActivity(intent)
                        Toast.makeText(context, "Abriendo video de prueba en YouTube...", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "No se pudo abrir YouTube", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("🧪 Probar YouTube (Video de Prueba)")
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
