package com.fury.tiktoksample.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.fury.tiktoksample.utils.CameraUtils
import java.io.File

class RecorderViewModel : ViewModel() {

    var audio: Uri? = null
    val segments = arrayListOf<CameraUtils.RecordSegment>()
    var songId = 0
    var speed: Float = 1f
    var video: File? = null


    fun recorded(): Long {
        var recorded: Long = 0
        for (segment in segments) {
            recorded += segment.duration
        }
        return recorded
    }

}