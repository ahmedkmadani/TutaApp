package com.tutaapp.tuta.fargment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tutaapp.tuta.adapter.TrucksAdapter
import com.tutaapp.tuta.model.Trucks
import com.tutaapp.tuta.R
import java.util.ArrayList

class ServiceFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.fragment_service, container, false)
        val recyclerview = rootView.findViewById(R.id.recyclerview) as RecyclerView
        recyclerview.layoutManager = LinearLayoutManager(activity)

        val users = ArrayList<Trucks>()



        val adapter = TrucksAdapter(users)

        recyclerview.adapter = adapter

        return rootView
    }

}