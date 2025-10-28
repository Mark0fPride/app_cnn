package com.cnn.mushroom.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Scaffold
import androidx.compose.ui.semantics.SemanticsActions.OnClick
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
        Column(modifier = Modifier.padding(innerPadding)) {
            Button(onClick = deleteAllMushroom) {
                Text("Delete All")
            }

            LazyColumn {
                items(mushrooms) {
                    MushroomItem(
                        navController = navCon,
                        mushroom = it,
                        modifier = Modifier.padding(dimensionResource(R.dimen.padding_small))
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
            .clickable {
                navController.navigate("search_content/${mushroom.id}")
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.padding_small)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MushroomIcon(mushroom)
            MushroomInformation(mushroom)
        }
    }
}


@Composable
fun MushroomIcon(
    mushroom: MushroomEntity,
    modifier: Modifier = Modifier
) {

    Log.d("MushroomDebug", "imagePath (raw): ${mushroom.imagePath}")

    val uri = try {
        mushroom.imagePath.toUri()
    } catch (e: Exception) {
        Log.e("MushroomDebug", "Uri parsing error: ${e.message}")
        null
    }
    Log.d("MushroomDebug", "Parsed URI: $uri, scheme=${uri?.scheme}")
    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(LocalContext.current)
            .data(uri)
            .build()
    )
    Image(
        modifier = modifier
            .size(dimensionResource(R.dimen.image_size))
            .padding(dimensionResource(R.dimen.padding_small))
            .clip(MaterialTheme.shapes.small),
        contentScale = ContentScale.Crop,
        painter = painter,
        contentDescription = null
    )
}

@Composable
fun MushroomInformation(mushroom: MushroomEntity, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = mushroom.name,
            style = MaterialTheme.typography.titleLarge, // Changed for better sizing
            modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_small))
        )
        Text(
            text = "Scientific Name",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}


