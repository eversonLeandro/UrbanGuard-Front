package co.edu.unicauca.urbanguard.core.nfc

import android.app.Activity
import android.content.Context
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext

/**
 * Lectura de tags NFC físicos para vincularlos a puntos de control.
 *
 * Usa el modo lector (`enableReaderMode`): no necesita filtros en el
 * manifiesto y solo escucha mientras la pantalla del formulario está visible.
 * El id del tag se entrega en hexadecimal en mayúsculas.
 */

/** Verdadero si el equipo puede leer tags NFC. */
fun isNfcAvailable(context: Context): Boolean {
    val adapter = NfcAdapter.getDefaultAdapter(context)
    return adapter != null && adapter.isEnabled
}

/** Id del tag en hex (`04A3F2…`), estable para comparar y guardar. */
fun Tag.tagIdHex(): String =
    id.joinToString(separator = "") { "%02X".format(it) }

/**
 * Activa el lector NFC mientras [enabled] sea verdadero y entrega cada tag
 * acercado en [onTagRead]. Al salir se desactiva solo.
 */
@Composable
fun NfcReaderEffect(
    enabled: Boolean,
    onTagRead: (String) -> Unit
) {
    val context = LocalContext.current
    val latestCallback = rememberUpdatedState(onTagRead)

    DisposableEffect(enabled) {
        val activity = context as? Activity
        val adapter = activity?.let { NfcAdapter.getDefaultAdapter(it) }
        if (!enabled || activity == null || adapter == null) {
            onDispose { }
        } else {
            val flags = NfcAdapter.FLAG_READER_NFC_A or
                NfcAdapter.FLAG_READER_NFC_B or
                NfcAdapter.FLAG_READER_NFC_F or
                NfcAdapter.FLAG_READER_NFC_V or
                NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK
            val options = Bundle()
            adapter.enableReaderMode(activity, { tag ->
                latestCallback.value(tag.tagIdHex())
            }, flags, options)
            onDispose {
                adapter.disableReaderMode(activity)
            }
        }
    }
}
