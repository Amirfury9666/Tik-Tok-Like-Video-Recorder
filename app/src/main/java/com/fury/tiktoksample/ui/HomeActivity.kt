package com.fury.tiktoksample.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import com.fury.tiktoksample.R
import com.fury.tiktoksample.base.BaseActivity
import com.fury.tiktoksample.databinding.ActivityHomeBinding
import com.fury.tiktoksample.extension.Permissions
import com.fury.tiktoksample.extension.isPermissionAvailable
import com.fury.tiktoksample.extension.launchActivity
import com.fury.tiktoksample.utils.Collections

class HomeActivity : BaseActivity<ActivityHomeBinding>() {

    private val storagePermissions by lazy {
        arrayOf(
            Permissions.readStorage,
            Permissions.writeStorage,
            Permissions.recordAudio
        )
    }

    override val layoutRes: Int get() = R.layout.activity_home

    override fun getToolbar(binding: ActivityHomeBinding): Toolbar = binding.toolbar

    override fun onActivityReady(instanceState: Bundle?, binding: ActivityHomeBinding) {
        binding.apply {
            recordButton.setOnClickListener {
                if (isPermissionAvailable(storagePermissions)) {
                    navigateToVideoRecording()
                } else {
                    requestPermissionResult.launch(storagePermissions)
                }
            }
            pickVideoButton.setOnClickListener {

            }
        }
        Collections().main()
    }

    private fun navigateToVideoRecording() {
        launchActivity(Intent(this, VideoRecorderActivity::class.java))
    }

    private val requestPermissionResult =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            var isGranted = true
            result.entries.forEach {
                if (!it.value) {
                    isGranted = it.value
                    return@forEach
                }
            }
            if (isGranted) {
                navigateToVideoRecording()
            }
        }
}