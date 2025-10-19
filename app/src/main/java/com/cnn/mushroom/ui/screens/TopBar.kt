package com.cnn.mushroom.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cnn.mushroom.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(modifier: Modifier = Modifier) {
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
                    textAlign = TextAlign.Center
                )
            }
        },
        modifier = modifier
    )
}


@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarPreview(){
    TopAppBar()
}