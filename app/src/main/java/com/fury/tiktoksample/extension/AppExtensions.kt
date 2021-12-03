package com.fury.tiktoksample.extension

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.util.Log
import android.util.TypedValue
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.fragment.app.FragmentActivity
import com.fury.tiktoksample.data.VideoFilter

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.Q, lambda = 0)
inline fun <T> sdk29OrUp(onSdk29: () -> T): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        onSdk29()
    } else null
}

fun Context.launchActivity(intent: Intent) {
    (this as FragmentActivity)
    startActivity(intent)
}

fun Context.string(stringId: Int): String = resources.getString(stringId)

internal fun Any.printLog(tag: String, message: String?) = Log.d(tag, message.toString())

fun Any.filtersAsList(): ArrayList<VideoFilter> =
    arrayListOf<VideoFilter>().apply {
        VideoFilter.values().forEach {
            add(it)
        }
    }

fun String.makeFirstLatterCapital() : String{
    return this.substring(0,1).uppercase() + this.substring(1)
}

fun Int.dpToPx(): Int {
    return this.toFloat().dpToPx()
}

fun Float.dpToPx(): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        Resources.getSystem().displayMetrics
    ).toInt()
}