package co.edu.unicauca.urbanguard.ui.features.guard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

@Composable
internal fun CompleteRoundButton(done: Int, total: Int, isFinishing: Boolean, onClick: () -> Unit) {
    Column {
        Button(
            onClick = onClick,
            enabled = total > 0 && done == total && !isFinishing,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = UgNavy),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Icon(Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(8.dp))
            Text("Completar ronda", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        if (done < total) {
            Spacer(Modifier.height(6.dp))
            Text(
                "Escanea todos los puntos para completar la ronda.",
                fontSize = 12.sp,
                color = UgTextSecondary
            )
        }
    }
}

@Composable
internal fun RoundCompletionDialog(onBackToRounds: () -> Unit) {
    AlertDialog(
        onDismissRequest = onBackToRounds,
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp),
        icon = {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = UgEmerald,
                modifier = Modifier.size(48.dp)
            )
        },
        title = { Text("¡Ronda completada!", fontWeight = FontWeight.Bold, color = UgNavy) },
        text = {
            Text("Todos los puntos quedaron registrados correctamente.", color = UgTextSecondary)
        },
        confirmButton = {
            Button(
                onClick = onBackToRounds,
                colors = ButtonDefaults.buttonColors(containerColor = UgNavy),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver a rondas", fontWeight = FontWeight.Bold)
            }
        }
    )
}
