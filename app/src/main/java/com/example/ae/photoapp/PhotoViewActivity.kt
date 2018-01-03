package com.example.ae.photoapp

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import kotlinx.android.synthetic.main.activity_driver_navigation.*
import kotlinx.android.synthetic.main.activity_photo_view.*
import java.io.File
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.widget.Toast
import com.parse.*
import java.io.ByteArrayOutputStream


class PhotoViewActivity : AppCompatActivity() {
    var username : String = ""
    var requestId : String = ""
    internal var userLat:Double = 0.0
    internal var userLng:Double = 0.0
    val bmp :Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_view)
        val bmp = BitmapFactory.decodeStream(applicationContext
                .openFileInput("myImage"))
        username = intent.getStringExtra("username")
        requestId=intent.getStringExtra("requestId")
        userLat=intent.getDoubleExtra("userLat",0.0)
        userLng=intent.getDoubleExtra("userLng",0.0)


        photoView.setImageBitmap(bmp)
        closePic.setOnClickListener {
            onBackPressed()
        }
        fab_upload_photo.setOnClickListener {
            if(bmp!=null)
            {
                saveImage(bmp)
            }

        }
    }
    fun saveImage(photo: Bitmap) {
            val bitmap = getResizedBitmap(photo, 500)
            var pObj = ParseObject ("Document");
            val loc :ParseGeoPoint = ParseGeoPoint(userLat,userLng)
            pObj.put("username", username);
            pObj.put("requestId", requestId);
            pObj.put("location", loc);
            pObj.put("takenBy", ParseUser.getCurrentUser().username);

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
                    pFile.save()
                    pObj.put("FileName", pFile)
                    pObj.saveInBackground{e->
                        if(e==null)
                        {
                            val intent= Intent(applicationContext,MainActivity::class.java)
                            startActivity(intent)
                            Toast.makeText(applicationContext,"Image SuccessFully Uploaded", Toast.LENGTH_LONG).show()
                        }
                        else
                        {
                            Toast.makeText(applicationContext,"Image Not SuccessFully Uploaded", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                catch (e : ParseException)
                {
                    e.printStackTrace()
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
