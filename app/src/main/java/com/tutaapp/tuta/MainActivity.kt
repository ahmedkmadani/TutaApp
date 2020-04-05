package com.tutaapp.tuta

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.tutaapp.tuta.adapter.TrucksAdapter
import com.tutaapp.tuta.model.Trucks
import com.tutaapp.tuta.model.User
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


@Suppress("NAME_SHADOWING")
class MainActivity : AppCompatActivity() {

    internal lateinit var viewDialog: ViewDialog
    internal lateinit var user: User

    private var truckList: List<Trucks>? = null
    private var mAdapter: TrucksAdapter? = null


    val truck = ArrayList<Trucks>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewDialog = ViewDialog(this)


        val user = SharedPrefManager.getInstance(this).user

        val FirstName  = user.FirstName
        val LastName = user.LastName
        val token = user.token


        UserName.text = "Hello $FirstName $LastName !!!"


        mAdapter = TrucksAdapter(truck)
        truckList = ArrayList()


        recyclerview!!.adapter = mAdapter
        recyclerview!!.setHasFixedSize(true)

        val mLayoutManager = LinearLayoutManager(applicationContext)
        recyclerview!!.layoutManager = mLayoutManager

        recyclerview!!.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))

        recyclerview!!.itemAnimator = DefaultItemAnimator()
        recyclerview!!.adapter = mAdapter

        getTrucks(token)

        recyclerview!!.addOnItemTouchListener(
            RecyclerTouchListener(
                applicationContext,
                recyclerview!!,
                object : RecyclerTouchListener.ClickListener {
                    override fun onLongClick(view: View?, position: Int) {
                        val truck = truck.get(position)
                        val intent = Intent(baseContext, MapsActivity::class.java)

                        intent.putExtra("TRUCK_ID", truck.Id.toString())
                        startActivity(intent)
                    }

                    override fun onClick(view: View, position: Int) {
                        val truck = truck.get(position)
                        val intent = Intent(baseContext, MapsActivity::class.java)

                        intent.putExtra("TRUCK_ID", truck.Id.toString())
                        startActivity(intent)
                    }

                })
        )


    }

    private fun getTrucks(token: String) {

        viewDialog.showDialog()

        val jRequest = object : JsonObjectRequest(
            Method.GET, URLs.URL_GET_TRUCKS, null,
            Response.Listener { response ->

                val JsonReq = response.getJSONObject("data")
                val TrucksArray = JsonReq.getJSONArray("vehicle_types")
                Log.d("vehicle_types" , "$TrucksArray")

                for(i in 0 until TrucksArray.length()) {

                    val TruckObject = TrucksArray.getJSONObject(i)

                    truck.add(
                        Trucks(
                            TruckObject.getInt("id"),
                            TruckObject.getString("name"),
                            TruckObject.getString("description"),
                            TruckObject.getString("created_at"),
                            TruckObject.getString("updated_at"),
                            TruckObject.getString("deleted_at"),
                            TruckObject.getInt("base_charge"),
                            TruckObject.getDouble("price_per_kilometer"),
                            TruckObject.getDouble("price_per_second"),
                            TruckObject.getInt("average_speed")
                        )
                    )
                }

                mAdapter!!.notifyDataSetChanged()
                viewDialog.hideDialog()

    },
    Response.ErrorListener {
        viewDialog.hideDialog()

    }) {
        override fun parseNetworkError(volleyError: VolleyError): VolleyError {
            Log.d("volleyError", ""+ volleyError.message)
            viewDialog.hideDialog()
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



    VolleySingleton.getInstance(this).addToRequestQueue(jRequest)

    }

}