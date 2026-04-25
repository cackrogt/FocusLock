package com.example.focuslock

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.view.accessibility.AccessibilityManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import android.widget.ProgressBar
import android.view.View
import com.example.focuslock.lock_overlay.LockService
import com.example.focuslock.objects.AllowedAppStore
import com.example.focuslock.objects.FocusSessionManager
import com.example.focuslock.objects.FocusSessionState
import com.example.focuslock.objects.LaunchableAppsStore

class MainActivity : AppCompatActivity() {

    private var selectedDurationMs = 5 * 60 * 1000L // default 5 min

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(this, FocusDeviceAdminReceiver::class.java)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (!Settings.canDrawOverlays(this)) {
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
            )
        }

        findViewById<Button>(R.id.btnEnable).setOnClickListener @androidx.annotation.RequiresPermission(
            android.Manifest.permission.SCHEDULE_EXACT_ALARM
        ) {

            if (!isAccessibilityServiceEnabled(this)) {
                showAccessibilityRequiredDialog()
                return@setOnClickListener
            }

            if (!dpm.isAdminActive(adminComponent)) {
                requestDeviceAdmin()
                return@setOnClickListener
            }

            showLoading()
            initializeKnownAppsAndStart()
        }

        val btnSelectApp = findViewById<Button>(R.id.btnSelectApp)

        btnSelectApp.setOnClickListener {
            showAppPicker()
        }

        val btnTimer = findViewById<Button>(R.id.btnTimer)

        btnTimer.setOnClickListener {
            showTimerPicker(btnTimer)
        }
    }

    fun showTimerPicker(button: Button) {

        val options = arrayOf("2 min", "5 min", "10 min", "15 min", "30 min")
        val values = listOf(
            2 * 60 * 1000L,
            5 * 60 * 1000L,
            10 * 60 * 1000L,
            15 * 60 * 1000L,
            30 * 60 * 1000L
        )

        AlertDialog.Builder(this)
            .setTitle("Select Focus Duration")
            .setItems(options) { _, which ->
                setSelectedDuration(values[which]);

                val minutes = getSelectedDuration() / 60000
                button.text = "Timer Set For ($minutes min)"
            }
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun initializeKnownAppsAndStart() {
        Thread {
            val launchableApps = getLaunchableAppsList(this)

            runOnUiThread {

                LaunchableAppsStore.set(launchableApps)
                launchableApps.forEach {
                    Log.i("ANKU_FOCUS_LAUNCHABLE", it)
                }
                hideLoading()
                startFocusSession()
            }
        }.start()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun startFocusSession() {
        val duration = selectedDurationMs

        FocusSessionState.start(duration)
        FocusSessionManager.startSession(this, duration)

        val dpm =
            getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager

        dpm.lockNow()

        startForegroundService(Intent(this, LockService::class.java))
    }

    public fun setSelectedDuration(duration: Long) {
        selectedDurationMs = duration;
    }

    public fun getSelectedDuration(): Long {
        return selectedDurationMs
    }

    private fun getAllRelevantApps(): Set<String> {
        val pm = packageManager
        val result = mutableSetOf<String>()

        // 1. Launcher apps (important)
        val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val launcherApps = pm.queryIntentActivities(launcherIntent, 0)
        launcherApps.forEach {
            result.add(it.activityInfo.packageName)
        }

        return result
    }

    private fun showAppPicker() {
        val apps = getLaunchableApps(this)

        val labels = apps.map { it.label }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Select App")
            .setItems(labels) { _, which ->
                val selectedApp = apps[which]

                AllowedAppStore.set(this, selectedApp.packageName)

                Toast.makeText(
                    this,
                    "Selected: ${selectedApp.label}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .show()
    }

    private fun getLaunchableAppsList(context: Context): Set<String> {
        val pm = context.packageManager
        val result = mutableSetOf<String>()

        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfoList = pm.queryIntentActivities(intent, 0)

        resolveInfoList.forEach {
            result.add(it.activityInfo.packageName)
        }

        return result
    }

    fun getLaunchableApps(context: Context): List<LaunchableApp> {
        val pm = context.packageManager

        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        return pm.queryIntentActivities(intent, 0)
            .map {
                LaunchableApp(
                    label = it.loadLabel(pm).toString(),
                    packageName = it.activityInfo.packageName
                )
            }
            .sortedBy { it.label.lowercase() }
    }

    private fun showLoading() {
        findViewById<ProgressBar>(R.id.loading).visibility = View.VISIBLE
    }

    private fun hideLoading() {
        findViewById<ProgressBar>(R.id.loading).visibility = View.GONE
    }

    private fun showAccessibilityRequiredDialog() {
        AlertDialog.Builder(this)
            .setTitle("Accessibility required")
            .setMessage(
                "Focus Lock needs Accessibility access to detect when you leave allowed apps " +
                        "and to block distractions.\n\n" +
                        "We do NOT read your screen content."
            )
            .setPositiveButton("Enable") { _, _ ->
                openAccessibilitySettings(this)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val am =
            context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager

        val enabledServices =
            am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)

        return enabledServices.any {
            it.resolveInfo.serviceInfo.packageName == context.packageName &&
                    it.resolveInfo.serviceInfo.name == FocusAccessibilityService::class.java.name
        }
    }

    fun openAccessibilitySettings(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    private fun requestDeviceAdmin() {
        val component = ComponentName(
            this,
            FocusDeviceAdminReceiver::class.java
        )

        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, component)
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Required to lock the phone during Focus Mode"
            )
        }

        startActivity(intent)
    }

}
