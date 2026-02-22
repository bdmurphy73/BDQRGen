package com.bdqrgen.util

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.Canvas
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

object QRCodeGenerator {
    
    private const val QR_SIZE = 512
    private const val TEXT_HEIGHT = 80
    
    fun generateQRCode(content: String, size: Int = QR_SIZE): Bitmap? {
        return try {
            val hints = hashMapOf<EncodeHintType, Any>().apply {
                put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
                put(EncodeHintType.MARGIN, 1)
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
            }
            
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)
            
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun generateWebsiteQRWithText(url: String): Bitmap? {
        val qrBitmap = generateQRCode(url) ?: return null
        return addTextBelowQRCode(qrBitmap, url)
    }
    
    fun generateWifiQRWithText(ssid: String, password: String): Bitmap? {
        val wifiString = "WIFI:T:WPA;S:${escapeWifiString(ssid)};P:${escapeWifiString(password)};;"
        val qrBitmap = generateQRCode(wifiString) ?: return null
        val text = "Network: $ssid\nPassword: $password"
        return addTextBelowQRCode(qrBitmap, text)
    }
    
    fun generateContactQRWithText(name: String, phone: String, email: String): Bitmap? {
        val vCardString = generateVCardString(name, phone, email)
        val qrBitmap = generateQRCode(vCardString) ?: return null
        val text = buildString {
            append("Name: $name")
            if (phone.isNotBlank()) append("\nPhone: $phone")
            if (email.isNotBlank()) append("\nEmail: $email")
        }
        return addTextBelowQRCode(qrBitmap, text)
    }
    
    private fun addTextBelowQRCode(qrBitmap: Bitmap, text: String): Bitmap {
        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 28f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
            textAlign = Paint.Align.LEFT
        }
        
        val maxWidth = (QR_SIZE - 40).toFloat()
        val wrappedLines = wrapText(text, paint, maxWidth)
        
        val lineHeight = paint.fontMetrics.bottom - paint.fontMetrics.top + 8
        val textAreaHeight = (wrappedLines.size * lineHeight).toInt() + 40
        
        val combinedHeight = QR_SIZE + textAreaHeight
        val combinedBitmap = Bitmap.createBitmap(QR_SIZE, combinedHeight, Bitmap.Config.RGB_565)
        val canvas = Canvas(combinedBitmap)
        
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(qrBitmap, 0f, 0f, null)
        
        val startY = QR_SIZE + 30f
        wrappedLines.forEachIndexed { index, line ->
            val y = startY + index * lineHeight
            canvas.drawText(line, 20f, y, paint)
        }
        
        return combinedBitmap
    }
    
    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val lines = mutableListOf<String>()
        val inputLines = text.split("\n")
        
        inputLines.forEach { line ->
            if (paint.measureText(line) <= maxWidth) {
                lines.add(line)
            } else {
                val words = line.split(" ")
                var currentLine = StringBuilder()
                
                words.forEach { word ->
                    val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                    if (paint.measureText(testLine) <= maxWidth) {
                        currentLine.append(if (currentLine.isEmpty()) word else " $word")
                    } else {
                        if (currentLine.isNotEmpty()) {
                            lines.add(currentLine.toString())
                        }
                        if (paint.measureText(word) > maxWidth) {
                            var remaining = word
                            while (remaining.isNotEmpty()) {
                                var i = remaining.length
                                while (i > 0 && paint.measureText(remaining.substring(0, i)) > maxWidth) {
                                    i--
                                }
                                if (i == 0) i = 1
                                lines.add(remaining.substring(0, i))
                                remaining = remaining.substring(i)
                            }
                        } else {
                            currentLine = StringBuilder(word)
                        }
                    }
                }
                
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine.toString())
                }
            }
        }
        
        return lines
    }
    
    fun generateWifiString(ssid: String, password: String): String {
        return "WIFI:T:WPA;S:${escapeWifiString(ssid)};P:${escapeWifiString(password)};;"
    }
    
    fun generateVCardString(name: String, phone: String, email: String): String {
        return buildString {
            append("BEGIN:VCARD\n")
            append("VERSION:3.0\n")
            append("FN:$name\n")
            if (phone.isNotBlank()) append("TEL:$phone\n")
            if (email.isNotBlank()) append("EMAIL:$email\n")
            append("END:VCARD")
        }
    }
    
    private fun escapeWifiString(input: String): String {
        return input
            .replace("\\", "\\\\")
            .replace(";", "\\;")
            .replace(",", "\\,")
            .replace(":", "\\:")
            .replace("\"", "\\\"")
    }
}
