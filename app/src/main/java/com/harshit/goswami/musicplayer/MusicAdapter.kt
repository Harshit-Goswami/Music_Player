package com.harshit.goswami.musicplayer

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.harshit.goswami.musicplayer.databinding.MusicViewBinding

class MusicAdapter(private val context:Context, private var musicList: ArrayList<Music>,private val plalistDetails: Boolean = false
,private val selectionActivity: Boolean = false): RecyclerView.Adapter<MusicAdapter.MyHolder>() {
    class MyHolder(binding:MusicViewBinding) : RecyclerView.ViewHolder(binding.root) {
      val title=binding.itemSongName
        val album =binding.itemAlbum
        val image=binding.itemImg
        val duration =binding.itemDuration
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
    return MyHolder(MusicViewBinding.inflate(LayoutInflater.from(context),parent,false))
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.title.text =musicList[position].title
        holder.album.text =musicList[position].album
        holder.duration.text = timeFormat(musicList[position].duration)
        Glide.with(context)
            .load(musicList[position].artUri)
            .apply(RequestOptions().placeholder(R.drawable.musical_note_icon2)).centerCrop().into(holder.image)
        when{
            plalistDetails -> {
                holder.root.setOnClickListener {
                    sendIntent("PlalistDitailsAdapter",position)
                }
            }
            selectionActivity ->{
                holder.root.setOnClickListener {
                    if (addSong(musicList[position]))
                        holder.root.setBackgroundColor(ContextCompat.getColor(context,R.color.cool_pink))
                    else
                        holder.root.setBackgroundColor(ContextCompat.getColor(context,R.color.white))

                }
            }
            else -> {
                    holder.root.setOnClickListener{
                        when{
                            MainActivity.search -> sendIntent("MusicAdapterSearch", pos = position)

                            musicList[position].id == PlayerActivity.nowPlayingId ->
                                sendIntent("NowPlaying",PlayerActivity.songPosition)

                            else ->  sendIntent(ref = "MusicAdapter", pos = position)
                        }
                    }
                }
        }

    }

    private fun addSong(song: Music): Boolean {
       Plalist.musicPlalist.ref[PlalistDetails.currentPlalisPos].plalist.forEachIndexed { index, music ->
           if (song.id == music.id){
               Plalist.musicPlalist.ref[PlalistDetails.currentPlalisPos].plalist.removeAt(index)
               return false
           }
       }
        Plalist.musicPlalist.ref[PlalistDetails.currentPlalisPos].plalist.add(song)
    return true
    }

    override fun getItemCount(): Int {
       return musicList.size
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateMusicList(searchList : ArrayList<Music>){
        musicList = ArrayList()
        musicList.addAll(searchList)
        notifyDataSetChanged()
    }
    private fun sendIntent(ref: String ,pos: Int){
        val intent = Intent(context,PlayerActivity::class.java)
        intent.putExtra("index",pos)
        intent.putExtra("class",ref)
  //      context.startActivity(intent)
         ContextCompat.startActivity(context,intent,null)
    }
    fun refreshPlalist(){
        musicList = ArrayList()
        musicList = Plalist.musicPlalist.ref[PlalistDetails.currentPlalisPos].plalist
        notifyDataSetChanged()
    }
}