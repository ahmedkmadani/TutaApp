package com.tutaapp.tuta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList

class ServiceFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.fragment_service, container, false)
        val recyclerview = rootView.findViewById(R.id.recyclerview) as RecyclerView
        recyclerview.layoutManager = LinearLayoutManager(activity)

        val users = ArrayList<Trucks>()

        users.add(Trucks("1 Ton Truck", "Basic appliance","Quick loading", "Easy access"))
        users.add(Trucks("3 Ton Truck", "Medium appliance", "Heavy loading", "Extra help"))
        users.add(Trucks("5 Ton Truck", "Heavy appliance", "Bulk loading", "More manpower"))

        val adapter = CustomAdapter(users)

        recyclerview.adapter = adapter

        return rootView
    }

}