package com.example.kaffacafeteria.data.local

import android.content.Context
import android.os.Environment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

/**
 * Registro persistente de reportes PDF.
 *
 * Dónde se guarda:
 *  - PDF: `Documents/Kaffa/reportes` en el directorio externo de la app
 *    (visible en "Descargas" de exploradores de archivos, sobrevive al cierre
 *    de la app y no requiere permisos de almacenamiento).
 *    Si el almacenamiento externo no está disponible (caso raro), cae a
 *    `filesDir/reportes` y el archivo se comparte igual por FileProvider.
 *  - Índice: `reportes.json` con los metadatos, para listar y borrar sin
 *    recorrer el disco.
 *
 * Reglas del negocio:
 *  - El reporte se guarda una vez generado y se puede volver a abrir,
 *    compartir o descargar cuantas veces se quiera.
 *  - Sólo un administrador puede eliminar el archivo (verifica el rol en
 *    ReportArchiveViewModel antes de llamar a [eliminar]).
 */
class ReportArchive(private val context: Context) {

    data class ReporteArchivado(
        val id: String,
        val titulo: String,
        val tipo: String,
        val periodo: String,
        val nombreArchivo: String,
        val generadoPor: String,
        val fechaGenerado: String,
        val tamanoBytes: Long
    )

    private val gson = Gson()
    private val locale = Locale("es", "CO")

    private val dirExterno: File?
        get() = runCatching {
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.resolve("Kaffa/reportes")
        }.getOrNull()

    private val dirInterno: File
        get() = File(context.filesDir, "reportes")

    /** Directorio donde realmente quedaron los archivos. */
    private fun dirEfectivo(): File {
        val ext = dirExterno?.takeIf { it.parentFile?.canWrite() != false || it.mkdirs() } ?: dirInterno
        ext.mkdirs()
        return ext
    }

    private fun archivoIndice(): File = File(dirEfectivo(), "reportes.json")

    fun archivoDe(reporte: ReporteArchivado): File = File(dirEfectivo(), reporte.nombreArchivo)

    // ── Índice ────────────────────────────────────────────────────────────────

    private fun leerIndice(): MutableList<ReporteArchivado> {
        val archivo = archivoIndice()
        if (!archivo.exists()) return mutableListOf()
        return try {
            archivo.readText()
                .takeIf { it.isNotBlank() }
                ?.let { gson.fromJson<MutableList<ReporteArchivado>>(it, LISTA_TYPE) }
                ?.toMutableList()
                ?: mutableListOf()
        } catch (e: Exception) {
            // Un índice corrupto no debe impedir el uso de la app.
            mutableListOf()
        }
    }

    private fun escribirIndice(items: List<ReporteArchivado>) {
        try {
            archivoIndice().writeText(gson.toJson(items))
        } catch (_: Exception) {
        }
    }

    /**
     * Registra un PDF recién generado.
     *
     * @return el reporte archivado con su id asignado.
     */
    fun registrar(
        titulo: String,
        tipo: String,
        periodo: String,
        archivo: File,
        generadoPor: String
    ): ReporteArchivado {
        val reporte = ReporteArchivado(
            id = UUID.randomUUID().toString(),
            titulo = titulo,
            tipo = tipo,
            periodo = periodo,
            nombreArchivo = archivo.name,
            generadoPor = generadoPor,
            fechaGenerado = SimpleDateFormat("dd/MM/yyyy HH:mm", locale).format(System.currentTimeMillis()),
            tamanoBytes = if (archivo.exists()) archivo.length() else 0L
        )
        val items = leerIndice()
        items.add(0, reporte)
        escribirIndice(items)
        return reporte
    }

    /** Reportes del tipo indicado, más recientes primero. */
    fun listarPorTipo(tipo: String): List<ReporteArchivado> =
        leerIndice().filter { it.tipo == tipo }.sortedByDescending { it.fechaGenerado }

    fun listarTodos(): List<ReporteArchivado> =
        leerIndice().sortedByDescending { it.fechaGenerado }

    /** El archivo más reciente de un tipo, si existe y sigue en disco. */
    fun ultimoReporte(tipo: String): ReporteArchivado? =
        listarPorTipo(tipo).firstOrNull { archivoDe(it).exists() }

    /**
     * Elimina el PDF del disco y su entrada del índice.
     *
     * @return true si el archivo físico se borró.
     */
    fun eliminar(id: String): Boolean {
        val items = leerIndice()
        val reporte = items.firstOrNull { it.id == id } ?: return false
        val borrado = try {
            archivoDe(reporte).delete()
        } catch (e: Exception) {
            false
        }
        escribirIndice(items.filterNot { it.id == id })
        return borrado
    }

    /** Ruta sugerida para un PDF nuevo dentro del archivo. */
    fun nuevoArchivo(prefijo: String): File = File(dirEfectivo(), "${prefijo}_${System.currentTimeMillis()}.pdf")

    /**
     * Copia el PDF a Descargas usando MediaStore (sin permisos en Android 10+;
     * en versiones anteriores se recurre al directorio público).
     *
     * @return el Uri público o null si no se pudo publicar.
     */
    fun publicarEnDescargas(archivo: File): android.net.Uri? {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val values = android.content.ContentValues().apply {
                put(android.provider.MediaStore.Downloads.DISPLAY_NAME, archivo.name)
                put(android.provider.MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(android.provider.MediaStore.Downloads.IS_PENDING, 1)
            }
            val uri = context.contentResolver.insert(
                android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, values
            ) ?: return null
            return try {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    archivo.inputStream().use { it.copyTo(out) }
                } ?: return null
                values.clear()
                values.put(android.provider.MediaStore.Downloads.IS_PENDING, 0)
                context.contentResolver.update(uri, values, null, null)
                uri
            } catch (e: Exception) {
                context.contentResolver.delete(uri, null, null)
                null
            }
        } else {
            return try {
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                dir.mkdirs()
                val destino = File(dir, archivo.name)
                archivo.copyTo(destino, overwrite = true)
                android.net.Uri.fromFile(destino)
            } catch (e: Exception) {
                null
            }
        }
    }

    companion object {
        /** Tipo genérico usado por Gson para deserializar el índice. */
        private val LISTA_TYPE = object : TypeToken<MutableList<ReporteArchivado>>() {}.type

        const val TIPO_DIARIO = "diario"
        const val TIPO_SEMANAL = "semanal"
        const val TIPO_MENSUAL = "mensual"
        const val TIPO_INVENTARIO = "inventario"

        fun etiquetaTipo(tipo: String): String = when (tipo) {
            TIPO_DIARIO -> "Reporte diario"
            TIPO_SEMANAL -> "Reporte semanal"
            TIPO_MENSUAL -> "Reporte mensual"
            TIPO_INVENTARIO -> "Reporte de inventario"
            else -> "Reporte"
        }
    }
}
