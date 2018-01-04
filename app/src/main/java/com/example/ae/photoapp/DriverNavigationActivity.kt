package com.example.ae.photoapp

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.location.Address
import android.location.Geocoder
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_driver_navigation.*
import java.io.IOException
import android.provider.MediaStore
import android.os.Environment.getExternalStorageDirectory
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.location.Location
import android.location.LocationManager
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.text.Html
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import com.parse.ParseException
import com.parse.ParseFile
import com.parse.ParseObject
import kotlinx.android.synthetic.main.activity_request.*
import java.io.ByteArrayOutputStream
import java.io.File


class DriverNavigationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    internal var driverLat: Double = 0.0
    internal var driverLng:Double = 0.0
    internal var userLat:Double = 0.0
    internal var userLng:Double = 0.0
    internal var username: String = ""
    internal var requestId: String = ""
    val TAKE_PHOTO_REQUEST=10
    var mCurrentPhotoPath : String = ""
    val STORE_RESULT=20

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_navigation)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        val intent = intent
        driverLat = intent.getDoubleExtra("driverLat", 0.0)
        driverLng = intent.getDoubleExtra("driverLng", 0.0)
        userLat = intent.getDoubleExtra("userLat", 0.0)
        userLng = intent.getDoubleExtra("userLng", 0.0)
        username = intent.getStringExtra("username")
        requestId = intent.getStringExtra("requestId")
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
        mMap = googleMap

        val driver = LatLng(driverLat, driverLng)
        var marker1=mMap.addMarker(MarkerOptions().position(driver).title("You").snippet(getAddressFromLocation(driver)).icon(bitmapDescriptorFromVector(this, R.drawable.ic_directions_walk_black_24dp)))
        marker1.showInfoWindow()
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(driver, 15f))
        val bitmapdraw = resources.getDrawable(R.drawable.target) as BitmapDrawable
        val b = bitmapdraw.bitmap
        val iconx = Bitmap.createScaledBitmap(b, 100, 100, false)
        val icon = BitmapDescriptorFactory.fromBitmap(iconx)
        val user = LatLng(userLat, userLng)
        var marker2=mMap.addMarker(MarkerOptions().position(user).title("Request").snippet(getAddressFromLocation(user)).icon(icon))
        marker2.showInfoWindow()
        takePicture.setOnClickListener {
            val intent = Intent(this@DriverNavigationActivity,CameraActivity::class.java)
            intent.putExtra("username",username)
            intent.putExtra("requestId",requestId)
            intent.putExtra("userLat",userLat)
            intent.putExtra("userLng",userLng)

            startActivity(intent)
        }
    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        vectorDrawable.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
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
