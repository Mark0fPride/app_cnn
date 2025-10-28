package com.cnn.mushroom.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.cnn.mushroom.MyApplication


@Composable
fun MushroomDetailScreen(
    mushroomId: Int,
) {
    val repository = MyApplication.instance.repository
    val mushroomState = repository.getMushroomById(mushroomId)
        .collectAsState(initial = null)

    val mushroom = mushroomState.value

    if (mushroom != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = rememberImagePainter(mushroom.imagePath),
                contentDescription = mushroom.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Name: ${mushroom.name}", style = MaterialTheme.typography.headlineSmall)
            Text(text = "Timestamp: ${mushroom.timestamp}")
            Text(text = "Confidence Score: ${mushroom.confidenceScore ?: "Unknown"}")
            Text(text = "Is Edible: ${mushroom.isEdible ?: "Unknown"}")
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
