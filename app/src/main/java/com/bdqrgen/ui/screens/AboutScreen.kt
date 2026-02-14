package com.bdqrgen.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.bdqrgen.AppVersion
import com.bdqrgen.ui.components.WatermarkBackground

@Composable
fun AboutScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Box(modifier = modifier.fillMaxSize()) {
        WatermarkBackground()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Version ${AppVersion.VERSION}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = buildAnnotatedString {
                    append("Developer note:\n")
                    append("I wanted a simple app on my phone to create QR codes. Other apps require an account and a login. Worst, they send information to a server. If you want to make a QR code for your WiFi network and password, you have to share your network credentials with someone else. This app requires NO account, NO login. It does all the creation locally. NO servers.\n\n")
                    append("To put it simply, I don't want your information.\n\n")
                    append("If you have any problems, you can open an issue on GitHub. I do NOT commit to checking every day, and I won't change things that will require servers, logins, etc. ")
                }
            )
            
            TextButton(onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/bdmurphy73/BDQRGen"))
                context.startActivity(intent)
            }) {
                Text("https://github.com/bdmurphy73/BDQRGen")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            TextButton(onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://buymeacoffee.com/bdmurph73i"))
                context.startActivity(intent)
            }) {
                Text("https://buymeacoffee.com/bdmurph73i")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            TextButton(onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://authorbdmurphy.com/"))
                context.startActivity(intent)
            }) {
                Text("Website: https://authorbdmurphy.com/")
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Privacy & Terms of Service",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Privacy Policy",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "This app is designed with your privacy in mind. It does not collect, store, or transmit any personally identifiable information (PII). There are no user accounts, no login requirements, and no hidden tracking analytics.",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Disclaimer of Warranty",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "This software is provided \"as is,\" without warranty of any kind, express or implied, including but not limited to the warranties of merchantability or fitness for a particular purpose. The entire risk as to the quality and performance of the app is with you.",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Limitation of Liability",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "In no event shall the developer be liable for any claim, damages, or other liability, whether in an action of contract, tort, or otherwise, arising from, out of, or in connection with the software or the use or other dealings in the software. You assume all responsibility for the use of the app and any consequences resulting from its misuse.",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}
