package com.harshit.goswami.musicplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            ApplicationClass.PREVIOUS -> prevNextSong(false, context!!)

            ApplicationClass.PLAY -> {
                if (PlayerActivity.isPlaying) pausemusic() else playmusic()
            }

            ApplicationClass.NEXT -> prevNextSong(true, context!!)
            ApplicationClass.EXIT -> {
                exitApplication()
            }

        }
    }

    fun playmusic() {
        PlayerActivity.isPlaying = true
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        PlayerActivity.musicService!!.showNotification(R.drawable.ic_pause)
        PlayerActivity.binding.playPauseBtn.setIconResource(R.drawable.ic_pause)
        NowPlaying.binding.playPauseBtnNP.setIconResource(R.drawable.ic_pause)
    }

    fun pausemusic() {
        PlayerActivity.isPlaying = false
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        PlayerActivity.musicService!!.showNotification(R.drawable.ic_play)
        PlayerActivity.binding.playPauseBtn.setIconResource(R.drawable.ic_play)
        NowPlaying.binding.playPauseBtnNP.setIconResource(R.drawable.ic_play)

    }

    fun prevNextSong(increment: Boolean, context: Context) {
        setSongPosition(increment)
        PlayerActivity.musicService!!.createMediaPlayer()
        Glide.with(context)
            .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.musical_note_icon2)).centerCrop()
            .into(
                PlayerActivity.binding.playerImg
            )
        PlayerActivity.binding.txtSongNamePA.text =
            PlayerActivity.musicListPA[PlayerActivity.songPosition].title
        PlayerActivity.binding.tvSeekBarEnd.text =
            timeFormat(PlayerActivity.musicListPA[PlayerActivity.songPosition].duration)
        //NowPlaying fragment
        Glide.with(context)
            .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.musical_note_icon2)).centerCrop()
            .into(
                NowPlaying.binding.songImgNP
            )
        NowPlaying.binding.songNameNP.text =
            PlayerActivity.musicListPA[PlayerActivity.songPosition].title
        playmusic()
        PlayerActivity.findex =
            favouriteChecker(PlayerActivity.musicListPA[PlayerActivity.songPosition].id)
        if (PlayerActivity.isFavourite) PlayerActivity.binding.favouriteBtnPA.setImageResource(R.drawable.ic_favorite)
        else PlayerActivity.binding.favouriteBtnPA.setImageResource(R.drawable.ic_favorite_border)
    }
}

