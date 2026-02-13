package com.bdqrgen.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.bdqrgen.ui.components.QRCodeImage
import com.bdqrgen.ui.components.QRActionButtons
import com.bdqrgen.ui.components.WatermarkBackground
import com.bdqrgen.util.QRCodeGenerator
import com.bdqrgen.viewmodel.QRViewModel

@Composable
fun ContactScreen(
    viewModel: QRViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.contactQrState.collectAsState()
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    
    val hasRequiredFields = name.isNotBlank() && (email.isNotBlank() || phone.isNotBlank())
    
    var hasContactsPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasContactsPermission = isGranted
    }
    
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        result.data?.data?.let { contactUri ->
            var contactName = ""
            var contactEmail = ""
            var contactPhone = ""
            
            context.contentResolver.query(contactUri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
                    if (nameIndex >= 0) {
                        contactName = cursor.getString(nameIndex) ?: ""
                    }
                }
            }
            
            if (contactName.isNotEmpty()) {
                val phoneUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                val phoneSelection = "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?"
                val phoneSelectionArgs = arrayOf(contactUri.lastPathSegment)
                
                context.contentResolver.query(
                    phoneUri,
                    arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                    phoneSelection,
                    phoneSelectionArgs,
                    null
                )?.use { phoneCursor ->
                    if (phoneCursor.moveToFirst()) {
                        val phoneIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        if (phoneIndex >= 0) {
                            contactPhone = phoneCursor.getString(phoneIndex) ?: ""
                        }
                    }
                }
                
                val emailUri = ContactsContract.CommonDataKinds.Email.CONTENT_URI
                val emailSelection = "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?"
                
                context.contentResolver.query(
                    emailUri,
                    arrayOf(ContactsContract.CommonDataKinds.Email.ADDRESS),
                    emailSelection,
                    phoneSelectionArgs,
                    null
                )?.use { emailCursor ->
                    if (emailCursor.moveToFirst()) {
                        val emailIndex = emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)
                        if (emailIndex >= 0) {
                            contactEmail = emailCursor.getString(emailIndex) ?: ""
                        }
                    }
                }
            }
            
            name = contactName
            email = contactEmail
            phone = contactPhone
        }
    }
    
    LaunchedEffect(name, email, phone) {
        if (hasRequiredFields) {
            viewModel.generateContactQR(name, phone, email)
        }
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
        Text(
            text = "Contact QR Code Generator",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Button(
            onClick = {
                if (hasContactsPermission) {
                    val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
                    contactPickerLauncher.launch(intent)
                } else {
                    permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        ) {
            Icon(
                imageVector = Icons.Default.ContactPage,
                contentDescription = "Select Contact"
            )
            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
            Text(if (hasContactsPermission) "Select from Contacts" else "Grant Permission to Select Contact")
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                capitalization = KeyboardCapitalization.None
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done
            )
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (hasRequiredFields) {
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
                    text = "Name: $name",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp)
                )
                if (email.isNotBlank()) {
                    Text(
                        text = "Email: $email",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (phone.isNotBlank()) {
                    Text(
                        text = "Phone: $phone",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                QRActionButtons(
                    onSave = {
                        state.bitmap?.let { bitmap ->
                            viewModel.saveQRCode(bitmap) { success, message ->
                                viewModel.clearMessages()
                            }
                        }
                    },
                    onShare = {
                        state.bitmap?.let { bitmap ->
                            viewModel.shareQRCode(bitmap)
                        }
                    },
                    onEmail = {
                        val vCard = QRCodeGenerator.generateVCardString(name, phone, email)
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:?subject=Contact QR Code&body=${Uri.encode(vCard)}")
                        }
                        context.startActivity(intent)
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
                    text = "Enter contact details to generate a QR code\n(Name and either email or phone required)",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(20.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(60.dp))
        
        SnackbarHost(hostState = snackbarHostState)
    }
    }
}
