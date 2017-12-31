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
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
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
            permCheck()
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

    private fun launchCamera() {
        val values = ContentValues(1)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
        val fileUri = contentResolver
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        values)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if(intent.resolveActivity(packageManager) != null) {
            mCurrentPhotoPath = fileUri.toString()
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            startActivityForResult(intent, TAKE_PHOTO_REQUEST)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int,
                                  data: Intent?) {
        if (resultCode == Activity.RESULT_OK
                && requestCode == TAKE_PHOTO_REQUEST) {
            processCapturedPhoto()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
    private fun processCapturedPhoto() {
        val cursor = contentResolver.query(Uri.parse(mCurrentPhotoPath),
                Array(1) {android.provider.MediaStore.Images.ImageColumns.DATA},
                null, null, null)
        cursor.moveToFirst()
        val photoPath = cursor.getString(0)
        cursor.close()
        val file = File(photoPath)

        val uri = Uri.fromFile(file)

        val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)

        var ei = ExifInterface(photoPath);
        val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                                     ExifInterface.ORIENTATION_UNDEFINED);
        var rotatedBitmap : Bitmap = bitmap
        if(orientation == ExifInterface.ORIENTATION_ROTATE_90)
        {
            Log.i("info","Rotate 90")
            rotatedBitmap = rotateImage(bitmap, 90f);
        }
        else if(orientation == ExifInterface.ORIENTATION_ROTATE_180)
        {
            Log.i("info","Rotate 180")
            rotatedBitmap = rotateImage(bitmap, 180f);
        }
        else if(orientation == ExifInterface.ORIENTATION_ROTATE_270)
        {
            Log.i("info","Rotate 270")
            rotatedBitmap = rotateImage(bitmap, 270f);
        }
        else
        {
            Log.i("info","No Rotate")
        }

        imgv_photo.setImageBitmap(rotatedBitmap)


        val bitmapdraw = imgv_photo.getDrawable() as BitmapDrawable
        val b = bitmapdraw.bitmap
        val iconx = Bitmap.createScaledBitmap(b, 150, 250, false)
        val icon = BitmapDescriptorFactory.fromBitmap(iconx)
        val user = LatLng(userLat, userLng)
        var marker2=mMap.addMarker(MarkerOptions().position(user).title("Request").snippet(getAddressFromLocation(user)).icon(icon))
        marker2.showInfoWindow()
        saveImage(rotatedBitmap)
    }
    fun permCheck(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), STORE_RESULT)

        } else {
            launchCamera()
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORE_RESULT) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permCheck()
            }
        }
        else
        {
            var snackbar1=Snackbar.make(mainContainer, Html.fromHtml("<font color=\"#ffffff\">No Camera Access</font>"),Snackbar.LENGTH_LONG)
            val snackBarView = snackbar1.getView()
            snackBarView.setBackgroundColor(getColor(R.color.colorAccent))
        }
    }

    fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height,
                matrix, true)
    }
    fun saveImage(photo: Bitmap)
    {
        val bitmap = getResizedBitmap(photo, 500)
        var pObj = ParseObject ("Document");
        pObj.put("username", username);
        pObj.put("requestId", requestId);

        if (bitmap == null ) {
            Log.d ("Error" , "Problem with image")
        }
        else
        {
            var stream =  ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            var pFile = ParseFile("DocImage.jpg", stream.toByteArray());
            try
            {
                pFile.save();
                pObj.put("FileName", pFile);
                pObj.saveInBackground{e->
                    if(e==null)
                    {
                        Toast.makeText(applicationContext,"Image SuccessFully Uploaded",Toast.LENGTH_LONG).show()
                    }
                    else
                    {
                        Toast.makeText(applicationContext,"Image Not SuccessFully Uploaded",Toast.LENGTH_LONG).show()
                    }
                }
            }
            catch (e : ParseException)
            {
                e.printStackTrace();
            }
        }


    }

    /**
     * reduces the size of the image
     * @param image
     * @param maxSize
     * @return
     */
    fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap {
        var width = image.width
        var height = image.height

        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }
}
