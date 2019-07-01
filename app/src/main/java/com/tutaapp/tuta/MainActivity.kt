package com.tutaapp.tuta

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {


    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val users = ArrayList<Trucks>()
        val adapter = CustomAdapter(users)


        recyclerview!!.adapter = adapter
        recyclerview!!.layoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
        recyclerview.adapter = adapter

        users.add(Trucks("1 Ton Truck", "Basic appliance","Quick loading", "Easy access"))
        users.add(Trucks("3 Ton Truck", "Medium appliance", "Heavy loading", "Extra help"))
        users.add(Trucks("5 Ton Truck", "Heavy appliance", "Bulk loading", "More manpower"))



        adapter.notifyDataSetChanged()





    }
}



 

