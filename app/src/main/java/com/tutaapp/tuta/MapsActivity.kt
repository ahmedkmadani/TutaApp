package com.tutaapp.tuta

import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.GeoDataClient
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.IOException




class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var map: GoogleMap
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var mGeoDataClient: GeoDataClient
    lateinit var placesAdapter: PlacesAdapter
    val BOUNDS_INDIA = LatLngBounds(LatLng(23.63936, 68.14712), LatLng(28.20453, 97.34466))
    var isAutoCompleteLocation = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mGeoDataClient = Places.getGeoDataClient(this, null);

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)
        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)

        setUpMap()

    }


    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        map.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->

            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
                map.addMarker(MarkerOptions().position(currentLatLng).title("Current Location"))
                showBottomSheetDialog(currentLatLng)
            }
        }
    }


    private fun showBottomSheetDialog(location: LatLng) {
        val view = layoutInflater.inflate(R.layout.bottom_sheet, null)
        val dialog = BottomSheetDialog(this)

        val btn_drop = view.findViewById(R.id.btn_drop) as Button
        val txt_pickup = view.findViewById(R.id.txt_pickup) as TextView
        val edit_drop = view.findViewById(R.id.edit_drop) as AutoCompleteTextView

        dialog.setContentView(view)
        dialog.show()

        val usr_address = getAddress(location)
        txt_pickup.text = usr_address

        map!!.setOnMarkerClickListener {
            dialog.show()
            false
        }

        btn_drop.setOnClickListener {

            val desLatLng = LatLng(5.6421535, -0.155071)
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(desLatLng, 12f))
            map.addMarker(MarkerOptions().position(desLatLng ).title("Distention Location"))

            dialog.dismiss()
            showDetentionSheet()

        }

//        placesAdapter = PlacesAdapter(this, android.R.layout.simple_list_item_1, mGeoDataClient, null, BOUNDS_INDIA)
//        edit_drop.setAdapter(placesAdapter)
//
//        edit_drop.addTextChangedListener(object : TextWatcher {
//            override fun afterTextChanged(s: Editable?) {
//            }
//
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//            }
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                if (count > 0) {
////                    cancel.visibility = View.VISIBLE
//                } else {
////                    cancel.visibility = View.GONE
//                }
//            }
//        })
//
//
//        edit_drop.setOnItemClickListener { parent, view, position, id ->
//            //getLatLong(placesAdapter.getPlace(position))
//            hideKeyboard()
//            val item = placesAdapter.getItem(position)
//            val placeId = item?.getPlaceId()
//            val primaryText = item?.getPrimaryText(null)
//
//            Log.i("Autocomplete", "Autocomplete item selected: " + primaryText)
//
//
//            val placeResult = mGeoDataClient.getPlaceById(placeId)
//            placeResult.addOnCompleteListener { task ->
//                val places = task.result
//                val place = places!!.get(0)
//
//                isAutoCompleteLocation = true
//
//                places!!.release()
//            }
//
//            Toast.makeText(
//                applicationContext, "Clicked: " + primaryText,
//                Toast.LENGTH_SHORT
//            ).show()
//        }
////        cancel.setOnClickListener {
////            enter_place.setText("")
////        }


    }


    private fun showDetentionSheet() {

        val view = layoutInflater.inflate(R.layout.bottom_sheet_des, null)
        val dialog = BottomSheetDialog(this)

        val btn_order = view.findViewById(R.id.btn_order) as Button
        val btn_cancel = view.findViewById(R.id.btn_cancel) as Button

        dialog.setContentView(view)
        dialog.show()

        btn_order.setOnClickListener {

            dialog.dismiss()
            showOrderSheet()

        }

        btn_cancel.setOnClickListener {
            map.clear()
            dialog.dismiss()
            setUpMap()

        }
    }


//    private fun placeMarkerOnMap(location: LatLng) {
//        val markerOptions = MarkerOptions().position(location)
//        val usr_address = getAddress(location)  // add these two lines
//        Toast.makeText(applicationContext, usr_address , Toast.LENGTH_SHORT)
//            .show()
//
//    }

    private fun showOrderSheet() {

        val view = layoutInflater.inflate(R.layout.bottom_sheet_order, null)
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(view)
        dialog.show()


    }




    private fun getAddress(latLng: LatLng): String {
        val geocoder = Geocoder(this)
        val addresses: List<Address>?
        val address: Address?
        var addressText = ""

        try {

            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

            if (null != addresses && !addresses.isEmpty()) {
                address = addresses[0]
                for (i in 0 until address.maxAddressLineIndex) {
                    addressText += if (i == 0) address.getAddressLine(i) else "\n" + address.getAddressLine(i)
                }
            }
        } catch (e: IOException) {
            Log.e("MapsActivity", e.localizedMessage)
        }

        return addressText
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }


    override fun onMarkerClick(p0: Marker?) = false



    fun hideKeyboard() {
        try {
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

}