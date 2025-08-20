package com.example.myapplicationfinal

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplicationfinal.ui.theme.MyApplicationFinalTheme
import kotlinx.coroutines.launch

class MessageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationFinalTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MessageScreen(onBackPressed = { finish() })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageScreen(onBackPressed: () -> Unit) {
    val context = LocalContext.current
    var messageText by remember { mutableStateOf("") }
    var toastDuration by remember { mutableStateOf("SHORT") }
    var snackbarMessage by remember { mutableStateOf("") }
    var messageHistory by remember { mutableStateOf(listOf<String>()) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Mensajes predefinidos
    val predefinedMessages = listOf(
        "¡Hola! Esta es una aplicación multifuncional",
        "Mensaje de prueba para Toast",
        "¡Funcionalidad completada exitosamente!",
        "Bienvenido a la aplicación Android",
        "¡Gracias por usar nuestra app!"
    )

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Mostrar Mensajes") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título
            Text(
                text = "Sistema de Mensajes",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Escribir Mensaje Personalizado",
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp
                    )

                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        label = { Text("Escribe tu mensaje aquí") },
                        placeholder = { Text("Ingresa el mensaje que quieres mostrar") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )

                    // Selector de duración para Toast
                    Text(
                        text = "Duración del Toast:",
                        fontWeight = FontWeight.Medium
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            onClick = { toastDuration = "SHORT" },
                            label = { Text("Corto") },
                            selected = toastDuration == "SHORT"
                        )
                        FilterChip(
                            onClick = { toastDuration = "LONG" },
                            label = { Text("Largo") },
                            selected = toastDuration == "LONG"
                        )
                    }

                    // Botones de acción
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (messageText.isNotBlank()) {
                                    val duration = if (toastDuration == "LONG") Toast.LENGTH_LONG else Toast.LENGTH_SHORT
                                    Toast.makeText(context, messageText, duration).show()
                                    messageHistory = messageHistory + "Toast: $messageText"
                                } else {
                                    Toast.makeText(context, "Por favor escribe un mensaje", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Toast")
                        }

                        Button(
                            onClick = {
                                if (messageText.isNotBlank()) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = messageText,
                                            actionLabel = "OK",
                                            duration = SnackbarDuration.Long
                                        )
                                    }
                                    messageHistory = messageHistory + "Snackbar: $messageText"
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Por favor escribe un mensaje")
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Snackbar")
                        }
                    }
                }
            }

            // Mensajes predefinidos
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Mensajes Predefinidos",
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp
                    )

                    predefinedMessages.forEach { message ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = message,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    TextButton(
                                        onClick = {
                                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                            messageHistory = messageHistory + "Toast: $message"
                                        }
                                    ) {
                                        Text("Toast", fontSize = 12.sp)
                                    }
                                    TextButton(
                                        onClick = {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    message = message,
                                                    actionLabel = "OK",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                            messageHistory = messageHistory + "Snackbar: $message"
                                        }
                                    ) {
                                        Text("Snackbar", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Historial de mensajes
            if (messageHistory.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Historial de Mensajes",
                                fontWeight = FontWeight.Medium,
                                fontSize = 18.sp
                            )
                            TextButton(
                                onClick = { messageHistory = emptyList() }
                            ) {
                                Text("Limpiar")
                            }
                        }

                        messageHistory.takeLast(10).forEach { historyItem ->
                            Text(
                                text = "• $historyItem",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }

                        if (messageHistory.size > 10) {
                            Text(
                                text = "... y ${messageHistory.size - 10} más",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                }
            }

            // Información adicional
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "ℹ️ Información",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Toast: Mensaje temporal que aparece por unos segundos\n" +
                                "• Snackbar: Mensaje en la parte inferior con opción de acción\n" +
                                "• Los mensajes se guardan en el historial temporalmente",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}
