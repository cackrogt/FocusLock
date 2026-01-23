package com.example.focuslock

import android.content.Context
import com.example.focuslock.LockOverlay


object OverlayController {

    private var overlay: LockOverlay? = null

    fun init(context: Context, allowedApps: Set<String>) {
        if (overlay == null) {
            overlay = LockOverlay(context, allowedApps)
        }
    }

    fun show() {
        overlay?.show()
    }

    fun hide() {
        overlay?.hide()
    }
}
