package com.example.kaffacafeteria.util

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Generador de reportes PDF.
 *
 * Decisión técnica: se usa `android.graphics.pdf.PdfDocument` (API 19+) en
 * lugar de librerías externas (iText / PdfBox-Android). Motivos:
 *  - No agrega dependencias ni infla el peso del APK.
 *  - No exige licencias comerciales ni AGPL.
 *  - Funciona sin conexión: el PDF se arma en el dispositivo.
 *
 * El archivo generado se guarda en el directorio privado de la app
 * (ver ReportArchive) y se comparte mediante FileProvider.
 */
object PdfReportBuilder {

    // A4 a 72 dpi (puntos): 595 x 842 puntos.
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 42f
    private const val CONTENT_WIDTH = PAGE_WIDTH - MARGIN * 2

    private val locale = Locale("es", "CO")
    private val currency: NumberFormat = NumberFormat.getCurrencyInstance(locale).apply {
        maximumFractionDigits = 2
    }

    private fun dateFormat() = SimpleDateFormat("dd/MM/yyyy HH:mm", locale)
    private fun fileStamp() = SimpleDateFormat("yyyyMMdd_HHmmss", locale)

    /** Color de marca de Kaffa (verde) usado en cabeceras y acentos. */
    private val brandGreen = Color.rgb(0x2E, 0x6B, 0x4F)
    private val accentTerracotta = Color.rgb(0xC9, 0x7B, 0x4A)
    private val lightGray = Color.rgb(0xF2, 0xF2, 0xF2)
    private val darkText = Color.rgb(0x22, 0x22, 0x22)
    private val midText = Color.rgb(0x66, 0x66, 0x66)

    /**
     * Bloque de tabla del reporte.
     *
     * @param titulo    encabezado de la sección
     * @param headers   columnas de la tabla
     * @param filas     celdas; cada fila debe tener el mismo tamaño que [headers]
     * @param anchos    peso relativo de cada columna (opcional, se reparte el ancho)
     */
    data class Seccion(
        val titulo: String,
        val headers: List<String>,
        val filas: List<List<String>>,
        val anchos: List<Float>? = null
    )

    /**
     * Genera el PDF y lo guarda en [destino].
     *
     * @return el archivo escrito o `null` si el disco falló.
     */
    fun generar(
        context: Context,
        destino: File,
        titulo: String,
        subtitulo: String,
        generadoPor: String,
        kpis: List<Pair<String, String>> = emptyList(),
        secciones: List<Seccion> = emptyList(),
        pie: String = "Reporte generado desde la aplicación Kaffa Cafeteria"
    ): File? {
        return try {
            destino.parentFile?.mkdirs()
            val doc = PdfDocument()

            val pTitulo = Paint().apply {
                isAntiAlias = true
                color = brandGreen
                textSize = 20f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                isUnderlineText = false
            }
            val pSubtitulo = Paint().apply {
                isAntiAlias = true
                color = midText
                textSize = 10f
                typeface = Typeface.SANS_SERIF
            }
            val pSeccion = Paint().apply {
                isAntiAlias = true
                color = brandGreen
                textSize = 12f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }
            val pHeader = Paint().apply {
                isAntiAlias = true
                color = Color.WHITE
                textSize = 9f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }
            val pCelda = Paint().apply {
                isAntiAlias = true
                color = darkText
                textSize = 9f
                typeface = Typeface.SANS_SERIF
            }
            val pKpiLabel = Paint().apply {
                isAntiAlias = true
                color = midText
                textSize = 8f
                typeface = Typeface.SANS_SERIF
            }
            val pKpiValue = Paint().apply {
                isAntiAlias = true
                color = darkText
                textSize = 13f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }
            val pPie = Paint().apply {
                isAntiAlias = true
                color = midText
                textSize = 7.5f
                typeface = Typeface.SANS_SERIF
            }

            val headerBrush = Paint().apply {
                isAntiAlias = true
                color = brandGreen
                style = Paint.Style.FILL
            }
            val zebraBrush = Paint().apply {
                isAntiAlias = true
                color = lightGray
                style = Paint.Style.FILL
            }
            val dividerPaint = Paint().apply {
                isAntiAlias = true
                color = Color.rgb(0xDD, 0xDD, 0xDD)
                strokeWidth = 0.7f
            }

            var pageNumber = 1
            var page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
            var canvas = page.canvas
            var y = 0f

            // Encabezado en cada página (por si el reporte se parte).
            fun drawHeader() {
                y = MARGIN
                canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 3f, headerBrush)
                y += 14f
                canvas.drawText("KAFFA CAFETERIA", MARGIN, y, pSubtitulo.apply { color = accentTerracotta })
                y += 20f
                canvas.drawText(titulo, MARGIN, y, pTitulo)
                y += 14f
                canvas.drawText(subtitulo, MARGIN, y, pSubtitulo)
                y += 12f
                canvas.drawText(
                    "Generado por $generadoPor · ${dateFormat().format(Date())}",
                    MARGIN, y, pSubtitulo
                )
                y += 18f
            }

            // Pie en cada página.
            fun drawFooter() {
                val yPie = PAGE_HEIGHT - MARGIN + 8f
                canvas.drawLine(MARGIN, yPie - 10f, PAGE_WIDTH - MARGIN, yPie - 10f, dividerPaint)
                canvas.drawText(pie, MARGIN, yPie, pPie)
                val num = "Pagina $pageNumber"
                canvas.drawText(num, PAGE_WIDTH - MARGIN - pPie.measureText(num), yPie, pPie)
            }

            drawHeader()

            // Tarjetas de indicadores (KPIs).
            if (kpis.isNotEmpty()) {
                val topEspacio = y
                val columnas = if (kpis.size >= 4) 4 else kpis.size
                val anchoCaja = (CONTENT_WIDTH - (columnas - 1) * 8f) / columnas
                val filasKpi = (kpis.size + columnas - 1) / columnas
                val altoCaja = 42f

                kpis.forEachIndexed { index, (label, value) ->
                    val fila = index / columnas
                    val col = index % columnas
                    val left = MARGIN + col * (anchoCaja + 8f)
                    val top = topEspacio + fila * (altoCaja + 8f)
                    zebraBrush.color = Color.rgb(0xEF, 0xF5, 0xF1)
                    canvas.drawRoundRect(left, top, left + anchoCaja, top + altoCaja, 6f, 6f, zebraBrush)
                    canvas.drawText(recortar(label, pKpiLabel, anchoCaja - 12f), left + 6f, top + 14f, pKpiLabel)
                    canvas.drawText(recortar(value, pKpiValue, anchoCaja - 12f), left + 6f, top + 30f, pKpiValue)
                }
                y = topEspacio + filasKpi * (altoCaja + 8f) + 8f
            }

            // Secciones con paginación automática.
            secciones.forEach { seccion ->
                if (seccion.headers.isEmpty()) return@forEach

                // Reparte el ancho disponible entre columnas.
                val pesos = seccion.anchos ?: List(seccion.headers.size) { 1f }
                val totalPesos = pesos.sum().takeIf { it > 0f } ?: 1f
                val anchos = pesos.map { (CONTENT_WIDTH / totalPesos) * it }

                fun drawSeccionHeader() {
                    if (y > PAGE_HEIGHT - MARGIN - 120f) {
                        drawFooter()
                        doc.finishPage(page)
                        pageNumber += 1
                        page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
                        canvas = page.canvas
                        drawHeader()
                    }
                    canvas.drawText(seccion.titulo, MARGIN, y, pSeccion)
                    y += 6f
                    headerBrush.color = brandGreen
                    canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 18f, headerBrush)
                    var x = MARGIN
                    seccion.headers.forEachIndexed { i, h ->
                        canvas.drawText(recortar(h, pHeader, anchos[i] - 10f), x + 5f, y + 12f, pHeader)
                        x += anchos[i]
                    }
                    y += 18f
                }

                drawSeccionHeader()

                seccion.filas.forEachIndexed { rowIndex, fila ->
                    // Salto de página antes de escribir la fila.
                    if (y > PAGE_HEIGHT - MARGIN - 20f) {
                        drawFooter()
                        doc.finishPage(page)
                        pageNumber += 1
                        page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
                        canvas = page.canvas
                        drawHeader()
                        // Se repite el encabezado de la tabla en la nueva página.
                        headerBrush.color = brandGreen
                        canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 18f, headerBrush)
                        var xh = MARGIN
                        seccion.headers.forEachIndexed { i, h ->
                            canvas.drawText(recortar(h, pHeader, anchos[i] - 10f), xh + 5f, y + 12f, pHeader)
                            xh += anchos[i]
                        }
                        y += 18f
                    }

                    val altoFila = 16f
                    if (rowIndex % 2 == 1) {
                        zebraBrush.color = lightGray
                        canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + altoFila, zebraBrush)
                    }
                    var x = MARGIN
                    fila.forEachIndexed { i, celda ->
                        val paint = if (i == 0) {
                            pCelda.apply { typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD) }
                        } else {
                            pCelda.apply { typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL) }
                        }
                        canvas.drawText(recortar(celda, paint, anchos[i] - 10f), x + 5f, y + 11f, paint)
                        x += anchos[i]
                    }
                    y += altoFila
                    canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, dividerPaint)
                }
                y += 14f
            }

            if (secciones.isEmpty() && kpis.isEmpty()) {
                canvas.drawText("No hay datos disponibles para el periodo seleccionado.", MARGIN, y + 8f, pSubtitulo)
            }

            drawFooter()
            doc.finishPage(page)

            FileOutputStream(destino).use { out -> doc.writeTo(out) }
            doc.close()
            destino
        } catch (e: Exception) {
            null
        } catch (e: OutOfMemoryError) {
            null
        }
    }

    /** Acorta el texto con "..." para que no invada la columna. */
    private fun recortar(texto: String, paint: Paint, anchoMax: Float): String {
        val limpio = texto.replace('\n', ' ').trim()
        if (limpio.isEmpty()) return ""
        if (paint.measureText(limpio) <= anchoMax) return limpio
        var texto2 = limpio
        while (texto2.isNotEmpty() && paint.measureText("$texto2...") > anchoMax) {
            texto2 = texto2.dropLast(1)
        }
        return if (texto2.isEmpty()) "..." else "$texto2..."
    }

    fun money(valor: Double): String = currency.format(valor)

    fun money(valor: String?): String = valor?.let { texto ->
        runCatching { currency.format(texto.toDouble()) }.getOrDefault(texto)
    } ?: "-"

    fun int(valor: Int): String = String.format(locale, "%,d", valor)

    /** Nombre de archivo seguro y ordenable por fecha. */
    fun nombreArchivo(prefijo: String, extension: String = "pdf"): String =
        "Kaffa_${prefijo}_${fileStamp().format(Date())}.$extension"
}
