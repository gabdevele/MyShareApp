package dev.gabdevele.myshare.ui.setup.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import dev.gabdevele.myshare.data.UserPreferences
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

class PersonalDataViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferences = UserPreferences(application)

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _gender = MutableStateFlow("")
    val gender: StateFlow<String> = _gender.asStateFlow()

    private val _nameVisible = MutableStateFlow(false)
    val nameVisible: StateFlow<Boolean> = _nameVisible.asStateFlow()

    private val _genderVisible = MutableStateFlow(false)
    val genderVisible: StateFlow<Boolean> = _genderVisible.asStateFlow()

    private var saveJob: Job? = null

    init {
        viewModelScope.launch {
            combine(userPreferences.usernameFlow, userPreferences.sexFlow) { savedName, savedGender ->
                savedName to savedGender
            }
                .distinctUntilChanged()
                .collect { (savedName, savedGender) ->
                    _name.value = savedName ?: ""
                    _gender.value = savedGender ?: ""
                }
        }

        viewModelScope.launch {
            _nameVisible.value = true
            delay(50)
            _genderVisible.value = true
        }
    }

    fun onNameChange(newName: String) {
        _name.value = newName
        debounceSavePersonalData()
    }

    fun onGenderChange(newGender: String) {
        _gender.value = newGender
        debounceSavePersonalData()
    }

    private fun debounceSavePersonalData() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(500)
            userPreferences.saveUserInfoPreferences(name.value, gender.value)
        }
    }
}