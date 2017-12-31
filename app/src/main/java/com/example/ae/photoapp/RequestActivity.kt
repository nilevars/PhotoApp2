package com.example.ae.photoapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import com.google.android.gms.maps.model.*
import android.content.DialogInterface
import android.graphics.BitmapFactory
import android.location.*
import android.os.Build
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.text.Html
import android.view.ContextThemeWrapper
import com.parse.*
import kotlinx.android.synthetic.main.activity_request.*
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


class RequestActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    val LOC_RESULT = 100
    var count=0;
    private var locationManager : LocationManager? = null
    private var locationListener: LocationListener? = null
    private val mHashMap = HashMap<Marker, Int>()

    /**OnCreate **/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        // Create persistent LocationManager reference
        locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {

            //    updateMap(location)

            }

            override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {

            }

            override fun onProviderEnabled(s: String) {

            }

            override fun onProviderDisabled(s: String) {

            }
        }


        //define the listener
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
        val height = 100
        val width = 100
        mMap = googleMap
        val bitmapdraw = resources.getDrawable(R.drawable.goal) as BitmapDrawable
        val b = bitmapdraw.bitmap
        val iconx = Bitmap.createScaledBitmap(b, width, height, false)
        val icon = BitmapDescriptorFactory.fromBitmap(iconx)

        var location = permCheck();
        Log.i("info","Loc is : "+location?.latitude);
        mMap!!.setOnMapClickListener(object : GoogleMap.OnMapClickListener {
            override fun onMapClick(latLng: LatLng) {
                count++
                Log.i("info","Loc is : "+latLng.latitude);
                val bitmapdraw = resources.getDrawable(R.drawable.goal) as BitmapDrawable
                val b = bitmapdraw.bitmap
                val iconx = Bitmap.createScaledBitmap(b, width, height, false)
                val icon = BitmapDescriptorFactory.fromBitmap(iconx)

                var marker:Marker = mMap.addMarker(MarkerOptions().position(latLng).title("Requested by You "+count).icon(icon).snippet("m"+count))
                //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

                mHashMap.put(marker, count);
                addRequest(marker)
            }
        })
        mMap.setOnMarkerClickListener(object : GoogleMap.OnMarkerClickListener{
            override fun onMarkerClick(p0: Marker?): Boolean {
                val pos = mHashMap[p0]
                val name = p0!!.title
                if(name.equals("Request1")){
                    Log.i("info","You Clicked Request 1")
                }
                Log.i("info","You Clicked Request "+pos)
                if(!(p0).title.equals("You")) {
                    showAlert(p0)
                }
                return true
            }
        })
        displayRequests()
    }
    /**     Check for Location Access Permission **/
    fun permCheck(): Location? {
        var lastKnownLocation: Location? = null
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOC_RESULT)

        } else {
            locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
            lastKnownLocation = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (lastKnownLocation != null) {
                Log.i("info","Loc is : "+lastKnownLocation.latitude);
                updateMap(lastKnownLocation)
            }
        }
        return lastKnownLocation
    }
    /**     Updating Map with User Location **/
    fun updateMap(location: Location) {
        val userLocation = LatLng(location.latitude, location.longitude)
       // mMap.clear()
        val marker=mMap.addMarker(MarkerOptions().position(userLocation).title("You").snippet(getAddressFromLocation(userLocation)))
        marker.showInfoWindow();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
    }

    /**     Alert to remove a marker **/
    fun showAlert(marker : Marker)
    {
        val builder: AlertDialog.Builder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
       //     builder = AlertDialog.Builder(applicationContext,new ContextThemeWrapper( R.style.myDialog))

            builder= AlertDialog.Builder(ContextThemeWrapper(this, R.style.myDialog));
        } else {
            builder = AlertDialog.Builder(applicationContext)
        }
        builder.setTitle("Delete Request")
                .setMessage("Are you sure you want to delete this request?")
                .setPositiveButton(android.R.string.yes, DialogInterface.OnClickListener { dialog, which ->
                    // continue with delete
                    if(!(marker.title.equals("You"))) {
                        marker.remove()
                        deleteRequest(marker)
                    }
                })
                .setNegativeButton(android.R.string.no, DialogInterface.OnClickListener { dialog, which ->
                    // do nothing
                })
                .setIcon(R.drawable.ic_warning_black_24dp)
                .show()
    }
    /**     Add Request to Parse Server **/
    fun addRequest(marker : Marker)
    {
        val parseObject = ParseObject("PhotoRequest")
        var requestLocation : LatLng = marker.position
        val loc = ParseGeoPoint(requestLocation.latitude, requestLocation.longitude)

        parseObject.put("username", ParseUser.getCurrentUser().username)
        parseObject.put("location", loc)

        parseObject.saveInBackground { e ->
            if (e == null) {
                Log.i("Info","Info has been saved")
                var snackbar1=Snackbar.make(frame, Html.fromHtml("<font color=\"#ffffff\">Photo Request Added at "+getAddressFromLocation(requestLocation)+"</font>"),Snackbar.LENGTH_LONG)
                val snackBarView = snackbar1.getView()
                snackBarView.setBackgroundColor(getColor(R.color.colorAccent))

                snackbar1.show()
            }
            else
            {
                Log.i("Info","Info has NOT been saved")
            }
        }

    }
    /**     Delete Request From Parse Server **/
    fun deleteRequest(marker: Marker)
    {
        var requestLocation : LatLng = marker.position
        val loc = ParseGeoPoint(requestLocation.latitude, requestLocation.longitude)

        val parseQuery = ParseQuery<ParseObject>("PhotoRequest")
        parseQuery.whereEqualTo("username", ParseUser.getCurrentUser().username)
        parseQuery.whereEqualTo("location",loc)

        parseQuery.findInBackground { objects, e ->
            if (e == null) {
                if (objects.size > 0) {
                    for (`object` in objects) {
                        `object`.deleteInBackground{e->
                            if(e==null)
                            {
                                Log.i("Info","Info has been deleted")
                            }
                            else
                            {
                                Log.i("Info","Info has NOT been deleted")
                            }
                        }
                    }
                }
            }
        }
    }
    /**     Displaying All Requests in Parse Server for a particular user at Map initialization **/
    fun displayRequests()
    {
        val parseQuery = ParseQuery<ParseObject>("PhotoRequest")
        parseQuery.whereEqualTo("username", ParseUser.getCurrentUser().username)

        parseQuery.findInBackground { objects, e ->
            if (e == null) {
                if (objects.size > 0) {
                    for (`object` in objects) {
                        val geoPoint = `object`.getParseGeoPoint("location")
                        addMarker(geoPoint,`object`.objectId)

                    }
                }
            }
        }
    }
    /**     Adding Marker to map at location clicked **/
    fun addMarker(location: ParseGeoPoint , requestId : String)
    {
        count++
        val userRequest = LatLng(location.latitude, location.longitude)
        val bitmapdraw = resources.getDrawable(R.drawable.target) as BitmapDrawable
       // val b = bitmapdraw.bitmap
        val b= getMarkerIcon(requestId)
        val iconx = Bitmap.createScaledBitmap(b, 100, 100, false)
        val icon = BitmapDescriptorFactory.fromBitmap(iconx)
        var marker:Marker = mMap.addMarker(MarkerOptions().position(userRequest).title("Requested by You "+count).icon(icon).snippet("m"+count))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userRequest, 15f))
        mHashMap.put(marker, count)

    }
    fun getMarkerIcon (requestId: String) : Bitmap
    {
        val bitmapdraw = resources.getDrawable(R.drawable.target) as BitmapDrawable
        var b = bitmapdraw.bitmap
        val parseQuery = ParseQuery<ParseObject>("Document")
        parseQuery.whereEqualTo("requestId", requestId)

        parseQuery.findInBackground { objects, e ->
            if (e == null) {
                if (objects.size > 0) {
                    for (`object` in objects) {
                        var postImage = `object`.getParseFile("FileName")
                         postImage.dataInBackground.onSuccess {
                             Log.i("Info","Data is : ")
                         }
                        Log.i("Info","Data is : ")
                      //  b = getBitmapFromURL(postImage.url)
                    }
                }
            }
        }
        return b
    }

    fun getBitmapFromURL(src: String): Bitmap? {
        try {
            val url = URL(src)
            val connection = url.openConnection() as HttpURLConnection
            connection.setDoInput(true)
            connection.connect()
            val input = connection.getInputStream()
            return BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            // Log exception
            return null
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
