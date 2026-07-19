package com.umbratools.umbraqrgen.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream

object ImageHelper {
    
    private const val TAG = "ImageHelper"
    private const val GALLERY_FOLDER = "UmbraQRGen"
    private const val PREFS_NAME = "saved_qr_images"
    private const val KEY_ENTRIES = "image_entries"
    
    fun saveImageToGallery(context: Context, bitmap: Bitmap, fileName: String = "qrcode_${System.currentTimeMillis()}"): Uri? {
        return try {
            val savedUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "$fileName.png")
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/" + GALLERY_FOLDER)
                }
                
                val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let {
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    }
                }
                uri
            } else {
                val directory = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    GALLERY_FOLDER
                )
                if (!directory.exists()) {
                    directory.mkdirs()
                }
                
                val file = File(directory, "$fileName.png")
                FileOutputStream(file).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                }
                
                Uri.fromFile(file)
            }
            
            if (savedUri != null) {
                trackEntry(context, "$fileName.png", savedUri.toString())
            }
            
            savedUri
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save image to gallery", e)
            null
        }
    }
    
    fun shareImage(context: Context, bitmap: Bitmap): Uri? {
        return try {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            
            val file = File(cachePath, "shared_qrcode.png")
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            context.startActivity(Intent.createChooser(shareIntent, "Share QR Code"))
            
            uri
        } catch (e: Exception) {
            Log.e(TAG, "Failed to share image", e)
            null
        }
    }
    
    fun getSavedImages(context: Context): List<Pair<String, Uri>> {
        val entries = getTrackedEntries(context)
        val valid = mutableListOf<Pair<String, Uri>>()
        val updated = mutableListOf<String>()
        
        for (entry in entries) {
            val parts = entry.split("\n", limit = 2)
            if (parts.size != 2) continue
            val name = parts[0]
            val uriString = parts[1]
            val uri = Uri.parse(uriString)
            
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    stream.read()
                }
                valid.add(name to uri)
                updated.add(entry)
            } catch (_: Exception) {
                // URI no longer valid
            }
        }
        
        if (updated.size != entries.size) {
            saveTrackedEntries(context, updated)
        }
        
        return valid
    }
    
    fun deleteImage(context: Context, uri: Uri): Boolean {
        return try {
            val deleted = context.contentResolver.delete(uri, null, null) > 0
            if (deleted) {
                removeTrackedEntry(context, uri.toString())
            }
            deleted
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete image", e)
            false
        }
    }
    
    private fun getPrefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    private fun getTrackedEntries(context: Context): List<String> {
        val json = getPrefs(context).getString(KEY_ENTRIES, null) ?: return emptyList()
        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { array.getString(it) }
        } catch (_: Exception) {
            emptyList()
        }
    }
    
    private fun saveTrackedEntries(context: Context, entries: List<String>) {
        val array = JSONArray()
        entries.forEach { array.put(it) }
        getPrefs(context).edit().putString(KEY_ENTRIES, array.toString()).apply()
    }
    
    private fun trackEntry(context: Context, name: String, uri: String) {
        val current = getTrackedEntries(context).toMutableList()
        current.add(0, "$name\n$uri")
        saveTrackedEntries(context, current)
    }
    
    private fun removeTrackedEntry(context: Context, uri: String) {
        val current = getTrackedEntries(context).toMutableList()
        current.removeAll { it.endsWith("\n$uri") }
        saveTrackedEntries(context, current)
    }
}
