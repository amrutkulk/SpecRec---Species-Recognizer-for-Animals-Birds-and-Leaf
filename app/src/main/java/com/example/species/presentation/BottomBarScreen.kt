package com.example.species.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.ui.graphics.vector.ImageVector

const val ROOT_ROUTE = "ROOT_ROUTE"
const val PROFILE_ROUTE = "PROFILE_ROUTE"

sealed class BottomBarScreen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home: BottomBarScreen(
        "animal_screen",
        "Animals",
        Icons.Filled.Pets
    )
    object Sites: BottomBarScreen(
        "bird_screen",
        "Birds",
        Icons.Rounded.Pets
    )
    object Info: BottomBarScreen(
        "leaf_screen",
        "Leaves",
        Icons.Filled.Pets
    )
}
