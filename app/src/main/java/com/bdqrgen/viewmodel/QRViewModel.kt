package com.bdqrgen.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bdqrgen.util.ImageHelper
import com.bdqrgen.util.QRCodeGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class QrCodeState(
    val bitmap: Bitmap? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

data class SavedImagesState(
    val images: List<Pair<String, Uri>> = emptyList(),
    val isLoading: Boolean = false
)

class QRViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _websiteQrState = MutableStateFlow(QrCodeState())
    val websiteQrState: StateFlow<QrCodeState> = _websiteQrState.asStateFlow()
    
    private val _wifiQrState = MutableStateFlow(QrCodeState())
    val wifiQrState: StateFlow<QrCodeState> = _wifiQrState.asStateFlow()
    
    private val _contactQrState = MutableStateFlow(QrCodeState())
    val contactQrState: StateFlow<QrCodeState> = _contactQrState.asStateFlow()
    
    private val _savedImagesState = MutableStateFlow(SavedImagesState())
    val savedImagesState: StateFlow<SavedImagesState> = _savedImagesState.asStateFlow()
    
    fun generateWebsiteQR(url: String) {
        if (url.isBlank()) {
            _websiteQrState.value = QrCodeState()
            return
        }
        
        viewModelScope.launch {
            _websiteQrState.value = _websiteQrState.value.copy(isLoading = true)
            
            val bitmap = withContext(Dispatchers.Default) {
                QRCodeGenerator.generateWebsiteQRWithText(url)
            }
            
            _websiteQrState.value = QrCodeState(
                bitmap = bitmap,
                isLoading = false,
                errorMessage = if (bitmap == null) "Failed to generate QR code" else null
            )
        }
    }
    
    fun generateWifiQR(ssid: String, password: String) {
        if (ssid.isBlank() || password.isBlank()) {
            _wifiQrState.value = QrCodeState()
            return
        }
        
        viewModelScope.launch {
            _wifiQrState.value = _wifiQrState.value.copy(isLoading = true)
            
            val bitmap = withContext(Dispatchers.Default) {
                QRCodeGenerator.generateWifiQRWithText(ssid, password)
            }
            
            _wifiQrState.value = QrCodeState(
                bitmap = bitmap,
                isLoading = false,
                errorMessage = if (bitmap == null) "Failed to generate QR code" else null
            )
        }
    }
    
    fun generateContactQR(name: String, phone: String, email: String) {
        if (name.isBlank() || (phone.isBlank() && email.isBlank())) {
            _contactQrState.value = QrCodeState()
            return
        }
        
        viewModelScope.launch {
            _contactQrState.value = _contactQrState.value.copy(isLoading = true)
            
            val bitmap = withContext(Dispatchers.Default) {
                QRCodeGenerator.generateContactQRWithText(name, phone, email)
            }
            
            _contactQrState.value = QrCodeState(
                bitmap = bitmap,
                isLoading = false,
                errorMessage = if (bitmap == null) "Failed to generate QR code" else null
            )
        }
    }
    
    fun saveQRCode(bitmap: Bitmap, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val uri = withContext(Dispatchers.IO) {
                ImageHelper.saveImageToGallery(getApplication(), bitmap)
            }
            
            if (uri != null) {
                onComplete(true, "QR Code saved to gallery!")
            } else {
                onComplete(false, "Failed to save QR code")
            }
        }
    }
    
    fun shareQRCode(bitmap: Bitmap) {
        ImageHelper.shareImage(getApplication(), bitmap)
    }
    
    fun loadSavedImages() {
        viewModelScope.launch {
            _savedImagesState.value = _savedImagesState.value.copy(isLoading = true)
            
            val images = withContext(Dispatchers.IO) {
                ImageHelper.getSavedImages(getApplication())
            }
            
            _savedImagesState.value = SavedImagesState(
                images = images,
                isLoading = false
            )
        }
    }
    
    fun deleteImage(uri: Uri, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) {
                ImageHelper.deleteImage(getApplication(), uri)
            }
            
            if (success) {
                loadSavedImages()
            }
            onComplete(success)
        }
    }
    
    fun saveToCache(context: Context, bitmap: Bitmap): Uri? {
        return try {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            
            val file = File(cachePath, "qrcode_email.png")
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
            
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun clearMessages() {
        _websiteQrState.value = _websiteQrState.value.copy(errorMessage = null, successMessage = null)
        _wifiQrState.value = _wifiQrState.value.copy(errorMessage = null, successMessage = null)
        _contactQrState.value = _contactQrState.value.copy(errorMessage = null, successMessage = null)
    }
}
