package com.cnn.mushroom.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.cnn.mushroom.R
import com.cnn.mushroom.data.formatMushroomName
import com.cnn.mushroom.data.formatTimestamp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MushroomDetailScreen(
    mushroomId: Int,
    viewModel: MushroomViewModel = hiltViewModel(), // <- Hilt wstrzykuje ViewModel
    onNavigateBack: () -> Unit
) {
    val mushroomState = viewModel.getMushroomById(mushroomId)
        .collectAsState(initial = null)
    val mushroom = mushroomState.value

    val settings by viewModel.userSettings.collectAsState()
    val context = LocalContext.current

    if (mushroom != null) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Log.d("MushroomDetailScreen", "Mushroom image path: ${mushroom.imagePath}")
                    Image(
                        painter = rememberAsyncImagePainter(mushroom.imagePath),
                        contentDescription = mushroom.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val displayName = formatMushroomName(
                        commonName = viewModel.getCommonName(mushroom.name),
                        scientificName = mushroom.name,
                        format = settings.nameDisplayFormat
                    )

                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // 2. Warunkowe wyświetlanie daty/czasu
                    if (settings.displayTimestamp) {
                        val formattedTime = formatTimestamp(
                            timestamp = mushroom.timestamp,
                            format = settings.timeDisplayFormat,
                            context = context
                        )
                        InfoRow(
                            label = stringResource(id = R.string.timestamp),
                            value = formattedTime
                        )
                    }

                    // 3. Pozostałe InfoRow pozostają bez zmian
                    InfoRow(label = stringResource(id = R.string.confidence_score), value = mushroom.confidenceScore?.toString() ?: "Unknown")
                    InfoRow(label = stringResource(id = R.string.is_edible), value = mushroom.isEdible?.let { if (it) stringResource(id = R.string.yes) else stringResource(id = R.string.no) } ?: "Unknown")
                }
            }
            item {
                Button(
                    onClick = {
                        // 3. Wywołujemy usunięcie i nawigację wsteczną
                        viewModel.deleteMushroom(mushroom)
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error // Czerwony kolor ostrzegawczy
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, bottom = 16.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onError
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.delete_mushroom_button_label), // lub wpisz "Usuń" ręcznie jeśli nie masz stringa
                        color = MaterialTheme.colorScheme.onError
                    )
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground // <-- używamy koloru z tematu
        )
    }
}

