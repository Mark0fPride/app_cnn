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
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build

import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest


import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.cnn.mushroom.MyApplication
import com.cnn.mushroom.classifyMushroom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onNavigateToSearch: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val activity = LocalView.current.context as Activity
    var showPermanentlyDeniedDialog by remember { mutableStateOf(false) }
    var callPermissionsRequester by remember { mutableStateOf(false) }
    var isCameraPermissionGranted by remember { mutableStateOf(isPermissionGranted(Manifest.permission.CAMERA, activity)) }
    var isStoragePermissionGranted by remember { mutableStateOf(isPermissionGranted(Manifest.permission.READ_MEDIA_IMAGES, activity)) }
    var neededPermission = ""

    val context = LocalContext.current
    val photoState = rememberPhotoState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        photoState.onPhotoTaken(success)
    }

    val launcherStorage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        photoState.onPhotoUpload(uri)
    }

    Scaffold(
        topBar = { TopAppBar() }
    ) { innerPadding ->

        if (showPermanentlyDeniedDialog) {
            OnPermanentlyDeniedDialog(
                missingPermission = neededPermission,
                onDismiss = { showPermanentlyDeniedDialog = false; callPermissionsRequester = false }
            )
        } else if (callPermissionsRequester) {
            PermissionRequester(
                permission = neededPermission,
                activity = activity,
                onPermissionGranted = {
                    callPermissionsRequester = false
                    if (neededPermission == Manifest.permission.CAMERA) {
                        isCameraPermissionGranted = true
                    } else {
                        isStoragePermissionGranted = true
                    }
                },
                onPermissionDenied = { callPermissionsRequester = false },
                onPermanentlyDenied = { showPermanentlyDeniedDialog = true }
            )
        } else {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Podgląd zdjęcia
                    Box(
                        modifier = Modifier
                            .size(300.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(2.dp, Color.Gray, RoundedCornerShape(16.dp))
                    ) {
                        val painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(LocalContext.current)
                                .data(photoState.displayPhoto)
                                .build()
                        )
                        Image(
                            painter = painter,
                            contentDescription = "Photo Preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Text(
                        text = stringResource(id = R.string.app_name),
                        textAlign = TextAlign.Center,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // Przyciski akcji
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = {
                                callPermissionsRequester = !isCameraPermissionGranted
                                if (!isCameraPermissionGranted)
                                    neededPermission = Manifest.permission.CAMERA
                                else {
                                    val uri = photoState.prepareNewPhoto(context)
                                    launcher.launch(uri)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(0.7f)
                        ) { Text("Take Photo") }

                        Button(
                            onClick = {
                                callPermissionsRequester = !isStoragePermissionGranted
                                if (!isStoragePermissionGranted)
                                    neededPermission = Manifest.permission.READ_MEDIA_IMAGES
                                else {
                                    launcherStorage.launch("image/*")
                                }
                            },
                            modifier = Modifier.fillMaxWidth(0.7f)
                        ) { Text("Upload Photo") }

                        Button(
                            onClick = onNavigateToSearch,
                            modifier = Modifier.fillMaxWidth(0.7f)
                        ) { Text("Go to Search") }

                        Button(
                            onClick = onNavigateToSettings,
                            modifier = Modifier.fillMaxWidth(0.7f)
                        ) { Text("Go to Settings") }
                    }
                }
            }
        }
    }
}



fun isPermissionGranted(permission: String, context: Context): Boolean {
    return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}

fun onImageSaved(path: String){
    //send to ML model
    var mushroom = classifyMushroom(path)
    val repository = MyApplication.instance.repository

    CoroutineScope(Dispatchers.IO).launch {
        repository.addMushroom(mushroom)
    }
}


fun onImageUpload(path: String){
    //TO DO
}

class PhotoState {
    var currentPhotoUri: Uri? by mutableStateOf(null)
        private set

    var displayPhoto: Any? by mutableStateOf(R.drawable.logo_background)
        private set

    fun prepareNewPhoto(context: Context): Uri {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "IMG_$timestamp.jpg"

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CNN") // 👈 visible in Gallery
        }

        val uri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )

        currentPhotoUri = uri
        return uri ?: throw IOException("Failed to create MediaStore entry for new photo")
    }


    fun onPhotoTaken(success: Boolean) {
        if (success) {
            displayPhoto = currentPhotoUri
            currentPhotoUri?.toString()?.let { onImageSaved(it) }
        }
        currentPhotoUri = null
    }

    fun onPhotoUpload(uri: Uri?){
        if(uri!=null){
            currentPhotoUri = uri
            displayPhoto = currentPhotoUri
            currentPhotoUri?.toString()?.let { onImageUpload(it) }
        }
        currentPhotoUri = null

    }
}

@Composable
fun rememberPhotoState() = remember { PhotoState() }

//@RequiresApi(Build.VERSION_CODES.TIRAMISU)
//@Preview(showBackground = true)
//@Composable
//fun MainScreenPreview(){
//    CNNTheme {
//        MainScreen(modifier = Modifier)
//    }
//}


