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
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Collections
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.example.species.MainActivity
import com.example.species.R
import com.example.species.data.modals.leaf.Data
import com.example.species.data.remote.MainRepository
import com.example.species.logic.LeafVM
import com.example.species.logic.PickImageFormGallery
import com.example.species.ml.LeafTFfinal
import com.example.species.ui.theme.ComicNeue
import com.example.species.ui.theme.LeafBG
import com.example.species.ui.theme.LeafText
import com.example.species.ui.theme.YellowPrimary
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.tensorflow.lite.support.image.TensorImage
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class LeafScreen {
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

        val leafInfo = mutableStateOf<Data?>(null)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LeafScreen(navController: NavHostController, context: Context) {

    LaunchedEffect(key1 = 1) { MainActivity.screenNumber.value = 2 }

    lateinit var outputDirectory: File
    lateinit var cameraExecutor: ExecutorService

    val job = Job()
    val vm = LeafVM(job)

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
            LeafScreen.shouldShowCamera.value = true
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

    if (LeafScreen.refresh.value) {
        LeafScreen.cameraLaunched.value = false
        LeafScreen.refresh.value = false
        LeafScreen.photoLabel = outputGeneratorLeaf(
            MediaStore.Images.Media.getBitmap(
                context.contentResolver,
                LeafScreen.photoUri.value
            ), context
        )
        if (LeafScreen.photoLabel != "") {
            val repo = MainRepository()
            vm.getLeafData(context, repo, job, LeafScreen.photoLabel)
            isSheetOpen.value = true
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
//            if (LeafScreen.photoLabel.lowercase() != LeafScreen.leafInfo.value?.title.toString()
//                    .lowercase()
//            ) {
            if (LeafScreen.leafInfo.value != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(LeafText)
                        .padding(start = 10.dp, end = 10.dp)
                        .scrollable(rememberScrollState(), orientation = Orientation.Vertical)
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
                        painter = rememberImagePainter(LeafScreen.leafInfo.value?.image),
                        contentDescription = "bottom sheet image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )

                    if (LeafScreen.photoLabel != "") {
                        Text(
                            text = LeafScreen.leafInfo.value?.title.toString(),
                            modifier = Modifier.fillMaxWidth(),
                            color = LeafBG,
                            fontFamily = ComicNeue,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(15.dp))

                    if (LeafScreen.photoLabel != "") {
                        Text(
                            text = LeafScreen.leafInfo.value?.content.toString(),
                            modifier = Modifier.weight(1f),
                            color = LeafBG,
                            fontSize = 16.sp,
                            fontFamily = ComicNeue
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(LeafText),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = YellowPrimary)
                }
            }
        },
        sheetBackgroundColor = Color.White,
        sheetPeekHeight = 0.dp,
        backgroundColor = LeafBG,
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

            if (LeafScreen.cameraLaunched.value) {
                if (LeafScreen.shouldShowCamera.value) {
                    CameraView(
                        outputDirectory = outputDirectory,
                        executor = cameraExecutor,
                        onImageCaptured = ::handleImageCapture,
                        onError = { Log.e("kiloleaf", "View error:", it) },
                    )
                }
            }

            var selectedImage by remember {
                mutableStateOf<Uri?>(null)
            }
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri ->
                selectedImage = uri
            }

            val galleryButtonClicked = remember {
                mutableStateOf(false)
            }

            if (galleryButtonClicked.value) {
                PickImageFormGallery(selectedImage, galleryButtonClicked) {
                    launcher.launch("image/png")
                }
                if (selectedImage != null) {
                    LeafScreen.photoUri.value = selectedImage
                    LeafScreen.photoLabel = outputGeneratorLeaf(
                        MediaStore.Images.Media.getBitmap(
                            context.contentResolver,
                            selectedImage
                        ), context
                    )
                    galleryButtonClicked.value = false
                }
                if (LeafScreen.photoLabel != "") {
                    val repo = MainRepository()
                    vm.getLeafData(context, repo, job, LeafScreen.photoLabel)
                    isSheetOpen.value = true
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
                        galleryButtonClicked.value = true
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
                text = "GUESS WHO I AM?",
                modifier = Modifier
                    .fillMaxWidth(),
                color = LeafText,
                textAlign = TextAlign.Center,
                fontSize = 30.sp,
                fontFamily = ComicNeue
            )

            if (LeafScreen.shouldShowCamera.value) {
                Image(
                    painter = rememberImagePainter(LeafScreen.photoUri.value),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(2f)
                )
            }

            if (LeafScreen.photoUri.value == null) {
                Image(
                    painter = rememberImagePainter(R.drawable.leaves_image),
                    contentDescription = null,
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxWidth()
                        .fillMaxHeight()
                )
            }

//            if (LeafScreen.photoLabel != "") {
//                Text(
//                    text = LeafScreen.photoLabel,
//                    modifier = Modifier.weight(1f), color = Color.Black
//                )
//            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(3f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        LeafScreen.cameraLaunched.value = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp, end = 10.dp)
                ) {
                    Icon(
                        painter = rememberImagePainter(R.drawable.circle),
                        contentDescription = "Click Me",
                        tint = Color.White,
                        modifier = Modifier.fillMaxWidth(0.2f)
                    )
                }
            }
        }
    }
}

private fun outputGeneratorLeaf(bitmap: Bitmap, context: Context): String {
    //declaring tensor flow lite model variable
    val leafModel = LeafTFfinal.newInstance(context)

    //Converting bitmap to tensor flow lite image
    val newBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    val tfimage = TensorImage.fromBitmap(newBitmap)

    //process the image using trained model and sort it in descending order
    val outputs = leafModel.process(tfimage)
        .probabilityAsCategoryList.apply {
            sortByDescending { it.score }
        }

//    for (i in outputs) {
//        println(i.label + " " + i.displayName)
//    }

    //getting result having high probability
    val highprobabilityOutput = outputs[0]
    println(highprobabilityOutput.label)

    return highprobabilityOutput.label

    //setting output text
    Log.i("Tag", "outputGenerator: $highprobabilityOutput")

}

private fun handleImageCapture(uri: Uri) {
    Log.i("kilo", "Image captured: $uri")
    LeafScreen.shouldShowCamera.value = false
    LeafScreen.photoUri.value = uri
    LeafScreen.shouldShowPhoto.value = true
    LeafScreen.refresh.value = true
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
            LeafScreen.shouldShowCamera.value = true
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
