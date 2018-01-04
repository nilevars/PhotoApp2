package com.example.ae.photoapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.activity_photo_display.*
import kotlinx.android.synthetic.main.picture_display.*

class PhotoDisplayActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_display)
        closePic.setOnClickListener {
            onBackPressed()
        }
        var url=intent.getStringExtra("url")
        Picasso.with(applicationContext).load(url).into(photoView, object : Callback {
            override fun onSuccess() {
                println("loaded Image")
            }

            override fun onError() {
                println("Unable to load Image")
            }
        })
    }
}
