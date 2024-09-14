package com.example.species

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.species.presentation.BottomBarScreen
import com.example.species.presentation.components.AnimalScreen
import com.example.species.presentation.components.BirdScreen
import com.example.species.presentation.components.LeafScreen
import com.example.species.presentation.modals.MainScreens
import com.example.species.ui.theme.*
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class MainActivity : ComponentActivity() {

    companion object {
        val screenNumber = mutableStateOf(0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            val systemUiController = rememberSystemUiController()
            val useDarkIcons = MaterialTheme.colors.isLight

            val bgColor = animateColorAsState(
                targetValue = when (screenNumber.value) {
                    0 -> AnimalBG
                    1 -> BirdsBG
                    2 -> LeafBG
                    else -> BlueBG
                }
            )

            systemUiController.setSystemBarsColor(
                color = bgColor.value,
                darkIcons = false
            )

//            BirdScreen(context = this, navController = rememberNavController())

            val navController = rememberNavController()
            Scaffold(
                bottomBar = {
                    BottomBar(navController = navController, bgColor = bgColor)
                }
            ) {
                NavHost(
                    navController = navController,
                    startDestination = MainScreens.Animal.route
                ) {
                    composable(route = MainScreens.Animal.route) {
                        AnimalScreen(navController, this@MainActivity)
                    }
                    composable(route = MainScreens.Bird.route) {
                        BirdScreen(navController = navController, context = this@MainActivity)
                    }
                    composable(route = MainScreens.Leaf.route) {
                        LeafScreen(navController, this@MainActivity)
                    }
                }
            }
        }
    }
}

@Composable
fun BottomBar(navController: NavHostController, bgColor: State<Color>) {
    val screens = listOf(
        BottomBarScreen.Home,
        BottomBarScreen.Sites,
        BottomBarScreen.Info
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    BottomNavigation(backgroundColor = bgColor.value, contentColor = YellowPrimary) {
        screens.forEach { screen ->
            AddItem(
                screen = screen,
                currentDestination = currentDestination,
                navController = navController
            )
        }
    }
}

@Composable
fun RowScope.AddItem(
    screen: BottomBarScreen,
    currentDestination: NavDestination?,
    navController: NavHostController
) {
    BottomNavigationItem(
        label = {
            Text(
                text = screen.title, fontFamily = ComicNeue
            )
        },
        icon = {
            Icon(
                imageVector = screen.icon,
                contentDescription = "Screen Icon"
            )
        },
        selected = currentDestination?.hierarchy?.any {
            it.route == screen.route
        } == true,
        onClick = {
            navController.navigate(screen.route)
        }
    )
}