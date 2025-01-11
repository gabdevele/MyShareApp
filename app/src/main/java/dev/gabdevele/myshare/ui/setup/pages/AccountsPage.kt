package dev.gabdevele.myshare.ui.setup.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.gabdevele.myshare.R
import dev.gabdevele.myshare.ui.setup.viewmodel.AccountsViewModel

@Composable
fun AccountsPage(viewModel: AccountsViewModel = viewModel()) {
    val socialNetworks = listOf(
        R.drawable.ic_myshare to "MyShare",
        R.drawable.ic_instagram to "Instagram",
        R.drawable.ic_github to "Github",
        R.drawable.ic_telegram to "Telegram"
    )
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Add your accounts",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        socialNetworks.forEach { (iconRes, social) ->
            SocialField(
                icon = painterResource(id = iconRes),
                social = social,
                accountName = accounts[social] ?: "",
                onAccountChange = { viewModel.onAccountChange(social, it) }
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun SocialField(icon: Painter, social: String, accountName: String, onAccountChange: (String) -> Unit) {
    Column {
        Text(
            text = if (social == "MyShare") "Your MyShare username" else "Your $social account",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = accountName,
            onValueChange = onAccountChange,
            label = { Text(if (social == "MyShare") "MyShare username" else "$social account") },
            leadingIcon = { Icon(icon, contentDescription = "$social icon") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )
    }
}