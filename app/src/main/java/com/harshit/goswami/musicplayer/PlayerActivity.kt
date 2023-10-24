package com.harshit.goswami.musicplayer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.database.Cursor
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.harshit.goswami.musicplayer.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener {

    companion object {
        lateinit var musicListPA: ArrayList<Music>
        var songPosition: Int = 0
        var isPlaying = false
        var musicService: MusicService? = null
        lateinit var binding: ActivityPlayerBinding
        var repeat: Boolean = false
        var min15: Boolean = false
        var min30: Boolean = false
        var min60: Boolean = false
        var nowPlayingId: String = ""
        var isFavourite: Boolean = false
        var findex: Int = -1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.themes[MainActivity.themeIndex])
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        musicListPA = ArrayList()

        if (intent.data?.scheme.contentEquals("content")) {
            StartService()
            musicListPA = ArrayList()
            musicListPA.add(getMusicDetails(intent.data!!))
            Glide.with(this)
                .load(getImgArt(musicListPA[songPosition].path))
                .apply(RequestOptions().placeholder(R.drawable.musical_note_icon2)).centerCrop()
                .into(binding.playerImg)
            binding.txtSongNamePA.text = musicListPA[songPosition].title
        } else {
            initializeLayout()
        }
        binding.playerActivityBack.setOnClickListener { finish() }
        binding.playPauseBtn.setOnClickListener { if (isPlaying) pauseMusic() else playMusic() }
        binding.btnPrev.setOnClickListener { prevNextSong(false) }
        binding.btnNext.setOnClickListener { prevNextSong(true) }
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) musicService!!.mediaPlayer!!.seekTo(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })
        binding.repeatBtn.setOnClickListener {
            if (!repeat) {
                repeat = true
                binding.repeatBtn.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
            } else {
                repeat = false
                binding.repeatBtn.setColorFilter(ContextCompat.getColor(this, R.color.cool_pink))
            }
        }
        binding.eqlizerBtn.setOnClickListener {
            try {
                val eqIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                eqIntent.putExtra(
                    AudioEffect.EXTRA_AUDIO_SESSION,
                    musicService!!.mediaPlayer!!.audioSessionId
                )
                eqIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, baseContext.packageName)
                eqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                startActivityForResult(eqIntent, 100)
            } catch (e: Exception) {
                Toast.makeText(this, "Equalizer Feature not Supported", Toast.LENGTH_SHORT).show()
            }
        }
        binding.timerBtn.setOnClickListener {
            try {
                val timer = min15 || min30 || min60
                if (!timer) {
                    showBottomSheetDialog()
                } else {
                    val builder = MaterialAlertDialogBuilder(this)
                    builder.setTitle("Stop Timer").setMessage("Do you want to stop timer?")
                        .setPositiveButton("Yes") { _, _ ->
                            min15 = false
                            min30 = false
                            min60 = false
                            binding.timerBtn.setColorFilter(
                                ContextCompat.getColor(
                                    this,
                                    R.color.cool_pink
                                )
                            )

                        }
                        .setNegativeButton("No") { dialog, _ ->
                            dialog.dismiss()
                        }
                    val customDialog = builder.create()
                    customDialog.show()
                    customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
                    customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
                }
            } catch (e: Exception) {
                Toast.makeText(this, "$e", Toast.LENGTH_SHORT).show()
            }
        }
        binding.shareBtn.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "audio/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(musicListPA[songPosition].path))
            startActivity(Intent.createChooser(shareIntent, "Sharing Music File!!"))
        }
        binding.favouriteBtnPA.setOnClickListener {
            if (isFavourite) {
                isFavourite = false
                binding.favouriteBtnPA.setImageResource(R.drawable.ic_favorite_border)
                Favorate.favouriteSongs.removeAt(findex)
            } else {
                isFavourite = true
                binding.favouriteBtnPA.setImageResource(R.drawable.ic_favorite)
                Favorate.favouriteSongs.add(musicListPA[songPosition])
            }
        }
    }


    private fun setLayout() {
        Glide.with(applicationContext)
            .load(musicListPA[songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.musical_note_icon2)).centerCrop()
            .into(binding.playerImg)
        binding.txtSongNamePA.text = musicListPA[songPosition].title
        binding.tvSeekBarEnd.text = timeFormat(musicListPA[songPosition].duration)
        if (repeat) binding.repeatBtn.setColorFilter(
            ContextCompat.getColor(
                this,
                R.color.purple_500
            )
        )
        if (min15 || min30 || min60) binding.timerBtn.setColorFilter(
            ContextCompat.getColor(
                this,
                R.color.purple_500
            )
        )

        findex = favouriteChecker(musicListPA[songPosition].id)
        if (isFavourite) binding.favouriteBtnPA.setImageResource(R.drawable.ic_favorite)
        else binding.favouriteBtnPA.setImageResource(R.drawable.ic_favorite_border)

        //For applying gradiant
        val img = getImgArt(musicListPA[songPosition].path)
        val image = if (img != null) {
            BitmapFactory.decodeByteArray(img, 0, img.size)
        } else {
            BitmapFactory.decodeResource(resources, R.drawable.music_player_icon_playstore)
        }
        val bgColor = getMainColor(image)
        val gradient =
            GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, intArrayOf(0xFFFFFF, bgColor))
        binding.root.background = gradient
        window.statusBarColor = bgColor

    }

    private fun createMediaPlayer() {
        try {
            if (musicService!!.mediaPlayer == null) musicService!!.mediaPlayer = MediaPlayer()
            musicService!!.mediaPlayer!!.reset()
            musicService!!.mediaPlayer!!.setDataSource(musicListPA[songPosition].path)
            musicService!!.mediaPlayer!!.prepare()
            musicService!!.mediaPlayer!!.start()
            isPlaying = true
            binding.playPauseBtn.setIconResource(R.drawable.ic_pause)
            musicService!!.showNotification(R.drawable.ic_pause)
            binding.tvSeekBarEnd.text = timeFormat(musicService!!.mediaPlayer!!.duration.toLong())
            binding.tvSeekBarStart.text =
                timeFormat(musicService!!.mediaPlayer!!.currentPosition.toLong())
            binding.seekBar.progress = 0
            binding.seekBar.max = musicService!!.mediaPlayer!!.duration
            musicService!!.mediaPlayer!!.setOnCompletionListener(this)
            nowPlayingId = musicListPA[songPosition].id

        } catch (e: Exception) {
            return
        }
    }

    private fun initializeLayout() {
        songPosition = intent.getIntExtra("index", 0)
        when (intent.getStringExtra("class")) {
            "PlalistDitailsAdapter" -> {
                StartService()
                musicListPA = ArrayList()
                musicListPA.addAll(Plalist.musicPlalist.ref[PlalistDetails.currentPlalisPos].plalist)
                setLayout()
            }

            "plalistDetailsShuffle" -> {
                StartService()
                musicListPA = ArrayList()
                musicListPA.addAll(Plalist.musicPlalist.ref[PlalistDetails.currentPlalisPos].plalist)
                musicListPA.shuffle()
                setLayout()
            }

            "FavouriteShuffle" -> {
                StartService()
                musicListPA = ArrayList()
                musicListPA.addAll(Favorate.favouriteSongs)
                musicListPA.shuffle()
                setLayout()
            }

            "FavouriteAdapter" -> {
                StartService()
                musicListPA = ArrayList()
                musicListPA.addAll(Favorate.favouriteSongs)
                setLayout()
            }

            "MusicAdapterSearch" -> {
                StartService()
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.musicListSearch)
                setLayout()
            }

            "MusicAdapter" -> {
                StartService()
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.MusicListMA)
                setLayout()
            }

            "MainActivity" -> {
                StartService()
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.MusicListMA)
                musicListPA.shuffle()
                setLayout()
            }

            "NowPlaying" -> {
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.MusicListMA)
                setLayout()
                binding.tvSeekBarStart.text =
                    timeFormat(musicService!!.mediaPlayer!!.currentPosition.toLong())
                binding.tvSeekBarEnd.text =
                    timeFormat(musicService!!.mediaPlayer!!.duration.toLong())
                binding.seekBar.progress = musicService!!.mediaPlayer!!.currentPosition
                binding.seekBar.max = musicService!!.mediaPlayer!!.duration
                if (isPlaying) binding.playPauseBtn.setIconResource(R.drawable.ic_pause)
                else binding.playPauseBtn.setIconResource(R.drawable.ic_play)
            }
        }
    }

    private fun StartService() {
        val intentService = Intent(this, MusicService::class.java)
        bindService(intentService, this, BIND_AUTO_CREATE)
        startService(intentService)
    }

    private fun playMusic() {
        binding.playPauseBtn.setIconResource(R.drawable.ic_pause)
        musicService!!.showNotification(R.drawable.ic_pause)
        isPlaying = true
        musicService!!.mediaPlayer!!.start()
        musicService!!.onAudioFocusChange(1) //new
    }

    private fun pauseMusic() {
        binding.playPauseBtn.setIconResource(R.drawable.ic_play)
        try {
            musicService!!.showNotification(R.drawable.ic_play)
            isPlaying = false
            musicService!!.mediaPlayer!!.pause()
            musicService!!.onAudioFocusChange(0) //new

        } catch (e: Exception) {
            Log.e("main", e.toString())
        }
    }

    private fun prevNextSong(increment: Boolean) {
        if (increment) {
            setSongPosition(increment)
            setLayout()
            createMediaPlayer()
        } else {
            setSongPosition(false)
            setLayout()
            createMediaPlayer()
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        if (musicService == null) {
            val binder = service as MusicService.MyBinder
            musicService = binder.currentService()
            musicService!!.audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            musicService!!.audioManager.requestAudioFocus(
                musicService,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
        createMediaPlayer()
        musicService!!.seekbarSetup()
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }

    override fun onCompletion(mp: MediaPlayer?) {
        setSongPosition(true)
        setLayout()
        createMediaPlayer()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 || requestCode == RESULT_OK) {
            return
        }
    }

    private fun showBottomSheetDialog() {
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(R.layout.bottom_sheet_dialog)
        dialog.show()
        dialog.findViewById<LinearLayout>(R.id.min_15)!!.setOnClickListener {
            Toast.makeText(this, "Music will stop after 15 minutes", Toast.LENGTH_SHORT).show()
            binding.timerBtn.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
            min15 = true
            Thread {
                Thread.sleep(15 * 60000)
                if (min15) exitApplication()
            }.start()
            dialog.dismiss()
        }
        dialog.findViewById<LinearLayout>(R.id.min_30)!!.setOnClickListener {
            Toast.makeText(this, "Music will stop after 30 minutes", Toast.LENGTH_SHORT).show()
            binding.timerBtn.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
            min30 = true
            Thread {
                Thread.sleep(30 * 60000)
                if (min30) {
                    exitApplication()
                }
            }.start()
            dialog.dismiss()
        }
        dialog.findViewById<LinearLayout>(R.id.min_60)!!.setOnClickListener {
            Toast.makeText(this, "Music will stop after 60 minutes", Toast.LENGTH_SHORT).show()
            binding.timerBtn.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
            min60 = true
            Thread {
                Thread.sleep(60 * 60000)
                if (min60) {
                    exitApplication()
                }
            }.start()
            dialog.dismiss()
        }
    }

    private fun getMusicDetails(contentUris: Uri): Music {
        var cursor: Cursor? = null
        try {
            val projection = arrayOf(MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DURATION)
            cursor = this.contentResolver.query(contentUris, projection, null, null, null)
            val dataColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val durationColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            cursor!!.moveToFirst()
            val path = dataColumn?.let { cursor.getString(it) }
            val duration = durationColumn?.let { cursor.getLong(it) }!!

            return Music(
                id = "Unknown",
                album = "Unknown",
                title = path.toString(),
                artist = "Unknown",
                duration = duration,
                artUri = "Unknown",
                path = path.toString()
            )
        } finally {
            cursor?.close()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        if (musicListPA[songPosition].id == "Unknown" && !isPlaying) exitApplication()
    }
}
