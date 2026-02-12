package com.deviceid.changer

import android.util.Log
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * GitHub Releases API ilə son versiyanı yoxlayır.
 * Repo: https://github.com/AliMammadli-PB/android-device-
 */
object UpdateChecker {

    private const val TAG = "PND.UpdateChecker"
    private const val REPO = "AliMammadli-PB"
    private const val REPO_NAME = "android-device-"
    private const val API_URL = "https://api.github.com/repos/$REPO/$REPO_NAME/releases/latest"

    data class ReleaseInfo(
        val versionName: String,
        val downloadUrl: String,
        val tagName: String
    )

    /**
     * Mövcud versiya (məs. "1.0") yenisindən kiçikdirsə true.
     */
    fun isNewerVersion(current: String, latest: String): Boolean {
        val cur = parseVersion(current)
        val lat = parseVersion(latest)
        for (i in 0 until maxOf(cur.size, lat.size)) {
            val c = cur.getOrElse(i) { 0 }
            val l = lat.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }

    private fun parseVersion(s: String): List<Int> {
        return s.replace(Regex("[^0-9.]"), "").split(".")
            .mapNotNull { it.toIntOrNull() }
    }

    /**
     * GitHub API-dan son release məlumatını alır.
     * Tag adı "v1.0.0" formatında olmalıdır; APK asset kimi əlavə edilməlidir.
     */
    fun fetchLatestRelease(): ReleaseInfo? {
        return try {
            val url = URL(API_URL)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json")
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            conn.inputStream.bufferedReader().use { reader ->
                val json = reader.readText()
                val obj = JSONObject(json)
                val tagName = obj.optString("tag_name", "").trim().removePrefix("v")
                val assets = obj.optJSONArray("assets") ?: return null
                if (assets.length() == 0) {
                    Log.w(TAG, "Release-də APK asset yoxdur")
                    return null
                }
                val first = assets.getJSONObject(0)
                val downloadUrl = first.optString("browser_download_url", "")
                if (downloadUrl.isEmpty()) return null
                ReleaseInfo(
                    versionName = tagName,
                    downloadUrl = downloadUrl,
                    tagName = tagName
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Release yoxlanılmadı", e)
            null
        }
    }
}
