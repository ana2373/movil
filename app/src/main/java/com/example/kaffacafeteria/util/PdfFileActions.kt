package com.example.kaffacafeteria.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

/**
 * Acciones sobre un PDF guardado: abrir y compartir.
 *
 * Android 7+ bloquea `file://` hacia otras apps, por eso los reportes se
 * exponen con FileProvider (authority = `${applicationId}.fileprovider`).
 */
object PdfFileActions {

    fun compartir(context: Context, archivo: File, titulo: String) {
        val uri = uriDe(context, archivo) ?: return
        val enviar = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, titulo)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(enviar, "Compartir reporte").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    fun abrir(context: Context, archivo: File) {
        val uri = uriDe(context, archivo) ?: return
        val ver = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        runCatching {
            context.startActivity(Intent.createChooser(ver, "Abrir reporte").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }

    private fun uriDe(context: Context, archivo: File): android.net.Uri? = runCatching {
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
    }.getOrNull()
}
