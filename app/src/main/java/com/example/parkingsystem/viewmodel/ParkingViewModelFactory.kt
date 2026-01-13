package com.example.parkingsystem.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory for creating ParkingViewModel with Application parameter
 * 
 * @property application Application context
 */
class ParkingViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ParkingViewModel::class.java)) {
            return ParkingViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
