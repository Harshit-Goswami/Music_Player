package com.harshit.goswami.musicplayer

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder
import com.harshit.goswami.musicplayer.databinding.ActivityPlailistDetailsBinding

class PlalistDetails : AppCompatActivity() {
    lateinit var binding: ActivityPlailistDetailsBinding
    lateinit var adapter: MusicAdapter
    companion object{
        var currentPlalisPos:Int = -1
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.themes[MainActivity.themeIndex])
        binding = ActivityPlailistDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        currentPlalisPos = intent.extras?.get("index") as Int
        Plalist.musicPlalist.ref[currentPlalisPos].plalist = checkPlalist(Plalist.musicPlalist.ref[currentPlalisPos].plalist)

        binding.backBtnPD.setOnClickListener { finish() }

        binding.playlistDetailsRV.setItemViewCacheSize(15)
        binding.playlistDetailsRV.setHasFixedSize(true)
        binding.playlistDetailsRV.layoutManager = LinearLayoutManager(this)
        adapter = MusicAdapter(this,Plalist.musicPlalist.ref[currentPlalisPos].plalist,plalistDetails = true)
        binding.playlistDetailsRV.adapter = adapter

        binding.shuffleBtnPD.setOnClickListener {
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra("index",0)
            intent.putExtra("class","plalistDetailsShuffle")
            startActivity(intent)
        }
        binding.addBtnPD.setOnClickListener {
            startActivity(Intent(this , SelectionActivity::class.java))
        }
        binding.backBtnPD.setOnClickListener { finish() }
        binding.removeAllPD.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(this)
            builder.setTitle("Remove").setMessage("Do you want to remove all from plalist?")
                .setPositiveButton("Yes"){ dialog, _ ->
                 Plalist.musicPlalist.ref[currentPlalisPos].plalist.clear()
                    adapter.refreshPlalist()
                    dialog.dismiss()
                }
                .setNegativeButton("No"){ dialog, _ ->
                    dialog.dismiss()
                }
            val customDialog = builder.create()
            customDialog.show()
            customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
            customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
        }

    }
 
    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        binding.playlistNamePD.text = Plalist.musicPlalist.ref[currentPlalisPos].name
        binding.moreInfoPD.text = "Total ${adapter.itemCount} Songs. \n\n" +
                "Create On:\n ${Plalist.musicPlalist.ref[currentPlalisPos].createdOn}\n\n" +
                " --${Plalist.musicPlalist.ref[currentPlalisPos].createdBy}"
        if (adapter.itemCount > 0){
            Glide.with(this)
                .load(Plalist.musicPlalist.ref[currentPlalisPos].plalist[0].artUri)
                .apply(RequestOptions().placeholder(R.drawable.musical_note_icon2)).centerCrop().into(binding.playlistImgPD)
            binding.shuffleBtnPD.visibility = View.VISIBLE
        }
        adapter.notifyDataSetChanged()

        val editor = getSharedPreferences("Favourtes", MODE_PRIVATE).edit()
        val jsonStringPlalist = GsonBuilder().create().toJson(Plalist.musicPlalist)
        editor.putString("MusicPlalist",jsonStringPlalist)
        editor.apply()
    }
}