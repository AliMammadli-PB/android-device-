package com.deviceid.changer

import android.util.Log
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Logları Node.js servisinə POST edir.
 */
object LogSender {

    private const val TAG = "PND.LogSender"

    fun send(
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
        if (baseUrl.isBlank()) return false
        val urlString = baseUrl.trimEnd('/') + "/api/logs"
        return try {
            val json = JSONObject().apply {
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
}
