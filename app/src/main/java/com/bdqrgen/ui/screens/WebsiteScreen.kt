package com.bdqrgen.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.bdqrgen.ui.components.QRCodeImage
import com.bdqrgen.ui.components.QRActionButtons
import com.bdqrgen.ui.components.WatermarkBackground
import com.bdqrgen.viewmodel.QRViewModel

@Composable
fun WebsiteScreen(
    viewModel: QRViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.websiteQrState.collectAsState()
    val context = LocalContext.current
    var url by remember { mutableStateOf("https://authorbdmurphy.com") }
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(url) {
        viewModel.generateWebsiteQR(url)
    }
    
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        WatermarkBackground()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            label = { Text("Enter website URL") },
            placeholder = { Text("https://example.com") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Done
            )
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (url.isNotBlank()) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                QRCodeImage(
                    bitmap = state.bitmap,
                    isLoading = state.isLoading
                )
            }
            
            if (state.bitmap != null) {
                Text(
                    text = url,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                QRActionButtons(
                    onSave = {
                        state.bitmap?.let { bitmap ->
                            viewModel.saveQRCode(bitmap) { success, message ->
                                viewModel.clearMessages()
                            }
                        }
                    },
                    onEmail = {
                        state.bitmap?.let { bitmap ->
                            val uri = viewModel.saveToCache(context, bitmap)
                            if (uri != null) {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "image/png"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    putExtra(Intent.EXTRA_SUBJECT, "QR Code - BDQRGen")
                                    putExtra(Intent.EXTRA_TEXT, "QR Code for: $url")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Send QR Code"))
                            }
                        }
                    },
                    enabled = state.bitmap != null
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Enter a URL to generate a QR code",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        SnackbarHost(hostState = snackbarHostState)
    }
    }
}
