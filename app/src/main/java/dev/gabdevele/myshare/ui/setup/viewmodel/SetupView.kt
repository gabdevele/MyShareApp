package dev.gabdevele.myshare.ui.setup.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.gabdevele.myshare.data.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SetupView(application: Application) : AndroidViewModel(application) {

    private val userPreferences = UserPreferences(application)

    private val _setupFinished = MutableStateFlow(false)
    val setupFinished = _setupFinished.asStateFlow()


    fun onFinishButtonClicked() {
        viewModelScope.launch {
            userPreferences.setSetupFinished(true)
            _setupFinished.value = true
        }
    }
}
