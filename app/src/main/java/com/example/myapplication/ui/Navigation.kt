package com.example.myapplication.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myapplication.ui.screens.BasicCalculatorScreen
import com.example.myapplication.ui.screens.FunctionPlotterScreen
import com.example.myapplication.ui.screens.BaseConverterScreen
import com.example.myapplication.ui.screens.GeometryCalculatorScreen
import com.example.myapplication.ui.screens.AIAssistantScreen

sealed class Screen(val route: String) {
    object BasicCalculator : Screen("basic_calculator")
    object FunctionPlotter : Screen("function_plotter")
    object BaseConverter : Screen("base_converter")
    object GeometryCalculator : Screen("geometry_calculator")
    object AIAssistant : Screen("ai_assistant")
}

@Composable
fun AppNavigation(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.BasicCalculator.route,
        modifier = modifier
    ) {
        composable(Screen.BasicCalculator.route) {
            BasicCalculatorScreen()
        }
        composable(Screen.FunctionPlotter.route) {
            FunctionPlotterScreen()
        }
        composable(Screen.BaseConverter.route) {
            BaseConverterScreen()
        }
        composable(Screen.GeometryCalculator.route) {
            GeometryCalculatorScreen()
        }
        composable(Screen.AIAssistant.route) {
            AIAssistantScreen()
        }
    }
} 