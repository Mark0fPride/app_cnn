package com.cnn.mushroom.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource

import androidx.compose.ui.tooling.preview.Preview
import com.cnn.mushroom.R
import com.cnn.mushroom.data.Mushroom
import androidx.compose.foundation.lazy.items

import com.cnn.mushroom.data.mushrooms

@Composable
fun SearchScreen(modifier: Modifier) {
    MushroomList(mushrooms)
}

@Composable
fun MushroomList(
    mushrooms: List<Mushroom>,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(mushrooms) { mushroom ->
            MushroomItem(mushroom)
        }
    }
}

@Composable
fun MushroomItem(
    mushroom: Mushroom,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.padding_small)),
            verticalAlignment = Alignment.CenterVertically // Aligns items vertically
        ) {
            MushroomIcon(mushroom)
            MushroomInformation(mushroom)
        }
    }
}

@Composable
fun MushroomIcon(
    mushroom: Mushroom,
    modifier: Modifier = Modifier
) {
    Image(
        modifier = modifier
            .size(dimensionResource(R.dimen.image_size))
            .padding(dimensionResource(R.dimen.padding_small))
            .clip(MaterialTheme.shapes.small),
        contentScale = ContentScale.Crop,
        painter = painterResource(mushroom.imageResourceId),
        contentDescription = null
    )
}

@Composable
fun MushroomInformation(mushroom: Mushroom, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(mushroom.name),
            style = MaterialTheme.typography.titleLarge, // Changed for better sizing
            modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_small))
        )
        Text(
            text = "Scientific Name",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}



@Preview(showBackground = true)
@Composable
fun MushroomItemPreview() {
    var mh = Mushroom(R.drawable.logo_background, R.string.app_name)
    MushroomItem(mh)
}