package com.example.ae.photoapp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.parse.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ParseAnalytics.trackAppOpenedInBackground(intent)
        val query = ParseQuery<ParseObject>("Request")
        query.findInBackground { objects, e ->
            if (e == null) {
                for (request in objects) {
                    Log.d("DEBUG", request.getString("username"))
                }
            } else {
                Log.d("Error", "Error: " + e!!.message)
            }
        }
        requestBtn.setOnClickListener{
            val intent= Intent(applicationContext,RequestActivity::class.java)
            startActivity(intent)
        };
        requestBtn2.setOnClickListener{
            val intent= Intent(applicationContext,Request2Activity::class.java)
            startActivity(intent)
        };
        requestBtn3.setOnClickListener{
            val intent= Intent(applicationContext,ViewRequestsActivity::class.java)
            startActivity(intent)
        };
        cam.setOnClickListener{
            val intent= Intent(applicationContext,CameraActivity::class.java)
            startActivity(intent)
        };
        pic.setOnClickListener{
            val intent= Intent(applicationContext,PhotoViewActivity::class.java)
            startActivity(intent)
        };
        photoReq.setOnClickListener{
            val intent= Intent(applicationContext,RequestPhotoActivity::class.java)
            startActivity(intent)
        };


    }
}
