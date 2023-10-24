package com.harshit.goswami.musicplayer

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.harshit.goswami.musicplayer.databinding.ActivityMainBinding
import java.io.File
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var bindind: ActivityMainBinding
    private lateinit var musicAdapter: MusicAdapter

    companion object {
        lateinit var MusicListMA: ArrayList<Music>
        lateinit var musicListSearch: ArrayList<Music>
        var search: Boolean = false
        var themeIndex: Int = 0
        val themes = arrayOf(
            R.style.coolPink,
            R.style.coolBlue,
            R.style.coolPurple,
            R.style.coolGreen,
            R.style.coolBlack
        )
        val navThemes = arrayOf(
            R.style.coolPinkNav,
            R.style.coolBlueNav,
            R.style.coolPurpleNav,
            R.style.coolGreenNav,
            R.style.coolBlackNav
        )
        val headerGradients = arrayOf(
            R.drawable.gradient_pink,
            R.drawable.gradient_blue,
            R.drawable.gradient_purple,
            R.drawable.gradient_green,
            R.drawable.gradient_black
        )
        var sortOrder: Int = 0
        val sortingList = arrayOf(
            MediaStore.Audio.Media.DATE_ADDED + " DESC",
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.SIZE + " DESC"
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val themeEditer = getSharedPreferences("THEMES", MODE_PRIVATE)
        themeIndex = themeEditer.getInt("themeIndex", 0)
        setTheme(navThemes[themeIndex])
        bindind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bindind.root)
        if (PlayerActivity.isPlaying) bindind.nowPlaying.visibility = View.VISIBLE
        // for nav drawer
        toggle = ActionBarDrawerToggle(this, bindind.root, R.string.open, R.string.close)
        bindind.root.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
// Chinging if request permission is granted or nor if granted than proceed
        if (requestRuntimePermission()) {
            try {
                initializeLayout()
            } catch (e: Exception) {
                Toast.makeText(this, "$e", Toast.LENGTH_SHORT).show()
            }
            // For retrieving favourite data using shared preferences
            Favorate.favouriteSongs = ArrayList()
            val editor = getSharedPreferences("Favourtes", MODE_PRIVATE)
            val jsonString = editor.getString("FavouriteSongs", null)
            val typeToken = object : TypeToken<ArrayList<Music>>() {}.type
            if (jsonString != null) {
                val data: ArrayList<Music> = GsonBuilder().create().fromJson(jsonString, typeToken)
                Favorate.favouriteSongs.addAll(data)
            }
            val jsonStringPlalist = editor.getString("MusicPlalist", null)
            if (jsonStringPlalist != null) {
                val dataPlalist: MusicPlaylist =
                    GsonBuilder().create().fromJson(jsonStringPlalist, MusicPlaylist::class.java)
                Plalist.musicPlalist = dataPlalist
            }
        }

        bindind.imgFavorate.setOnClickListener { startActivity(Intent(this, Favorate::class.java)) }
        bindind.imgPlalist.setOnClickListener { startActivity(Intent(this, Plalist::class.java)) }
        bindind.imgShuffle.setOnClickListener {
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra("index", 0)
            intent.putExtra("class", "MainActivity")
            startActivity(intent)
        }
        //Navigation Drawer
        bindind.navbar.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.feedback -> startActivity(Intent(this, FeedbackActivity::class.java))
                R.id.settings -> startActivity(Intent(this, SettingsActivity::class.java))
                R.id.about -> startActivity(Intent(this, AboutActivity::class.java))
                R.id.exit -> {
                    val builder = MaterialAlertDialogBuilder(this)
                    builder.setTitle("Exit").setMessage("Do you want to close app?")
                        .setPositiveButton("Yes") { _, _ ->
                            if (PlayerActivity.musicService != null) {
                                PlayerActivity.musicService!!.stopForeground(Service.STOP_FOREGROUND_DETACH)
                                PlayerActivity.musicService!!.mediaPlayer!!.release()
                                PlayerActivity.musicService = null
                            }
                            exitProcess(1)
                        }
                        .setNegativeButton("No") { dialog, _ ->
                            dialog.dismiss()
                        }
                    val customDialog = builder.create()
                    customDialog.show()
                    customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
                    customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
                }

            }
            true
        }

    }

    // For RunTime Permission
    private fun requestRuntimePermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.READ_MEDIA_AUDIO),
                    13
                )
//                return false
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    13
                )
//                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 13) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                initializeLayout()
            } else
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    13
                )
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item))
            return true
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("SetTextI18n")
    private fun initializeLayout() {
        search = false
        MusicListMA = ArrayList()
        val sortEditer = getSharedPreferences("SORTING", MODE_PRIVATE)
        sortOrder = sortEditer.getInt("sortOrder", 0)
        MusicListMA = getAllAudio()
        bindind.musicRV.setHasFixedSize(true)
        bindind.musicRV.setItemViewCacheSize(10)
        bindind.musicRV.layoutManager = LinearLayoutManager(this@MainActivity)
        musicAdapter = MusicAdapter(this@MainActivity, MusicListMA)
        bindind.musicRV.adapter = musicAdapter
        bindind.totalSong.text = "Total songs : " + MusicListMA.size
    }

    private fun getAllAudio(): ArrayList<Music> {
        val templist = ArrayList<Music>()
        val selection = MediaStore.Audio.Media.IS_MUSIC + "!=0"
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )

        val cursor = this.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sortingList[sortOrder]
        )
        if (cursor != null) {
            if (cursor.moveToFirst())
                do {
                    val titleC =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                    val albumC =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
                    val artistC =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                    val idC =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                    val durationC =
                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                    val pathC =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                    val albumIdC =
                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
                            .toString()
                    val uri = Uri.parse("content://media/external/audio/albumart")
                    val artUriC = Uri.withAppendedPath(uri, albumIdC).toString()
                    val music = Music(idC, albumC, artistC, titleC, durationC, pathC, artUriC)

                    val file = File(music.path)
                    if (file.exists())
                        templist.add(music)
                } while (cursor.moveToNext())
            cursor.close()
        }
        return templist
    }

    override fun onResume() {
        super.onResume()
        if (PlayerActivity.isPlaying) bindind.nowPlaying.visibility = View.VISIBLE
        // For storing favourite data using shared preferences
        val editor = getSharedPreferences("Favourtes", MODE_PRIVATE).edit()
        val jsonString = GsonBuilder().create().toJson(Favorate.favouriteSongs)
        editor.putString("FavouriteSongs", jsonString)
        val jsonStringPlalist = GsonBuilder().create().toJson(Plalist.musicPlalist)
        editor.putString("MusicPlalist", jsonStringPlalist)
        editor.apply()
        //for Sorting
        val sortEditer = getSharedPreferences("SORTING", MODE_PRIVATE)
        val sortValue = sortEditer.getInt("sortOrder", 0)
        if (sortOrder != sortValue) {
            sortOrder = sortValue
            MusicListMA = getAllAudio()
            musicAdapter.updateMusicList(MusicListMA)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!PlayerActivity.isPlaying && PlayerActivity.musicService != null) {
            exitApplication()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_view_menu, menu)
        findViewById<RelativeLayout>(R.id.navHeader)?.setBackgroundResource(headerGradients[themeIndex])
        val searchView = menu?.findItem(R.id.searchView)?.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                musicListSearch = ArrayList()
                if (newText != null) {
                    val userInput = newText.lowercase()
                    for (song in MusicListMA)
                        if (song.title.lowercase().contains(userInput))
                            musicListSearch.add(song)
                    search = true
                    musicAdapter.updateMusicList(musicListSearch)
                }
                return true
            }

        })
        return super.onCreateOptionsMenu(menu)
    }
}