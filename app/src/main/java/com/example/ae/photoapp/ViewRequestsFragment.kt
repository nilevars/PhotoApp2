package com.example.ae.photoapp

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


/**
 * Created by A E on 09-Jan-18.
 */
class ViewRequestsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
        var view = inflater!!.inflate(R.layout.activity_view_requests, container, false);
    }

}