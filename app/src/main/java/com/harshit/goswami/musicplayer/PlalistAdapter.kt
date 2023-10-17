package com.harshit.goswami.musicplayer

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.harshit.goswami.musicplayer.databinding.PlalistViewBinding

class PlalistAdapter(private val context: Context, private var plalistList: ArrayList<AddPlaylist>): RecyclerView.Adapter<PlalistAdapter.MyHolder>() {
    class MyHolder(binding: PlalistViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val image = binding.plalistImg
        val name = binding.plalistText
        val deleteImg = binding.plalistDelete
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(PlalistViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.name.text = plalistList[position].name
        holder.name.isSelected = true
        holder.deleteImg.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(context)
            builder.setTitle(plalistList[position].name).setMessage("Do you want to delete plalist?")
                .setPositiveButton("Yes"){ dialog, _ ->
                    Plalist.musicPlalist.ref.removeAt(position)
                    refreshPlalist()
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
        holder.root.setOnClickListener {
            val intent = Intent(context,PlalistDetails::class.java)
            intent.putExtra("index", position)
            context.startActivity(intent)
        }
        if (Plalist.musicPlalist.ref[position].plalist.size > 0)
        {
            Glide.with(context)
                .load(Plalist.musicPlalist.ref[position].plalist[0].artUri)
                .apply(RequestOptions().placeholder(R.drawable.musical_note_icon2)).centerCrop().into(holder.image)
        }
    }

    override fun getItemCount(): Int {
        return plalistList.size
    }
    @SuppressLint("NotifyDataSetChanged")
    fun refreshPlalist(){
        plalistList = ArrayList()
        plalistList.addAll(Plalist.musicPlalist.ref)
        notifyDataSetChanged()
    }
}
