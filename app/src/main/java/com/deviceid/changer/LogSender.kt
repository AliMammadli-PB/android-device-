package com.deviceid.changer

import android.util.Log
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Bütün hadisələri Node.js servisinə göndərir: giriş, id dəyişikliyi, reboot sonrası yoxlama.
 */
object LogSender {

    private const val TAG = "PND.LogSender"

    private fun post(baseUrl: String, json: JSONObject): Boolean {
        if (baseUrl.isBlank()) return false
        val urlString = baseUrl.trimEnd('/') + "/api/logs"
        return try {
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            conn.doOutput = true
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            conn.outputStream.use { it.write(json.toString().toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            val ok = code in 200..299
            if (!ok) Log.e(TAG, "Log server cavab: $code")
            ok
        } catch (e: Exception) {
            Log.e(TAG, "Log göndərmə uğursuz", e)
            false
        }
    }

    /** İstifadəçi tətbiqə girdikdə — cari bütün ID-lər göndərilir */
    fun sendAppOpen(
        baseUrl: String,
        appVersion: String,
        androidId: String,
        serialno: String,
        apSerial: String,
        bluetooth: String,
        imei: String,
        rilModel: String
    ): Boolean {
        val json = JSONObject().apply {
            put("event", "app_open")
            put("app_version", appVersion)
            put("android_id", androidId)
            put("serialno", serialno)
            put("ap_serial", apSerial)
            put("bluetooth", bluetooth)
            put("imei", imei)
            put("ril_model", rilModel)
        }
        return post(baseUrl, json)
    }

    /** ID dəyişdirildikdə — köhnə və yeni dəyərlər */
    fun sendIdChange(
        baseUrl: String,
        appVersion: String,
        androidIdOld: String,
        androidIdNew: String,
        serialnoOld: String,
        serialnoNew: String,
        apSerialOld: String,
        apSerialNew: String,
        bluetoothOld: String,
        bluetoothNew: String,
        rilModelOld: String,
        rilModelNew: String,
        imei: String
    ): Boolean {
        val json = JSONObject().apply {
            put("event", "id_change")
            put("app_version", appVersion)
            put("android_id_old", androidIdOld)
            put("android_id_new", androidIdNew)
            put("serialno_old", serialnoOld)
            put("serialno_new", serialnoNew)
            put("ap_serial_old", apSerialOld)
            put("ap_serial_new", apSerialNew)
            put("bluetooth_old", bluetoothOld)
            put("bluetooth_new", bluetoothNew)
            put("ril_model_old", rilModelOld)
            put("ril_model_new", rilModelNew)
            put("imei", imei)
        }
        return post(baseUrl, json)
    }

    /** Reboot sonrası ilk giriş — gözlənilən vs faktiki, hansıların qaldığını yoxlayır */
    fun sendPostRebootVerification(
        baseUrl: String,
        appVersion: String,
        expectedAndroidId: String,
        actualAndroidId: String,
        expectedSerialno: String,
        actualSerialno: String,
        expectedApSerial: String,
        actualApSerial: String,
        expectedBluetooth: String,
        actualBluetooth: String,
        expectedRilModel: String,
        actualRilModel: String
    ): Boolean {
        val json = JSONObject().apply {
            put("event", "post_reboot_verification")
            put("app_version", appVersion)
            put("expected_android_id", expectedAndroidId)
            put("actual_android_id", actualAndroidId)
            put("android_id_persisted", expectedAndroidId == actualAndroidId)
            put("expected_serialno", expectedSerialno)
            put("actual_serialno", actualSerialno)
            put("serialno_persisted", expectedSerialno == actualSerialno)
            put("expected_ap_serial", expectedApSerial)
            put("actual_ap_serial", actualApSerial)
            put("ap_serial_persisted", expectedApSerial == actualApSerial)
            put("expected_bluetooth", expectedBluetooth)
            put("actual_bluetooth", actualBluetooth)
            put("bluetooth_persisted", expectedBluetooth == actualBluetooth)
            put("expected_ril_model", expectedRilModel)
            put("actual_ril_model", actualRilModel)
            put("ril_model_persisted", expectedRilModel == actualRilModel)
        }
        return post(baseUrl, json)
    }
}
