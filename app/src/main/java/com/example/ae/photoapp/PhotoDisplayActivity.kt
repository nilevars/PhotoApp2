package com.example.ae.photoapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.activity_photo_display.*
import kotlinx.android.synthetic.main.picture_display.*

class PhotoDisplayActivity : AppCompatActivity() {

    var count=0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_display)
        closePic.setOnClickListener {
            onBackPressed()
        }
        //var url=intent.getStringExtra("url")
        var picData: PictureData = intent.getSerializableExtra("picData") as PictureData
        var url = picData.images.get(0)
        loadPic(url)

        if (picData.count > 1)
        {
                Log.i("count","$count")


                img_layout.setOnTouchListener(object: OnSwipeTouchListener(this) {
                    override fun onSwipeLeft() {
                        super.onSwipeLeft()
                        Log.i("info","Swipe left")
                        if(count<picData.count-1)
                        {
                            count=count+1
                            loadPic(picData.images.get(count))
                        }

                    }
                    override fun onSwipeRight() {
                        super.onSwipeRight()
                        Log.i("info","Swipe right")
                        if(count>0)
                        {
                            count=count-1
                            loadPic(picData.images.get(count))
                        }
                    }
                }
                )
        }

    }
    fun loadPic(url : String)
    {
        Picasso.with(applicationContext).load(url).into(photoView, object : Callback {
            override fun onSuccess(){
                println("loaded Image")
            }

            override fun onError(){
                println("Unable to load Image")
            }
        })
    }
}
