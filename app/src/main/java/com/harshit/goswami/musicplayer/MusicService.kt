package com.harshit.goswami.musicplayer

import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.KeyEvent
import androidx.core.app.NotificationCompat
import com.google.android.exoplayer2.ControlDispatcher
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector

class MusicService : Service(), AudioManager.OnAudioFocusChangeListener {
    private var myBinder = MyBinder()
    var mediaPlayer: MediaPlayer? = null
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var runnable: Runnable
    lateinit var audioManager: AudioManager

    override fun onBind(intent: Intent?): IBinder {
        mediaSession = MediaSessionCompat(baseContext, "My Music")
        return myBinder
    }

    fun showNotification(playPauseBtn: Int, playbackSpeed: Float) {
        val intent = Intent(baseContext, MainActivity::class.java)
        intent.putExtra("notiClick", true)
        val contentIntent = PendingIntent.getActivity(
            baseContext, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )//,0

        val prevIntent = Intent(
            baseContext,
            NotificationReceiver::class.java
        ).setAction(ApplicationClass.PREVIOUS)

        val prevPendingIntent =
            PendingIntent.getBroadcast(baseContext, 0, prevIntent, PendingIntent.FLAG_IMMUTABLE)

        val playIntent =
            Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.PLAY)
        val playPendingIntent =
            PendingIntent.getBroadcast(baseContext, 0, playIntent, PendingIntent.FLAG_IMMUTABLE)

        val nextIntent =
            Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.NEXT)
        val nextPendingIntent =
            PendingIntent.getBroadcast(baseContext, 0, nextIntent, PendingIntent.FLAG_IMMUTABLE)

        val exitIntent =
            Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.EXIT)
        val exitPendingIntent =
            PendingIntent.getBroadcast(baseContext, 0, exitIntent, PendingIntent.FLAG_IMMUTABLE)

        val imgArt = getImgArt(PlayerActivity.musicListPA[PlayerActivity.songPosition].path)
        val image = if (imgArt != null) {
            BitmapFactory.decodeByteArray(imgArt, 0, imgArt.size)
        } else {
            BitmapFactory.decodeResource(resources, R.drawable.music_player_icon_playstore)
        }

        val notification = NotificationCompat.Builder(baseContext, ApplicationClass.CHANNEL_ID)
            .setContentIntent(contentIntent)
            .setContentTitle(PlayerActivity.musicListPA[PlayerActivity.songPosition].title)
            .setContentText("Music Player")
            .setSmallIcon(R.drawable.musical_note_icon)
            .setLargeIcon(image)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            //   .setCustomContentView(RemoteViews("hello",))
            //   .setCustomHeadsUpContentView(RemoteViews)
            //   .setCustomBigContentView()
            .addAction(R.drawable.ic_previous, "Previous", prevPendingIntent)
            .addAction(playPauseBtn, "" + " Play", playPendingIntent)
            .addAction(R.drawable.ic_next, "Next", nextPendingIntent)
            .addAction(R.drawable.ic_exit, "Exit", exitPendingIntent)

        //  Build.VERSION_CODES.S = android 12
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mediaSession.setMetadata(
                MediaMetadataCompat.Builder()
                    .putLong(
                        MediaMetadataCompat.METADATA_KEY_DURATION,
                        mediaPlayer!!.duration.toLong()
                    )
                    .build()
            )
            val playBackState = PlaybackStateCompat.Builder()
                .setState(
                    PlaybackStateCompat.STATE_PLAYING,
                    mediaPlayer!!.currentPosition.toLong(),
                    playbackSpeed
                )
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY
                            or PlaybackStateCompat.ACTION_PAUSE
                            or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                            or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                            or PlaybackStateCompat.ACTION_SEEK_TO
                )
                .addCustomAction(ApplicationClass.EXIT, "Exit", R.drawable.ic_exit)
                .build()
            mediaSession.apply {
                setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
                setPlaybackState(playBackState)
            }
            val callback = object : MediaSessionCompat.Callback() {
                override fun onSeekTo(pos: Long) {
                    super.onSeekTo(pos)
                    mediaPlayer!!.seekTo(pos.toInt())
                    val playBackStateNew = PlaybackStateCompat.Builder()
                        .setState(
                            PlaybackStateCompat.STATE_PLAYING,
                            mediaPlayer!!.currentPosition.toLong(),
                            playbackSpeed
                        )
                        .setActions(
                            PlaybackStateCompat.ACTION_PLAY
                                    or PlaybackStateCompat.ACTION_PAUSE
                                    or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                                    or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                                    or PlaybackStateCompat.ACTION_SEEK_TO
                        )
                        .addCustomAction(ApplicationClass.EXIT, "Exit", R.drawable.ic_exit)
                        .build()
                    mediaSession.setPlaybackState(playBackStateNew)
                }

                override fun onPlay() {
                    Log.d("mediaBtn", "play called")
                    NotificationReceiver().playmusic()
                    val playBackStateNew = PlaybackStateCompat.Builder()
                        .setState(
                            PlaybackStateCompat.STATE_PLAYING,
                            mediaPlayer!!.currentPosition.toLong(),
                            playbackSpeed
                        )
                        .setActions(
                            PlaybackStateCompat.ACTION_PLAY
                                    or PlaybackStateCompat.ACTION_PAUSE
                                    or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                                    or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                                    or PlaybackStateCompat.ACTION_SEEK_TO

                        )
                        .addCustomAction(ApplicationClass.EXIT, "Exit", R.drawable.ic_exit)
                        .build()
                    mediaSession.setPlaybackState(playBackStateNew)
                }

                override fun onPause() {
                    Log.d("mediaBtn", "pause called")
                    NotificationReceiver().pausemusic()
                    val playBackStateNew = PlaybackStateCompat.Builder()
                        .setState(
                            PlaybackStateCompat.STATE_PAUSED,
                            mediaPlayer!!.currentPosition.toLong(),
                            playbackSpeed
                        )
                        .setActions(
                            PlaybackStateCompat.ACTION_PLAY
                                    or PlaybackStateCompat.ACTION_PAUSE
                                    or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                                    or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                                    or PlaybackStateCompat.ACTION_SEEK_TO
                        )
                        .addCustomAction(ApplicationClass.EXIT, "Exit", R.drawable.ic_exit)
                        .build()
                    mediaSession.setPlaybackState(playBackStateNew)
                }

                override fun onSkipToNext() {
                    NotificationReceiver().prevNextSong(true, baseContext)
                    val playBackStateNew = PlaybackStateCompat.Builder()
                        .setState(
                            PlaybackStateCompat.STATE_PLAYING,
                            mediaPlayer!!.currentPosition.toLong(),
                            playbackSpeed
                        )
                        .setActions(
                            PlaybackStateCompat.ACTION_PLAY
                                    or PlaybackStateCompat.ACTION_PAUSE
                                    or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                                    or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                                    or PlaybackStateCompat.ACTION_SEEK_TO
                        )
                        .addCustomAction(ApplicationClass.EXIT, "Exit", R.drawable.ic_exit)
                        .build()

                    mediaSession.setPlaybackState(playBackStateNew)
                }

                override fun onSkipToPrevious() {
                    NotificationReceiver().prevNextSong(false, baseContext)
                    val playBackStateNew = PlaybackStateCompat.Builder()
                        .setState(
                            PlaybackStateCompat.STATE_PLAYING,
                            mediaPlayer!!.currentPosition.toLong(),
                            playbackSpeed
                        )
                        .setActions(
                            PlaybackStateCompat.ACTION_PLAY
                                    or PlaybackStateCompat.ACTION_PAUSE
                                    or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                                    or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                                    or PlaybackStateCompat.ACTION_SEEK_TO
                        )
                        .addCustomAction(ApplicationClass.EXIT, "Exit", R.drawable.ic_exit)
                        .build()
                    mediaSession.setPlaybackState(playBackStateNew)
                }

                override fun onCustomAction(action: String?, extras: Bundle?) {
                    when (action) {
                        ApplicationClass.EXIT -> exitApplication()
                    }
                }

               /* override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
                    val event: KeyEvent = mediaButtonEvent?.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
                        ?: return false
                    if(event.action == KeyEvent.ACTION_UP) {
                        when(event.keyCode) {
                            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                                Log.d("keyevent","EvenFired")
                            }
                            KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                                Log.d("keyevent","EvenFired")

                            }
                            KeyEvent.KEYCODE_MEDIA_PLAY -> {
                                Log.d("keyevent","EvenFired")

                            }
                            KeyEvent.KEYCODE_MEDIA_NEXT -> {
                                Log.d("keyevent","EvenFired")

                            } KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                                Log.d("keyevent","EvenFired")

                            }
                        }
                    }
                    return true
                }*/
            }
            mediaSession.setCallback(callback)
            mediaSession.isActive = true

        }

        startForeground(13, notification.build())
    }

    fun createMediaPlayer() {
        try {
            if (PlayerActivity.musicService!!.mediaPlayer == null) PlayerActivity.musicService!!.mediaPlayer =
                MediaPlayer()
            PlayerActivity.musicService!!.mediaPlayer!!.reset()
            PlayerActivity.musicService!!.mediaPlayer!!.setDataSource(PlayerActivity.musicListPA[PlayerActivity.songPosition].path)
            PlayerActivity.musicService!!.mediaPlayer!!.prepare()
            PlayerActivity.binding.playPauseBtn.setIconResource(R.drawable.ic_pause)
            PlayerActivity.musicService!!.showNotification(R.drawable.ic_pause, 0F)
            PlayerActivity.binding.tvSeekBarEnd.text =
                timeFormat(mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.binding.tvSeekBarStart.text = timeFormat(mediaPlayer!!.duration.toLong())
            PlayerActivity.binding.seekBar.progress = 0
            PlayerActivity.binding.seekBar.max = mediaPlayer!!.duration
            PlayerActivity.nowPlayingId = PlayerActivity.musicListPA[PlayerActivity.songPosition].id
        } catch (e: Exception) {
            return
        }
    }

    fun seekbarSetup() {
        runnable = Runnable {
            PlayerActivity.binding.tvSeekBarStart.text =
                timeFormat(mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.binding.seekBar.progress = mediaPlayer!!.currentPosition
            Handler(Looper.getMainLooper()).postDelayed(runnable, 200)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable, 0)
    }

    inner class MyBinder : Binder() {
        fun currentService(): MusicService {
            return this@MusicService
        }
    }

    override fun onAudioFocusChange(focusChange: Int) {
        if (focusChange <= 0) {
            PlayerActivity.binding.playPauseBtn.setIconResource(R.drawable.ic_play)
            NowPlaying.binding.playPauseBtnNP.setIconResource(R.drawable.ic_play)
            showNotification(R.drawable.ic_play, 0F)
            PlayerActivity.isPlaying = false
            mediaPlayer!!.pause()
        } else {
            PlayerActivity.binding.playPauseBtn.setIconResource(R.drawable.ic_pause)
            NowPlaying.binding.playPauseBtnNP.setIconResource(R.drawable.ic_pause)
            showNotification(R.drawable.ic_pause, 1F)
            PlayerActivity.isPlaying = true
            mediaPlayer!!.start()
        }

    }
}