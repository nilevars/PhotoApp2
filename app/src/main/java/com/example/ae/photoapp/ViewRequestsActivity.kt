package com.example.ae.photoapp

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.parse.FindCallback
import com.parse.ParseException
import com.parse.ParseGeoPoint
import com.parse.ParseObject
import com.parse.ParseQuery
import com.parse.ParseUser
import com.parse.SaveCallback
import kotlinx.android.synthetic.main.activity_view_requests.*
import java.io.IOException

import java.util.ArrayList
import java.util.HashMap

class ViewRequestsActivity : AppCompatActivity() {

   // internal var requestListView : ListView = ListView()
    internal var requests = ArrayList<String>()
    internal var requestLatitudes = ArrayList<Double>()
    internal var requestLongitudes = ArrayList<Double>()
    internal var usernames = ArrayList<String>()
    internal var requestIds = ArrayList<String>()
    lateinit var arrayAdapter : ArrayAdapter<String>

    private var locationManager : LocationManager? = null
    private var locationListener: LocationListener? = null
    private val mHashMap = HashMap<Marker, Int>()
    internal var LOC_RESULT = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_requests)

        arrayAdapter = ArrayAdapter<String>(this@ViewRequestsActivity, android.R.layout.simple_list_item_1, requests)

        //  var requestListView : ListView =  findViewById<ListView>(R.id.listview1)
        listview1.adapter = arrayAdapter

        requests.clear()
        requests.add("Getting Location Updates....")

      //  arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, requests)

        listview1.adapter = arrayAdapter

        listview1.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            Log.i("info", "You Clicked on " + i)
            Log.i("info", "Lat is" + requestLatitudes[i])
            Log.i("info", "Lng is" + requestLongitudes[i])

            val intent = Intent(this@ViewRequestsActivity, DriverNavigationActivity::class.java)
            val driverLocation = permCheck()
            if (driverLocation != null) {
                intent.putExtra("driverLat", driverLocation.latitude)
                intent.putExtra("driverLng", driverLocation.longitude)
                intent.putExtra("userLat", requestLatitudes[i])
                intent.putExtra("userLng", requestLongitudes[i])
                intent.putExtra("username", usernames[i])
                intent.putExtra("requestId", requestIds[i])
                startActivity(intent)
            }

        }

        locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location?) {

                updateListView(location)
                if (location != null) {
                    ParseUser.getCurrentUser().put("location", ParseGeoPoint(location.latitude, location.longitude))
                    ParseUser.getCurrentUser().saveInBackground { e ->
                        if (e == null) {
                            Log.i("info", "Location Saved")
                        } else {
                            Log.i("info", "Location not Saved")
                        }
                    }
                } else {
                    Log.i("info", "Location is null")
                }

            }

            override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {

            }

            override fun onProviderEnabled(s: String) {

            }

            override fun onProviderDisabled(s: String) {

            }
        }
        val location = permCheck()
        if (location != null) {
            ParseUser.getCurrentUser().put("location", ParseGeoPoint(location.latitude, location.longitude))
            ParseUser.getCurrentUser().saveInBackground { e ->
                if (e == null) {
                    Log.i("info", "Location Saved")
                } else {
                    Log.i("info", "Location not Saved")
                }
            }
        } else {
            Log.i("info", "Location is null")
        }


    }

    fun permCheck(): Location? {
        var lastKnownLocation: Location? = null
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOC_RESULT)

        } else {

            locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
            lastKnownLocation = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (lastKnownLocation != null) {
                updateListView(lastKnownLocation)
            }
        }
        return lastKnownLocation
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOC_RESULT) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permCheck()
            }
        }
    }

    fun updateListView(location: Location?) {
        if (location != null) {
            Log.i("info", "Location : " + location)

            val parseGeoPoint = ParseGeoPoint(location.latitude, location.longitude)

            val parseQuery = ParseQuery<ParseObject>("PhotoRequest")

           // parseQuery.whereNear("location", parseGeoPoint)
            parseQuery.whereWithinKilometers("location",parseGeoPoint,1.0)
            parseQuery.limit = 10
            parseQuery.findInBackground { objects, e ->
                if (e == null) {
                    if (objects.size > 0) {
                        requests.clear()
                        requestLatitudes.clear()
                        requestLongitudes.clear()
                        usernames.clear()
                        for (`object` in objects) {
                            val geoPoint = `object`.getParseGeoPoint("location")
                            val latlng : LatLng = LatLng(geoPoint.latitude,geoPoint.longitude)
                            val distanceInMiles = parseGeoPoint.distanceInKilometersTo(geoPoint)
                            val dist = Math.round(distanceInMiles * 10).toDouble() / 10
                            val loc=getAddressFromLocation(latlng)
                            requests.add("Request At : "+loc+" located "+dist.toString() + " Km")
                            requestLatitudes.add(geoPoint.latitude)
                            requestLongitudes.add(geoPoint.longitude)
                            usernames.add(`object`.getString("username"))
                            requestIds.add(`object`.objectId)
                            Log.i("Info","Request id is : "+`object`.objectId)
                            Log.i("info", "Distance is " + requests)
                            arrayAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }

        } else {
            requests.clear()
            requests.add("No Nearby Locations")
            arrayAdapter.notifyDataSetChanged()
        }

    }
    /**     Getting Address from location Coordinates **/
    fun getAddressFromLocation(location: LatLng) : String? {
        var latitude=location.latitude
        var longitude=location.longitude

        var geocodeMatches: List<Address>? = null
        var Address1: String? = null
        val Address2: String?
        var State: String? = null
        val Zipcode: String?
        val Country: String?

        try {
            geocodeMatches = Geocoder(this).getFromLocation(latitude, longitude, 1)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        if (geocodeMatches != null) {
            Address1 = geocodeMatches[0].getAddressLine(0)
            Address2 = geocodeMatches[0].getAddressLine(1)
            State = geocodeMatches[0].adminArea
            Zipcode = geocodeMatches[0].postalCode
            Country = geocodeMatches[0].countryName
        }
        return Address1
    }


}
