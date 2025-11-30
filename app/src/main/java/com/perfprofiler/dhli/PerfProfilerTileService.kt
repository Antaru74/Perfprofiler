package com.perfprofiler.dhli

import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast

class PerfProfilerTileService : TileService() {

    private val PREFS_NAME = "PerfProfilerPrefs"
    private val KEY_CURRENT_MODE = "CurrentMode"
    private lateinit var sharedPrefs: SharedPreferences

    private val modes = listOf("battery", "balance", "performance", "hell")
    private val modeLabels = mapOf(
        "battery" to "🔋 Battery (60Hz)",
        "balance" to "⚖️ Balance (60Hz)",
        "performance" to "🚀 Performance (120Hz)",
        "hell" to "🔥 HELL (120Hz)"
    )
    
    // Sesuaikan path ini dengan lokasi skrip Anda di perangkat Anda
    private val SCRIPT_PATH = "/data/local/tmp/power_profile.sh"

    override fun onCreate() {
        super.onCreate()
        sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override fun onStartListening() {
        // Dipanggil saat tile terlihat di panel QS
        updateTileState(getCurrentMode())
    }

    override fun onClick() {
        val currentMode = getCurrentMode()
        val nextMode = getNextMode(currentMode)
        
        // 1. Jalankan skrip dengan mode baru
        executeRootScript(nextMode)
        
        // 2. Simpan mode baru
        saveCurrentMode(nextMode)
        
        // 3. Perbarui UI Tile
        updateTileState(nextMode)
        
        // Tampilkan notifikasi
        Toast.makeText(this, "Profile Applied: ${modeLabels[nextMode]}", Toast.LENGTH_SHORT).show()
    }

    private fun getCurrentMode(): String {
        return sharedPrefs.getString(KEY_CURRENT_MODE, modes.first()) ?: modes.first()
    }

    private fun saveCurrentMode(mode: String) {
        sharedPrefs.edit().putString(KEY_CURRENT_MODE, mode).apply()
    }

    private fun getNextMode(currentMode: String): String {
        val currentIndex = modes.indexOf(currentMode)
        val nextIndex = (currentIndex + 1) % modes.size
        return modes[nextIndex]
    }
    
    private fun updateTileState(mode: String) {
        qsTile?.let { tile ->
            tile.label = modeLabels[mode]
            tile.icon = Icon.createWithResource(this, R.drawable.ic_launcher_foreground) // Ganti dengan ikon yang sesuai
            tile.state = Tile.STATE_ACTIVE
            tile.updateTile()
        }
    }

    private fun executeRootScript(mode: String) {
        // Perintah untuk menjalankan skrip shell dengan izin root (su)
        val command = "su -c sh $SCRIPT_PATH $mode"
        
        try {
            // Menjalankan perintah root
            Runtime.getRuntime().exec(command)
        } catch (e: Exception) {
            Toast.makeText(this, "ROOT EXECUTION FAILED: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
