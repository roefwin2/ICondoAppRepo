package com.idsolution.icondoapp.feature.rtsp.presenter

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testkmpapp.feature.ssh.domain.CondoSSHRepository
import com.idsolution.icondoapp.core.data.networking.DataError
import com.idsolution.icondoapp.core.data.networking.Result
import com.idsolution.icondoapp.feature.ssh.domain.models.Camera
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CameraState(
    val cameras: List<Camera> = emptyList(),
    val selectedCamera: Camera? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isFullScreen: Boolean = false,
    val siteId: Int = 0,
    val siteName: String = ""
)

class CameraViewModel(
    private val repository: CondoSSHRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {


    private val siteId: Int = savedStateHandle.get<Int>("siteId")?: 0
    private val siteName: String = savedStateHandle.get<String>("siteName") ?: ""

    private val _state = MutableStateFlow(CameraState(
        siteId = siteId,
        siteName = siteName
    ))
    val state: StateFlow<CameraState> = _state.asStateFlow()

    init {
        loadCameras()
    }

    fun loadCameras() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            when (val result = repository.getCameras(siteId)) {
                is Result.Success -> {
                    val cameras = result.data
                    _state.update {
                        it.copy(
                            cameras = cameras,
                            selectedCamera = cameras.firstOrNull(),
                            isLoading = false,
                            error = null
                        )
                    }
                    println("✅ Cameras loaded: ${cameras.size}")
                }

                is Result.Error -> {
                    val errorMessage = when (result.error) {
                        DataError.Network.UNAUTHORIZED -> "Session expirée. Veuillez vous reconnecter."
                        DataError.Network.NO_INTERNET -> "Pas de connexion Internet"
                        DataError.Network.SERVER_ERROR -> "Erreur serveur"
                        else -> "Erreur lors du chargement des caméras"
                    }
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = errorMessage
                        )
                    }
                    println("❌ Error loading cameras: $errorMessage")
                }
            }
        }
    }

    fun selectCamera(camera: Camera) {
        _state.update {
            it.copy(
                selectedCamera = camera,
                isFullScreen = false
            )
        }
        println("📹 Camera selected: ${camera.name}")
    }

    fun toggleFullScreen() {
        _state.update {
            it.copy(isFullScreen = !it.isFullScreen)
        }
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }
}