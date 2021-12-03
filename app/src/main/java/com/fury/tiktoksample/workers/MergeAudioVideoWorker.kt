package com.fury.tiktoksample.workers

import android.content.ClipData
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.fury.tiktoksample.utils.VideoUtils
import com.google.common.util.concurrent.ListenableFuture
import com.otaliastudios.transcoder.Transcoder
import com.otaliastudios.transcoder.TranscoderListener
import com.otaliastudios.transcoder.engine.TrackType
import com.otaliastudios.transcoder.source.BlankAudioDataSource
import com.otaliastudios.transcoder.source.ClipDataSource
import com.otaliastudios.transcoder.strategy.DefaultVideoStrategy
import com.otaliastudios.transcoder.strategy.size.PassThroughResizer
import java.io.File
import java.util.concurrent.TimeUnit

class MergeAudioVideoWorker(private val context: Context, params: WorkerParameters) :
    ListenableWorker(context, params) {

    companion object {
        const val keyAudio = "audio"
        const val keyOutput = "output"
        const val keyVideo = "video"
        const val tag = "MergeAudioVideoWorker"
    }

    override fun startWork(): ListenableFuture<Result> {
        val audio = inputData.getString(keyAudio) ?: ""
        val video: File = File(inputData.getString(keyVideo) ?: "")
        val output = File(inputData.getString(keyOutput) ?: "")
        return CallbackToFutureAdapter.getFuture {
            doActualWork(video, audio,output,it)
        }
    }

    private fun doActualWork(
        video: File,
        audio: String,
        output: File,
        completer: CallbackToFutureAdapter.Completer<Result>
    ) {
        var audio2 = VideoUtils.createDataSource(applicationContext, audio)
        val transcoder = Transcoder.into(output.absolutePath)
        val duration = TimeUnit.MICROSECONDS.toMillis(audio2.durationUs)
        val duration2 = VideoUtils.duration(applicationContext, Uri.fromFile(video))

        if (duration > duration2) {
            audio2 = ClipDataSource(audio2, 0, TimeUnit.MILLISECONDS.toMicros(duration2))
            transcoder.addDataSource(TrackType.AUDIO, audio2)
        } else {
            audio2 = ClipDataSource(audio2, 0, TimeUnit.MILLISECONDS.toMicros(duration))
            transcoder.addDataSource(TrackType.AUDIO, audio2)
            transcoder.addDataSource(
                TrackType.AUDIO,
                BlankAudioDataSource(TimeUnit.MILLISECONDS.toMicros(duration2 - duration))
            )
        }
        transcoder.addDataSource(TrackType.VIDEO, video.absolutePath)
        transcoder.setListener(object : TranscoderListener {
            override fun onTranscodeProgress(progress: Double) {

            }

            override fun onTranscodeCompleted(successCode: Int) {
                completer.set(Result.success())
                if (!video.delete()) {
                    Log.d(tag, "Could not delete video file $video")
                }
            }

            override fun onTranscodeCanceled() {
                completer.setCancelled()
                if (!output.delete()) {
                    Log.d(tag, "Could not delete output file $output")
                }
            }

            override fun onTranscodeFailed(exception: Throwable) {
                completer.setException(exception)
                if (!output.delete()) {
                    Log.d(tag, "Could not delete failed output file $output")
                }
            }
        })
        transcoder.setVideoTrackStrategy(DefaultVideoStrategy.Builder(PassThroughResizer()).build())
        transcoder.transcode()
    }
}