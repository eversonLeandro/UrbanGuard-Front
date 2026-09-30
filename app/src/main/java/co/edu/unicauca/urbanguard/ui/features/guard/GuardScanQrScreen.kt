package co.edu.unicauca.urbanguard.ui.features.guard

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

@Composable
fun GuardScanQrScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var scannedCode by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UgBackground)
    ) {
        UrbanGuardTopBar(
            title = "Escanear código QR",
            onNavigateBack = onNavigateBack
        )

        when {
            scannedCode != null -> {
                // Resultado del escaneo
                ScanResultContent(
                    code = scannedCode!!,
                    onScanAgain = { scannedCode = null },
                    onNavigateBack = onNavigateBack
                )
            }

            hasCameraPermission -> {
                // Vista de cámara
                CameraPreviewContent(
                    onCodeScanned = { code ->
                        scannedCode = code
                    }
                )
            }

            else -> {
                // Sin permiso
                PermissionDeniedContent(
                    onRequestPermission = {
                        launcher.launch(Manifest.permission.CAMERA)
                    }
                )
            }
        }
    }
}

@Suppress("TooGenericExceptionCaught")
@Composable
private fun CameraPreviewContent(
    onCodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasScanned by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    val barcodeScanner = BarcodeScanning.getClient()
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setTargetResolution(Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null && !hasScanned) {
                            val inputImage = InputImage.fromMediaImage(
                                mediaImage,
                                imageProxy.imageInfo.rotationDegrees
                            )
                            barcodeScanner.process(inputImage)
                                .addOnSuccessListener { barcodes ->
                                    for (barcode in barcodes) {
                                        if (barcode.format == Barcode.FORMAT_QR_CODE && !hasScanned) {
                                            barcode.rawValue?.let { value ->
                                                hasScanned = true
                                                onCodeScanned(value)
                                            }
                                        }
                                    }
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                }
                        } else {
                            imageProxy.close()
                        }
                    }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        // El catch amplio cubre la superficie de CameraX
                        // (CameraInfoUnavailableException, IllegalArgumentException,
                        // SecurityException y otras unchecked). No esta en una
                        // corrutina, asi que no corre riesgo de tragarse una
                        // CancellationException. Si la camara no arranca, la pantalla
                        // se queda sin previsualizacion y el vigilante lo ve en el log.
                        Log.e("GuardScanQr", "Error al iniciar cámara", e)
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay con marco de escaneo
        CameraScanOverlay()
    }
}

/**
 * Marco de escaneo e instrucción, superpuestos sobre la previsualización.
 *
 * No introduce un nodo de layout propio: la Column que emite sigue siendo hija
 * directa del Box de [CameraPreviewContent], así que el overlay se superpone al
 * preview igual que antes.
 */
@Composable
private fun CameraScanOverlay() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(260.dp)
                .border(3.dp, UgEmerald, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = null,
                tint = UgEmerald.copy(alpha = 0.4f),
                modifier = Modifier.size(80.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Apunta la cámara al código QR del visitante",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(UgNavy.copy(alpha = 0.7f))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

// Enum para simular los diferentes estados de validación
enum class PassStatus {
    APPROVED, EXPIRED, USED
}

// Simulamos los datos recuperados de la base de datos al escanear el QR
private data class ScanSummary(
    val visitorName: String,
    val extraGuests: Int,
    val destination: String
)

// Data class para agrupar la información visual de cada estado
private data class PassStatusInfo(
    val color: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val description: String,
    val summary: ScanSummary? = null
)

private fun getStatusInfo(status: PassStatus): PassStatusInfo {
    return when (status) {
        PassStatus.APPROVED -> PassStatusInfo(
            color = UgEmerald,
            icon = Icons.Default.CheckCircle,
            title = "Acceso Autorizado",
            description = "El pase QR es válido. Permita el ingreso al visitante.",
            summary = ScanSummary(
                visitorName = "Carlos López",
                extraGuests = 2,
                destination = "Apto 402 (Flia. Martínez)"
            )
        )
        PassStatus.EXPIRED -> PassStatusInfo(
            color = Color(0xFFE53935), // Rojo
            icon = androidx.compose.material.icons.Icons.Default.Cancel ?: Icons.Default.QrCodeScanner,
            title = "Pase Expirado",
            description = "Este pase fue emitido para una fecha anterior y ya no es válido."
        )
        PassStatus.USED -> PassStatusInfo(
            color = Color(0xFFF57C00), // Naranja
            icon = androidx.compose.material.icons.Icons.Default.Info ?: Icons.Default.QrCodeScanner,
            title = "Pase Ya Utilizado",
            description = "Este pase ya fue registrado previamente en la portería."
        )
    }
}

@Composable
private fun ScanResultContent(
    code: String,
    onScanAgain: () -> Unit,
    onNavigateBack: () -> Unit
) {
    // Simulamos el estado basado en el código escaneado o de forma aleatoria.
    // AQUÍ EL VIEWMODEL: Cuando conectes el backend, aquí llamarás al ViewModel para validar el 'code'.
    // Si el backend responde 200 OK, el ViewModel actualizará automáticamente el historial de visitantes.
    val status = remember(code) { 
        PassStatus.entries.toTypedArray().random() 
    }
    val statusInfo = getStatusInfo(status)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        StatusCard(statusInfo = statusInfo)
        Spacer(Modifier.height(40.dp))
        ScanResultActions(onScanAgain = onScanAgain, onNavigateBack = onNavigateBack)
    }
}

@Composable
private fun StatusCard(statusInfo: PassStatusInfo) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StatusCardHeader(statusInfo)
            
            // Solo mostrar el resumen de visita si hay datos (ej. Aprobado)
            if (statusInfo.summary != null) {
                Spacer(Modifier.height(24.dp))
                VisitorSummaryBox(summary = statusInfo.summary)
            }
        }
    }
}

@Composable
private fun StatusCardHeader(statusInfo: PassStatusInfo) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .background(statusInfo.color.copy(alpha = 0.1f), RoundedCornerShape(40.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = statusInfo.icon,
            contentDescription = null,
            tint = statusInfo.color,
            modifier = Modifier.size(48.dp)
        )
    }

    Spacer(Modifier.height(24.dp))

    Text(
        text = statusInfo.title,
        fontSize = 22.sp,
        fontWeight = FontWeight.ExtraBold,
        color = statusInfo.color
    )

    Spacer(Modifier.height(12.dp))

    Text(
        text = statusInfo.description,
        fontSize = 15.sp,
        color = UgTextSecondary,
        textAlign = TextAlign.Center,
        lineHeight = 22.sp
    )
}

@Composable
private fun VisitorSummaryBox(summary: ScanSummary) {
    Surface(
        color = UgBackground,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("👤 Visitante:", fontSize = 12.sp, color = UgTextSecondary)
            Text(
                text = "${summary.visitorName} (+${summary.extraGuests} acomp.)",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = UgNavy
            )
            Spacer(Modifier.height(8.dp))
            Text("🏠 Destino:", fontSize = 12.sp, color = UgTextSecondary)
            Text(
                text = summary.destination,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = UgNavy
            )
        }
    }
}

@Composable
private fun ScanResultActions(onScanAgain: () -> Unit, onNavigateBack: () -> Unit) {
    Button(
        onClick = onScanAgain,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = UgNavy),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Icon(
            Icons.Default.QrCodeScanner,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text("Escanear siguiente pase", fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }

    Spacer(Modifier.height(16.dp))

    OutlinedButton(
        onClick = onNavigateBack,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = UgNavy),
        border = ButtonDefaults.outlinedButtonBorder,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Text("Volver al menú", fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
private fun PermissionDeniedContent(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            tint = UgTextSecondary,
            modifier = Modifier.size(64.dp)
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Se necesita acceso a la cámara",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = UgNavy,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Para escanear códigos QR de visitantes es necesario otorgar permiso de cámara.",
            fontSize = 14.sp,
            color = UgTextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onRequestPermission,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = UgNavy),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Conceder permiso de cámara", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}
