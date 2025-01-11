package dev.gabdevele.myshare.ui.setup.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.gabdevele.myshare.R
import dev.gabdevele.myshare.ui.theme.CardNotSelectedColor
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.gabdevele.myshare.ui.setup.viewmodel.PersonalDataViewModel

@Composable
fun PersonalDataPage(viewModel: PersonalDataViewModel = viewModel()) {
    val nameVisible by viewModel.nameVisible.collectAsStateWithLifecycle()
    val genderVisible by viewModel.genderVisible.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Tell us about you",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        AnimatedVisibility(
            visible=nameVisible,
            enter = fadeIn()
        ) {
            NameSection(viewModel)
        }
        Spacer(modifier = Modifier.height(40.dp))

        AnimatedVisibility(
            visible = genderVisible,
            enter = fadeIn()
        ) {
            SexSection(viewModel)
        }
    }
}

@Composable
fun NameSection(viewModel: PersonalDataViewModel) {
    val name by viewModel.name.collectAsStateWithLifecycle()
    var isTouched by remember { mutableStateOf(false) }

    Column {
        Text(
            text = "Your name",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = name,
            onValueChange = {
                if (!isTouched) isTouched = true
                viewModel.onNameChange(it)
            },
            label = { Text("Name") },
            isError = isTouched && name.isBlank(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        if (isTouched && name.isBlank()) {
            Text(
                text = "Name is required",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun SexSection(viewModel: PersonalDataViewModel) {
    val gender by viewModel.gender.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Your sex",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Left
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconSquareCard(
                icon = painterResource(id = R.drawable.baseline_male_24),
                title = "Male",
                isSelected = gender == "Male",
                onClick = { viewModel.onGenderChange("Male") }
            )
            IconSquareCard(
                icon = painterResource(id = R.drawable.baseline_female_24),
                title = "Female",
                isSelected = gender == "Female",
                onClick = { viewModel.onGenderChange("Female") }
            )
        }
        IconRectangleCard(
            icon = painterResource(id = R.drawable.baseline_insert_emoticon_24),
            title = "I prefer not to say",
            isSelected = gender == "Prefer not to say",
            onClick = { viewModel.onGenderChange("Prefer not to say") }
        )
    }
}

@Composable
fun IconSquareCard(icon: Painter, title: String, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected)
        MaterialTheme.colorScheme.primary else
            CardNotSelectedColor
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(32.dp),
        modifier = Modifier
            .padding(8.dp)
            .size(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = title,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun IconRectangleCard(icon: Painter, title: String, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected)
        MaterialTheme.colorScheme.primary else
        CardNotSelectedColor
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .height(64.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}