package com.cnn.mushroom.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import com.cnn.mushroom.MyApplication
import com.cnn.mushroom.R
import com.cnn.mushroom.data.MushroomEntity
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.ui.semantics.SemanticsActions.OnClick
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun SearchScreen(navCon: NavController, modifier: Modifier) {
    var repository = MyApplication.instance.repository
    val mushrooms by repository.getAllMushrooms().collectAsState(initial = emptyList())

    MushroomList(navCon, mushrooms, {
        CoroutineScope(Dispatchers.IO).launch {
            repository.deleteAllMushrooms()
        }
    }) //flow <MushroomEntity>
}

@Composable
fun MushroomList(
    navCon: NavController,
    mushrooms: List<MushroomEntity>,
    deleteAllMushroom: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { TopAppBar() }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp) // padding dla całej zawartości
        ) {
            Button(
                onClick = deleteAllMushroom,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text("Delete All")
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp) // odstęp między kartami
            ) {
                items(mushrooms) { mushroom ->
                    MushroomItem(
                        navController = navCon,
                        mushroom = mushroom,
                    )
                }
            }
        }
    }
}

@Composable
fun MushroomItem(
    mushroom: MushroomEntity,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate("search_content/${mushroom.id}")
            },
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MushroomIcon(mushroom)

            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = mushroom.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Science Name",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun MushroomIcon(
    mushroom: MushroomEntity,
    modifier: Modifier = Modifier
) {
    val uri = try {
        mushroom.imagePath.toUri()
    } catch (e: Exception) {
        null
    }

    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(LocalContext.current)
            .data(uri)
            .crossfade(true) // płynne wczytywanie obrazków
            .build()
    )

    Image(
        painter = painter,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(72.dp)
            .clip(MaterialTheme.shapes.small)
    )
}



