package com.harshit.goswami.musicplayer

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.harshit.goswami.musicplayer.databinding.ActivityPlalistBinding
import com.harshit.goswami.musicplayer.databinding.AddPlaylistDialogBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Plalist : AppCompatActivity() {
    private lateinit var binding: ActivityPlalistBinding
   lateinit var adapter: PlalistAdapter
    companion object{
        var musicPlalist: MusicPlaylist = MusicPlaylist()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.themes[MainActivity.themeIndex])
        binding = ActivityPlalistBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.BackBtnPA.setOnClickListener{finish()}
        adapter =  PlalistAdapter(this, musicPlalist.ref)
        binding.plalistRV.setHasFixedSize(true)
        binding.plalistRV.setItemViewCacheSize(10)
        binding.plalistRV.layoutManager = GridLayoutManager(this,2)
        binding.plalistRV.adapter = adapter
        binding.addPlalist.setOnClickListener { customAlertDialog() }
    }
    private fun customAlertDialog(){
        val customDialog = LayoutInflater.from(this@Plalist).inflate(R.layout.add_playlist_dialog,binding.root,false)
        val binder = AddPlaylistDialogBinding.bind(customDialog)
        val builder = MaterialAlertDialogBuilder(this)
        builder.setView(customDialog)
            .setTitle("Plalist Details")
            .setPositiveButton("Add"){ dialog, _ ->
                val plalistName =  binder.playlistName.text
                val createdBy =  binder.yourName.text
                if (plalistName != null && createdBy != null)
                    if (plalistName.isNotEmpty() && createdBy.isNotEmpty()){
                        addPlalist(plalistName.toString(),createdBy.toString())
                    }

                dialog.dismiss()
        }.show()
    }

    private fun addPlalist(name: String, createdBy: String) {
        var plalistExist = false
        for (i in musicPlalist.ref){
            if (name.equals(i.name)){
                plalistExist = true
                break
            }
        }
        if (plalistExist) Toast.makeText(this,"Plalist Exist!!",Toast.LENGTH_SHORT).show()
        else{
            val temPlalist = AddPlaylist()
            temPlalist.name = name
            temPlalist.plalist = ArrayList()
            temPlalist.createdBy = createdBy
            val calender = Calendar.getInstance().time
            val sdf = SimpleDateFormat("dd MMM yyyy",Locale.ENGLISH)
            temPlalist.createdOn = sdf.format(calender)
            musicPlalist.ref.add(temPlalist)
            adapter.refreshPlalist()

        }
    }

    override fun onResume() {
        super.onResume()
    adapter.notifyDataSetChanged()
    }
}