package com.fury.tiktoksample.extension

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat


object Permissions {
    const val readStorage = Manifest.permission.READ_EXTERNAL_STORAGE
    const val writeStorage = Manifest.permission.WRITE_EXTERNAL_STORAGE
    const val recordAudio = Manifest.permission.RECORD_AUDIO
    const val camera = Manifest.permission.CAMERA
}

internal fun Context.isPermissionAvailable(permissions: Array<String>): Boolean {
    permissions.forEach {
        if (ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
    }
    return true
}
