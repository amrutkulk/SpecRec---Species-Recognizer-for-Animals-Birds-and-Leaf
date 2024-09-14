package com.example.species.presentation.components

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Collections
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.example.species.MainActivity
import com.example.species.R
import com.example.species.ml.TFAnimal
import com.example.species.ui.theme.AnimalBG
import com.example.species.ui.theme.AnimalText
import com.example.species.ui.theme.ComicNeue
import kotlinx.coroutines.launch
import org.tensorflow.lite.support.image.TensorImage
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AnimalScreen {
    companion object {
        val shouldShowCamera =
            mutableStateOf(false)

        val shouldShowPhoto =
            mutableStateOf(false)

        var photoUri: MutableState<Uri?> = mutableStateOf(null)

        val cameraLaunched =
            mutableStateOf(false)

        val refresh = mutableStateOf(false)

        var photoLabel = ""
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AnimalScreen(navController: NavHostController, context: Context) {

    LaunchedEffect(key1 = 1) { MainActivity.screenNumber.value = 0 }

    lateinit var outputDirectory: File
    lateinit var cameraExecutor: ExecutorService

    val sheetState = rememberBottomSheetState(
        initialValue = BottomSheetValue.Collapsed
    )
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = sheetState
    )

    val coroutineScope = rememberCoroutineScope()
    val isSheetOpen = remember {
        mutableStateOf(false)
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.i("Status: ", "Permission Granted")
            AnimalScreen.shouldShowCamera.value = true
        } else {
            Log.i("Status: ", "Permission Denied")
        }
    }

    if (isSheetOpen.value) {
        SideEffect {
            coroutineScope.launch {
                sheetState.expand()
            }
        }
    }

    if (AnimalScreen.refresh.value) {
        AnimalScreen.cameraLaunched.value = false
        AnimalScreen.refresh.value = false
        AnimalScreen.photoLabel = outputGeneratorAnimal(
            MediaStore.Images.Media.getBitmap(
                context.contentResolver,
                AnimalScreen.photoUri.value
            ), context
        )
        isSheetOpen.value = true
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AnimalText)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "", modifier = Modifier.weight(1f))
                    IconButton(onClick = {
                        coroutineScope.launch { sheetState.collapse() }
                    }, modifier = Modifier.padding(5.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "",
                            tint = Color.White
                        )
                    }
                }
                Image(
                    painter = rememberImagePainter(AnimalScreen.photoUri.value),
                    contentDescription = "bottom sheet image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(2f)
                )
                Text(
                    text = AnimalScreen.photoLabel,
                    modifier = Modifier.weight(1f), color = Color.White
                )

//                Text(text = AnimalScreen.photoLabel, color = Color.White)
            }
        },
        sheetBackgroundColor = Color.White,
        sheetPeekHeight = 0.dp,
        backgroundColor = AnimalBG,
        sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            val launchCamera = remember {
                mutableStateOf(false)
            }

            if (launchCamera.value) {
                SideEffect {
                    requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }

            requestCameraPermission(context, requestPermissionLauncher, launchCamera)
            outputDirectory = getOutputDirectory(context)
            cameraExecutor = Executors.newSingleThreadExecutor()

            if (AnimalScreen.cameraLaunched.value) {
                if (AnimalScreen.shouldShowCamera.value) {
                    CameraView(
                        outputDirectory = outputDirectory,
                        executor = cameraExecutor,
                        onImageCaptured = ::handleImageCapture,
                        onError = { Log.e("kiloanimal", "View error:", it) },
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Text(text = "", modifier = Modifier.weight(1f))
                IconButton(
                    onClick = {

                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Collections,
                        contentDescription = "Gallery",
                        tint = Color.White
                    )
                }
            }

            Text(
                text = if(AnimalScreen.photoLabel == "") "GUESS WHO I AM?" else AnimalScreen.photoLabel,
                modifier = Modifier
                    .fillMaxWidth(),
                color = AnimalText,
                textAlign = TextAlign.Center,
                fontSize = 30.sp,
                fontFamily = ComicNeue
            )

            if (AnimalScreen.shouldShowCamera.value) {
                Image(
                    painter = rememberImagePainter(AnimalScreen.photoUri.value),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(2f)
                )
            }

            if (AnimalScreen.photoUri.value == null) {
//                Box(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .weight(2f), contentAlignment = Alignment.Center
//                ) {
                Image(
                    painter = rememberImagePainter(R.drawable.animal_image),
                    contentDescription = null,
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxWidth()
                        .fillMaxHeight()
                )
//                }
            }

//            if (AnimalScreen.photoLabel != "") {
//                Text(
//                    text = AnimalScreen.photoLabel,
//                    modifier = Modifier.weight(1f), color = Color.Black
//                )
//            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(3f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = {
                        AnimalScreen.cameraLaunched.value = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp, end = 10.dp)
                        .clip(CircleShape),
                ) {
                    Icon(
                        painter = rememberImagePainter(R.drawable.circle),
                        contentDescription = "Click Me",
                        tint = Color.White,
                        modifier = Modifier.fillMaxWidth(0.2f)
                    )
                }

//                Button(
//                    onClick = {
//
//                    },
//                    modifier = Modifier
//                        .weight(1f)
//                        .padding(start = 10.dp, end = 10.dp)
//                ) {
//                    Text(text = "Gallery")
//                }
            }
        }
    }
}

private fun outputGeneratorAnimal(bitmap: Bitmap, context: Context): String {
    //declaring tensor flow lite model variable
    val animalModel = TFAnimal.newInstance(context)

    //Converting bitmap to tensor flow lite image
    val newBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    val tfimage = TensorImage.fromBitmap(newBitmap)

    //process the image using trained model and sort it in descending order
    val outputs = animalModel.process(tfimage)
        .probabilityAsCategoryList.apply {
            sortByDescending { it.score }
        }

    for (i in outputs) {
        println(i.label + " " + i.displayName)
    }

    //getting result having high probability
    val highprobabilityOutput = outputs[0]
    println(highprobabilityOutput.label)

    return highprobabilityOutput.label

    //setting output text
    Log.i("Tag", "outputGenerator: $highprobabilityOutput")

}

private fun handleImageCapture(uri: Uri) {
    Log.i("kilo", "Image captured: $uri")
    AnimalScreen.shouldShowCamera.value = false
    AnimalScreen.photoUri.value = uri
    AnimalScreen.shouldShowPhoto.value = true
    AnimalScreen.refresh.value = true
}

private fun getOutputDirectory(context: Context): File {
    val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
        File(it, context.resources.getString(R.string.app_name)).apply { mkdirs() }
    }

    return if (mediaDir != null && mediaDir.exists()) mediaDir else context.filesDir
}

private fun ColumnScope.requestCameraPermission(
    context: Context,
    requestPermissionLauncher: ManagedActivityResultLauncher<String, Boolean>,
    launchCamera: MutableState<Boolean>
) {
    when {
        ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED -> {
            Log.i("Status: ", "Permission already granted")
            AnimalScreen.shouldShowCamera.value = true
        }

        ActivityCompat.shouldShowRequestPermissionRationale(
            context as Activity,
            Manifest.permission.CAMERA
        ) -> Log.i("Status: ", "Show camera permissions dialog")
        else ->
            launchCamera.value = true

    }
}

private fun createImageFile(context: Context): File {
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile("temp_image", ".jpg", storageDir)
}
