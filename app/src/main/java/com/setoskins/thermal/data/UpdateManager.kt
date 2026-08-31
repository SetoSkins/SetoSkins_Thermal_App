package com.setoskins.thermal.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object UpdateManager {
    private const val LATEST_RELEASE_URL = "https://api.github.com/repos/SetoSkins/SetoSkins_Thermal_App/releases/latest"

    data class UpdateInfo(
        val hasUpdate: Boolean,
        val latestVersion: String,
        val releaseNotes: String,
        val downloadUrl: String
    )

    suspend fun checkAppUpdate(context: Context): UpdateInfo = withContext(Dispatchers.IO) {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val currentVersion = packageInfo.versionName ?: "0.0.0"
            
            val connection = URL(LATEST_RELEASE_URL).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            connection.setRequestProperty("User-Agent", "SetoSkins-Thermal-App")
            
            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                
                val tagName = json.optString("tag_name", "")
                val body = json.optString("body", "")
                val htmlUrl = json.optString("html_url", "")
                
                val hasUpdate = isVersionGreater(tagName, currentVersion)
                
                UpdateInfo(
                    hasUpdate = hasUpdate,
                    latestVersion = tagName,
                    releaseNotes = body,
                    downloadUrl = htmlUrl
                )
            } else {
                UpdateInfo(false, "", "", "")
            }
        } catch (e: Exception) {
            UpdateInfo(false, "", "", "")
        }
    }

    private fun isVersionGreater(latest: String, current: String): Boolean {
        val latestClean = latest.removePrefix("v").substringBefore("-")
        val currentClean = current.removePrefix("v").substringBefore("-")
        
        val latestParts = latestClean.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = currentClean.split(".").mapNotNull { it.toIntOrNull() }
        
        val length = maxOf(latestParts.size, currentParts.size)
        for (i in 0 until length) {
            val l = latestParts.getOrNull(i) ?: 0
            val c = currentParts.getOrNull(i) ?: 0
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }
}
