package com.example.species.presentation.modals

sealed class MainScreens(val route: String){
    object Animal : MainScreens("animal_screen")
    object Bird : MainScreens("bird_screen")
    object Leaf : MainScreens("leaf_screen")
}
