package com.example.focuslock

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
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.startActivity

class LockOverlay(
    private val context: Context,
    private val allowedApps: Set<String>
) {

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
        windowManager.addView(view, params)
    }

    fun show2() {
        hide()

        val inflater = LayoutInflater.from(context)
        view = inflater.inflate(R.layout.overlay_lock, null)

        view!!.isClickable = true
        view!!.isFocusable = true
        view!!.setOnTouchListener { _, _ -> true }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.OPAQUE
        )

        setupButtons(view!!)
        windowManager.addView(view, params)
        isAttached = true
    }


    fun hide() {
//        if (isAttached && view != null) {
//            windowManager.removeViewImmediate(view)
//            view = null
//            isAttached = false
//        }
        view?.let {
            windowManager.removeView(it)
            view = null
        }
    }



//    fun remove() {
//        if (isAttached) {
//            windowManager.removeViewImmediate(rootView)
//            isAttached = false
//        }
//    }

    private fun setupButtons(v: View) {
        v.findViewById<Button>(R.id.btnApp1).setOnClickListener {
            launchApp("com.samsungsds.nexsign.client.singleid.pub")
        }

        v.findViewById<Button>(R.id.btnApp2).setOnClickListener {
            launchApp("com.google.android.apps.messaging")
        }

        v.findViewById<Button>(R.id.btnLock).setOnClickListener {
            lockDevice()
        }
    }

    private fun lockDevice() {
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
}
