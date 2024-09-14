package com.example.species.logic

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState

@Composable
fun PickImageFormGallery(
    selectedImage: Uri? = null,
    galleryButtonClicked: MutableState<Boolean>,
    onImageClicked: () -> Unit
) {
    onImageClicked()
}