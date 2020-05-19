package com.tutaapp.tuta.fargment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.tutaapp.tuta.R

class LocationFragment : Fragment(), OnMapReadyCallback {

    private lateinit var Map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        var mapFragment : SupportMapFragment?=null
        val TAG: String = MapFragment::class.java.simpleName
        fun newInstance() = MapFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        var rootView = inflater.inflate(R.layout.fragment_map, container, false)

        mapFragment = childFragmentManager.findFragmentById(
            R.id.map
        ) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@)

        return rootView
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        Map = googleMap!!

        val sydney = LatLng(-34.0, 151.0)
        Map.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        Map.moveCamera(CameraUpdateFactory.newLatLng(sydney))

    }


}