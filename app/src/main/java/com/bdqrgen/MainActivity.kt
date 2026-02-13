package com.bdqrgen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bdqrgen.ui.screens.ContactScreen
import com.bdqrgen.ui.screens.SavedScreen
import com.bdqrgen.ui.screens.WifiScreen
import com.bdqrgen.ui.screens.WebsiteScreen
import com.bdqrgen.viewmodel.QRViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BDQRGenApp()
        }
    }
}

data class BottomNavItem(
    val title: String,
    val icon: ImageVector
)

@Composable
fun BDQRGenApp() {
    val viewModel: QRViewModel = viewModel()
    
    val navItems = listOf(
        BottomNavItem("Website", Icons.Default.Link),
        BottomNavItem("WiFi", Icons.Default.Wifi),
        BottomNavItem("Contact", Icons.Default.Person),
        BottomNavItem("Saved", Icons.Default.Save)
    )
    
    var selectedIndex by remember { mutableIntStateOf(0) }
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title
                            )
                        },
                        label = { Text(item.title) }
                    )
                }
            }
        }
    ) { paddingValues ->
        when (selectedIndex) {
            0 -> WebsiteScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(paddingValues)
            )
            1 -> WifiScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(paddingValues)
            )
            2 -> ContactScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(paddingValues)
            )
            3 -> SavedScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}
