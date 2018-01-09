package com.example.ae.photoapp

import android.util.Log
import java.io.Serializable

/**
 * Created by A E on 03-Jan-18.
 */
class PictureData  internal constructor(id: String,images: String,location: String) : Serializable  {
    var id: String
        internal set
    var images: Array<String> = Array(10,{""})
        internal set
    var location: String
        internal set
    var count: Int = 0
        internal set
    init {
        this.id = id
        this.images.set(0,images)
        this.location = location
        this.count = 1
    }
    fun addUrl(url : String)
    {
        var i = this.count
        Log.i("info", "Size is : "+i)
        if(i<10)
        {
            this.images?.set(i,url)
            this.count=count+1

        }

    }
}