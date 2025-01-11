package dev.gabdevele.myshare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.gabdevele.myshare.ui.theme.MyShareTheme
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import dev.gabdevele.myshare.data.UserPreferences
import dev.gabdevele.myshare.ui.setup.SetupActivity
import dev.gabdevele.myshare.ui.share.ShareActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val userPreferences = UserPreferences(this)

        lifecycleScope.launch {
            val isSetupFinished = userPreferences.setupFinishedFlow.first() ?: false
            if (!isSetupFinished) {
                startActivity(Intent(this@MainActivity, SetupActivity::class.java))
            } else {
                startActivity(Intent(this@MainActivity, ShareActivity::class.java))
            }
            finish()
        }
    }
}

@Composable
fun TitleImage() {
    Image(
        painter = painterResource(id = R.drawable.myshare_title),
        contentDescription = "MyShare Title",
        modifier = Modifier
            .width(200.dp)
            .height(43.dp)
    )
}
