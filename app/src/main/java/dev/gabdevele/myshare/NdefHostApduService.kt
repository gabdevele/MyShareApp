package dev.gabdevele.myshare

import android.content.Context
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.cardemulation.HostApduService
import android.os.Build
import android.os.Bundle
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.VibratorManager
import dev.gabdevele.myshare.data.UserPreferences
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class NdefHostApduService : HostApduService() {

    private lateinit var mNdefRecordFile: ByteArray
    private var mAppSelected = false
    private var mCcSelected = false
    private var mNdefSelected = false

    private lateinit var userPreferences: UserPreferences
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _nfcEnabledFlow = MutableStateFlow(true)
    private val _sharedUrlFlow = MutableStateFlow("")

    override fun onCreate() {
        super.onCreate()
        userPreferences = UserPreferences(this)

        serviceScope.launch {
            launch {
                userPreferences.nfcEnabledFlow.collect { enabled ->
                    _nfcEnabledFlow.value = enabled
                }
            }
            launch {
                userPreferences.sharedUrlFlow.collect { url ->
                    _sharedUrlFlow.value = url ?: ""
                    updateNdefMessage(_sharedUrlFlow.value)
                }
            }
        }
    }

    private fun updateNdefMessage(url: String) {
        val truncatedUrl = if (url.length > 246) url.substring(0, 246) else url
        val ndefMessage = getNdefUrlMessage(truncatedUrl)

        ndefMessage?.let {
            val nlen = it.byteArrayLength
            mNdefRecordFile = ByteArray(nlen + 2)
            mNdefRecordFile[0] = ((nlen and 0xff00) / 256).toByte()
            mNdefRecordFile[1] = (nlen and 0xff).toByte()
            System.arraycopy(it.toByteArray(), 0, mNdefRecordFile, 2, it.byteArrayLength)
        }
    }

    private fun getNdefUrlMessage(url: String): NdefMessage? {
        return if (url.isEmpty()) null else NdefMessage(NdefRecord.createUri(url))
    }

    private fun vibrateToNotifySuccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.vibrate(
                CombinedVibration.createParallel(
                    VibrationEffect.createWaveform(longArrayOf(0, 200, 150, 50, 100, 50), intArrayOf(0, 255, 0, 255, 0, 255), -1)
                )
            )
        }
    }

    override fun processCommandApdu(commandApdu: ByteArray, extras: Bundle?): ByteArray {
        if (!_nfcEnabledFlow.value) {
            return FAILURE_SW
        }

        return when {
            SELECT_APP.contentEquals(commandApdu) -> {
                mAppSelected = true; mCcSelected = false; mNdefSelected = false
                SUCCESS_SW
            }
            mAppSelected && SELECT_CC_FILE.contentEquals(commandApdu) -> {
                mCcSelected = true; mNdefSelected = false
                SUCCESS_SW
            }
            mAppSelected && SELECT_NDEF_FILE.contentEquals(commandApdu) -> {
                mCcSelected = false; mNdefSelected = true
                SUCCESS_SW
            }
            commandApdu[0] == 0x00.toByte() && commandApdu[1] == 0xb0.toByte() -> {
                handleReadBinary(commandApdu)
            }
            else -> FAILURE_SW
        }
    }

    private fun handleReadBinary(commandApdu: ByteArray): ByteArray {
        val offset = (0x00ff and commandApdu[2].toInt()) * 256 + (0x00ff and commandApdu[3].toInt())
        val le = 0x00ff and commandApdu[4].toInt()
        val responseApdu = ByteArray(le + SUCCESS_SW.size)

        return when {
            mCcSelected && offset == 0 && le == CC_FILE.size -> {
                System.arraycopy(CC_FILE, offset, responseApdu, 0, le)
                System.arraycopy(SUCCESS_SW, 0, responseApdu, le, SUCCESS_SW.size)
                vibrateToNotifySuccess()
                responseApdu
            }
            mNdefSelected && offset + le <= mNdefRecordFile.size -> {
                System.arraycopy(mNdefRecordFile, offset, responseApdu, 0, le)
                System.arraycopy(SUCCESS_SW, 0, responseApdu, le, SUCCESS_SW.size)
                vibrateToNotifySuccess()
                responseApdu
            }
            else -> FAILURE_SW
        }
    }

    override fun onDeactivated(reason: Int) {
        mAppSelected = false
        mCcSelected = false
        mNdefSelected = false
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        private val SELECT_APP = byteArrayOf(0x00, 0xa4.toByte(), 0x04, 0x00, 0x07, 0xd2.toByte(), 0x76.toByte(), 0x00, 0x00, 0x85.toByte(), 0x01, 0x01, 0x00)
        private val SELECT_CC_FILE = byteArrayOf(0x00, 0xa4.toByte(), 0x00, 0x0c, 0x02, 0xe1.toByte(), 0x03)
        private val SELECT_NDEF_FILE = byteArrayOf(0x00, 0xa4.toByte(), 0x00, 0x0c, 0x02, 0xe1.toByte(), 0x04)

        private val SUCCESS_SW = byteArrayOf(0x90.toByte(), 0x00)
        private val FAILURE_SW = byteArrayOf(0x6a.toByte(), 0x82.toByte())

        private val CC_FILE = byteArrayOf(0x00, 0x0f, 0x20, 0x00, 0x3b, 0x00, 0x34, 0x04, 0x06, 0xe1.toByte(), 0x04, 0x00.toByte(), 0xff.toByte(), 0x00, 0xff.toByte())
    }
}


