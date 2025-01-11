package dev.gabdevele.myshare.ui.setup.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import dev.gabdevele.myshare.data.UserPreferences
import kotlinx.coroutines.flow.combine

class AccountsViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferences = UserPreferences(application)

    private val _accounts = MutableStateFlow<Map<String, String>>(emptyMap())
    val accounts: StateFlow<Map<String, String>> = _accounts

    init {
        viewModelScope.launch {
            combine(
                userPreferences.myShareFlow,
                userPreferences.instagramFlow,
                userPreferences.githubFlow,
                userPreferences.telegramFlow
            ) { myShare, instagram, github, telegram ->
                mapOf(
                    "MyShare" to (myShare ?: ""),
                    "Instagram" to (instagram ?: ""),
                    "Github" to (github ?: ""),
                    "Telegram" to (telegram ?: "")
                )
            }.collect { updatedAccounts ->
                _accounts.value = updatedAccounts
            }
        }
    }

    fun onAccountChange(platform: String, accountName: String) {
        _accounts.value = _accounts.value.toMutableMap().apply {
            this[platform] = accountName
        }.toMap()
        saveAccounts()
    }

    private fun saveAccounts() {
        viewModelScope.launch {
            val accounts = _accounts.value
            userPreferences.saveAccountsPreferences(
                myShare = accounts["MyShare"] ?: "",
                instagram = accounts["Instagram"] ?: "",
                github = accounts["Github"] ?: "",
                telegram = accounts["Telegram"] ?: ""
            )
        }
    }
}