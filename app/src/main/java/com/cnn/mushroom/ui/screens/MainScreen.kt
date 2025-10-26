package com.cnn.mushroom.ui.screens

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cnn.mushroom.ui.theme.CNNTheme
import com.cnn.mushroom.R
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.material3.Button
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val activity = LocalView.current.context as Activity

    var showPermanentlyDeniedDialog by remember { mutableStateOf(false) }
    var callPermissionsRequester by  remember { mutableStateOf(false) }
    var isCameraPermissionGranted by remember { mutableStateOf(isPermissionGranted(Manifest.permission.CAMERA, activity)) }
    var isStoragePermissionGranted by remember { mutableStateOf(isPermissionGranted(Manifest.permission.READ_MEDIA_IMAGES, activity))}
    var neededPermission = ""

    val context = LocalContext.current
    val photoState = rememberPhotoState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        photoState.onPhotoTaken(success)
    }

    Scaffold(
        topBar = {
            TopAppBar()
        }
    ) { innerPadding ->

        if (showPermanentlyDeniedDialog) {
            OnPermanentlyDeniedDialog(
                missingPermission = neededPermission,
                onDismiss = { showPermanentlyDeniedDialog = false; callPermissionsRequester = false }
            )
        }
        else if(callPermissionsRequester){
            PermissionRequester(
                permission = neededPermission,
                activity = activity,
                onPermissionGranted = {
                    callPermissionsRequester = false
                    if(neededPermission == Manifest.permission.CAMERA){
                        isCameraPermissionGranted = true
                    }
                    else{
                        isStoragePermissionGranted = true
                    }
                },
                onPermissionDenied = {
                    callPermissionsRequester = false
                },
                onPermanentlyDenied = {
                    showPermanentlyDeniedDialog = true
                }
            )
        }
        else {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(color = Color(0xFFE0E0E0))
                    .padding(innerPadding),

                ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()

                ) {
                    Spacer(modifier = Modifier.size(16.dp))
                    Box(modifier = Modifier.size(300.dp)) {
                        val painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(LocalContext.current)
                                .data(photoState.displayPhoto)
                                .build()
                        )
                        Image(
                            painter = painter,
                            contentDescription = null,
                            modifier = Modifier.matchParentSize()
                        )
                    }

                    Spacer(modifier = Modifier.size(16.dp))

                    Text(
                        text = stringResource(id = R.string.app_name),
                        textAlign = TextAlign.Center,
                        fontSize = 24.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Button(onClick = {
                        callPermissionsRequester = !isCameraPermissionGranted
                        if(!isCameraPermissionGranted)
                            neededPermission = Manifest.permission.CAMERA
                        else{
                            val uri = photoState.prepareNewPhoto(context)
                            launcher.launch(uri)
                        }
                    }) {
                        Text("Take Photo")
                    }

                    Button(onClick = {
                        callPermissionsRequester = !isStoragePermissionGranted
                        if(!isStoragePermissionGranted)
                            neededPermission = Manifest.permission.READ_MEDIA_IMAGES
                        else{
                            //todo
                        }
                    }) {
                        Text("Upload Photo")
                    }

                }
            }
        }
    }
}

fun createImageFile(context: Context): File {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHss", Locale.getDefault()).format(Date())
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile("IMG_${timestamp}_", ".jpg", storageDir)
}

fun isPermissionGranted(permission: String, context: Context): Boolean {
    return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}

fun onImageSaved(path: String){
   //TO DO
}


class PhotoState {
    var currentPhotoUri: Uri? by mutableStateOf(null)
        private set

    var displayPhoto: Uri? by mutableStateOf(null)
        private set

    fun prepareNewPhoto(context: Context): Uri {
        val photoFile = createImageFile(context)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
        currentPhotoUri = uri
        return uri
    }

    fun onPhotoTaken(success: Boolean) {
        if (success) {
            displayPhoto = currentPhotoUri
            currentPhotoUri?.path?.let { onImageSaved(it) }
        }
        currentPhotoUri = null
    }
}


@Composable
fun rememberPhotoState() = remember { PhotoState() }

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Preview(showBackground = true)
@Composable
fun MainScreenPreview(){
    CNNTheme {
        MainScreen(modifier = Modifier)
    }
}


