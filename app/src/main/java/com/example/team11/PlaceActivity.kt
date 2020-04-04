package com.example.team11

import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.example.team11.viewmodels.MapActivityViewModel
import com.example.team11.viewmodels.PlaceActivityViewModel
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource

class PlaceActivity : AppCompatActivity() {
    private val viewModel: PlaceActivityViewModel by viewModels{ PlaceActivityViewModel.InstanceCreator() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.access_token))
        setContentView(R.layout.activity_place)
        Log.d("tagPlace", "kommet inn ")

        Log.d("tagPlace", viewModel.place!!.value.toString())

        viewModel.place!!.observe(this, Observer { place ->
            //Skriver ut slik at vi kan se om vi har riktig badestrand

            Log.d("tagPlace", place.toString())
            makeAboutPage(place, savedInstanceState)
        })

        Log.d("tagPlace", "ferdig")


    }

    private fun makeAboutPage(place: Place, savedInstanceState: Bundle?) {
        val namePlace = findViewById<TextView>(R.id.namePlace)
        val directionButton = findViewById<Button>(R.id.directionButton)

        namePlace.text = "Navn: " + place.name

        makeMap(place, savedInstanceState)
    }

    private fun makeMap(place: Place, savedInstanceState: Bundle?) {
        val mapView = findViewById<MapView>(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { mapboxMap ->
            mapboxMap.setStyle(Style.MAPBOX_STREETS)
            mapboxMap.getStyle { style ->
                val ICON_ID_RED = "ICON_ID_RED"
                val geoId = "GEO_ID"
                val icon = BitmapFactory.decodeResource(
                    this@PlaceActivity.resources,
                    R.drawable.mapbox_marker_icon_default
                )
                style.addImage(ICON_ID_RED, icon)

                val feature = viewModel.getFeature(place)
                val geoJsonSource = GeoJsonSource(geoId, FeatureCollection.fromFeatures(
                    arrayListOf(feature)))
                style.addSource(geoJsonSource)

                val symbolLayer = SymbolLayer("SYMBOL_LAYER_ID", geoId)
                symbolLayer.withProperties(
                    PropertyFactory.iconImage(ICON_ID_RED),
                    PropertyFactory.iconAllowOverlap(true),
                    PropertyFactory.iconIgnorePlacement(true)
                )
                style.addLayer(symbolLayer)
            }
            val position = CameraPosition.Builder()
                .target(LatLng(place.lat, place.lng))
                .zoom(15.0)
                .build()
            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 2)
        }
    }
}
