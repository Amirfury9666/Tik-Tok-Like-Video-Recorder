package com.fury.tiktoksample.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fury.tiktoksample.viewmodel.RecorderViewModel

class ViewModelFactory : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T = with(modelClass) {
        when {
            isAssignableFrom(RecorderViewModel::class.java) -> RecorderViewModel()
            else -> error("Invalid ViewModel")
        }
    } as T
}