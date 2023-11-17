package com.cmaye.streetpluse

import android.content.pm.PackageManager
import android.graphics.Rect
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.cmaye.streetpluse.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


class LocationOverlayActivity : AppCompatActivity(),MapListener {
    private lateinit var binding: ActivityMainBinding

    private lateinit var mMap : MapView
    private lateinit var myLocationOverlay: MyLocationNewOverlay
    private lateinit var mapController: IMapController

    private var currentPoint : GeoPoint ?= null
    private var initialPoint : GeoPoint ?= null
    private var permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){isGranted ->
        if (isGranted)
        {
            requestLocationUpdate()
            Toast.makeText(this, "Granted", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this, "Denied!", Toast.LENGTH_SHORT).show()
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mMap = binding.streetMapView
        initializeOSM()
    }

    private fun initializeOSM()
    {
        Configuration.getInstance().load(this,getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE))
//        mMap.setTileSource(TileSourceFactory.MAPNIK)
//        mMap.mapCenter
        mMap.setMultiTouchControls(true)
//        mMap.getLocalVisibleRect(Rect())

        myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this),mMap)
        mapController = mMap.controller



        // Set the center of the map
        initialPoint = GeoPoint(17.312240, 96.516172) // Example: Bago
        mapController.setCenter(initialPoint)
//        mapView.controller.animateTo(initialPoint)
        mapController.setZoom(16.0)

        // Add a marker for the initial location
        val initialMarker = Marker(mMap)
        initialMarker.position = initialPoint
        initialMarker.title = "Initial Location"
        mMap.overlays.add(initialMarker)



        if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED )
        {
            permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }else{
            requestLocationUpdate()
        }


    }


    private fun requestLocationUpdate()
    {
        myLocationOverlay.enableMyLocation()
        myLocationOverlay.enableFollowLocation()
        myLocationOverlay.isDrawAccuracyEnabled = true

        myLocationOverlay.runOnFirstFix {
            GlobalScope.launch(Dispatchers.Main) {
                currentPoint = myLocationOverlay.myLocation
                mapController.setCenter(currentPoint)
                mapController.animateTo(currentPoint)

                mMap.overlays.add(myLocationOverlay)
                // TODO: Other UI updates
                Log.i("CMA_LatLng", currentPoint!!.latitude.toString())
                Log.i("CMA_LatLng", currentPoint!!.longitude.toString())

                mapController.setZoom(19.0)

//                // Add a custom marker
//                val customMarker = Marker(mMap)
//                customMarker.position = currentPoint
//                customMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
//                customMarker.icon = ContextCompat.getDrawable(this@LocationOverlayActivity, R.drawable.icon_marker_line_map_svgrepo_com)
//                mMap.overlays.add(customMarker)

                updateTextView(currentPoint!!)
                mMap.addMapListener(this@LocationOverlayActivity)

            }
        }
    }

    override fun onScroll(event: ScrollEvent?): Boolean {
//        TODO("Not yet implemented")
        return true
    }

    override fun onZoom(event: ZoomEvent?): Boolean {
//        TODO("Not yet implemented")
        return false
    }


    private fun updateTextView(geoPoint: GeoPoint) {
        val latitude = geoPoint.latitude
        val longitude = geoPoint.longitude
        binding.tvLatLng.text = "Latitude: $latitude, Longitude: $longitude"
    }


}