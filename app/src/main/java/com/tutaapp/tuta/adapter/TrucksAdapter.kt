package com.tutaapp.tuta.adapter

import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tutaapp.tuta.R
import com.tutaapp.tuta.model.Trucks


class TrucksAdapter(private val truckList: List<Trucks>) : RecyclerView.Adapter<TrucksAdapter.MyViewHolder>() {

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var truckload: TextView = view.findViewById<View>(R.id.textLoad) as TextView
        var truckdec1: TextView = view.findViewById<View>(R.id.textDecOne) as TextView


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyecler_item, parent, false)

        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val trucks = truckList[position]
        holder.truckload.text = Html.fromHtml(trucks.name)
        holder.truckdec1.text = trucks.description

    }

    override fun getItemCount(): Int {
        return truckList.size
    }
}

