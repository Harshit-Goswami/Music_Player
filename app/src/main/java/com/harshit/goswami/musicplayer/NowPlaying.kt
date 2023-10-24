package com.harshit.goswami.musicplayer

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.harshit.goswami.musicplayer.databinding.FragmentNowPlayingBinding


class NowPlaying : Fragment() {
    companion object{
    lateinit var binding: FragmentNowPlayingBinding}
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        requireContext().theme.applyStyle(MainActivity.themes[MainActivity.themeIndex],true)

        val view  =  inflater.inflate(R.layout.fragment_now_playing, container, false)
        binding = FragmentNowPlayingBinding.bind(view)
        binding.root.visibility = View.INVISIBLE
        binding.songNameNP.isSelected = true

        binding.playPauseBtnNP.setOnClickListener {
            if (PlayerActivity.isPlaying) pauseMusic() else playMusic() }

        binding.nextBtnNP.setOnClickListener {
            setSongPosition(true)
            PlayerActivity.musicService!!.createMediaPlayer()
            Glide.with(this)
                .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
                .apply(RequestOptions().placeholder(R.drawable.musical_note_icon2)).centerCrop().into(
                    binding.songImgNP)
            binding.songNameNP.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
            PlayerActivity.musicService!!.showNotification(R.drawable.ic_pause)
            playMusic()
        }

        binding.root.setOnClickListener{
            val intent = Intent(requireContext(),PlayerActivity::class.java)
            intent.putExtra("index",PlayerActivity.songPosition)
            intent.putExtra("class","NowPlaying")
            ContextCompat.startActivity(requireContext(),intent,null)
        }
        return view
    }
    override fun onResume() {
        super.onResume()
        if (PlayerActivity.musicService != null)
            binding.root.visibility = View.VISIBLE
        try {
        Glide.with(this)
            .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.musical_note_icon2)).centerCrop().into(
                binding.songImgNP)
       binding.songNameNP.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
        if (PlayerActivity.isPlaying) binding.playPauseBtnNP.setIconResource(R.drawable.ic_pause)
        else binding.playPauseBtnNP.setIconResource(R.drawable.ic_play)
        }catch (e: Exception){
         e.printStackTrace()
        }
    }
    private fun playMusic(){
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        binding.playPauseBtnNP.setIconResource(R.drawable.ic_pause)
        PlayerActivity.binding.playPauseBtn.setIconResource(R.drawable.ic_pause)
        PlayerActivity.musicService!!.showNotification(R.drawable.ic_pause)
        PlayerActivity.musicService!!.onAudioFocusChange(1)//new
        PlayerActivity.isPlaying = true
    }
    private fun pauseMusic(){
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        binding.playPauseBtnNP.setIconResource(R.drawable.ic_play)
        PlayerActivity.binding.playPauseBtn.setIconResource(R.drawable.ic_play)
        PlayerActivity.musicService!!.showNotification(R.drawable.ic_play)
        PlayerActivity.musicService!!.onAudioFocusChange(0)//new
        PlayerActivity.isPlaying = false
    }
}