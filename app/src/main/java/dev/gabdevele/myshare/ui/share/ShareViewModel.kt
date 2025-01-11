package dev.gabdevele.myshare.ui.share

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.gabdevele.myshare.data.UserPreferences
import kotlinx.coroutines.launch

class ShareViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferences = UserPreferences(application)

    private val _selectedItem = mutableStateOf("Github")
    val selectedItem: State<String> = _selectedItem

    private val _shareOpen = mutableStateOf(false)
    val shareOpen: State<Boolean> = _shareOpen

    private val _shareUrl = mutableStateOf("")
    val shareUrl: State<String> = _shareUrl

    fun selectItem(item: String) {
        _selectedItem.value = item
    }

    fun sheetButtonOpen() {
        _shareOpen.value = true
        val itemLowercase = _selectedItem.value.lowercase()

        viewModelScope.launch {
            val account = userPreferences.getAccount(itemLowercase)
            val url = when (_selectedItem.value) {
                "Instagram" -> "https://www.instagram.com/$account"
                "Github" -> "https://github.com/$account"
                "Telegram" -> "https://t.me/$account"
                else -> "Nothing found"
            }
            _shareUrl.value = url
        }


        viewModelScope.launch {
            userPreferences.setNfcSharingEnabled(true)
            userPreferences.updateSharedUrl(_shareUrl.value)
        }


    }

    fun sheetButtonClose() {
        _shareOpen.value = false
        viewModelScope.launch {
            userPreferences.setNfcSharingEnabled(false)
        }
    }


    fun shareLink(context: Context) {
        Log.d("ShareViewModel", "Sharing link")
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareUrl.value)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share link"))
    }


    fun copyLinkToClipboard() {
        Log.d("ShareViewModel", "Copying link to clipboard")
        val context = getApplication<Application>().applicationContext
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Share URL", _shareUrl.value)
        clipboard.setPrimaryClip(clip)
    }

}