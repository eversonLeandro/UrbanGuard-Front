package co.edu.unicauca.urbanguard.core.nfc

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * Lectura de tags NFC físicos para vincularlos a puntos de control.
 *
 * Usa el modo lector (`enableReaderMode`): no necesita filtros en el
 * manifiesto y solo escucha mientras la pantalla del formulario está visible.
 * El id del tag se entrega en hexadecimal en mayúsculas.
 *
 * FLAG_READER_SKIP_NDEF_CHECK evita el despacho NDEF del sistema mientras
 * el modo lector está activo; NO_PLATFORM_SOUNDS silencia su sonido.
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
 * Recorre la cadena de ContextWrapper hasta encontrar la Activity real.
 * En Compose, LocalContext entrega un ContextWrapper, no la Activity directa.
 */
private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

/**
 * Activa el lector NFC mientras [enabled] sea verdadero y entrega cada tag
 * acercado en [onTagRead]. Al salir se desactiva solo.
 *
 * Con `enableReaderMode` activo y la pantalla reanudada, Android entrega el
 * tag al callback de esta Activity en lugar de despacharlo a otras apps.
 */
@Composable
fun NfcReaderEffect(
    enabled: Boolean,
    onTagRead: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val latestCallback = rememberUpdatedState(onTagRead)

    DisposableEffect(enabled, context, lifecycleOwner) {
        val activity = context.findActivity()
        val adapter = activity?.let { NfcAdapter.getDefaultAdapter(it) }
        if (!enabled || activity == null || adapter == null) {
            onDispose { }
        } else {
            val flags = NfcAdapter.FLAG_READER_NFC_A or
                NfcAdapter.FLAG_READER_NFC_B or
                NfcAdapter.FLAG_READER_NFC_F or
                NfcAdapter.FLAG_READER_NFC_V or
                NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK or
                NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS
            val callback = NfcAdapter.ReaderCallback { tag ->
                latestCallback.value(tag.tagIdHex())
            }
            var readerActive = false
            fun startReader() {
                if (!readerActive && adapter.isEnabled) {
                    adapter.enableReaderMode(activity, callback, flags, Bundle.EMPTY)
                    readerActive = true
                }
            }
            fun stopReader() {
                if (readerActive) {
                    runCatching { adapter.disableReaderMode(activity) }
                    readerActive = false
                }
            }
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> startReader()
                    Lifecycle.Event.ON_PAUSE -> stopReader()
                    else -> Unit
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                startReader()
            }
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
                stopReader()
            }
        }
    }
}
