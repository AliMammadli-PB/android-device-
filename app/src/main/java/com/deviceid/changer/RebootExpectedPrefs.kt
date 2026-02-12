package com.deviceid.changer

import android.content.Context
import android.content.SharedPreferences

/**
 * Reboot-dan əvvəl təyin etdiyimiz dəyərləri saxlayır; reboot sonrası yoxlamada istifadə olunur.
 */
object RebootExpectedPrefs {

    private const val PREFS = "pnd_reboot_expected"
    private const val KEY_ANDROID_ID = "expected_android_id"
    private const val KEY_SERIALNO = "expected_serialno"
    private const val KEY_AP_SERIAL = "expected_ap_serial"
    private const val KEY_BLUETOOTH = "expected_bluetooth"
    private const val KEY_RIL_MODEL = "expected_ril_model"

    fun get(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun saveExpected(
        context: Context,
        androidId: String,
        serialno: String,
        apSerial: String,
        bluetooth: String,
        rilModel: String
    ) {
        get(context).edit()
            .putString(KEY_ANDROID_ID, androidId)
            .putString(KEY_SERIALNO, serialno)
            .putString(KEY_AP_SERIAL, apSerial)
            .putString(KEY_BLUETOOTH, bluetooth)
            .putString(KEY_RIL_MODEL, rilModel)
            .apply()
    }

    fun getExpected(context: Context): ExpectedValues? {
        val p = get(context)
        val androidId = p.getString(KEY_ANDROID_ID, null) ?: return null
        return ExpectedValues(
            androidId = androidId,
            serialno = p.getString(KEY_SERIALNO, "") ?: "",
            apSerial = p.getString(KEY_AP_SERIAL, "") ?: "",
            bluetooth = p.getString(KEY_BLUETOOTH, "") ?: "",
            rilModel = p.getString(KEY_RIL_MODEL, "") ?: ""
        )
    }

    fun clear(context: Context) {
        get(context).edit().clear().apply()
    }

    data class ExpectedValues(
        val androidId: String,
        val serialno: String,
        val apSerial: String,
        val bluetooth: String,
        val rilModel: String
    )
}
