package com.example.myapplication.data.ViewModelFactory
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.CacheServerRepo
import com.example.myapplication.data.ViewModels.ArticlesViewModel
import com.example.myapplication.data.ViewModels.HomeViewModel
import com.example.myapplication.data.ViewModels.SavedViewModel
import com.example.myapplication.data.ViewModels.SearchViewModel
class ViewModelFactory(
    private val repository: CacheServerRepo
) : ViewModelProvider.Factory {
    @RequiresApi(Build.VERSION_CODES.O)
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(repository) as T
            }
            modelClass.isAssignableFrom(SearchViewModel::class.java) -> {
                SearchViewModel(repository) as T
            }
            modelClass.isAssignableFrom(SavedViewModel::class.java) -> {
                SavedViewModel(repository) as T
            }
            modelClass.isAssignableFrom(ArticlesViewModel::class.java) -> {
                ArticlesViewModel(repository) as T
            }
            else -> {
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}