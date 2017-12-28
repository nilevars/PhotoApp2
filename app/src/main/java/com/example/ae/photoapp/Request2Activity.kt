package com.example.ae.photoapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast

import com.eegeo.indoors.IndoorMapView
import com.eegeo.mapapi.EegeoApi
import com.eegeo.mapapi.EegeoMap
import com.eegeo.mapapi.MapView
import com.eegeo.mapapi.map.OnMapReadyCallback

class Request2Activity : AppCompatActivity() {

    private var m_mapView: MapView? = null
    private var m_eegeoMap: EegeoMap? = null
    private var m_interiorView: IndoorMapView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EegeoApi.init(this, getString(R.string.eegeo_api_key))

        setContentView(R.layout.activity_request2)
        m_mapView = findViewById<MapView>(R.id.mapView)
        m_mapView!!.onCreate(savedInstanceState)

        m_mapView!!.getMapAsync { map ->
            m_eegeoMap = map

            val uiContainer = findViewById<View>(R.id.eegeo_ui_container) as RelativeLayout
            m_interiorView = IndoorMapView(m_mapView, uiContainer, m_eegeoMap)

            Toast.makeText(this@Request2Activity, "Hello World!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        m_mapView!!.onResume()
    }

    override fun onPause() {
        super.onPause()
        m_mapView!!.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        m_mapView!!.onDestroy()
    }
}
