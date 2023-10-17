package com.harshit.goswami.musicplayer

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.harshit.goswami.musicplayer.databinding.ActivityFavorateBinding

class Favorate : AppCompatActivity() {
    private lateinit var binding : ActivityFavorateBinding
    companion object{var favouriteSongs : ArrayList<Music> = ArrayList()}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(MainActivity.themes[MainActivity.themeIndex])
        binding = ActivityFavorateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.BackBtnFA.setOnClickListener{ finish() }

        favouriteSongs = checkPlalist(favouriteSongs)
        val adapter =  FavorateAdapter(this, favouriteSongs)
        binding.favorateRV.setHasFixedSize(true)
        binding.favorateRV.setItemViewCacheSize(10)
        binding.favorateRV.layoutManager = GridLayoutManager(this,4)
        binding.favorateRV.adapter = adapter
        if (favouriteSongs.size < 1 ) binding.shuffleBtnFA.visibility = View.INVISIBLE
        binding.shuffleBtnFA.setOnClickListener {
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra("index",0)
            intent.putExtra("class","FavouriteShuffle")
            startActivity(intent)
        }
    }

}