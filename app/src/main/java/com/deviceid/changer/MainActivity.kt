package com.deviceid.changer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import com.deviceid.changer.databinding.ActivityMainBinding
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val executor = Executors.newSingleThreadExecutor()
    private var pendingRelease: UpdateChecker.ReleaseInfo? = null

    companion object {
        private const val TAG = "PND"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = getString(R.string.app_name)

        binding.textVersion.text = getString(R.string.version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)

        binding.btnRequestRoot.setOnClickListener { requestRootPermission() }
        binding.btnChangeAll.setOnClickListener { changeAllIdentifiers() }
        binding.btnDownloadUpdate.setOnClickListener { pendingRelease?.let { downloadAndInstall(it) } }

        refreshAll()
        checkForUpdate()
        appendLog("PND açıldı.")
    }

    override fun onResume() {
        super.onResume()
        refreshAll()
    }

    private fun refreshAll() {
        runOnUiThread { setPlaceholders() }
        executor.execute {
            val androidId = getAndroidId()
            val serialno = runShellGetProp("ro.serialno")
            val apSerial = runShellGetProp("ro.boot.ap_serial")
            val bluetooth = runShellSettingsGet("secure", "bluetooth_address")
            val imei = runShellImei()
            val rilModel = runShellGetProp("ril.model_id")

            runOnUiThread {
                binding.textAndroidId.text = androidId.ifEmpty { getString(R.string.unknown) }
                binding.textSerialno.text = serialno.ifEmpty { getString(R.string.unknown) }
                binding.textApSerial.text = apSerial.ifEmpty { getString(R.string.unknown) }
                binding.textBluetooth.text = bluetooth.ifEmpty { getString(R.string.unknown) }
                binding.textImei.text = imei.ifEmpty { getString(R.string.unknown) }
                binding.textRilModel.text = rilModel.ifEmpty { getString(R.string.unknown) }
            }
        }
    }

    private fun setPlaceholders() {
        binding.textAndroidId.text = getString(R.string.refreshing)
        binding.textSerialno.text = getString(R.string.refreshing)
        binding.textApSerial.text = getString(R.string.refreshing)
        binding.textBluetooth.text = getString(R.string.refreshing)
        binding.textImei.text = getString(R.string.refreshing)
        binding.textRilModel.text = getString(R.string.refreshing)
    }

    private fun getAndroidId(): String {
        return try {
            Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) ?: ""
        } catch (e: Exception) { "" }
    }

    private fun runShellGetProp(prop: String): String {
        return try {
            var s = runGetProp(prop)
            if (s.isEmpty()) s = runRootCommandGet("getprop $prop")
            s
        } catch (e: Exception) { "" }
    }

    private fun runGetProp(prop: String): String {
        return try {
            val p = Runtime.getRuntime().exec(arrayOf("getprop", prop))
            BufferedReader(InputStreamReader(p.inputStream)).use { it.readLine()?.trim() ?: "" }
        } catch (e: Exception) { "" }
    }

    private fun runShellSettingsGet(namespace: String, key: String): String {
        return runRootCommandGet("settings get $namespace $key")
    }

    private fun runRootCommandGet(cmd: String): String {
        return try {
            val p = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
            BufferedReader(InputStreamReader(p.inputStream)).use { reader ->
                reader.readLine()?.trim()?.takeIf { it != "null" } ?: ""
            }
        } catch (e: Exception) { "" }
    }

    private fun runShellImei(): String {
        val out = runRootCommandGetFull("service call iphonesubinfo 1")
        if (out.isEmpty()) return ""
        val digits = out.replace(Regex("[^0-9]"), "")
        return digits.take(15)
    }

    private fun runRootCommandGetFull(cmd: String): String {
        return try {
            val p = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
            BufferedReader(InputStreamReader(p.inputStream)).use { reader ->
                reader.readText().trim()
            }
        } catch (e: Exception) { "" }
    }

    private fun appendLog(message: String) {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val line = "[$time] $message"
        Log.d(TAG, message)
        runOnUiThread {
            val logText = binding.textLog.text?.toString().orEmpty()
            binding.textLog.text = if (logText.isEmpty()) line else "$logText\n$line"
        }
    }

    private fun requestRootPermission() {
        binding.progress.isVisible = true
        binding.btnRequestRoot.isEnabled = false
        executor.execute {
            val granted = runRootCommand("id")
            runOnUiThread {
                binding.progress.isVisible = false
                binding.btnRequestRoot.isEnabled = true
                if (granted) {
                    Toast.makeText(this, R.string.root_granted, Toast.LENGTH_SHORT).show()
                    appendLog("Root icazəsi verildi (Magisk).")
                    refreshAll()
                } else {
                    Toast.makeText(this, R.string.root_request_failed, Toast.LENGTH_LONG).show()
                    appendLog("Root icazəsi verilmədi.")
                }
            }
        }
    }

    private fun changeAllIdentifiers() {
        val oldAndroidId = getAndroidId()
        val oldSerialno = binding.textSerialno.text.toString()
        val oldBluetooth = binding.textBluetooth.text.toString()
        val oldRilModel = binding.textRilModel.text.toString()

        val newAndroidId = generateHex(16)
        val newSerialno = generateHex(16)
        val newApSerial = "0x" + generateHex(12).uppercase()
        val newBluetooth = generateMacAddress()
        val newRilModel = "QB" + (1..8).map { ('0'..'9').random() }.joinToString("")

        appendLog("Əvvəlki: android_id=$oldAndroidId, serialno=$oldSerialno, ap_serial=${binding.textApSerial.text}, bluetooth=$oldBluetooth, ril.model_id=$oldRilModel")
        appendLog("Yeni: android_id=$newAndroidId, serialno=$newSerialno, bluetooth=$newBluetooth, ril.model_id=$newRilModel")

        binding.progress.isVisible = true
        binding.textStatus.isVisible = false
        binding.btnChangeAll.isEnabled = false

        executor.execute {
            val okAndroidId = runRootCommand("settings put secure android_id $newAndroidId")
            val okSerialno = runRootCommand("setprop ro.serialno $newSerialno")
            val okApSerial = runRootCommand("setprop ro.boot.ap_serial $newApSerial")
            val okBluetooth = runRootCommand("settings put secure bluetooth_address $newBluetooth")
            val okRilModel = runRootCommand("setprop ril.model_id $newRilModel")

            val anyOk = okAndroidId || okSerialno || okApSerial || okBluetooth || okRilModel
            val mainOk = okAndroidId // Əsas dəyişiklik

            runOnUiThread {
                binding.progress.isVisible = false
                binding.btnChangeAll.isEnabled = true
                if (mainOk) {
                    binding.textStatus.isVisible = true
                    binding.textStatus.text = getString(R.string.id_changed)
                    binding.textStatus.setTextColor(0xFF4CAF50.toInt())
                    Toast.makeText(this, R.string.id_changed, Toast.LENGTH_LONG).show()
                    appendLog("android_id=${if (okAndroidId) "OK" else "fail"}, serialno=${if (okSerialno) "OK" else "fail"}, ap_serial=${if (okApSerial) "OK" else "fail"}, bluetooth=${if (okBluetooth) "OK" else "fail"}, ril.model=${if (okRilModel) "OK" else "fail"}")
                    appendLog("Yenidən başladılır…")
                    refreshAll()
                    Thread {
                        Thread.sleep(1500)
                        runRootReboot()
                    }.start()
                } else if (anyOk) {
                    binding.textStatus.isVisible = true
                    binding.textStatus.text = getString(R.string.id_changed)
                    binding.textStatus.setTextColor(0xFF4CAF50.toInt())
                    appendLog("Bəzi əmrlər uğursuz (setprop cihazda məhdud ola bilər). Yenidən başladılır…")
                    Thread {
                        Thread.sleep(1500)
                        runRootReboot()
                    }.start()
                } else {
                    binding.textStatus.isVisible = true
                    binding.textStatus.text = getString(R.string.error_root)
                    binding.textStatus.setTextColor(0xFFF44336.toInt())
                    Toast.makeText(this, R.string.error_root, Toast.LENGTH_LONG).show()
                    appendLog("Xəta: Heç bir root əmri uğurla icra olunmadı. Magiskdən root verib yenidən cəhd edin.")
                }
            }
        }
    }

    private fun generateHex(len: Int): String {
        val hex = "0123456789abcdef"
        return (1..len).map { hex.random() }.joinToString("")
    }

    private fun generateMacAddress(): String {
        val hex = "0123456789ABCDEF"
        return (1..6).map { (1..2).map { hex.random() }.joinToString("") }.joinToString(":")
    }

    private fun runRootCommand(cmd: String): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
            // Stdout/stderr-i oxumalıyıq, yoxsa proses bloklana bilər
            Thread { process.inputStream.use { it.readBytes() } }.start()
            Thread { process.errorStream.use { it.readBytes() } }.start()
            process.waitFor() == 0
        } catch (e: Exception) {
            Log.e(TAG, "Root command failed: $cmd", e)
            false
        }
    }

    private fun runRootReboot() {
        try {
            Runtime.getRuntime().exec(arrayOf("su", "-c", "reboot"))
        } catch (e: Exception) {
            Log.e(TAG, "Reboot failed", e)
        }
    }

    private fun checkForUpdate() {
        binding.cardUpdate.isVisible = true
        binding.textUpdateInfo.text = getString(R.string.checking_update)
        binding.btnDownloadUpdate.isEnabled = false
        executor.execute {
            val release = UpdateChecker.fetchLatestRelease()
            runOnUiThread {
                if (release == null) {
                    binding.cardUpdate.isVisible = false
                    return@runOnUiThread
                }
                val current = BuildConfig.VERSION_NAME
                if (UpdateChecker.isNewerVersion(current, release.versionName)) {
                    pendingRelease = release
                    binding.textUpdateInfo.text = getString(R.string.update_available, release.versionName)
                    binding.btnDownloadUpdate.isEnabled = true
                    appendLog("Yeni versiya: ${release.versionName}")
                } else {
                    binding.cardUpdate.isVisible = false
                }
            }
        }
    }

    private fun downloadAndInstall(release: UpdateChecker.ReleaseInfo) {
        binding.btnDownloadUpdate.isEnabled = false
        binding.textUpdateInfo.text = getString(R.string.downloading)
        executor.execute {
            val dir = getExternalFilesDir(null) ?: return@execute
            val file = java.io.File(dir, "PND-update.apk")
            try {
                val url = URL(release.downloadUrl)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.instanceFollowRedirects = true
                conn.connectTimeout = 15000
                conn.readTimeout = 60000
                conn.inputStream.use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
                runOnUiThread { installApk(file) }
            } catch (e: Exception) {
                Log.e(TAG, "Download failed", e)
                runOnUiThread {
                    Toast.makeText(this, R.string.download_failed, Toast.LENGTH_LONG).show()
                    binding.textUpdateInfo.text = getString(R.string.download_failed)
                    binding.btnDownloadUpdate.isEnabled = true
                }
            }
        }
    }

    private fun installApk(file: java.io.File) {
        if (!file.exists()) return
        val uri: Uri = FileProvider.getUriForFile(
            this,
            "${BuildConfig.APPLICATION_ID}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
        binding.textUpdateInfo.text = getString(R.string.install_apk)
    }
}
