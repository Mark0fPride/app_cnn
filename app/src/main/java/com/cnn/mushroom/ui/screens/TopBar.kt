package com.cnn.mushroom.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cnn.mushroom.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    onNavigateToSettings: () -> Unit,
    onNavigateToSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.CenterStart)
                        .padding(start = 8.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.logo_background),
                        contentDescription = null,
                        modifier = Modifier.matchParentSize()
                    )
                    Image(
                        painter = painterResource(R.drawable.logo_foreground),
                        contentDescription = null,
                        modifier = Modifier.matchParentSize()
                    )
                }

                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(
                            imageVector = Icons.Default.Search, // ikona wyszukiwania
                            contentDescription = "Search"
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings, // ikona ustawień
                            contentDescription = "Settings"
                        )
                    }
                }
            }
        },
        modifier = modifier
    )
}


//@Preview
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun TopAppBarPreview(){
//    TopAppBar()
//}