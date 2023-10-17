package com.harshit.goswami.musicplayer

import android.app.Service
import android.media.AudioFocusRequest
import android.media.MediaMetadataRetriever
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

class Music(
    val id: String,
    val album: String,
    val artist: String,
    val title: String,
    val duration: Long = 0,
    val path: String,
    val artUri: String
)

class AddPlaylist {
    lateinit var name: String
    lateinit var plalist: ArrayList<Music>
    lateinit var createdBy: String
    lateinit var createdOn: String
}

class MusicPlaylist {
    var ref: ArrayList<AddPlaylist> = ArrayList()
}

fun timeFormat(duration: Long): String {
    val minutes = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
    val seconds = (TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS) -
            minutes * TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES))
    return String.format("%02d:%02d", minutes, seconds)
}

fun getImgArt(path: String): ByteArray? {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(path)
    return retriever.embeddedPicture
}

fun setSongPosition(increment: Boolean) {
    if (!PlayerActivity.repeat) {
        if (increment) {
            if (PlayerActivity.musicListPA.size - 1 == PlayerActivity.songPosition)
                PlayerActivity.songPosition = 0
            else ++PlayerActivity.songPosition
        } else {
            if (PlayerActivity.songPosition == 0)
                PlayerActivity.songPosition = PlayerActivity.musicListPA.size - 1
            else --PlayerActivity.songPosition
        }
    }
}

fun exitApplication() {
    if (PlayerActivity.musicService != null) {
        PlayerActivity.musicService!!.audioManager
            .abandonAudioFocus(PlayerActivity.musicService)
        PlayerActivity.musicService!!.stopForeground(Service.STOP_FOREGROUND_DETACH)
        PlayerActivity.musicService!!.mediaPlayer!!.release()
        PlayerActivity.musicService = null
        exitProcess(1)
    }
}

fun favouriteChecker(id: String): Int {
    PlayerActivity.isFavourite = false
    Favorate.favouriteSongs.forEachIndexed { index, music ->
        if (id == music.id) {
            PlayerActivity.isFavourite = true
            return index
        }
    }
    return -1
}

fun checkPlalist(plalist: ArrayList<Music>): ArrayList<Music> {
    plalist.forEachIndexed { index, music ->
        val file = File(music.path)
        if (!file.exists())
            plalist.removeAt(index)
    }
    return plalist
}