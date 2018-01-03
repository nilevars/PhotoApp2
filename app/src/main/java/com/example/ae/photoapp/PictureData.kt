package com.example.ae.photoapp

/**
 * Created by A E on 03-Jan-18.
 */
class PictureData internal constructor(id: String,images: String,location: String) {
    var id: String
        internal set
    var images: String
        internal set
    var location: String
        internal set

    init {
        this.id = id
        this.images = images
        this.location = location
    }
}