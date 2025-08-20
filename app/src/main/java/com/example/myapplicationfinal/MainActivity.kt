package com.example.myapplicationfinal

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.myapplicationfinal.ui.theme.MyApplicationFinalTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

// --- ACTIVITY PRINCIPAL Y NAVEGACIÓN ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationFinalTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "welcome_screen") {
        composable("welcome_screen") { WelcomeScreen(navController = navController) }
        composable("main_menu") { MainMenuScreen(navController = navController) }
        composable("camera_screen") { CameraScreen(navController = navController) }
        composable("gallery_screen") { GalleryScreen(navController = navController) }
        composable("video_screen") { VideoScreen(navController = navController) }
        composable("message_screen") { MessageScreen(navController = navController) }
    }
}

// --- PANTALLAS DE LA APLICACIÓN ---

@Composable
fun WelcomeScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Proyecto Final Multifuncional", fontSize = 28.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))
        AuthorCard(name = "Henry Castro", role = "1-21-4112")
        Spacer(modifier = Modifier.height(8.dp))
        AuthorCard(name = "Lissette Rodríguez", role = "1-19-3824")
        Spacer(modifier = Modifier.height(8.dp))
        AuthorCard(name = "Miguel Berroa", role = "2-16-3694")
        Spacer(modifier = Modifier.height(48.dp))
        Button(onClick = { navController.navigate("main_menu") { popUpTo("welcome_screen") { inclusive = true } } }) {
            Text("Entrar")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(navController: NavController) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Menú Principal") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            Button(onClick = { navController.navigate("camera_screen") }, modifier = Modifier.fillMaxWidth()) { Text("1. Tomar Foto") }
            Button(onClick = { navController.navigate("gallery_screen") }, modifier = Modifier.fillMaxWidth()) { Text("2. Seleccionar Imagen de Galería") }
            Button(onClick = { navController.navigate("video_screen") }, modifier = Modifier.fillMaxWidth()) { Text("3. Reproducir Video") }
            Button(onClick = { navController.navigate("message_screen") }, modifier = Modifier.fillMaxWidth()) { Text("4. Mostrar Mensaje") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCameraPermission by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val imageCapture = remember { ImageCapture.Builder().build() }

    LaunchedEffect(key1 = true) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("1. Tomar Foto") }, navigationIcon = { BackButton(navController) }) }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (hasCameraPermission) {
                if (imageUri == null) {
                    CameraPreview(
                        modifier = Modifier.weight(1f),
                        imageCapture = imageCapture,
                        lifecycleOwner = lifecycleOwner
                    )
                    Button(
                        onClick = { takePhoto(context, imageCapture) { uri -> imageUri = uri } },
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) { Text("Tomar Foto") }
                } else {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = "Foto Capturada",
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    )
                    Button(
                        onClick = { imageUri = null },
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) { Text("Tomar Otra Foto") }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Se necesita permiso de la cámara para usar esta función.")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(navController: NavController) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> imageUri = uri }
    )

    Scaffold(
        topBar = { TopAppBar(title = { Text("2. Galería") }, navigationIcon = { BackButton(navController) }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = "Imagen Seleccionada",
                    modifier = Modifier.weight(1f).fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            Button(onClick = { galleryLauncher.launch("image/*") }) {
                Text(if (imageUri == null) "Abrir Galería" else "Seleccionar Otra Imagen")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoScreen(navController: NavController) {
    val context = LocalContext.current
    // Corrección de advertencia: Usando .toUri()
    val videoUri = "android.resource://${context.packageName}/${R.raw.sample_video}".toUri()
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUri))
            prepare()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("3. Reproducir Video") }, navigationIcon = { BackButton(navController) }) }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            DisposableEffect(Unit) {
                onDispose {
                    exoPlayer.release()
                }
            }
            AndroidView(
                factory = {
                    PlayerView(it).apply {
                        player = exoPlayer
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageScreen(navController: NavController) {
    val context = LocalContext.current
    var text by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = { TopAppBar(title = { Text("4. Mostrar Mensaje") }, navigationIcon = { BackButton(navController) }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Escribe un mensaje") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                if (text.isNotBlank()) {
                    Toast.makeText(context, text, Toast.LENGTH_LONG).show()
                    scope.launch {
                        snackbarHostState.showSnackbar(message = text)
                    }
                }
            }) {
                Text("Mostrar Mensaje")
            }
        }
    }
}

// --- COMPONENTES AUXILIARES ---

@Composable
fun CameraPreview(modifier: Modifier, imageCapture: ImageCapture, lifecycleOwner: LifecycleOwner) {
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context) }

    LaunchedEffect(Unit) {
        val cameraProvider = suspendCoroutine<ProcessCameraProvider> { continuation ->
            cameraProviderFuture.addListener({
                continuation.resume(cameraProviderFuture.get())
            }, ContextCompat.getMainExecutor(context))
        }
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                androidx.camera.core.Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) },
                imageCapture
            )
        } catch (e: Exception) {
            Log.e("CameraPreview", "Error al vincular la cámara", e)
        }
    }
    AndroidView({ previewView }, modifier)
}

// Corrección de advertencia: Usando MediaStore para guardar la foto
fun takePhoto(context: Context, imageCapture: ImageCapture, onImageCaptured: (Uri) -> Unit) {
    val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis())
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Images")
        }
    }
    val outputOptions = ImageCapture.OutputFileOptions
        .Builder(context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues)
        .build()

    imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageSavedCallback {
        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
            outputFileResults.savedUri?.let {
                onImageCaptured(it)
                Toast.makeText(context, "Foto guardada en: ${it.path}", Toast.LENGTH_SHORT).show()
            }
        }
        override fun onError(exception: ImageCaptureException) {
            Toast.makeText(context, "Error al guardar foto: ${exception.message}", Toast.LENGTH_SHORT).show()
            Log.e("CameraX", "Error al guardar foto:", exception)
        }
    })
}


@Composable
fun AuthorCard(name: String, role: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = role, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun BackButton(navController: NavController) {
    IconButton(onClick = { navController.popBackStack() }) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
    }
}