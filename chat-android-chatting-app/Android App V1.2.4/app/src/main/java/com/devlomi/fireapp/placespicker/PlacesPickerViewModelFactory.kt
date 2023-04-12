package com.devlomi.fireapp.placespicker

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import com.devlomi.fireapp.activities.placespicker.PlacesPickerViewModel

class PlacesPickerViewModelFactory(private val context: Context, private val lifecycleOwner: LifecycleOwner) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {

        return PlacesPickerViewModel(context, lifecycleOwner) as T
    }
}