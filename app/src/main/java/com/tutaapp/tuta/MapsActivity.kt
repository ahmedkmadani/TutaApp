package com.tutaapp.tuta

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.*
import com.google.android.gms.location.places.GeoDataClient
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.bottom_sheet.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var map: GoogleMap
    private lateinit var lastLocation: Location

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var mGeoDataClient: GeoDataClient

    lateinit var placesAdapter: PlacesAdapter
    var isAutoCompleteLocation = false

    internal lateinit var user: User
    internal lateinit var viewDialog: ViewDialog

    private lateinit var  location: Location
    private lateinit var mLastLocation: Location

    lateinit var TRUCK_ID: String
    lateinit var token: String

    val PERMISSION_ID = 42
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var sheetBehavior: BottomSheetBehavior<LinearLayout>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)


        val user = SharedPrefManager.getInstance(this).user
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        sheetBehavior = BottomSheetBehavior.from<LinearLayout>(bottom_sheet)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mGeoDataClient = Places.getGeoDataClient(this, null)

        viewDialog = ViewDialog(this)

        TRUCK_ID = intent.getStringExtra("TRUCK_ID")
        token = user.token

        viewDialog.showDialog()

    }


    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map = googleMap

        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)
        map.uiSettings.isZoomControlsEnabled = true

        map.setOnMarkerClickListener(this)
        getLastLocation()

    }


    private fun getAllTrucks(token: String, truckId: String, latitude: Double, longitude: Double) {

        val currentLatLng = LatLng(latitude, longitude)

        val stringRequest: StringRequest = object : StringRequest( Method.POST, URLs.URL_GET_All_TRUCKS,
            Response.Listener { response ->

                try {

                    val jsonObject = JSONObject(response)
                    Log.d("Response", "$jsonObject")

                    viewDialog.hideDialog()
                    expandCloseSheet(currentLatLng)

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error ->

                Snackbar.make(
                    findViewById(android.R.id.content),
                    error.toString(), Snackbar.LENGTH_LONG).show()

                viewDialog.hideDialog()

            }) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["vehicle_type_id"] = truckId
                params["latitude"] =  latitude.toString()
                params["longitude"] = longitude.toString()
                return params
            }

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $token"
                headers["Content-Type"] = "application/x-www-form-urlencoded"
                return headers
            }

        }
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(stringRequest)
    }




    private fun expandCloseSheet(currentLatLng: LatLng) {
        if (sheetBehavior!!.state != BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED

            val user_Location = getAddress(currentLatLng)
            user_location_pickup.text = user_Location

        } else {
            sheetBehavior!!.state = BottomSheetBehavior.STATE_COLLAPSED

            val UserLocation = getAddress(currentLatLng)
            user_location_pickup.text = UserLocation

        }

        map.isMyLocationEnabled = true
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
        map.addMarker(MarkerOptions().position(currentLatLng).title("Current Location"))

    }



    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {

                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    var location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        getAllTrucks(token , TRUCK_ID , location.latitude , location.longitude )
                    }
                }
            } else {

                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Turn on location", Snackbar.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient!!.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var mLastLocation: Location = locationResult.lastLocation
            getAllTrucks(token, TRUCK_ID, mLastLocation.latitude, mLastLocation.longitude)
        }
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION),
            PERMISSION_ID
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastLocation()
            }
        }
    }

    private fun getAddress(location: LatLng): String {
        val geocoder = Geocoder(this)
        val addresses: List<Address>?
        var addressText = ""

        try {

            addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (null != addresses && !addresses.isEmpty()) {

                addressText = addresses[0].getAddressLine(0)
                Log.d("Adress", addresses.toString())
                Log.d("User Address : ", addressText)

            }
        } catch (e: IOException) {
            Log.d("MapsActivity", e.localizedMessage)
            Snackbar.make(
                    findViewById(android.R.id.content),
                e.localizedMessage, Snackbar.LENGTH_LONG).show()
        }

        return addressText
    }




    private fun showBottomSheetDialog(location: LatLng) {
        val view = layoutInflater.inflate(R.layout.bottom_sheet, null)
        val dialog = BottomSheetDialog(this)

        val btn_drop = view.findViewById(R.id.btn_drop) as Button
        val txt_pickup = view.findViewById(R.id.txt_pickup) as TextView
//        val edit_drop = view.findViewById(R.id.edit_drop) as AutoCompleteTextView

        dialog.setContentView(view)
        dialog.show()

        val usr_pickup_loc = getAddress(location)
        Log.e("usr loc" , usr_pickup_loc)
        txt_pickup.text = usr_pickup_loc

        map.setOnMarkerClickListener {
            dialog.show()
            false
        }

        btn_drop.setOnClickListener {

            val desLatLng = LatLng(5.6421535, -0.155071)
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(desLatLng, 12f))
            map.addMarker(MarkerOptions().position(desLatLng ).title("Distention Location"))

            dialog.dismiss()
            showDetentionSheet(usr_pickup_loc)

        }


    }


    private fun showDetentionSheet(usr_pickup_loc: String) {

        val view = layoutInflater.inflate(R.layout.bottom_sheet_des, null)
        val dialog = BottomSheetDialog(this)

        val btn_order = view.findViewById(R.id.btn_order) as Button
        val btn_cancel = view.findViewById(R.id.btn_cancel) as Button

        val txt_pickup = view.findViewById(R.id.txt_pickup) as TextView
        txt_pickup.text = usr_pickup_loc

        dialog.setContentView(view)
        dialog.show()

        btn_order.setOnClickListener {

            dialog.dismiss()
            showOrderSheet(usr_pickup_loc)

        }

        btn_cancel.setOnClickListener {
            map.clear()
            dialog.dismiss()
//            setUpMap()

        }
    }


//    private fun placeMarkerOnMap(location: LatLng) {
//        val markerOptions = MarkerOptions().position(location)
//        val usr_address = getAddress(location)  // add these two lines
//        Toast.makeText(applicationContext, usr_address , Toast.LENGTH_SHORT)
//            .show()
//
//    }

    private fun showOrderSheet(usr_pickup_loc: String) {

        val view = layoutInflater.inflate(R.layout.bottom_sheet_order, null)
        val dialog = BottomSheetDialog(this)

        val btn_confirm_order = view.findViewById(R.id.btn_confrim_order) as Button
        val btn_back = view.findViewById(R.id.btn_back) as Button

        val txt_pickup = view.findViewById(R.id.txt_pick) as TextView
        txt_pickup.text = usr_pickup_loc

        dialog.setContentView(view)
        dialog.show()

        btn_confirm_order.setOnClickListener {

            ShowConifrmdailog()

        }

        btn_back.setOnClickListener {
            dialog.dismiss()
            showDetentionSheet(usr_pickup_loc)
        }


    }

    private fun ShowConifrmdailog() {


        val cDialogView = LayoutInflater.from(this).inflate(R.layout.confirm_order_dailog, null)
        val mBuilder = AlertDialog.Builder(this).setView(cDialogView)

        val mAlertDialog = mBuilder.show()

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