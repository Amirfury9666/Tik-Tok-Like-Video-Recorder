package com.fury.tiktoksample.utils

import android.content.Context
import androidx.annotation.Nullable
import java.io.File
import java.util.*

object CameraUtils {

    class RecordSegment {
        var file: String = ""
        var duration: Long = 0
    }

    fun createNewFile(context: Context, @Nullable suffix: String): File {
        return createNewFile(context.cacheDir, suffix)
    }

    private fun createNewFile(directory: File, @Nullable suffix: String): File {
        return File(directory, UUID.randomUUID().toString() + suffix)
    }
}