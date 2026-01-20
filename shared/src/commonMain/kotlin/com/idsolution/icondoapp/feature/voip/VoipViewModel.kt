package com.idsolution.icondoapp.feature.voip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idsolution.icondoapp.core.data.networking.DataError
import com.idsolution.icondoapp.core.data.networking.Result
import com.idsolution.icondoapp.feature.ssh.domain.models.PhoneBook
import com.idsolution.icondoapp.feature.ssh.domain.usecases.GetPhonebookUseCase
import com.idsolution.icondoapp.feature.voip.domain.CallState
import com.idsolution.icondoapp.feature.voip.domain.VoipAccountState
import com.idsolution.icondoapp.feature.voip.domain.VoipCallInfo
import com.idsolution.icondoapp.feature.voip.domain.VoipService
import com.idsolution.icondoapp.feature.voip.domain.VoipTransportType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for VoIP functionality
 * Uses the multiplatform VoipService interface
 */
class VoipViewModel(
    private val getPhonebookUseCase: GetPhonebookUseCase,
    private val voipService: VoipService
) : ViewModel() {

    // Phonebook state
    private val _phonebookState: MutableStateFlow<Result<List<PhoneBook>, DataError.Network>> =
        MutableStateFlow(Result.Success(emptyList()))
    val phonebookState = _phonebookState.asStateFlow()

    // VoIP call state - directly from service
    val callState: StateFlow<VoipCallInfo> = voipService.callState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VoipCallInfo())

    // VoIP account state - directly from service
    val accountState: StateFlow<VoipAccountState> = voipService.accountState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VoipAccountState())

    init {
        // Initialize VoIP service
        voipService.initialize()
    }

    // ============== Phonebook Functions ==============

    fun getPhonebook() {
        println("VoipViewModel: Loading phonebook")
        viewModelScope.launch {
            val phoneBookResult = getPhonebookUseCase.invoke()
            _phonebookState.update { phoneBookResult }
        }
    }

    // ============== VoIP Functions ==============

    /**
     * Login to VoIP server
     */
    fun login(
        username: String,
        password: String,
        domain: String,
        transportType: VoipTransportType = VoipTransportType.UDP
    ) {
        println("VoipViewModel: Logging in as $username@$domain")
        voipService.login(username, password, domain, transportType)
    }

    /**
     * Logout from VoIP server
     */
    fun logout() {
        println("VoipViewModel: Logging out")
        voipService.logout()
    }

    /**
     * Make an outgoing call
     */
    fun makeCall(sipUri: String) {
        println("VoipViewModel: Making call to $sipUri")
        voipService.makeCall(sipUri)
    }

    /**
     * Answer incoming call
     */
    fun answerCall() {
        println("VoipViewModel: Answering call")
        voipService.answerCall()
    }

    /**
     * Hang up current call
     */
    fun hangUp() {
        println("VoipViewModel: Hanging up")
        voipService.hangUp()
    }

    /**
     * Toggle video on/off
     */
    fun toggleVideo() {
        voipService.toggleVideo()
    }

    /**
     * Switch camera
     */
    fun toggleCamera() {
        voipService.toggleCamera()
    }

    /**
     * Pause/resume call
     */
    fun pauseOrResume() {
        voipService.pauseOrResume()
    }

    /**
     * Toggle mute
     */
    fun toggleMute() {
        voipService.toggleMute()
    }

    /**
     * Toggle speaker
     */
    fun toggleSpeaker() {
        voipService.toggleSpeaker()
    }

    /**
     * Check if currently in a call
     */
    fun isInCall(): Boolean {
        val state = callState.value.state
        return state != CallState.IDLE && state != CallState.END && state != CallState.RELEASED
    }

    /**
     * Check if registered
     */
    fun isRegistered(): Boolean = voipService.isRegistered()

    override fun onCleared() {
        super.onCleared()
        // Don't destroy the service here - it should persist for incoming calls
        println("VoipViewModel: Cleared")
    }
}
