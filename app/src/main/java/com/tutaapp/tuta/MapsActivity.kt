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
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
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
import com.google.android.material.snackbar.Snackbar
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.channel.PrivateChannelEventListener
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange
import com.pusher.client.util.HttpAuthorizer
import com.tutaapp.tuta.model.TrucksDetails
import com.tutaapp.tuta.model.User
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.android.synthetic.main.bottom_sheet_order.*
import kotlinx.android.synthetic.main.bottom_sheet_start_trip.*
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

    lateinit var DROP_LAT: String
    lateinit var DROP_LON: String

    lateinit var PICK_LAT: String
    lateinit var PICK_LON: String

    lateinit var addressText: String
    lateinit var DROP_TEXT: String
    lateinit var UserId: String


    val PERMISSION_ID = 42
    lateinit var mFusedLocationClient: FusedLocationProviderClient

    private lateinit var sheetBehaviorOne: BottomSheetBehavior<LinearLayout>
    private lateinit var sheetBehaviorTwo: BottomSheetBehavior<LinearLayout>
    private lateinit var sheetBehaviorThree: BottomSheetBehavior<LinearLayout>


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
        sheetBehaviorThree = BottomSheetBehavior.from(bottom_sheet_start_trip)

        sheetBehaviorOne.state = BottomSheetBehavior.STATE_HIDDEN
        sheetBehaviorTwo.state = BottomSheetBehavior.STATE_HIDDEN
        sheetBehaviorThree.state = BottomSheetBehavior.STATE_HIDDEN


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mGeoDataClient = Places.getGeoDataClient(this, null)

        viewDialog = ViewDialog(this)

        TRUCK_ID = intent.getStringExtra("TRUCK_ID")
        token = user.token
        UserId = user.Id

        Log.d("User Id", UserId)
        Log.d("token", token)



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


    private fun expandStartSheet(driverName: String?, pickUpLatLng: LatLng, dropOfftLatLng: LatLng) {

            sheetBehaviorThree.state = BottomSheetBehavior.STATE_EXPANDED

            val user_pickup_Location = getAddress(pickUpLatLng)
            val user_dropoff_Location = getAddress(dropOfftLatLng)

            driver_name.text = driverName
            user_trip_pickup.text = user_pickup_Location
            user_trip_drop.text = user_dropoff_Location

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
                    val Trip = Data.getJSONObject("trip")

                    Log.d("res", jsonObject.toString())
                    Log.d("data res", Data.toString())
                    Log.d("trip res", Trip.toString())


                    val start_latitude = Trip.getString("start_latitude")
                    val start_longitude = Trip.getString("start_longitude")

                    val stop_latitude = Trip.getString("stop_latitude")
                    val stop_longitude = Trip.getString("stop_longitude")

                    OnSuccess(start_latitude,start_longitude,stop_latitude,stop_longitude)
//                    OnSuccess()

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

    private fun OnSuccess(startLatitude: String, startLongitude: String, stopLatitude: String, stopLongitude: String) {

        val PickUpLatLng = LatLng(startLatitude.toDouble(), startLongitude.toDouble())
        val DropOfftLatLng = LatLng(stopLatitude.toDouble(), stopLongitude.toDouble())

        val options = PusherOptions()
        options.setCluster("eu")

        val headers: HashMap<String, String> = HashMap()
        headers["Authorization"] = "Bearer $token"
        headers["Content-Type"] = "application/x-www-form-urlencoded"
        headers["Accept"] = "application/json"

        val authorizer = HttpAuthorizer(URLs.URL_AUTH)
        options.setAuthorizer(authorizer).isEncrypted
        authorizer.setHeaders(headers)

        val pusher = Pusher("0d7a677e7fd7526b0c97", options)

        pusher.connect(object: ConnectionEventListener {
            override fun onConnectionStateChange(change: ConnectionStateChange) {
                println(("State changed from " + change.previousState +
                        " to " + change.currentState))
            }

            override fun onError(message:String, code:String, e:Exception) {
                println(("There was a problem connecting! " +
                        "\ncode: " + code +
                        "\nmessage: " + message +
                        "\nException: " + e)
                )
            }
        }, ConnectionState.ALL)


        val channel = pusher.subscribePrivate("private-App.User.$UserId", object:
            PrivateChannelEventListener {
            override fun onEvent(channel: String?, eventName: String?, data: String?) {
                Log.d("Channel", channel)
                Log.d("Event Name", eventName)
                Log.d("Data", data)
            }

            override fun onAuthenticationFailure(p0: String?, p1: java.lang.Exception?) {
                Log.d("error", p1.toString())
            }

            override fun onSubscriptionSucceeded(p0: String?) {
                Log.d("res", p0)
            }
        })


        channel.bind("Illuminate\\Notifications\\Events\\BroadcastNotificationCreated", object : PrivateChannelEventListener {
            override fun onEvent(channel: String?, eventName: String?, data: String?) {
                Log.d("Channel", channel)
                Log.d("Event Name", eventName)
                Log.d("Data", data)

                val jsonObject = JSONObject(data)
                val driver_name = jsonObject.getString("driver_name")

                runOnUiThread {
                    viewDialog.hideDialog()
                    expandStartSheet(driver_name,PickUpLatLng,DropOfftLatLng)
                }
            }

            override fun onAuthenticationFailure(p0: String?, p1: java.lang.Exception?) {
                Log.d("error", p1.toString())
            }

            override fun onSubscriptionSucceeded(p0: String?) {
                Log.d("res", p0)
            }

        })
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