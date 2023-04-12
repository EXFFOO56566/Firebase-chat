package com.devlomi.fireapp.activities.placespicker

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.devlomi.fireapp.R
import kotlinx.android.synthetic.main.row_place.view.*

class NearbyPlacesAdapter(private val context: Context, private val places: List<Place>) : RecyclerView.Adapter<NearbyPlacesAdapter.NearbyPlacesHolder>() {
    var onClickListener: OnClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): NearbyPlacesHolder {
        val row = LayoutInflater.from(parent.context).inflate(R.layout.row_place, parent, false)
        return NearbyPlacesHolder(row)
    }

    override fun getItemCount(): Int = places.size

    override fun onBindViewHolder(holder: NearbyPlacesHolder, position: Int) {
        holder.bind(places[position])
    }

    inner class NearbyPlacesHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(place: Place) {
            itemView.tv_place_name.text = place.name
            itemView.tv_place_address.text = place.address
            Glide.with(context).load(place.iconUrl).into(itemView.icon_location)

            itemView.setOnClickListener {
                onClickListener?.onClick(it, place)
            }
        }
    }

    interface OnClickListener {
        fun onClick(view: View, place: Place)
    }
}