package com.example.ae.photoapp

import android.location.Address
import android.location.Geocoder
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.parse.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_request_photo.*
import java.io.IOException
import java.util.ArrayList

class RequestPhotoActivity : AppCompatActivity() {

    internal var requestIds = ArrayList<String>()
    var pictureDatas : List<PictureData>? = ArrayList<PictureData>()
    lateinit var pictureAdapter : PictureRecyclerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_photo)
        displayRequests()
        recycler_view.adapter = pictureAdapter
        recycler_view.layoutManager = LinearLayoutManager(applicationContext,LinearLayoutManager.VERTICAL,false)

    }
    fun displayRequests()
    {
        val parseQuery = ParseQuery<ParseObject>("Document")
        parseQuery.whereEqualTo("username", ParseUser.getCurrentUser().username)

        parseQuery.findInBackground { objects, e ->
            if (e == null) {
                if (objects.size > 0) {
                    for (`object` in objects) {
                        var requestId=`object`.objectId

                        var loc : ParseGeoPoint = `object`.getParseGeoPoint("location")
                        var location = getAddressFromLocation(loc)

                        var parseFile : ParseFile = `object`.getParseFile("FileName")
                        var url = parseFile.url

                        var data : PictureData = PictureData(requestId,url,location)


                        Picasso.with(applicationContext)
                                .load(url)
                                .into(pic1);
                        Log.i("info Photo Req Act","Req Id ="+requestId)
                        requestIds.add(requestId)
                    }
                }
            }
        }
    }
    /**     Getting Address from location Coordinates **/
    fun getAddressFromLocation(location: ParseGeoPoint) : String {
        var latitude=location.latitude
        var longitude=location.longitude

        var geocodeMatches: List<Address>? = null
        var Address1: String = ""
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
