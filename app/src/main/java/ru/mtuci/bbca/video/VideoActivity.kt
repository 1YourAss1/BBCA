@file:UnstableApi

package ru.mtuci.bbca.video

import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.STATE_ENDED
import androidx.media3.common.Player.STATE_READY
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import ru.mtuci.bbca.R
import ru.mtuci.bbca.sensors_data_writer.userActivityDataWriter


class VideoActivity : AppCompatActivity() {

    /**
     * Uri for video file to play.
     * If you want to change video file, put it to res/raw directory
     * and change "R.raw.video" argument to something else, if it has
     * a different name.
     */
    private val mediaUri by lazy(LazyThreadSafetyMode.NONE) {
        Uri.parse("android.resource://" + packageName + "/" + R.raw.video)
    }

    private var player: ExoPlayer? = null

    private val playerView by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<PlayerView>(R.id.playerView)
    }

    private val userActivityDataWriter by lazy(LazyThreadSafetyMode.NONE) {
        userActivityDataWriter(
            currentSessionPath = intent.getStringExtra("currentSessionPath").toString(),
            directoryName = "video",
            activityName = "video",
            activityColumns = listOf("timestamp", "orientation", "x_coordinate", "y_coordinate", "pressure", "action_type")
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_video)

        playerView.controllerAutoShow = false
        playerView.controllerHideOnTouch = false
        playerView.controllerShowTimeoutMs = -1

        playerView.setShowRewindButton(false)
        playerView.setShowFastForwardButton(false)
        playerView.setShowNextButton(false)
        playerView.setShowPreviousButton(false)
        playerView.showController()

        playerView.findViewById<View>(androidx.media3.ui.R.id.exo_extra_controls_scroll_view)
            .isVisible = false

        playerView.findViewById<View>(androidx.media3.ui.R.id.exo_basic_controls)
            .isVisible = false

        playerView.findViewById<View>(androidx.media3.ui.R.id.exo_controls_background)
            .setBackgroundColor(getColor(android.R.color.transparent))

        playerView.findViewById<View>(androidx.media3.ui.R.id.exo_center_controls)
            .isVisible = false

        playerView.setOnClickListener {
            if (player?.isPlaying == true) {
                pause()
            } else {
                play()
            }
        }

        initPlayer()
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        when (event?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                userActivityDataWriter.writeActivity(
                    listOf(System.currentTimeMillis(), resources.configuration.orientation, event.x, event.y, event.pressure, event.action)
                )
            }
            MotionEvent.ACTION_MOVE -> {
                userActivityDataWriter.writeActivity(
                    listOf(System.currentTimeMillis(), resources.configuration.orientation, event.x, event.y, event.pressure, event.action)
                )
            }
            MotionEvent.ACTION_UP -> {
                userActivityDataWriter.writeActivity(
                    listOf(System.currentTimeMillis(), resources.configuration.orientation, event.x, event.y, event.pressure, event.action)
                )
            }
        }

        return super.dispatchTouchEvent(event)
    }

    override fun onPause() {
        super.onPause()
        pause()
    }

    override fun onResume() {
        super.onResume()
        play()
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    private fun initPlayer(){
        player = ExoPlayer.Builder(this)
            .build()
            .apply {
                setMediaItem(MediaItem.fromUri(mediaUri))
                prepare()
                addListener(playerListener)
            }
    }

    private fun releasePlayer(){
        player?.apply {
            playWhenReady = false
            release()
        }
        player = null
    }

    private fun pause(){
        player?.playWhenReady = false
    }

    private fun play(){
        player?.playWhenReady = true
    }

    private val playerListener = object: Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)

            when (playbackState) {
                STATE_ENDED -> {
                    Toast.makeText(
                        this@VideoActivity,
                        R.string.task_successfully_done,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                STATE_READY -> {
                    playerView.player = player
                    play()
                }
                else -> Unit
            }
        }
    }
}