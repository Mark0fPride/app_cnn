package com.cnn.mushroom.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider

//@Composable
//fun TakePhoto(onImageSaved: (String) -> Unit) {
//    val context = LocalContext.current
//    var photoUri by remember { mutableStateOf<Uri?>(null) }
//
//    val launcher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.TakePicture()
//    ) { success ->
//        if (success && photoUri != null) {
//            onImageSaved(photoUri!!.path!!)  // Save path to your database
//        }
//    }
//    Button(onClick = {
//        val photoFile = createImageFile(context)
//        photoUri = FileProvider.getUriForFile(
//            context,
//            "${context.packageName}.fileprovider",
//            photoFile
//        )
//        launcher.launch(photoUri!!)
//    }) {
//        Text("Take Photo")
//    }
//}
