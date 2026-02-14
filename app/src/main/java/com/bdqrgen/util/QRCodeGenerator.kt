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
        val combinedHeight = QR_SIZE + TEXT_HEIGHT
        val combinedBitmap = Bitmap.createBitmap(QR_SIZE, combinedHeight, Bitmap.Config.RGB_565)
        val canvas = Canvas(combinedBitmap)
        
        canvas.drawColor(Color.WHITE)
        
        canvas.drawBitmap(qrBitmap, 0f, 0f, null)
        
        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 32f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        
        val lines = text.split("\n")
        val lineHeight = TEXT_HEIGHT.toFloat() / lines.size
        
        lines.forEachIndexed { index, line ->
            val y = QR_SIZE + (index + 1) * lineHeight - 10
            canvas.drawText(line, QR_SIZE / 2f, y, paint)
        }
        
        return combinedBitmap
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
