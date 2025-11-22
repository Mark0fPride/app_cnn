package com.cnn.mushroom.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
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
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(id = R.string.settings_title),
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            // --- 1. Opcja Wyświetlania Czasu ---
            SettingSection(
                title = stringResource(id = R.string.display_settings),
                icon = Icons.Outlined.Visibility
            ) {
                DisplayTimestampSetting(
                    displayTimestamp = settings.displayTimestamp,
                    onCheckedChange = { isChecked ->
                        viewModel.updateSettings(settings.copy(displayTimestamp = isChecked))
                    }
                )
            }

            // --- 2. Opcja Formatu Nazwy ---
            SettingSection(
                title = stringResource(id = R.string.name_format_section),
                icon = Icons.Outlined.Title
            ) {
                NameFormatSetting(
                    currentFormat = settings.nameDisplayFormat,
                    onFormatSelected = { format ->
                        viewModel.updateSettings(settings.copy(nameDisplayFormat = format))
                    }
                )
            }

            // --- 3. Opcja Formatu Czasu ---
            SettingSection(
                title = stringResource(id = R.string.time_format_section),
                icon = Icons.Outlined.Schedule
            ) {
                TimeFormatSetting(
                    currentFormat = settings.timeDisplayFormat,
                    onFormatSelected = { format ->
                        viewModel.updateSettings(settings.copy(timeDisplayFormat = format))
                    }
                )
            }
        }
    }
}

@Composable
fun SettingSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            // Header with icon and title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }

            // Content
            content()
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(id = R.string.settings_timestamp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(id = R.string.settings_timestamp_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Switch(
            checked = displayTimestamp,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
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
            text = stringResource(id = R.string.format_name_title),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Text(
            text = stringResource(id = R.string.format_name_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Column(Modifier.selectableGroup()) {
            NameDisplayFormat.entries.forEach { format ->
                val isSelected = format == currentFormat
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .selectable(
                            selected = isSelected,
                            onClick = { onFormatSelected(format) },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = null,
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary,
                            unselectedColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = when (format) {
                            NameDisplayFormat.SCIENTIFIC -> stringResource(id = R.string.format_name_scientific)
                            NameDisplayFormat.NON_SCIENTIFIC -> stringResource(id = R.string.format_name_non_scientific)
                            NameDisplayFormat.BOTH -> stringResource(id = R.string.format_name_both)
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
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
            text = stringResource(id = R.string.format_time_title),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Text(
            text = stringResource(id = R.string.format_time_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Column(Modifier.selectableGroup()) {
            TimeDisplayFormat.entries.forEach { format ->
                val isSelected = format == currentFormat
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .selectable(
                            selected = isSelected,
                            onClick = { onFormatSelected(format) },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = null,
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary,
                            unselectedColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = when (format) {
                            TimeDisplayFormat.MONTH_YEAR -> stringResource(id = R.string.format_time_month_year)
                            TimeDisplayFormat.DAY_MONTH_YEAR -> stringResource(id = R.string.format_time_day_month_year)
                            TimeDisplayFormat.TIME_DAY_MONTH_YEAR -> stringResource(id = R.string.format_time_time_day_month_year)
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}