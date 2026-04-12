package com.example.focuslock.objects

import android.content.Context
import com.example.focuslock.lock_overlay.LockOverlay

object OverlayController {

    private var overlay: LockOverlay? = null
    private var isVisible = false

    fun init(context: Context, allowedApps: Set<String>) {
        if (overlay == null) {
            overlay = LockOverlay(context, allowedApps)
        }
    }

    fun show() {
        if(isVisible) return
        overlay?.show()
        isVisible = true
    }

    fun hide() {
        if(!isVisible) return
        overlay?.hide()
        isVisible = false
    }
}