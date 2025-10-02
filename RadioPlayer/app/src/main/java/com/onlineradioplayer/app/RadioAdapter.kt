package com.onlineradioplayer.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RadioAdapter(
    private val radios: List<Radio>,
    private val onPlayClick: (Radio, Int) -> Unit,
    private val onDeleteClick: (Radio) -> Unit
) : RecyclerView.Adapter<RadioAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.radioName)
        val url: TextView = view.findViewById(R.id.radioUrl)
        val play: ImageButton = view.findViewById(R.id.playBtn)
        val delete: ImageButton = view.findViewById(R.id.deleteBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_radio, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val r = radios[position]
        holder.name.text = r.name
        holder.url.text = r.url
        holder.play.setOnClickListener { onPlayClick(r, position) }
        holder.delete.setOnClickListener { onDeleteClick(r) }
    }

    override fun getItemCount(): Int = radios.size
}
