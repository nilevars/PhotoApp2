package com.example.ae.photoapp

import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation

/**
 * Created by A E on 03-Jan-18.
 */
class PictureRecyclerAdapter(internal var context: Context, internal var pictureDatas: List<PictureData>) : RecyclerView.Adapter<PictureRecyclerAdapter.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder?.name?.setText(pictureDatas[position].location)
        holder?.count?.setText(pictureDatas[position].count.toString())
        Picasso.with(context).load(pictureDatas[position].images!!.get(0)).transform( CropCircleTransformation()).resize(400, 400).centerCrop().into(holder?.img, object : Callback {
            override fun onSuccess() {
                println("loaded Image")
            }

            override fun onError() {
                println("Unable to load Image")
            }
        })
        holder?.linearLayout!!.setOnClickListener {
            val in1 = Intent(context,PhotoDisplayActivity::class.java)
           // in1.putExtra("url",pictureDatas[position].images!!.get(0))
            in1.putExtra("picData",pictureDatas[position])
            context.startActivity(in1)
        }
        setScaleAnimation(holder?.linearLayout)
    }

    lateinit var rowView: View

    init {
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    inner class ViewHolder(rowView: View) : RecyclerView.ViewHolder(rowView) {
        internal var name: TextView
        internal var count: TextView
        internal var img: ImageView
        internal var linearLayout: LinearLayout? = null

        init {
            name = rowView.findViewById<View>(R.id.loc) as TextView
            count = rowView.findViewById<View>(R.id.count) as TextView
            img = rowView.findViewById<View>(R.id.pic) as ImageView
            linearLayout = rowView.findViewById(R.id.pic_layout) as LinearLayout
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PictureRecyclerAdapter.ViewHolder {
        rowView = inflater!!.inflate(R.layout.picture_display, null)
        return ViewHolder(rowView)
    }

    private fun setScaleAnimation(view: View?) {
        val anim = ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        anim.duration = FADE_DURATION.toLong()
        view!!.startAnimation(anim)
    }

    override fun getItemCount(): Int {
        return pictureDatas.size
    }

    companion object {
        private val FADE_DURATION = 500
        private var inflater: LayoutInflater? = null
    }
}