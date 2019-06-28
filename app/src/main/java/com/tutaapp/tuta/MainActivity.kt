package com.tutaapp.tuta

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(ServiceFragment(), "Service")

        adapter.addFragment(LocationFragment(), "Location")
        adapter.addFragment(ThreeFragment(), "Three")
        viewPager.adapter = adapter
        tabs.setupWithViewPager(viewPager)

    }
}
