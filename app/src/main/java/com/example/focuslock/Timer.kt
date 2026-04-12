package com.example.focuslock

import android.app.AlertDialog
import android.widget.Button

class Timer {

    fun showTimerPicker(button: Button, activity: MainActivity) {

        val options = arrayOf("2 min", "5 min", "10 min", "15 min", "30 min")
        val values = listOf(
            2 * 60 * 1000L,
            5 * 60 * 1000L,
            10 * 60 * 1000L,
            15 * 60 * 1000L,
            30 * 60 * 1000L
        )

        AlertDialog.Builder(activity)
            .setTitle("Select Focus Duration")
            .setItems(options) { _, which ->
                activity.setSelectedDuration(values[which]);

                val minutes = activity.getSelectedDuration() / 60000
                button.text = "Timer Set For ($minutes min)"
            }
            .show()
    }
}