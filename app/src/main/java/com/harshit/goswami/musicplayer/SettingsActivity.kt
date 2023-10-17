package com.harshit.goswami.musicplayer

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.harshit.goswami.musicplayer.databinding.ActivitySettingsBinding
import kotlin.system.exitProcess

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySettingsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.navThemes[MainActivity.themeIndex])
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Settings"
        when(MainActivity.themeIndex){
            0 -> binding.coolPinkTheme.setBackgroundColor(Color.GRAY)
            1 -> binding.coolBlueTheme.setBackgroundColor(Color.GRAY)
            2 -> binding.coolPurpleTheme.setBackgroundColor(Color.GRAY)
            3 -> binding.coolGreenTheme.setBackgroundColor(Color.GRAY)
            4 -> binding.coolBlackTheme.setBackgroundColor(Color.GRAY)
        }
        binding.versionName.text = setVersonDetail()
        binding.coolPinkTheme.setOnClickListener { saveTheme(0) }
        binding.coolBlueTheme.setOnClickListener { saveTheme(1) }
        binding.coolPurpleTheme.setOnClickListener { saveTheme(2) }
        binding.coolGreenTheme.setOnClickListener { saveTheme(3) }
        binding.coolBlackTheme.setOnClickListener { saveTheme(4) }
        binding.sortBtn.setOnClickListener {
           val menuList  = arrayOf("Recently Added","SongTitle","FileSize")
            var currentSort = MainActivity.sortOrder
            val builder = MaterialAlertDialogBuilder(this)
            builder.setTitle("Sorting")
                .setPositiveButton("OK"){ _, _ ->
                    val editer = getSharedPreferences("SORTING", MODE_PRIVATE).edit()
                    editer.putInt("sortOrder",currentSort)
                    editer.apply()
                }
                .setSingleChoiceItems(menuList,currentSort){_,SortIndex ->
                    currentSort = SortIndex
                }
            val customDialog = builder.create()
            customDialog.show()
            customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
        }
    }
    private fun saveTheme(index: Int){
        if(MainActivity.themeIndex != index){
            val editer = getSharedPreferences("THEMES", MODE_PRIVATE).edit()
            editer.putInt("themeIndex",index)
            editer.apply()
            val builder = MaterialAlertDialogBuilder(this)
            builder.setTitle("Apply Theme").setMessage("Do you want to apply theme?")
                .setPositiveButton("Yes"){ _, _ ->
                    exitProcess(1)
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
    private fun setVersonDetail():String {
        return "Version Name: ${BuildConfig.VERSION_NAME}"
    }
}

