package com.cnn.mushroom.ui.screens

import com.cnn.mushroom.R
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityCompat
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource


@Composable
fun PermissionRequester(
    permission: String,
    activity: Activity,
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit,
    onPermanentlyDenied: () -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        when {
            granted -> onPermissionGranted()
            !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission) ->
                onPermanentlyDenied()
            else -> onPermissionDenied()
        }
    }

    var launched by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!launched) {
            launcher.launch(permission)
            launched = true
        }
    }
}


@Composable
fun OnPermanentlyDeniedDialog(
    missingPermission: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.permission_denied_title)) },
        text = {
            Text(
                stringResource(
                    R.string.permission_denied_message,
                    missingPermission
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.permission_denied_open_settings))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.permission_denied_close))
            }
        }
    )
}


