package com.cnn.mushroom.ui.screens

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
        title = { Text("Brak uprawnień") },
        text = {
            Text(
                "Aby korzystać z tej funkcji, przejdź do ustawień i przyznaj wymagane uprawnienia: $missingPermission"
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
                Text(text = "Strona Ustawień")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Zamknij")
            }
        }
    )
}


