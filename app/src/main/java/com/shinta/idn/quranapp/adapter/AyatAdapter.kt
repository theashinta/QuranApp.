package com.shinta.idn.quranapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shinta.idn.quranapp.R
import com.shinta.idn.quranapp.model.ModelAyat

class AyatAdapter(
    private val mContext: Context,
private val items: List<ModelAyat>
): RecyclerView.Adapter<AyatAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AyatAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_ayat, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: AyatAdapter.ViewHolder, position: Int) {
        val data = items[position]
        holder.tvNomorAyat.text = data.nomor
        holder.tvArabic.text = data.arab
        holder.tvTerjemahan.text = data.indo

    }

    class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        var tvNomorAyat: TextView
        var tvArabic: TextView
        var tvTerjemahan: TextView

        init {
            tvNomorAyat = itemView.findViewById(R.id.tvNomerAyat)
            tvArabic = itemView.findViewById(R.id.tvArabic)
            tvTerjemahan = itemView.findViewById(R.id.tvTerjemahan)
        }

    }
}
