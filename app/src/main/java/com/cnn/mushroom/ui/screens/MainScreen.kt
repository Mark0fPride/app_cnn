package com.cnn.mushroom.ui.screens

import android.app.Activity
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat

import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import java.io.IOException
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController




@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MushroomViewModel = hiltViewModel(),
    navController: NavController,
) {
    val activity = LocalView.current.context as Activity
    var showPermanentlyDeniedDialog by remember { mutableStateOf(false) }
    var callPermissionsRequester by remember { mutableStateOf(false) }
    var isCameraPermissionGranted by remember { mutableStateOf(isPermissionGranted(Manifest.permission.CAMERA, activity)) }
    var isStoragePermissionGranted by remember { mutableStateOf(isPermissionGranted(Manifest.permission.READ_MEDIA_IMAGES, activity)) }
    var neededPermission = ""

    val context = LocalContext.current
    val photoState = rememberPhotoState()
    var photoUpload by remember { mutableStateOf(false) }


    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        photoState.onPhotoTaken(success, viewModel)
    }

    val launcherStorage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        photoState.onPhotoUpload(uri, viewModel)
    }

    Scaffold(
        topBar = { TopAppBar(
            onNavigateToSettings = {navController.navigate("user_setting")},
            onNavigateToSearch = {navController.navigate("search_content")})
        }
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
                        val uri = photoState.prepareNewPhoto(context)
                        launcher.launch(uri)
                    } else {
                        isStoragePermissionGranted = true
                        launcherStorage.launch("image/*")
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
                                .clickable{photoUpload = true}
                        )
                    }

                    Text(
                        text = stringResource(id = R.string.app_name),
                        textAlign = TextAlign.Center,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    val isComputing by viewModel.isComputing.collectAsState()
                    val mushroom by viewModel.recentMushroom.collectAsState()

                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

                        if (photoState.displayPhoto != R.drawable.logo_background && isComputing) {
                            Text("Computing...", style = MaterialTheme.typography.bodyLarge)

                        } else if (mushroom != null) {
                            mushroom?.let { m ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = m.name,
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    InfoRow(label = "Timestamp", value = m.timestamp.toString())
                                    InfoRow(
                                        label = "Confidence Score",
                                        value = m.confidenceScore?.toString() ?: "Unknown"
                                    )
                                    InfoRow(
                                        label = "Is Edible",
                                        value = m.isEdible?.let { if (it) "Yes" else "No" } ?: "Unknown"
                                    )
                                }
                            }
                        } else {
                            Text("Take a photo to classify!", style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    if(photoUpload){
                        Dialog(onDismissRequest = { photoUpload = false }) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {

                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,

                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                ) {

                                    IconButton(
                                        onClick = {
                                            callPermissionsRequester = !isCameraPermissionGranted
                                            photoUpload = false
                                            if (!isCameraPermissionGranted)
                                                neededPermission = Manifest.permission.CAMERA
                                            else {
                                                val uri = photoState.prepareNewPhoto(context)
                                                launcher.launch(uri)
                                            }
                                        },
                                        modifier = Modifier.size(56.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = "Take Photo",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }


                                    IconButton(
                                        onClick = {
                                            callPermissionsRequester = !isStoragePermissionGranted
                                            photoUpload = false
                                            if (!isStoragePermissionGranted)
                                                neededPermission = Manifest.permission.READ_MEDIA_IMAGES
                                            else {
                                                launcherStorage.launch("image/*")
                                            }
                                        },
                                        modifier = Modifier.size(56.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FileUpload,
                                            contentDescription = "Upload Photo",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


fun isPermissionGranted(permission: String, context: Context): Boolean {
    return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
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


    fun onPhotoTaken(success: Boolean, viewModel: MushroomViewModel) {
        if (success) {
            displayPhoto = currentPhotoUri
            currentPhotoUri?.toString()?.let { path ->
                viewModel.classifyPhoto(path)
            }
        }
        currentPhotoUri = null
    }


    fun onPhotoUpload(uri: Uri?, viewModel: MushroomViewModel){
        if(uri!=null){
            currentPhotoUri = uri
            displayPhoto = currentPhotoUri
            currentPhotoUri?.toString()?.let { path ->
                viewModel.classifyPhoto(path)
            }
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


