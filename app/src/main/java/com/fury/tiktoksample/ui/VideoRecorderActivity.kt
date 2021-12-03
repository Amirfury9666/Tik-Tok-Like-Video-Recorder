package com.fury.tiktoksample.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.viewModels
import androidx.annotation.Nullable
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.fury.tiktoksample.R
import com.fury.tiktoksample.adapter.FiltersAdapter
import com.fury.tiktoksample.animations.goneWithAnimation
import com.fury.tiktoksample.animations.visibleWithAnimation
import com.fury.tiktoksample.base.BaseActivity
import com.fury.tiktoksample.base.ViewModelFactory
import com.fury.tiktoksample.customs.ProgressBarListener
import com.fury.tiktoksample.data.VideoFilter
import com.fury.tiktoksample.databinding.ActivityVideoRecorderBinding
import com.fury.tiktoksample.extension.*
import com.fury.tiktoksample.filters.*
import com.fury.tiktoksample.listener.IItemClickListener
import com.fury.tiktoksample.utils.CameraUtils
import com.fury.tiktoksample.utils.Constants
import com.fury.tiktoksample.utils.ItemDecorator
import com.fury.tiktoksample.utils.VideoUtils
import com.fury.tiktoksample.viewmodel.RecorderViewModel
import com.fury.tiktoksample.workers.MergeAudioVideoWorker
import com.fury.tiktoksample.workers.MergeVideosWorker
import com.kaopiz.kprogresshud.KProgressHUD
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.VideoResult
import com.otaliastudios.cameraview.controls.*
import com.otaliastudios.cameraview.filter.Filter
import com.otaliastudios.cameraview.filter.Filters
import com.otaliastudios.cameraview.filters.BrightnessFilter
import com.otaliastudios.cameraview.filters.GammaFilter
import com.otaliastudios.cameraview.filters.SharpnessFilter
import java.io.File
import java.util.*


/**
 * Created By Amir Fury On 22 November
 *
 * Email fury.amir93@gmail.com
 */

private val TAG = VideoRecorderActivity::class.java.simpleName

class VideoRecorderActivity : BaseActivity<ActivityVideoRecorderBinding>() {

    private val permissions by lazy {
        arrayOf(
            Permissions.readStorage,
            Permissions.writeStorage,
            Permissions.recordAudio
        )
    }
    private val progressBar by lazy {
        KProgressHUD.create(this).apply {
            setStyle(KProgressHUD.Style.ANNULAR_DETERMINATE)
            setLabel(string(R.string.progressTitle))
            setCancellable(false)
            setMaxProgress(100)
        }
    }

    private val factory = ViewModelFactory()

    private val viewModel by viewModels<RecorderViewModel> { factory }

    private var isRecording = false

    private var isDone = false

    private val videoFiltersAdapter by lazy { FiltersAdapter(this, filtersClickListener) }

    override val layoutRes: Int get() = R.layout.activity_video_recorder

    override fun getToolbar(binding: ActivityVideoRecorderBinding): Toolbar? = null

    override fun onActivityReady(instanceState: Bundle?, binding: ActivityVideoRecorderBinding) {
        initializeRequiredComponents(binding)
        setClicks(binding)
    }

    private fun initializeRequiredComponents(binding: ActivityVideoRecorderBinding) {
        initializeVideoProgress(binding)
        initializeCamera(binding.camera)
        initializeFilters(binding)
    }

    private fun initializeFilters(binding: ActivityVideoRecorderBinding) {
        binding.filtersRecycler.adapter = videoFiltersAdapter
        binding.filtersRecycler.addItemDecoration(ItemDecorator(5, 5, 5, 5))
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.nature_image)
        videoFiltersAdapter.submitFilters(bitmap, filtersAsList())
    }

    private fun initializeCamera(camera: CameraView) {
        camera.apply {
            setLifecycleOwner(this@VideoRecorderActivity)
            facing = Facing.FRONT
            flash = Flash.AUTO
            videoCodec = VideoCodec.DEVICE_DEFAULT
            audioCodec = AudioCodec.DEVICE_DEFAULT
            whiteBalance = WhiteBalance.AUTO
            mode = Mode.VIDEO
            addCameraListener(cameraListener)
        }
    }

    private fun initializeVideoProgress(binding: ActivityVideoRecorderBinding) {
        binding.segments.apply {
            enableAutoProgressView(Constants.videoDuration)
            setDividerColor(Color.WHITE)
            setDividerEnabled(true)
            setDividerWidth(4f)
            listener = progressBarListener
            setShader(
                intArrayOf(
                    ContextCompat.getColor(
                        this@VideoRecorderActivity,
                        R.color.themeColor
                    ), ContextCompat.getColor(this@VideoRecorderActivity, R.color.themeColor)
                )
            )

        }
    }

    private val progressBarListener = object : ProgressBarListener {
        override fun timeInMillis(mills: Long) {

        }
    }

    private val filtersClickListener = object : IItemClickListener<VideoFilter> {
        override fun onItemClick(item: VideoFilter, position: Int) {
            applyPreviewFilter(item)
        }
    }

    private fun applyPreviewFilter(filter: VideoFilter) {
        val camera = binding.camera
        when (filter) {
            VideoFilter.BRIGHTNESS -> {
                val glf = Filters.BRIGHTNESS.newInstance() as BrightnessFilter
                glf.brightness = 1.2f
                camera.filter = glf
            }
            VideoFilter.EXPOSURE -> camera.filter = ExposureFilter()

            VideoFilter.GAMMA -> {
                val glf = Filters.GAMMA.newInstance() as GammaFilter
                glf.gamma = 2f
                camera.filter = glf
            }

            VideoFilter.GRAYSCALE -> camera.filter = Filters.GRAYSCALE.newInstance()

            VideoFilter.HAZE -> {
                val glf = HazeFilter()
                glf.slope = -0.5f
                camera.filter = glf
            }

            VideoFilter.INVERT -> camera.filter = Filters.INVERT_COLORS.newInstance()

            VideoFilter.MONOCHROME -> camera.filter = MonochromeFilter()

            VideoFilter.PIXELATED -> {
                val glf = PixelatedFilter()
                glf.pixel = 5f
                camera.filter = glf
            }
            VideoFilter.POSTERIZE -> camera.filter = Filters.POSTERIZE.newInstance()

            VideoFilter.SEPIA -> camera.filter = Filters.SEPIA.newInstance()

            VideoFilter.SHARP -> {
                val glf = Filters.SHARPNESS.newInstance() as SharpnessFilter
                glf.sharpness = 0.25f
                camera.filter = glf
            }

            VideoFilter.SOLARIZE -> camera.filter = SolarizeFilter()

            VideoFilter.VIGNETTE -> camera.filter = Filters.VIGNETTE.newInstance()

            else -> camera.filter = Filters.NONE.newInstance()
        }
    }

    private fun setClicks(binding: ActivityVideoRecorderBinding) {
        binding.apply {
            closeButton.setOnClickListener {
                super.onBackPressed()
            }
            flashButton.setOnClickListener {
                camera.apply {
                    if (isTakingVideo) {
                        toast(string(R.string.recorderInProgressError))
                        return@setOnClickListener
                    }
                    setFlash()
                    if (isFacingBack()) {
                        if (isFlashOff()) {
                            flashButton.setImageResource(R.drawable.ic_flash)
                        } else {
                            flashButton.setImageResource(R.drawable.ic_flash_on)
                        }
                    }
                }
            }
            cameraSwitchButton.setOnClickListener {
                camera.toggleFacing()
            }

            recordingButton.setOnClickListener {
                if (camera.isTakingVideo) {
                    isRecording = false
                    stopRecording()
                } else {
                    isRecording = true
                    startRecording()
                    if (filtersRecycler.isVisible()) {
                        hideFilters(binding)
                    }
                }
                rearrangeRecordingUi()
            }
            submitButton.setOnClickListener {
                if (viewModel.segments.isEmpty()) {
                    toast(string(R.string.plsRecordVideo))
                    return@setOnClickListener
                }
                submitForConcat(viewModel.segments, viewModel.audio)
            }
            filterButton.setOnClickListener {
                if (camera.isTakingVideo) {
                    toast(string(R.string.recorderInProgressError))
                    return@setOnClickListener
                }
                if (filtersRecycler.isVisible()) {
                    hideFilters(binding)
                    return@setOnClickListener
                }
                filtersRecycler.adapter = videoFiltersAdapter
                filtersRecycler.visibleWithAnimation(1)
            }
        }
    }

    private fun hideFilters(binding: ActivityVideoRecorderBinding) {
        binding.filtersRecycler.apply {
            goneWithAnimation(1, 300, object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    hide()
                }
            })
        }
    }

    private fun stopRecording() {
        binding.camera.stopVideo()
    }

    private fun startRecording() {
        isRecording = true
        val recorded = viewModel.recorded()
        if (recorded >= 15000) {
            toast("Error")
        } else {
            viewModel.video = CameraUtils.createNewFile(this, ".mp4")
            binding.camera.takeVideoSnapshot(viewModel.video!!, (15000 - recorded).toInt())
        }
    }

    private val cameraListener = object : CameraListener() {
        override fun onVideoTaken(result: VideoResult) {
            super.onVideoTaken(result)
            Log.d(TAG, "Recorded Video Path : ${result.file.absolutePath}")
        }

        override fun onVideoRecordingStart() {
            super.onVideoRecordingStart()
            binding.segments.resume()
        }

        override fun onVideoRecordingEnd() {
            super.onVideoRecordingEnd()
            binding.segments.apply {
                pause()
                addDivider()
            }
            saveCurrentRecording()
        }
    }

    private fun saveCurrentRecording() {
        val duration = VideoUtils.duration(this, Uri.fromFile(viewModel.video))
        if (viewModel.speed != 1f) {

        } else {
            val segment = CameraUtils.RecordSegment()
            segment.file = viewModel.video?.absolutePath ?: ""
            segment.duration = duration
            viewModel.segments.add(segment)
        }

        viewModel.video = null
    }

    private fun submitForConcat(segments: ArrayList<CameraUtils.RecordSegment>, audio: Uri?) {
        progressBar.show()
        setProgressUpdate()
        val merged = CameraUtils.createNewFile(this, ".mp4")
        val request = createConcatTask(segments, merged)
        val workManager = WorkManager.getInstance(this)
        workManager.enqueue(request)
        workManager.getWorkInfoByIdLiveData(request.id).observe(this, { info ->
            val ended =
                info.state == WorkInfo.State.CANCELLED || info.state == WorkInfo.State.FAILED || info.state == WorkInfo.State.SUCCEEDED
            if (ended) {
                isDone = true
                progressBar.dismiss()
            }
            if (info.state == WorkInfo.State.SUCCEEDED) {
                audio?.let {
                    submitForMerge(merged.absolutePath, it.path)
                } ?: proceedToUpload(merged.absolutePath)
            }
            printLog(TAG, info.state.toString())
        })
    }

    private fun setProgressUpdate() {
        progressBar.setMaxProgress(100)
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(object : Runnable {
            var currentProgress = 0
            override fun run() {
                currentProgress += 1
                progressBar.setProgress(currentProgress)
                if (currentProgress == 80) {
                    progressBar.setLabel("Almost Finish...")
                }
                if (!isDone) {
                    handler.postDelayed(this, 600)
                } else {
                    isDone = false
                }
            }
        }, 100)
    }

    private fun submitForMerge(video: String, audio: String?) {
        val merged = CameraUtils.createNewFile(this, ".mp4")
        val data = Data.Builder().apply {
            putString(MergeAudioVideoWorker.keyAudio, audio)
            putString(MergeAudioVideoWorker.keyVideo, video)
            putString(MergeAudioVideoWorker.keyOutput, merged.absolutePath)
        }.build()

        val request =
            OneTimeWorkRequest.Builder(MergeAudioVideoWorker::class.java).setInputData(data).build()

        val workManager = WorkManager.getInstance(this)
        workManager.enqueue(request)
        workManager.getWorkInfoByIdLiveData(request.id).observe(this, { info ->
            val ended =
                info.state == WorkInfo.State.CANCELLED || info.state == WorkInfo.State.FAILED || info.state == WorkInfo.State.SUCCEEDED
            if (ended) {
                isDone = true
                progressBar.dismiss()
            }
            if (info.state == WorkInfo.State.SUCCEEDED) {
                proceedToUpload(merged.absolutePath)
            }
        })
    }

    private fun proceedToUpload(video: String) {
        launchActivity(Intent(this, PreviewActivity::class.java).apply {
            putExtra(Constants.video, video)
            putExtra(Constants.songId, viewModel.songId)
        })
    }

    private fun createConcatTask(
        @Nullable segments: ArrayList<CameraUtils.RecordSegment>,
        merged: File
    ): OneTimeWorkRequest {
        val videos = arrayListOf<String>()
        for (segment in segments) {
            videos.add(segment.file)
        }

        val data = Data.Builder().apply {
            putStringArray(MergeVideosWorker.keyInputs, videos.toArray(arrayOfNulls(0)))
            putString(MergeVideosWorker.keyOutputs, merged.absolutePath)
        }.build()

        return OneTimeWorkRequest.Builder(MergeVideosWorker::class.java).setInputData(data).build()
    }

    private fun rearrangeRecordingUi() {
        binding.apply {
            if (isRecording) {
                recordingButton.setImageResource(R.drawable.ic_recording_on)
                galleryButton.goneWithAnimation(1, 1000, null)
                submitButton.goneWithAnimation(1, 1000, null)
                cameraActions.goneWithAnimation(1, 1000, null)
            } else {
                recordingButton.setImageResource(R.drawable.ic_recording_off)
                galleryButton.visibleWithAnimation(1)
                submitButton.visibleWithAnimation(1)
                cameraActions.visibleWithAnimation(1)
            }
        }
    }

}