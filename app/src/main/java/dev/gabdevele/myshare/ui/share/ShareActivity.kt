package dev.gabdevele.myshare.ui.share

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.lightspark.composeqr.DotShape
import com.lightspark.composeqr.QrCodeColors
import com.lightspark.composeqr.QrCodeView
import dev.gabdevele.myshare.R
import dev.gabdevele.myshare.ui.theme.MyShareTheme
import kotlinx.coroutines.launch

class ShareActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyShareTheme {
                val navController = rememberNavController()
                ShareScreen(navController)
            }
        }
    }
}

@Composable
fun ShareScreen(navController: NavHostController) {
    Scaffold(
        bottomBar = { BottomBar(navController) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            NavigationGraph(navController)
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: @Composable () -> Unit) {
    data object Home : Screen("home", "Home", { Icon(Icons.Filled.Home, contentDescription = "Home") })
    data object Profile : Screen("profile", "Profile", { Icon(Icons.Filled.Person, contentDescription = "Profile") })
}

val bottomScreens = listOf(Screen.Home, Screen.Profile)

@Composable
fun BottomBar(navController: NavHostController) {
    NavigationBar {
        val currentRoute by navController.currentBackStackEntryFlow.collectAsState(initial = navController.currentBackStackEntry)

        bottomScreens.forEach { screen ->
            val isSelected = currentRoute?.destination?.route == screen.route

            NavigationBarItem(
                icon = screen.icon,
                label = { Text(screen.title) },
                selected = isSelected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
fun NavigationGraph(navController: NavHostController) {
    NavHost(navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) { HomeScreen() }
        composable(Screen.Profile.route) { ProfileScreen() }
    }
}


@Composable
fun HomeScreen(viewModel: ShareViewModel = viewModel()) {
    val scale = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.myshare_title),
            contentDescription = "MyShare Title",
            modifier = Modifier.padding(top = 26.dp).size(120.dp, 26.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("developed by @gabdevele", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(36.dp))
        Text(
            "Tap to share\nyour accounts...",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Image(
            painter = painterResource(id = R.drawable.ic_myshare),
            contentDescription = "Circular Image",
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .padding(32.dp)
                .clickable {
                    coroutineScope.launch {
                        scale.animateTo(0.8f, tween(100, easing = LinearOutSlowInEasing))
                        scale.animateTo(1.1f, tween(200, easing = LinearOutSlowInEasing))
                        scale.animateTo(1f, tween(200, easing = LinearOutSlowInEasing))
                    }
                    viewModel.sheetButtonOpen()
                }
                .graphicsLayer(scaleX = scale.value, scaleY = scale.value)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text("Select which account you want to share", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(12.dp))
        SelectableList(viewModel)
        ShareBottomSheet(viewModel)
    }
}

@Composable
fun SelectableList(viewModel: ShareViewModel) {
    val items = listOf("Instagram", "Github", "Telegram")
    val selectedItem by viewModel.selectedItem

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp, 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(items) { item ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable { viewModel.selectItem(item) }.padding(12.dp, 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = item == selectedItem,
                    onClick = { viewModel.selectItem(item) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = item, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareBottomSheet(viewModel: ShareViewModel) {
    val shareOpen by viewModel.shareOpen
    val shareUrl by viewModel.shareUrl
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    if (shareOpen) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { viewModel.sheetButtonClose() },
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                QrCodeView(
                    data = shareUrl,
                    modifier = Modifier.size(190.dp),
                    colors = QrCodeColors(
                        background = MaterialTheme.colorScheme.background,
                        foreground = MaterialTheme.colorScheme.secondary,
                    ),
                    dotShape = DotShape.Circle
                )
                Spacer(modifier = Modifier.height(16.dp))
                SuggestionChip(
                    onClick = { },
                    label = { Text("NFC share is enabled") }
                )
                ButtonRow(viewModel)
            }
        }
    }
}

@Composable
fun ButtonRow(viewModel: ShareViewModel) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        OutlinedButton(
            onClick = { viewModel.shareLink(context) },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
        ) {
            Icon(Icons.Filled.Share, contentDescription = "Share")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Share")
        }
        OutlinedButton(
            onClick = {viewModel.copyLinkToClipboard() },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
        ) {
            Icon(painterResource(R.drawable.baseline_content_copy_24), contentDescription = "Copy", tint = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Copy", color = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
fun ProfileScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Still under development", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
    }
}
