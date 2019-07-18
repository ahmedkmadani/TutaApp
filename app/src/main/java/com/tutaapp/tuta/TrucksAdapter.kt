package com.tutaapp.tuta

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class TrucksAdapter(private val truckList: List<Trucks>) : RecyclerView.Adapter<TrucksAdapter.MyViewHolder>() {

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var truckload: TextView = view.findViewById<View>(R.id.textLoad) as TextView
        var truckdec1: TextView = view.findViewById<View>(R.id.textDecOne) as TextView
        var truckdec2: TextView = view.findViewById<View>(R.id.textDecTwo) as TextView
        var truckdec3: TextView = view.findViewById<View>(R.id.textDecThree) as TextView

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyecler_item, parent, false)

        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val trucks = truckList[position]
        holder.truckload.text = trucks.truckload
        holder.truckdec1.text = trucks.truckdec1
        holder.truckdec2.text = trucks.truckdec2
        holder.truckdec3.text = trucks.truckdec3
    }

    override fun getItemCount(): Int {
        return truckList.size
    }
}

