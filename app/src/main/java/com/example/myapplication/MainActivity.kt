package com.example.myapplication

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.CacheServerRepo
import com.example.myapplication.data.ViewModelFactory.ViewModelFactory
import com.example.myapplication.data.ViewModels.ArticlesViewModel
import com.example.myapplication.data.ViewModels.HomeViewModel
import com.example.myapplication.data.ViewModels.SavedViewModel
import com.example.myapplication.data.ViewModels.SearchViewModel
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val factory = (application as MyApplication).viewModelFactory
                    val homeViewModel: HomeViewModel = viewModel(factory = factory)
                    homeViewModel.getTodayNews()
                    val searchViewModel: SearchViewModel = viewModel(factory = factory)
                    val savedViewModel: SavedViewModel = viewModel(factory = factory)
                    val articlesViewModel: ArticlesViewModel = viewModel(factory = factory)
                    var canChange by remember { mutableStateOf(false) }
                    AnimatedVisibility(

                        visible=!canChange,
                        enter= slideInHorizontally(animationSpec = tween(durationMillis = 500)){fullWidth -> fullWidth},
                        exit = fadeOut(animationSpec = tween(durationMillis = 500))
                    ) {
                        SplashScreen(modifier=Modifier.padding(innerPadding)) { canChange = true }
                    }
                    AnimatedVisibility(
                        visible=canChange,
                        enter= slideInHorizontally(animationSpec = tween(durationMillis = 500)){fullWidth -> fullWidth},
                        exit = slideOutHorizontally(animationSpec = tween(durationMillis = 500)){fullWidth -> -fullWidth}
                    ){
                        App(modifier=Modifier.padding(innerPadding), homeViewModel, searchViewModel, savedViewModel, articlesViewModel)
                    }
                }
            }
        }
    }
}

