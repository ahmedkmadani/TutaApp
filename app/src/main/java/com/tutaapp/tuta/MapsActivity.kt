package com.tutaapp.tuta

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
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
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
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
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.tutaapp.tuta.model.TrucksDetails
import com.tutaapp.tuta.model.User
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.android.synthetic.main.bottom_sheet_order.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.lang.Byte.decode
import java.lang.Integer.decode
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
    lateinit var DROP_LAT: String
    lateinit var DROP_LON: String
    lateinit var PICK_LAT: String
    lateinit var PICK_LON: String
    lateinit var addressText: String
    lateinit var DROP_TEXT: String


    val PERMISSION_ID = 42
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var sheetBehaviorOne: BottomSheetBehavior<LinearLayout>
    private lateinit var sheetBehaviorTwo: BottomSheetBehavior<LinearLayout>


    val truck_deatils = ArrayList<TrucksDetails>()
    lateinit var dialog:AlertDialog

    var placesFileds = Arrays.asList(Place.Field.ID,
        Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)

    lateinit var placesClient:PlacesClient
    val AUTOCOMPLETE_REQUEST_CODE = 123


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)


        val user = SharedPrefManager.getInstance(this).user
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        sheetBehaviorOne = BottomSheetBehavior.from(bottom_sheet)
        sheetBehaviorTwo = BottomSheetBehavior.from(bottom_sheet_order)

        sheetBehaviorOne.state = BottomSheetBehavior.STATE_HIDDEN
        sheetBehaviorTwo.state = BottomSheetBehavior.STATE_HIDDEN

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mGeoDataClient = Places.getGeoDataClient(this, null)

        viewDialog = ViewDialog(this)

        TRUCK_ID = intent.getStringExtra("TRUCK_ID")
        token = user.token


        com.google.android.libraries.places.api.Places.initialize(this, getString(R.string.google_maps_key))
        placesClient = com.google.android.libraries.places.api.Places.createClient(this)
        val builder = AlertDialog.Builder(this)


        builder.setTitle("Our apologies ${user.FirstName}")
        builder.setMessage("There is no nearby trcuk at this moment .. \n You want to try again ?" )
        builder.setPositiveButton("Yes") { _, _ ->
            getLastLocation()
        }
        builder.setNegativeButton("No") { _, _ ->
            finish()
        }

        dialog = builder.create()
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
                    val data = jsonObject.getJSONObject("data")
                    val vehicle_locations = data.getJSONArray("vehicle_locations")
                    Log.d("res", "$jsonObject")

                    if(data != null) {

                        for (i in 0 until vehicle_locations.length()) {

                            val VechicleObject = vehicle_locations.getJSONObject(i)

                            truck_deatils.add(
                                TrucksDetails(
                                    VechicleObject.getInt("id"),
                                    VechicleObject.getInt("vehicle_id"),
                                    VechicleObject.getString("latitude"),
                                    VechicleObject.getString("longitude"),
                                    VechicleObject.getString("created_at"),
                                    VechicleObject.getString("updated_at"),
                                    VechicleObject.getString("deleted_at")

                                    )
                            )


                            val VehicleLocations = LatLng(truck_deatils[i].latitude!!.toDouble(),truck_deatils[i].longitude!!.toDouble())
                            map.addMarker(MarkerOptions().position(VehicleLocations).title(truck_deatils[i].vehicle_id.toString()))

                        }

                        Log.d("res", "$jsonObject")

                    } else {

                        viewDialog.hideDialog()
                        dialog.show()
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "There is no close Truck", Snackbar.LENGTH_LONG).setAction("Try Again") {
                            getLastLocation()

                        }.show()
                    }

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
                    Log.d("error", error.toString())
                viewDialog.hideDialog()
                dialog.show()

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
        stringRequest.retryPolicy =
            DefaultRetryPolicy(20 * 1000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        requestQueue.add(stringRequest)

    }

    private fun expandCloseSheet(currentLatLng: LatLng) {
        if (sheetBehaviorOne.state != BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehaviorOne.state = BottomSheetBehavior.STATE_EXPANDED


            val user_Location = getAddress(currentLatLng)
            user_location_pickup.text = user_Location
            user_location_drop.setOnClickListener {
                initPlaceSerach()
            }

            btn_drop.setOnClickListener {
                sheetBehaviorOne.state = BottomSheetBehavior.STATE_HIDDEN
                GetEstimate(currentLatLng.latitude, currentLatLng.longitude, DROP_LAT, DROP_LON, TRUCK_ID, token)
            }


        } else {
            sheetBehaviorOne.state = BottomSheetBehavior.STATE_COLLAPSED
            val UserLocation = getAddress(currentLatLng)
            user_location_pickup.text = UserLocation

        }

        map.isMyLocationEnabled = true
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
    }

    private fun GetEstimate(latitude: Double, longitude: Double, dropLat: String, dropLon: String, truckId: String, token: String) {
        viewDialog.showDialog()
        val stringRequest: StringRequest = object : StringRequest( Method.POST, URLs.URL_TRIP_ESTIMATE,
            Response.Listener { response ->

                try {

                    val jsonObject = JSONObject(response)
                    val Data = jsonObject.getJSONObject("data")

                    val Estimate = Data.getString("estimate")
                    viewDialog.hideDialog()

                    showOrderSheet(Estimate, latitude, longitude)

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error ->

                Snackbar.make(
                    findViewById(android.R.id.content),
                    error.toString(), Snackbar.LENGTH_LONG).show()
                Log.d("error", error.toString())
                viewDialog.hideDialog()

            }) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["vehicle_type_id"] = truckId
                params["start_latitude"] =  latitude.toString()
                params["start_longitude"] = longitude.toString()
                params["stop_latitude"] = dropLat
                params["stop_longitude"] = dropLon

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
        stringRequest.retryPolicy =
            DefaultRetryPolicy(20 * 1000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        requestQueue.add(stringRequest)

    }

    private fun initPlaceSerach() {

       val intent = Intent(Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, placesFileds).build(this))
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE ) {
            if (resultCode == Activity.RESULT_OK ) {

                val place = Autocomplete.getPlaceFromIntent(data!!)
                DROP_LAT = place.latLng!!.latitude.toString()
                DROP_LON = place.latLng!!.longitude.toString()
                DROP_TEXT = place.name.toString()
                user_location_drop.text = place.name

                val dropLocations = LatLng(place.latLng!!.latitude,place.latLng!!.longitude)
//                DrawRoute(place.latLng!!.latitude,place.latLng!!.longitude, DROP_LAT, DROP_LON)
                map.addMarker(MarkerOptions().position(dropLocations).title("Drop Location"))


            } else if (resultCode == AutocompleteActivity.RESULT_ERROR){
                val status = Autocomplete.getStatusFromIntent(data!!)
                Log.d("tag", status.statusMessage)
                user_location_drop.text = "Sorry, we could'nt get the location"

            } else if (resultCode == RESULT_CANCELED) {

            }
        }

    }

    private fun DrawRoute(latitude: Double, longitude: Double, dropLat: String, dropLon: String) {

        val path: MutableList<List<LatLng>> = ArrayList()
        val urlDirections = "https://maps.googleapis.com/maps/api/directions/json?origin=$latitude,$longitude&destination=$dropLat,$dropLon&key=${this.getString(R.string.google_maps_key)}"
        val directionsRequest = object : StringRequest(Method.GET, urlDirections, Response.Listener {
                response ->
            val jsonResponse = JSONObject(response)
            Log.d("rest", response)
            print(response)

            val routes = jsonResponse.getJSONArray("routes")
            val legs = routes.getJSONObject(0).getJSONArray("legs")
            val steps = legs.getJSONObject(0).getJSONArray("steps")

            for (i in 0 until steps.length()) {
                val points = steps.getJSONObject(i).getJSONObject("polyline").getString("points")
                Log.d("rest", points)
            }
            for (i in 0 until path.size) {
               map!!.addPolyline(PolylineOptions().addAll(path[i]).color(Color.RED))
            }
        }, Response.ErrorListener {
                _ ->
        }){}
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(directionsRequest)
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        viewDialog.showDialog()
        if (checkPermissions()) {
            if (isLocationEnabled()) {

                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    var location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {

                        PICK_LAT = location.latitude.toString()
                        PICK_LON = location.longitude.toString()
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
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var mLastLocation: Location = locationResult.lastLocation

            PICK_LON = mLastLocation.latitude.toString()
            PICK_LON = mLastLocation.longitude.toString()

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

    private fun getAddress(location: LatLng): String {
        val geocoder = Geocoder(this)
        val addresses: List<Address>?
        addressText = ""

        try {

            addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (null != addresses && !addresses.isEmpty()) {

                addressText = addresses[0].getAddressLine(0)

            }
        } catch (e: IOException) {
            Log.d("MapsActivity", e.localizedMessage)
            Snackbar.make(
                findViewById(android.R.id.content),
                e.localizedMessage, Snackbar.LENGTH_LONG).show()
        }

        return addressText
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastLocation()
            }
        }
    }

    private fun showOrderSheet(estimate: String, latitude: Double, longitude: Double) {
        if (sheetBehaviorTwo.state != BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehaviorTwo.state = BottomSheetBehavior.STATE_EXPANDED
            txtTripPrice.text = estimate + " RAND"
            txt_pick.text = addressText
            txt_drop.text = DROP_TEXT

            btn_confrim_order.setOnClickListener {
                sheetBehaviorOne.state = BottomSheetBehavior.STATE_HIDDEN
                StoreTrip(latitude,longitude, DROP_LAT, DROP_LON, TRUCK_ID, token)

            }

        } else {
            sheetBehaviorTwo.state = BottomSheetBehavior.STATE_COLLAPSED


        }


    }

    private fun StoreTrip(latitude: Double, longitude: Double, dropLat: String, dropLon: String, truckId: String, token: String) {
        viewDialog.showDialog()
        val stringRequest: StringRequest = object : StringRequest( Method.POST, URLs.URL_STORE_TRIP,
            Response.Listener { response ->

                try {

                    val jsonObject = JSONObject(response)
                    val Data = jsonObject.getJSONObject("data")
                    Log.d("res", jsonObject.toString())
                    Log.d("data res", Data.toString())
                    viewDialog.hideDialog()


                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error ->

                Snackbar.make(
                    findViewById(android.R.id.content),
                    error.toString(), Snackbar.LENGTH_LONG).show()
                Log.d("error", error.toString())
                viewDialog.hideDialog()

            }) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["vehicle_type_id"] = truckId
                params["start_latitude"] =  latitude.toString()
                params["start_longitude"] = longitude.toString()
                params["stop_latitude"] = dropLat
                params["stop_longitude"] = dropLon

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
        stringRequest.retryPolicy =
            DefaultRetryPolicy(20 * 1000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        requestQueue.add(stringRequest)

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

        }

        btn_cancel.setOnClickListener {
            map.clear()
            dialog.dismiss()
//            setUpMap()

        }
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

    private fun ShowConifrmdailog() {


        val cDialogView = LayoutInflater.from(this).inflate(R.layout.confirm_order_dailog, null)
        val mBuilder = AlertDialog.Builder(this).setView(cDialogView)

        val mAlertDialog = mBuilder.show()

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