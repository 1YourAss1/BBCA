@file:UnstableApi

package ru.mtuci.bbca.video

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.STATE_ENDED
import androidx.media3.common.Player.STATE_READY
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import ru.mtuci.bbca.data.Preferences
import ru.mtuci.bbca.R
import ru.mtuci.bbca.data.Task
import ru.mtuci.bbca.main.MainActivity
import ru.mtuci.bbca.sensors_data_writer.sensorsDataWriter
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
            currentSessionPath = Preferences.getSessionPath(),
            directoryName = "video",
            activityName = "video",
            activityColumns = listOf("timestamp", "orientation", "x_coordinate", "y_coordinate", "pressure", "action_type")
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_video)

        sensorsDataWriter(
            currentSessionPath = Preferences.getSessionPath(),
            directoryName = "video",
        )

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

                    sendBroadcast(
                        Intent(MainActivity.TASK_DONE_KEY).apply {
                            putExtra(MainActivity.TASK_DONE_KEY, Task.VIDEO)
                        }
                    )
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