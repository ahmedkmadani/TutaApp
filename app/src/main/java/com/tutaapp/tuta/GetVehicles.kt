package com.tutaapp.tuta

import android.app.Activity
import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.tutaapp.tuta.adapter.TrucksAdapter
import com.tutaapp.tuta.model.Trucks
import com.tutaapp.tuta.model.User
import com.tutaapp.tuta.utils.URLs
import com.tutaapp.tuta.utils.ViewDialog
import com.tutaapp.tuta.utils.VolleySingleton
import org.json.JSONObject
import java.util.ArrayList
import java.util.HashMap
import javax.security.auth.callback.Callback

class GetVehicles(internal var activity: Activity) {

     fun getTrucks(token: String) {

        val jRequest = object : JsonObjectRequest(Method.GET, URLs.URL_GET_TRUCKS, null, Response.Listener { response ->
                val JsonReq = response.getJSONObject("data")
                val TrucksArray = JsonReq.getJSONArray("vehicle_types")
                Log.d("vehicle_types" , "$TrucksArray")

            },
            Response.ErrorListener {

            }) {
            override fun parseNetworkError(volleyError: VolleyError): VolleyError {
                Log.d("volleyError", ""+ volleyError.message)
                return super.parseNetworkError(volleyError)
            }


            override fun getParams(): Map<String, String> {
                return HashMap()
            }

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $token"
                return headers
            }
        }


        VolleySingleton.getInstance(activity).addToRequestQueue(jRequest)
    }


}