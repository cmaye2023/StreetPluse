package com.cmaye.streetpluse

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.PixelCopy.Request
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.cmaye.streetpluse.databinding.ActivityMainBinding
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var myLocationOverlay: MyLocationNewOverlay
    private lateinit var mapController: IMapController

    lateinit var boundingBox: BoundingBox
    private var desiredZoom: Double = 0.0
    private var currentPoint: GeoPoint  ?= null
    private var checkPoint: GeoPoint ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (isCheckPermission()) {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            initializeOSM()
        }

        binding.btnCurrent.setOnClickListener {
            enableMyLocationOverlay()
        }

        binding.btnRouteWay.setOnClickListener {
            if (currentPoint != null && checkPoint != null)
            {
                // Calculate and display the route between the two points
                val routePoints = calculateRoute(currentPoint!!, checkPoint!!)
                displayRoute(routePoints)
            }

        }
    }


    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Permission Grant!", Toast.LENGTH_SHORT).show()
                initializeOSM()
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show()

            }
        }

    private fun isCheckPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_DENIED
    }


    //OpenStreetMap

    private fun initializeOSM() {
        Configuration.getInstance().load(applicationContext, getPreferences(MODE_PRIVATE))
        myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), binding.streetMapView)
        myLocationOverlay.enableMyLocation()
        binding.streetMapView.overlays.add(myLocationOverlay)

        mapController = binding.streetMapView.controller
        // Set the tile source (e.g., Mapnik, CycleMap, MapQuestOSM)
        binding.streetMapView.setTileSource(TileSourceFactory.MAPNIK)

        // Disable zoom and scroll gestures
        binding.streetMapView.setMultiTouchControls(true)
        binding.streetMapView.setBuiltInZoomControls(true)


        binding.streetMapView.isVerticalMapRepetitionEnabled = false
        binding.streetMapView.isHorizontalMapRepetitionEnabled = true
        binding.streetMapView.isTilesScaledToDpi = true
        binding.streetMapView.minZoomLevel = 4.0
        binding.streetMapView.maxZoomLevel = 19.0
        mapController.setZoom(5.0)

        myLocationOverlay.enableMyLocation()
        enableMyLocationOverlay()

        // Add map click events
        val eventsOverlay = MapEventsOverlay(mapEventsReceiver)
        binding.streetMapView.overlays.add(eventsOverlay)

    }

    private val mapEventsReceiver = object : MapEventsReceiver {
        override fun singleTapConfirmedHelper(geoPoint: GeoPoint?): Boolean {
            geoPoint?.let { it ->

                checkPoint = it
// Remove previous marker if any
                binding.streetMapView.overlays.removeAll { it is Marker }
// Add new marker at the clicked point
                val marker = Marker(binding.streetMapView)
                marker.position = it
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                binding.streetMapView.overlays.add(marker)


                mapController = binding.streetMapView.controller

                //TODO
                Log.e("CUR_Latitude", it.latitude.toString())
                Log.e("CUR_Longitude", it.longitude.toString())
                // Zoom to the marker's location
                boundingBox = binding.streetMapView.boundingBox
//                desiredZoom = calculateDesiredZoom(boundingBox,binding.streetMapView.width,binding.streetMapView.height)
                runOnUiThread {
                    mapController.setCenter(it)
                    mapController.setZoom(16.0)
                }
                // Refresh the map
                binding.streetMapView.invalidate()

            }
            return true
        }

        override fun longPressHelper(p: GeoPoint?): Boolean {
            return false
        }

    }

    override fun onResume() {
        super.onResume()
        // Update the location overlay
        myLocationOverlay.enableMyLocation()
    }

    override fun onPause() {
        super.onPause()
        // Update the location overlay
        myLocationOverlay.disableMyLocation()
    }

    private fun calculateDesiredZoom(
        boundingBox: BoundingBox,
        mapViewWidth: Int,
        mapViewHeight: Int
    ): Double {
        // Define constants for adjusting the zoom level calculation
        val Zoom_Level_Constant = 0.1
        val Min_Zoom_Level = 2.0
        // Calculate latitude and longitude span
        val latSpan = boundingBox.latitudeSpan
        val lonSpan = boundingBox.longitudeSpan
// Calculate desired zoom level based on latitude span (you can use longitude span as well)
        val latZoom = mapViewHeight / latSpan * Zoom_Level_Constant
        // Adjust the calculated zoom level to ensure it's not too small

        return Min_Zoom_Level.coerceAtLeast(latZoom)

    }

    private fun enableMyLocationOverlay() {
        myLocationOverlay.runOnFirstFix {
            val currentLocation = myLocationOverlay.myLocation
            Log.i(
                "Current Lat & Long",
                "${currentLocation.latitude}   ${currentLocation.longitude}"
            )

            if (currentLocation != null) {
                currentPoint = GeoPoint(
                    myLocationOverlay.myLocation.latitude,
                    myLocationOverlay.myLocation.longitude
                )

                // Calculate desired zoom level based on the width and height of the MapView

                boundingBox = binding.streetMapView.boundingBox
//                desiredZoom = calculateDesiredZoom(boundingBox,binding.streetMapView.width,binding.streetMapView.height)
                runOnUiThread {
                    mapController.setCenter(currentPoint)
                    mapController.setZoom(16.0)
                }

            }

        }
    }

    private fun calculateRoute(startPoint: GeoPoint, endPoint: GeoPoint): List<GeoPoint> {
        // You can calculate the route here using a routing service or library (not included in this example).
        // For simplicity, we return a direct line between the two points.

        val route = ArrayList<GeoPoint>()
        route.add(startPoint)
        route.add(endPoint)

        return route
    }

    private fun displayRoute(routePoints: List<GeoPoint>) {
        val routeOverlay = Polyline()
        routeOverlay.color = getColor(R.color.red)
        routeOverlay.width = 5.0f
        routeOverlay.setPoints(routePoints)

        binding.streetMapView.overlays.add(routeOverlay)
        binding.streetMapView.invalidate()
    }
}