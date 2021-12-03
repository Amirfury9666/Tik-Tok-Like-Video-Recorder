package com.fury.tiktoksample.extension

import android.content.Context
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.controls.Facing
import com.otaliastudios.cameraview.controls.Flash

fun Context.toast(message: String?) {
    Toast.makeText(this, message.toString(), Toast.LENGTH_SHORT).show()
}

fun View.show() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun View.isVisible(): Boolean = visibility == View.VISIBLE

fun TextView.setTextOnView(value: String?) {
    text = value.toString()
}

fun CameraView.setFlash() {
    flash = if (this.flash == Flash.OFF) Flash.TORCH else Flash.OFF
}

fun CameraView.isFlashOff(): Boolean = this.flash == Flash.OFF

fun CameraView.isFacingFront(): Boolean = this.facing == Facing.FRONT

fun CameraView.isFacingBack(): Boolean = this.facing == Facing.BACK