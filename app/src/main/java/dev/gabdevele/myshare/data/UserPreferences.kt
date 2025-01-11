package dev.gabdevele.myshare.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("user_preferences")

class UserPreferences(private val context: Context) {

    companion object {
        //user info
        val NAME_KEY = stringPreferencesKey("name")
        val SEX_KEY = stringPreferencesKey("sex")
        val SETUP_FINISHED_KEY = booleanPreferencesKey("setup_finished")

        //accounts
        val MYSHARE_KEY = stringPreferencesKey("myshare")
        val INSTAGRAM_KEY = stringPreferencesKey("instagram")
        val GITHUB_KEY = stringPreferencesKey("github")
        val TELEGRAM_KEY = stringPreferencesKey("telegram")

        //nfc
        val NFC_ENABLED_KEY = booleanPreferencesKey("nfc_enabled")
        val SHARED_URL_KEY = stringPreferencesKey("shared_url")
    }

    val usernameFlow: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[NAME_KEY] }

    val sexFlow: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[SEX_KEY] }

    val setupFinishedFlow: Flow<Boolean?> = context.dataStore.data
        .map { preferences -> preferences[SETUP_FINISHED_KEY] }

    val myShareFlow: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[MYSHARE_KEY] }

    val instagramFlow: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[INSTAGRAM_KEY] }

    val githubFlow: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[GITHUB_KEY] }

    val telegramFlow: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[TELEGRAM_KEY] }

    val nfcEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[NFC_ENABLED_KEY] ?: true }

    val sharedUrlFlow: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[SHARED_URL_KEY] }

    suspend fun saveUserInfoPreferences(name: String, sex: String) {
        context.dataStore.edit { preferences ->
            preferences[NAME_KEY] = name
            preferences[SEX_KEY] = sex
        }
    }

    suspend fun saveAccountsPreferences(myShare: String, instagram: String, github: String, telegram: String) {
        context.dataStore.edit { preferences ->
            preferences[MYSHARE_KEY] = myShare
            preferences[INSTAGRAM_KEY] = instagram
            preferences[GITHUB_KEY] = github
            preferences[TELEGRAM_KEY] = telegram
        }
    }

    suspend fun setSetupFinished(setupFinished: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SETUP_FINISHED_KEY] = setupFinished
        }
    }


    suspend fun getAccount(accountKey: String): String? {
        return context.dataStore.data.map { preferences ->
            preferences[stringPreferencesKey(accountKey)]
        }.firstOrNull()
    }

    suspend fun setNfcSharingEnabled(isEnabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NFC_ENABLED_KEY] = isEnabled
        }
    }

    suspend fun updateSharedUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[SHARED_URL_KEY] = url
        }
    }

}
