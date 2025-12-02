package com.idsolution.icondoapp.feature.rtsp.di

import androidx.lifecycle.SavedStateHandle
import com.idsolution.icondoapp.feature.rtsp.presenter.CameraViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val cameraModule = module {
    viewModel { (savedStateHandle: SavedStateHandle) ->
        CameraViewModel(
            repository = get(),
            savedStateHandle = savedStateHandle
        )
    }
}