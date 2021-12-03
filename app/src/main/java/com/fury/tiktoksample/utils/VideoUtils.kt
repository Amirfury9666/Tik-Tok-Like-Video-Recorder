package com.fury.tiktoksample.utils

import android.content.ContentResolver
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import com.otaliastudios.transcoder.source.DataSource
import com.otaliastudios.transcoder.source.FilePathDataSource
import com.otaliastudios.transcoder.source.UriDataSource

private val TAG = VideoUtils::class.java.simpleName

object VideoUtils {
    fun createDataSource(context: Context, file: String): DataSource {
        var newFile: String? = file
        if (file.startsWith(ContentResolver.SCHEME_CONTENT)) {
            return UriDataSource(context, Uri.parse(file))
        }
        if (file.startsWith(ContentResolver.SCHEME_FILE)) {
            newFile = Uri.parse(file).path
        }
        return FilePathDataSource(newFile!!)
    }


    fun duration(context: Context, uri: Uri): Long {
        var mediaMetaDataRetriever: MediaMetadataRetriever? = null
        try {
            mediaMetaDataRetriever = MediaMetadataRetriever()
            if (TextUtils.equals(uri.scheme, ContentResolver.SCHEME_FILE)) {
                mediaMetaDataRetriever.setDataSource(uri.path)
            } else {
                mediaMetaDataRetriever.setDataSource(context, uri)
            }
            val duration =
                mediaMetaDataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            duration?.let {
                return it.toLong()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unable extract duration from $uri", e)
        } finally {
            mediaMetaDataRetriever?.let {
                it.release()
            }
        }
        return 0
    }
}