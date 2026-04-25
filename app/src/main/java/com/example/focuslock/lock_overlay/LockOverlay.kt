package com.example.focuslock.lock_overlay

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import com.example.focuslock.FocusDeviceAdminReceiver
import com.example.focuslock.R
import com.example.focuslock.objects.AllowedAppStore
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import com.example.focuslock.objects.FocusSessionState

class LockOverlay(
    private val context: Context,
    private val allowedApps: Set<String>
) {

    private val handler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null

    private val windowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private var view: View? = null

    private var isAttached = false

    fun show() {
        if (view != null) return

        val inflater = LayoutInflater.from(context)
        view = inflater.inflate(R.layout.overlay_lock, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        setupButtons(view!!)
        startTimer(view!!)
        windowManager.addView(view, params)
    }


    fun hide() {
        timerRunnable?.let { handler.removeCallbacks(it) }
        timerRunnable = null
        view?.let {
            windowManager.removeView(it)
            view = null
        }
    }


    private fun setupButtons(v: View) {
        v.findViewById<Button>(R.id.btnApp1).setOnClickListener {
            launchApp("com.samsungsds.nexsign.client.singleid.pub")
        }

        v.findViewById<Button>(R.id.btnApp2).setOnClickListener {
            launchApp("com.google.android.apps.messaging")
        }
        val userBtn = v.findViewById<Button>(R.id.btnUserApp)
        val userAppPkg = AllowedAppStore.get(context)

        if (userAppPkg != null) {
            val pm = context.packageManager
            val appName = try {
                pm.getApplicationLabel(
                    pm.getApplicationInfo(userAppPkg, 0)
                ).toString()
            } catch (e: Exception) {
                "User App"
            }

            userBtn.text = appName
            userBtn.visibility = View.VISIBLE

            userBtn.setOnClickListener {
                launchApp(userAppPkg)
            }
        } else {
            userBtn.visibility = View.GONE
        }

        v.findViewById<Button>(R.id.btnLock).setOnClickListener {
            lockDevice()
        }
    }

    public fun lockDevice() {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE)
                as DevicePolicyManager
        val admin = ComponentName(
            context,
            FocusDeviceAdminReceiver::class.java
        )

        if (dpm.isAdminActive(admin)) {
            dpm.lockNow()
        }
    }

    private fun launchApp(pkg: String) {
//        Intent(Intent.ACTION_MAIN).apply {
//            `package` = pkg
//            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        }

        val intent = context.packageManager.getLaunchIntentForPackage(pkg)
        if (intent == null) {
            Log.e("FOCUS", "App not found: $pkg")
            return
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private fun startTimer(v: View) {
        val txtTimer = v.findViewById<TextView>(R.id.txtTimer)

        timerRunnable = object : Runnable {
            override fun run() {

                val remaining = FocusSessionState.getRemainingMs()

                if (remaining <= 0) {
                    txtTimer.text = "00:00"
                    return
                }

                val minutes = remaining / 60000
                val seconds = (remaining / 1000) % 60

                txtTimer.text = String.format("%02d:%02d", minutes, seconds)

                handler.postDelayed(this, 1000)
            }
        }

        handler.post(timerRunnable!!)
    }
}