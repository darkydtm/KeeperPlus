package com.tonapps.qr

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import zxingcpp.BarcodeReader

class QRImageAnalyzer : ImageAnalysis.Analyzer {
    private val reader = newReader()
    private val _flow = MutableStateFlow<String?>(null)
    val flow = _flow.asStateFlow().filterNotNull()

    private val _scanFlow = MutableSharedFlow<String>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val scanFlow = _scanFlow.asSharedFlow()

    override fun analyze(imageProxy: ImageProxy) {
        try {
            reader.read(imageProxy).forEach { barcode ->
                barcode.text?.let { value ->
                    _flow.tryEmit(value)
                    _scanFlow.tryEmit(value)
                }
            }
        } catch (_: Exception) {
            // A malformed camera frame must not stop subsequent scans.
        } finally {
            imageProxy.close()
        }
    }

    fun release() = Unit

    companion object {
        private fun newReader() = BarcodeReader(BarcodeReader.Options(
            formats = setOf(BarcodeReader.Format.QR_CODE),
            tryHarder = true,
            tryRotate = true,
            tryInvert = true,
        ))

        fun readImage(context: Context, uri: Uri): String? {
            val bitmap = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) } ?: return null
            return try {
                newReader().read(bitmap).firstNotNullOfOrNull { it.text }
            } finally {
                bitmap.recycle()
            }
        }
    }
}
