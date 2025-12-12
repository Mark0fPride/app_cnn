package com.cnn.mushroom.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.cnn.mushroom.data.MushroomEntity
import com.cnn.mushroom.data.NameDisplayFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SearchScreen(
    navCon: NavController,
    searchViewModel: SearchViewModel = hiltViewModel(),
    viewModel: MushroomViewModel = hiltViewModel(),
    deleteViewModel: DeleteViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val searchState by searchViewModel.searchState.collectAsState()
    val searchQuery by searchViewModel.searchQuery.collectAsState()
    val dateRange by searchViewModel.dateRange.collectAsState()
    val showAllMushrooms by searchViewModel.showAllMushrooms.collectAsState()
    val selectedIds by deleteViewModel.selectedIds.collectAsState()

    var fromDate by remember {
        mutableStateOf<Long?>(dateRange?.start)
    }
    var toDate by remember {
        mutableStateOf<Long?>(dateRange?.endInclusive)
    }

    val openFromDialog = remember { mutableStateOf(false) }
    val openToDialog = remember { mutableStateOf(false) }
    val fromState = rememberDatePickerState()
    val toState = rememberDatePickerState()

    // Date picker dialogs (bez zmian - tak jak w poprzednim kodzie)
    if (openFromDialog.value) {
        DatePickerDialog(
            onDismissRequest = { openFromDialog.value = false },
            confirmButton = {
                TextButton(onClick = {
                    fromState.selectedDateMillis?.let {
                        fromDate = it
                        searchViewModel.setDateRange(it, toDate ?: it)
                    }
                    openFromDialog.value = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { openFromDialog.value = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = fromState)
        }
    }

    if (openToDialog.value) {
        DatePickerDialog(
            onDismissRequest = { openToDialog.value = false },
            confirmButton = {
                TextButton(onClick = {
                    toState.selectedDateMillis?.let {
                        toDate = it
                        searchViewModel.setDateRange(fromDate ?: it, it)
                    }
                    openToDialog.value = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { openToDialog.value = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = toState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                onNavigateToSettings = { navCon.navigate("user_setting") },
                onNavigateToSearch = {}
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // ROW Z PRZYCISKAMI DAT I OPCJĄ WSZYSTKICH GRZYBÓW
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // FROM DATE BUTTON
                Button(
                    onClick = { openFromDialog.value = true },
                    modifier = Modifier.weight(1f),
                    enabled = !showAllMushrooms
                ) {
                    Text(fromDate?.let { formatDate(it) } ?: "From Date")
                }

                // TO DATE BUTTON
                Button(
                    onClick = { openToDialog.value = true },
                    modifier = Modifier.weight(1f),
                    enabled = !showAllMushrooms
                ) {
                    Text(toDate?.let { formatDate(it) } ?: "To Date")
                }
            }

            // PRZYCISK POKAŻ WSZYSTKIE GRZYBY
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Button(
                    onClick = { searchViewModel.toggleShowAllMushrooms() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showAllMushrooms) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(if (showAllMushrooms) "Showing All Mushrooms" else "Show All Mushrooms")
                }
            }

            Spacer(Modifier.height(12.dp))

            // SEARCH BAR
            TextField(
                value = searchQuery,
                onValueChange = { searchViewModel.onSearchQueryChanged(it) },
                placeholder = { Text("Search mushrooms...") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            )

            Spacer(Modifier.height(16.dp))

            // OBSŁUGA RÓŻNYCH STANÓW WYSZUKIWANIA
            when (searchState) {
                is SearchState.Idle -> {
                    // Początkowy stan - możesz wyświetlić placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Start searching...")
                    }
                }
                is SearchState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is SearchState.Success -> {
                    val mushrooms = (searchState as SearchState.Success).mushrooms
                    // MUSHROOM LIST
                    Column(modifier = Modifier.weight(1f)) {
                        MushroomList(
                            mushrooms = mushrooms,
                            onNavigation = { navCon.navigate("search_content/$it") },
                            viewmodel = viewModel,
                            deleteViewModel = deleteViewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                is SearchState.Error -> {
                    val errorMessage = (searchState as SearchState.Error).message
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Error: $errorMessage", color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { searchViewModel.resetSearch() }) {
                                Text("Try Again")
                            }
                        }
                    }
                }
            }

            // DELETE SELECTED BUTTON - FIXED AT BOTTOM
            if (selectedIds.isNotEmpty()) {
                Button(
                    onClick = { deleteViewModel.deleteSelected() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Delete Selected (${selectedIds.size})")
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(timestamp))
}

@Composable
fun MushroomList(
    mushrooms: List<MushroomEntity>,
    onNavigation: (id: Int) -> Unit,
    viewmodel: MushroomViewModel,
    deleteViewModel: DeleteViewModel,
    modifier: Modifier = Modifier
) {
    val selectedIds by deleteViewModel.selectedIds.collectAsState()

    if (mushrooms.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No mushrooms found",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(mushrooms) { mushroom ->
                MushroomItem(
                    mushroom = mushroom,
                    onNavigation = onNavigation,
                    viewModel = viewmodel,
                    isSelected = selectedIds.contains(mushroom.id),
                    onSelectionChange = {
                        deleteViewModel.toggleSelection(mushroom.id)
                    }
                )
            }
        }
    }
}
@Composable
fun MushroomItem(
    mushroom: MushroomEntity,
    onNavigation: (mushroomId: Int) -> Unit,
    viewModel: MushroomViewModel,
    isSelected: Boolean,
    onSelectionChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onNavigation(mushroom.id) },
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MushroomIcon(mushroom)

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically) // USUNIĘTO .align(Alignment.Bottom)
            ) {
                val settings = viewModel.userSettings.collectAsState().value
                val names = getDisplayNames(
                    scientificName = mushroom.name,
                    commonName = viewModel.getCommonName(mushroom.name),
                    format = settings.nameDisplayFormat
                )
                Text(
                    text = names.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = names.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.width(8.dp))

            Checkbox(
                checked = isSelected,
                onCheckedChange = onSelectionChange
            )
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
            .crossfade(true)
            .build()
    )

    Image(
        painter = painter,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(64.dp)
            .clip(MaterialTheme.shapes.small)
    )
}

data class DisplayNames(
    val title: String,
    val subtitle: String
)

fun getDisplayNames(
    scientificName: String,
    commonName: String,
    format: NameDisplayFormat
): DisplayNames {
    return when (format) {
        NameDisplayFormat.SCIENTIFIC -> DisplayNames(
            title = scientificName,
            subtitle = commonName
        )
        NameDisplayFormat.NON_SCIENTIFIC -> DisplayNames(
            title = commonName,
            subtitle = scientificName
        )
        NameDisplayFormat.BOTH -> DisplayNames(
            title = scientificName,
            subtitle = commonName
        )
    }
}