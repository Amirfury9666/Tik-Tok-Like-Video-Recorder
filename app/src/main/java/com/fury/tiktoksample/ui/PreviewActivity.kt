package com.fury.tiktoksample.ui

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.fury.tiktoksample.R
import com.fury.tiktoksample.base.BaseActivity
import com.fury.tiktoksample.databinding.ActivityPreviewBinding
import com.fury.tiktoksample.extension.string
import com.fury.tiktoksample.utils.Constants
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.LoopingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import java.io.File

class PreviewActivity : BaseActivity<ActivityPreviewBinding>() {

    private var position = 0L
    private var window = 0
    private var video: String? = null

    private val player: ExoPlayer by lazy { ExoPlayer.Builder(this).build() }

    override val layoutRes: Int get() = R.layout.activity_preview

    override fun getToolbar(binding: ActivityPreviewBinding): Toolbar? = null

    override fun onActivityReady(instanceState: Bundle?, binding: ActivityPreviewBinding) {
        binding.closeButton.setOnClickListener { super.onBackPressed() }
        binding.player.player = player
        video = intent.getStringExtra(Constants.video)
    }

    override fun onResume() {
        super.onResume()
        if (player.playbackState == Player.STATE_IDLE) {
            video?.let {
                startPlayer(it)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        stopPlayer()
    }


    private fun startPlayer(video: String) {
        val factory = DefaultDataSourceFactory(this, string(R.string.app_name))
        val source =
            ProgressiveMediaSource.Factory(factory).createMediaSource(Uri.fromFile(File(video)))
        player.playWhenReady = true
        player.seekTo(window, position)
        player.prepare(LoopingMediaSource(source), false, false)
    }

    private fun stopPlayer() {
        window = player.currentWindowIndex
        position = player.currentPosition
        player.playWhenReady = false
        player.stop()
    }
}