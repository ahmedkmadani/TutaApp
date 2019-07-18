package com.tutaapp.tuta

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val truck = ArrayList<Trucks>()
        val adapter = TrucksAdapter(truck)

        recyclerview!!.adapter = adapter

        recyclerview!!.setHasFixedSize(true)

        val mLayoutManager = LinearLayoutManager(applicationContext)

        recyclerview!!.layoutManager = mLayoutManager

        recyclerview!!.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))

        recyclerview!!.itemAnimator = DefaultItemAnimator()

        recyclerview!!.adapter = adapter

        recyclerview!!.addOnItemTouchListener(
            RecyclerTouchListener(
                applicationContext,
                recyclerview!!,
                object : RecyclerTouchListener.ClickListener {
                    override fun onLongClick(view: View?, position: Int) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onClick(view: View, position: Int) {
                        val truck = truck.get(position)
                        Toast.makeText(applicationContext, truck.truckload + " is selected!", Toast.LENGTH_SHORT)
                            .show()
                        startActivity(Intent(this@MainActivity, MapsActivity::class.java))
                        finish()
                    }

                })
        )


        truck.add(Trucks("1 Ton Truck", "Basic appliance","Quick loading", "Easy access"))
        truck.add(Trucks("3 Ton Truck", "Medium appliance", "Heavy loading", "Extra help"))
        truck.add(Trucks("5 Ton Truck", "Heavy appliance", "Bulk loading", "More manpower"))


        adapter!!.notifyDataSetChanged()
    }

}