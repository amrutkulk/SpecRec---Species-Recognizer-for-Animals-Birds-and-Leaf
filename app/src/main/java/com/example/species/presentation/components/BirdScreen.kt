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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Collections
import androidx.compose.material.icons.rounded.Translate
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
import com.example.species.data.modals.Languages
import com.example.species.data.remote.GoogleTranslate
import com.example.species.data.remote.MainRepository
import com.example.species.logic.BirdVM
import com.example.species.logic.PickImageFormGallery
import com.example.species.ml.BirdsModel
import com.example.species.ui.theme.BirdsBG
import com.example.species.ui.theme.BirdsText
import com.example.species.ui.theme.ComicNeue
import com.example.species.ui.theme.YellowPrimary
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.tensorflow.lite.support.image.TensorImage
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class BirdScreen {
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

        val birdInfo = mutableStateOf<com.example.species.data.modals.bird.Data?>(null)

        val birdDataNotFound = mutableStateOf(0)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BirdScreen(navController: NavHostController, context: Context) {

    LaunchedEffect(key1 = 1) { MainActivity.screenNumber.value = 1 }

    lateinit var outputDirectory: File
    lateinit var cameraExecutor: ExecutorService
//    lateinit var translator: FirebaseTranslator

    val translateTo = remember { mutableStateOf<Languages>(Languages.ENG) }
    val translated = remember {
        mutableStateOf(false)
    }
    // on below line we are creating our firebase translate option.
    // on below line we are creating our firebase translate option.
//    val options =
//        FirebaseTranslatorOptions.Builder() // below line we are specifying our source language.
//            .setSourceLanguage(FirebaseTranslateLanguage.EN) // in below line we are displaying our target language.
//            .setTargetLanguage(translateTo.value.lang) // after that we are building our options.
//            .build()

    // below line is to get instance
    // for firebase natural language.
    // below line is to get instance
    // for firebase natural language.
//    translator = FirebaseNaturalLanguage.getInstance().getTranslator(options)


    val translatedTitle = remember {
        mutableStateOf("")
    }

    val translatedContent = remember {
        mutableStateOf("")
    }

    val job = Job()
    val vm = BirdVM(job)

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
            BirdScreen.shouldShowCamera.value = true
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

    if (BirdScreen.refresh.value) {
        BirdScreen.cameraLaunched.value = false
        BirdScreen.refresh.value = false
        BirdScreen.photoLabel = outputGeneratorBird(
            MediaStore.Images.Media.getBitmap(
                context.contentResolver,
                BirdScreen.photoUri.value
            ), context
        )
        if (BirdScreen.photoLabel != "") {
            val repo = MainRepository()
//            println("app_level: ${BirdScreen.photoLabel}")
            vm.getBirdData(context, repo, job, BirdScreen.photoLabel)
            isSheetOpen.value = true
        }
    }

    var expanded by remember { mutableStateOf(false) }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = {
            expanded = false
        }
    ) {

        DropdownMenuItem(onClick = {
            translateTo.value = Languages.ENG
            expanded = false
            translateText(
                translated,
                context,
                translatedTitle,
                translatedContent,
                translateTo
            )
        }) {
            Text("ENGLISH")
        }
        DropdownMenuItem(onClick = {
            translateTo.value = Languages.MAR
            expanded = false
            translateText(
                translated,
                context,
                translatedTitle,
                translatedContent,
                translateTo
            )
        }) {
            Text("MARATHI")
        }
        DropdownMenuItem(onClick = {
            translateTo.value = Languages.HIN
            expanded = false
            translateText(
                translated,
                context,
                translatedTitle,
                translatedContent,
                translateTo
            )
        }) {
            Text("HINDI")
        }
        DropdownMenuItem(onClick = {
            translateTo.value = Languages.TAM
            expanded = false
            translateText(
                translated,
                context,
                translatedTitle,
                translatedContent,
                translateTo
            )
        }) {
            Text("TAMIL")
        }
        DropdownMenuItem(onClick = {
            translateTo.value = Languages.TEL
            expanded = false
            translateText(
                translated,
                context,
                translatedTitle,
                translatedContent,
                translateTo
            )
        }) {
            Text("TELUGU")
        }
        DropdownMenuItem(onClick = {
            translateTo.value = Languages.KAN
            expanded = false
            translateText(
                translated,
                context,
                translatedTitle,
                translatedContent,
                translateTo
            )
        }) {
            Text("KANNADA")
        }
        DropdownMenuItem(onClick = {
            translateTo.value = Languages.BEN
            expanded = false
            translateText(
                translated,
                context,
                translatedTitle,
                translatedContent,
                translateTo
            )
        }) {
            Text("BENGALI")
        }
        DropdownMenuItem(onClick = {
            translateTo.value = Languages.ARB
            expanded = false
            translateText(
                translated,
                context,
                translatedTitle,
                translatedContent,
                translateTo
            )
        }) {
            Text("ARABIC")
        }
        DropdownMenuItem(onClick = {
            translateTo.value = Languages.JAP
            expanded = false
            translateText(
                translated,
                context,
                translatedTitle,
                translatedContent,
                translateTo
            )
        }) {
            Text("JAPANESE")
        }
        DropdownMenuItem(onClick = {
            translateTo.value = Languages.KOR
            expanded = false
            translateText(
                translated,
                context,
                translatedTitle,
                translatedContent,
                translateTo
            )
        }) {
            Text("KOREAN")
        }
        DropdownMenuItem(onClick = {
            translateTo.value = Languages.IND
            expanded = false
            translateText(
                translated,
                context,
                translatedTitle,
                translatedContent,
                translateTo
            )
        }) {
            Text("INDONESIAN")
        }
        DropdownMenuItem(onClick = {
            translateTo.value = Languages.CHI
            expanded = false
            translateText(
                translated,
                context,
                translatedTitle,
                translatedContent,
                translateTo
            )
        }) {
            Text("CHINESE")
        }
        DropdownMenuItem(onClick = {
            translateTo.value = Languages.ESP
            expanded = false
            translateText(
                translated,
                context,
                translatedTitle,
                translatedContent,
                translateTo
            )
        }) {
            Text("SPANISH")
        }
        DropdownMenuItem(onClick = {
            translateTo.value = Languages.RUS
            expanded = false
            translateText(
                translated,
                context,
                translatedTitle,
                translatedContent,
                translateTo
            )
        }) {
            Text("RUSSIAN")
        }
    }


    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            if (BirdScreen.birdInfo.value != null) {

//                LaunchedEffect(key1 = 1) {
                if (!translated.value) {
                    translatedTitle.value = BirdScreen.birdInfo.value?.common.toString()
                    translatedContent.value = BirdScreen.birdInfo.value?.content.toString()
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BirdsText)
                        .padding(start = 10.dp, end = 10.dp)
                        .scrollable(rememberScrollState(), orientation = Orientation.Vertical)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        IconButton(onClick = {
                            if (BirdScreen.photoLabel != "") {
//                                translated.value = true
////                                translateTo.value = Languages.MAR
//                                val options = TranslatorOptions.Builder()
//                                    .setSourceLanguage(TranslateLanguage.ENGLISH)
//                                    .setTargetLanguage(translateTo.value.lang)
//                                    .build()
//                                val translator = Translation.getClient(options)
//                                gTranslate.downloadModal(
//                                    BirdScreen.birdInfo.value?.common.toString(),
//                                    context,
//                                    translator,
//                                    translatedTitle
//                                )
//                                gTranslate.downloadModal(
//                                    BirdScreen.birdInfo.value?.content.toString(),
//                                    context,
//                                    translator,
//                                    translatedContent
//                                )
                                expanded = true
                            }
                        }, modifier = Modifier.padding(5.dp)) {
                            Icon(
                                imageVector = Icons.Rounded.Translate,
                                contentDescription = "",
                                tint = Color.White
                            )
                        }
                        Text(text = "", modifier = Modifier.weight(1f))
                        IconButton(onClick = {
                            coroutineScope.launch {
                                sheetState.collapse()
                                translated.value = false
                            }
                        }, modifier = Modifier.padding(5.dp)) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "",
                                tint = Color.White
                            )
                        }
                    }

                    Image(
                        painter = rememberImagePainter(BirdScreen.birdInfo.value?.image),
                        contentDescription = "bottom sheet image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )

                    if (BirdScreen.photoLabel != "") {
                        Text(
                            text = translatedTitle.value,
                            modifier = Modifier.fillMaxWidth(),
                            color = BirdsBG,
                            fontFamily = ComicNeue,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

//                        val translater = TranslateService()
//                        val res = remember {
//                            mutableStateOf("")
//                        }
//                        translateText(coroutineScope, translater, res)
//                        Log.i("Translation", res.value)
                    }

                    Spacer(modifier = Modifier.height(15.dp))

                    if (BirdScreen.photoLabel != "") {
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            item {
                                Text(
                                    text = translatedContent.value,
                                    modifier = Modifier.fillMaxSize(),
                                    color = BirdsBG,
                                    fontSize = 16.sp,
                                    fontFamily = ComicNeue
                                )
                            }
                        }
                    }
                }
            } else {
                if (BirdScreen.birdDataNotFound.value == 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(BirdsText),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = YellowPrimary)
                    }
                } else if (BirdScreen.birdDataNotFound.value == -1) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(BirdsText),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No data found!!!",
                            fontFamily = ComicNeue,
                            fontSize = 30.sp,
                            color = YellowPrimary
                        )
                    }
                }
            }
        },
        sheetBackgroundColor = Color.White,
        sheetPeekHeight = 0.dp,
        backgroundColor = BirdsBG,
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

            if (BirdScreen.cameraLaunched.value) {
                if (BirdScreen.shouldShowCamera.value) {
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
                    BirdScreen.photoUri.value = selectedImage
                    BirdScreen.photoLabel = outputGeneratorBird(
                        MediaStore.Images.Media.getBitmap(
                            context.contentResolver,
                            selectedImage
                        ), context
                    )
                    galleryButtonClicked.value = false
                }
                if (BirdScreen.photoLabel != "") {
                    val repo = MainRepository()
                    vm.getBirdData(context, repo, job, BirdScreen.photoLabel)
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
                text = if (BirdScreen.photoLabel == "") "GUESS WHO I AM?" else BirdScreen.photoLabel,
                modifier = Modifier
                    .fillMaxWidth(),
                color = BirdsText,
                textAlign = TextAlign.Center,
                fontSize = 30.sp,
                fontFamily = ComicNeue
            )

            if (BirdScreen.shouldShowCamera.value) {
                Image(
                    painter = rememberImagePainter(BirdScreen.photoUri.value),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(2f)
                )
            }

            if (BirdScreen.photoUri.value == null) {
                Image(
                    painter = rememberImagePainter(R.drawable.birds_image),
                    contentDescription = null,
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxWidth()
                        .fillMaxHeight()
                )
            }

//            if (BirdScreen.photoLabel != "") {
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
                        BirdScreen.cameraLaunched.value = true
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

private fun outputGeneratorBird(bitmap: Bitmap, context: Context): String {
    //declaring tensor flow lite model variable
    val birdModel = BirdsModel.newInstance(context)

    //Converting bitmap to tensor flow lite image
    val newBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    val tfimage = TensorImage.fromBitmap(newBitmap)

    //process the image using trained model and sort it in descending order
    val outputs = birdModel.process(tfimage)
        .probabilityAsCategoryList.apply {
            sortByDescending { it.score }
        }
//
//    for (i in outputs) {
//        println(i.label + " " + i.displayName)
//    }

    //getting result having high probability
    val highprobabilityOutput = outputs[0]
    println("Identified Bird: " + highprobabilityOutput.label)

    //setting output text
    Log.i("Tag", "outputGenerator: $highprobabilityOutput")
    return highprobabilityOutput.label + " " + highprobabilityOutput.displayName


}

private fun handleImageCapture(uri: Uri) {
    Log.i("kilo", "Image captured: $uri")
    BirdScreen.shouldShowCamera.value = false
    BirdScreen.photoUri.value = uri
    BirdScreen.shouldShowPhoto.value = true
    BirdScreen.refresh.value = true
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
            BirdScreen.shouldShowCamera.value = true
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

fun translateText(
    translated: MutableState<Boolean>,
    context: Context,
    translatedTitle: MutableState<String>,
    translatedContent: MutableState<String>,
    translateTo: MutableState<Languages>,
) {
    translated.value = true
    val gTranslate = GoogleTranslate()
//                                translateTo.value = Languages.MAR
    val options = TranslatorOptions.Builder()
        .setSourceLanguage(TranslateLanguage.ENGLISH)
        .setTargetLanguage(translateTo.value.lang)
        .build()
    val translator = Translation.getClient(options)
    gTranslate.downloadModal(
        BirdScreen.birdInfo.value?.common.toString(),
        context,
        translator,
        translatedTitle
    )
    gTranslate.downloadModal(
        BirdScreen.birdInfo.value?.content.toString(),
        context,
        translator,
        translatedContent
    )
}
