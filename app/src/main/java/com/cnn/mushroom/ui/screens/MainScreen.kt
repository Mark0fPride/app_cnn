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
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
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
import androidx.core.net.toUri
import coil.compose.rememberAsyncImagePainter
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

    val photoState = PhotoState()
    val context = LocalContext.current
    var photoUpload by remember { mutableStateOf(false) }

    val classificationState by viewModel.classificationState.collectAsState()

    var currentPhotoID by remember { mutableStateOf<Long?>(null) }

    val launcherCamera = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        photoState.onPhotoTaken(success, currentPhotoID, viewModel)
    }

    val launcherStorage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val contentResolver = context.contentResolver

            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                photoState.onPhotoUpload(uri, viewModel)

            } catch (e: SecurityException) {
                Log.e("Upload", "Błąd utrwalenia uprawnień dla URI z Galerii: $uri", e)
                photoState.onPhotoUpload(uri, viewModel)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                onNavigateToSettings = { navController.navigate("user_setting") },
                onNavigateToSearch = { navController.navigate("search_content") }
            )
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
                        val id = getPhotoId(context = context, uri = uri)
                        currentPhotoID = id
                        launcherCamera.launch(uri)
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
                    Box(
                        modifier = Modifier
                            .size(300.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(2.dp, Color.Gray, RoundedCornerShape(16.dp))
                    ) {
                        val painter = when (classificationState) {
                            is ClassificationState.Idle -> rememberAsyncImagePainter(R.drawable.logo_background)

                            is ClassificationState.Loading -> rememberAsyncImagePainter(R.drawable.logo_background)

                            is ClassificationState.Success -> {
                                val mushroom = (classificationState as ClassificationState.Success).mushroom
                                if (context.contentResolver.openInputStream(mushroom.imagePath.toUri())?.use { inputStream ->
                                        true
                                    } == true) {
                                    rememberAsyncImagePainter(mushroom.imagePath)
                                } else {
                                    rememberAsyncImagePainter(R.drawable.ic_launcher_foreground)
                                }
                            }
                            is ClassificationState.Error -> rememberAsyncImagePainter(R.drawable.logo_background)
                        }

                        Image(
                            painter = painter,
                            contentDescription = stringResource(id = R.string.photo_preview),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { photoUpload = true }
                        )
                    }

                    Text(
                        text = stringResource(id = R.string.app_name),
                        textAlign = TextAlign.Center,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        when (classificationState) {
                            is ClassificationState.Loading -> {
                                Text(
                                    stringResource(id = R.string.computing),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }

                            is ClassificationState.Success -> {
                                val m = (classificationState as ClassificationState.Success).mushroom
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

                                    InfoRow(
                                        label = stringResource(id = R.string.timestamp),
                                        value = m.timestamp.toString()
                                    )
                                    InfoRow(
                                        label = stringResource(id = R.string.confidence_score),
                                        value = m.confidenceScore?.toString() ?: "Unknown"
                                    )
                                    InfoRow(
                                        label = stringResource(id = R.string.is_edible),
                                        value = m.isEdible?.let {
                                            if (it) stringResource(id = R.string.yes)
                                            else stringResource(id = R.string.no)
                                        } ?: "Unknown"
                                    )
                                }
                            }

                            is ClassificationState.Error -> {
                                Text(
                                    text = (classificationState as ClassificationState.Error).message,
                                    color = Color.Red,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            ClassificationState.Idle -> {
                                Text(
                                    stringResource(id = R.string.take_photo),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
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
                                                val id = getPhotoId(context = context, uri = uri)
                                                currentPhotoID = id
                                                launcherCamera.launch(uri)
                                            }
                                        },
                                        modifier = Modifier.size(56.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = stringResource(id=R.string.take_photo_content),
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
                                            contentDescription = stringResource(id = R.string.upload_photo_content),
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

fun getPhotoId(context: Context, uri: Uri): Long? {
    context.contentResolver.query(
        uri,
        arrayOf(MediaStore.Images.Media._ID),
        null,
        null,
        null
    )?.use { cursor ->
        if (cursor.moveToFirst()) {
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            return cursor.getLong(idColumn)
        }
    }
    return null
}

class PhotoState {

    fun prepareNewPhoto(context: Context): Uri {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "IMG_$timestamp.jpg"

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CNN")
        }

        val uri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )

        return uri ?: throw IOException("Failed to create MediaStore entry for new photo")
    }

    fun onPhotoTaken(success: Boolean, savedId: Long?, viewModel: MushroomViewModel) {

        if (success && savedId != null) {
            val permanentUri = ContentUris.withAppendedId(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                savedId
            )
            viewModel.classifyMushroom(permanentUri)
        }
    }

    fun onPhotoUpload(uri: Uri?, viewModel: MushroomViewModel) {
        if (uri != null) {
            viewModel.classifyMushroom(uri)
        }
    }

}




