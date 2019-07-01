package com.tutaapp.tuta

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.*


class CustomAdapter (private val trucksList: ArrayList<Trucks>) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val v = LayoutInflater.from(p0?.context).inflate(R.layout.recyecler_item, p0, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return trucksList.size
    }



    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        val truck: Trucks = trucksList[p1]

        p0?.txtViewLoad?.text = truck.load
        p0?.txtViewDecOne?.text = truck.dec1
        p0?.txtViewDecTwo?.text = truck.dec2
        p0?.txtViewDecThree?.text = truck.dec3

    }

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val txtViewLoad = itemView.findViewById(R.id.textLoad) as TextView
        val txtViewDecOne = itemView.findViewById(R.id.textDecOne) as TextView
        val txtViewDecTwo = itemView.findViewById(R.id.textDecTwo) as TextView
        val txtViewDecThree = itemView.findViewById(R.id.textDecThree) as TextView


    }

}