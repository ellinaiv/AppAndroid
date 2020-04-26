package com.example.team11.ui.direction

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.example.team11.Place
import com.example.team11.R
import com.example.team11.Transportation
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.core.constants.Constants
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DirectionActivity : AppCompatActivity() , PermissionsListener {

    private val viewModel: DirectionActivityViewModel by viewModels{ DirectionActivityViewModel.InstanceCreator() }
    private var permissionManager = PermissionsManager(this)
    private lateinit var mapboxMap: MapboxMap
    private val ROUTE_SOURCE_ID = "ROUTE_SOURCE_ID"
    private var way: Transportation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.access_token))
        setContentView(R.layout.activity_direction)
        supportActionBar!!.hide()
        val backButton = findViewById<ImageButton>(R.id.backButton)
        val aboutDirectionText = findViewById<TextView>(R.id.aboutDirectionText)
        backButton.setOnClickListener {
            finish()
        }

        //Observerer stedet som er valgt
        viewModel.place!!.observe(this, Observer { place ->
            viewModel.wayOfTransportation!!.observe(this, Observer { way->
                aboutDirectionText.text = when(way) {
                    Transportation.BIKE -> getString(
                        R.string.bikeDirection, place.name)
                    Transportation.CAR -> getString(
                        R.string.carDirection, place.name)
                    Transportation.WALK -> getString(R.string.walkDirection, place.name)
                    else -> getString(R.string.bikeDirection, place.name)
                }
                this.way = way
                makeMap(place, savedInstanceState)
            })
        })
    }

    /**
     * Lager kartet, tegner opp destionasjon og lokasjon. Viser rute, og zoomer inn på destinasjon
     * @param place: Stedet som skal vises på kartet
     * @param savedInstanceState: mapView trenger denne til onCreate metoden sin
     */
    private fun makeMap(place: Place, savedInstanceState: Bundle?) {
        val mapView = findViewById<MapView>(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { mapboxMap ->
            this.mapboxMap = mapboxMap
            mapboxMap.setStyle(Style.MAPBOX_STREETS)
            mapboxMap.getStyle { style ->
                style.addSource(GeoJsonSource(ROUTE_SOURCE_ID))
                makeRouteLayer(style)
                addDestinationMarker(place, style)
                val position = CameraPosition.Builder()
                    .target(LatLng(place.lat, place.lng))
                    .zoom(10.0)
                    .build()
                mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 2)
            }
        }
    }

    /**
     * Lager et layer på kartet, slik at man kan tegne det opp på et senere tidspunkt
     * @param style: stilen kartet skal tegnes oppå
     */
    private fun makeRouteLayer(style: Style){
        enableLocationComponent(style)
        val routeLayer = LineLayer("ROUTE_LAYER_ID", ROUTE_SOURCE_ID)

        routeLayer.setProperties(
            PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
            PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
            PropertyFactory.lineWidth(5f),
            PropertyFactory.lineColor(Color.parseColor("#009688"))
        )

        style.addLayer(routeLayer)
    }

    /**
     * Legger til pinnen til destinasjonen
     * @param place: stedet som er destinasjonen
     * @param style: stilen pinnen skla plaseres på
     */
    private fun addDestinationMarker(place: Place, style: Style){
        val ICON_ID_RED = "ICON_ID_RED"
        val geoId = "GEO_ID"
        val icon = BitmapFactory.decodeResource(
            this@DirectionActivity.resources,
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

    /**
     * Lager ruten, og tegner den på kartet
     * @param place: stedet, osm er destinasjonen
     */
    private fun getRoute(place: Place){
        //hentet stedet vi skal bruke
        val originLocation = mapboxMap.locationComponent.lastKnownLocation ?: return
        val originPoint = Point.fromLngLat(originLocation.longitude, originLocation.latitude)
        val profile = when(way){
            Transportation.BIKE -> DirectionsCriteria.PROFILE_CYCLING
            Transportation.CAR -> DirectionsCriteria.PROFILE_DRIVING
            Transportation.WALK -> DirectionsCriteria.PROFILE_WALKING
            else -> DirectionsCriteria.PROFILE_CYCLING
        }

        //lager en klient som er ansvarlig for alt rundt ruten
        val client = MapboxDirections.builder()
            .origin(originPoint)
            .destination(Point.fromLngLat(place.lng, place.lat))
            .overview(DirectionsCriteria.OVERVIEW_FULL)
            .profile(profile)
            .accessToken(getString(R.string.access_token))
            .steps(false)
            .build()


        //tar ansvar for å tegne opp selve ruta
        client.enqueueCall(object : Callback<DirectionsResponse> {
            override fun onResponse(call: Call<DirectionsResponse>, response: Response<DirectionsResponse>) {
                if(response.body() == null || response.body()!!.routes().size < 1){
                    Toast.makeText(this@DirectionActivity, "No routes found", Toast.LENGTH_SHORT).show()
                    return
                }

                val currentRoute = response.body()!!.routes()[0]
                val stringD = "Lengde: " + viewModel.convertToCorrectDistance(currentRoute.distance())
                val stringT = "\nTid: " + viewModel.convertTime(currentRoute.duration())
                Toast.makeText(this@DirectionActivity, stringD + stringT, Toast.LENGTH_LONG).show()

                mapboxMap.getStyle { style ->
                    val source = style.getSourceAs<GeoJsonSource>(ROUTE_SOURCE_ID)
                    source?.setGeoJson(
                        LineString.fromPolyline(currentRoute.geometry()!!,
                            Constants.PRECISION_6
                        ))
                }
            }

            override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                Toast.makeText(this@DirectionActivity, "Error: " + t.message, Toast.LENGTH_SHORT).show()
            }

        })
    }

    /**
     * Finner lokasjonen til brukeren og bestemmer hvordan stilen runt bruker punktet skal være
     * @param style: stilen sim skal tegnes opp
     */
    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(style: Style){
        if(PermissionsManager.areLocationPermissionsGranted(this)){
            val customLocationComponentOptions =
                LocationComponentOptions.builder(this)
                    .trackingGesturesManagement(true)
                    .accuracyColor(ContextCompat.getColor(this@DirectionActivity,
                        R.color.mapbox_blue
                    ))
                    .build()

            val locationComponentActivityOptions =
                LocationComponentActivationOptions.builder(this, style)
                    .locationComponentOptions(customLocationComponentOptions)
                    .build()

            mapboxMap.locationComponent.apply {
                activateLocationComponent(locationComponentActivityOptions)
                isLocationComponentEnabled = true
                cameraMode = CameraMode.TRACKING
                renderMode = RenderMode.NORMAL
            }
            getRoute(viewModel.place!!.value!!)
        }else{
            permissionManager = PermissionsManager(this)
            permissionManager.requestLocationPermissions(this)
        }
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Toast.makeText(this, getString(R.string.viTrengerPosFordi), Toast.LENGTH_LONG).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        permissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onPermissionResult(granted: Boolean) {
        if(granted){
            enableLocationComponent(mapboxMap.style!!)
        }else{
            Toast.makeText(this, getString(R.string.ikkeViseVei), Toast.LENGTH_LONG).show()
            finish()
        }
    }
}
