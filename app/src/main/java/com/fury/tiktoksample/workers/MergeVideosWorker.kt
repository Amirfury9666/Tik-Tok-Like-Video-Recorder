package com.fury.tiktoksample.workers

import android.content.Context
import android.util.Log
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.google.common.util.concurrent.ListenableFuture
import java.io.File
import androidx.concurrent.futures.CallbackToFutureAdapter
import com.fury.tiktoksample.utils.VideoUtils
import com.otaliastudios.transcoder.Transcoder
import com.otaliastudios.transcoder.TranscoderListener
import com.otaliastudios.transcoder.strategy.DefaultVideoStrategy
import com.otaliastudios.transcoder.strategy.size.PassThroughResizer


class MergeVideosWorker(private val context: Context, params: WorkerParameters) :
    ListenableWorker(context, params) {

    companion object {
        const val keyInputs = "inputs"
        const val keyOutputs = "outputs"
        const val tag = "MergeVideosWorker"
    }

    override fun startWork(): ListenableFuture<Result> {
        val paths: Array<String>? = inputData.getStringArray(keyInputs)
        val output = File(inputData.getString(keyOutputs))

        val files = arrayOfNulls<File>(paths!!.size)
        paths.forEachIndexed { index, s ->
            files[index] = File(s)
        }

        return CallbackToFutureAdapter.getFuture {
            doActualWork(files, output, it)
        }
    }

    private fun doActualWork(
        inputs: Array<File?>,
        output: File,
        completer: CallbackToFutureAdapter.Completer<Result>
    ) {

        val transcoder = Transcoder.into(output.absolutePath)
        inputs.forEach {
            transcoder.addDataSource(
                VideoUtils.createDataSource(
                    context.applicationContext,
                    it!!.absolutePath
                )
            )
        }
        transcoder.setListener(object : TranscoderListener {
            override fun onTranscodeProgress(progress: Double) {

            }

            override fun onTranscodeCompleted(successCode: Int) {
                completer.set(Result.success())
                inputs.forEach {
                    if (!it!!.delete()) {
                        Log.d(tag, "Could not delete input file $it")
                    }
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