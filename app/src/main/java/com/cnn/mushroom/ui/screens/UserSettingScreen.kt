package com.cnn.mushroom.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cnn.mushroom.R
import com.cnn.mushroom.data.NameDisplayFormat
import com.cnn.mushroom.data.TimeDisplayFormat


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSettingScreen(
    modifier: Modifier = Modifier,
    viewModel: MushroomViewModel = hiltViewModel()
) {
    val settings by viewModel.userSettings.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(id = R.string.settings_title)) })
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // --- 1. Opcja Wyświetlania Czasu ---
            DisplayTimestampSetting(
                displayTimestamp = settings.displayTimestamp,
                onCheckedChange = { isChecked ->
                    viewModel.updateSettings(settings.copy(displayTimestamp = isChecked))
                }
            )
            Divider(Modifier.padding(vertical = 8.dp))

            // --- 2. Opcja Formatu Nazwy ---
            NameFormatSetting(
                currentFormat = settings.nameDisplayFormat,
                onFormatSelected = { format ->
                    viewModel.updateSettings(settings.copy(nameDisplayFormat = format))
                }
            )
            Divider(Modifier.padding(vertical = 8.dp))

            // --- 3. Opcja Formatu Czasu ---
            TimeFormatSetting(
                currentFormat = settings.timeDisplayFormat,
                onFormatSelected = { format ->
                    viewModel.updateSettings(settings.copy(timeDisplayFormat = format))
                }
            )
        }
    }
}


@Composable
fun DisplayTimestampSetting(
    displayTimestamp: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!displayTimestamp) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(stringResource(id = R.string.settings_timestamp), style = MaterialTheme.typography.bodyLarge)
        Switch(
            checked = displayTimestamp,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun NameFormatSetting(
    currentFormat: NameDisplayFormat,
    onFormatSelected: (NameDisplayFormat) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            stringResource(id=R.string.format_name_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Column(Modifier.selectableGroup()) {
            NameDisplayFormat.entries.forEach { format ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .selectable(
                            selected = (format == currentFormat),
                            onClick = { onFormatSelected(format) },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (format == currentFormat),
                        onClick = null // Przechwycone przez kliknięcie Row
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = when (format) {
                            NameDisplayFormat.SCIENTIFIC -> stringResource(id = R.string.format_name_scientific)
                            NameDisplayFormat.NON_SCIENTIFIC -> stringResource(id = R.string.format_name_non_scientific )
                            NameDisplayFormat.BOTH -> stringResource(id = R.string.format_name_both)
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
fun TimeFormatSetting(
    currentFormat: TimeDisplayFormat,
    onFormatSelected: (TimeDisplayFormat) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            stringResource(id = R.string.format_time_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Column(Modifier.selectableGroup()) {
            TimeDisplayFormat.entries.forEach { format ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .selectable(
                            selected = (format == currentFormat),
                            onClick = { onFormatSelected(format) },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (format == currentFormat),
                        onClick = null
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = when (format) {
                            TimeDisplayFormat.MONTH_YEAR -> stringResource(id = R.string.format_time_month_year)
                            TimeDisplayFormat.DAY_MONTH_YEAR -> stringResource(id = R.string.format_time_day_month_year)
                            TimeDisplayFormat.TIME_DAY_MONTH_YEAR -> stringResource(id = R.string.format_time_time_day_month_year)
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}