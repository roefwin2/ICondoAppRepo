package com.idsolution.icondoapp.feature.rtsp.di

import com.idsolution.icondoapp.feature.rtsp.presenter.CameraViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val cameraModule = module {
    viewModel { (siteId: Int) ->
        CameraViewModel(
            repository = get(),
            siteId = siteId
        )
    }
}