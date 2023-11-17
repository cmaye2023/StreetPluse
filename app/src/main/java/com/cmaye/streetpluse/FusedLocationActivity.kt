package com.cmaye.streetpluse

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.cmaye.streetpluse.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

class FusedLocationActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var currentLocationMarker: Marker
    private lateinit var binding: ActivityMainBinding
    private var initializePoint : GeoPoint ? = null
    private var currentPoint : GeoPoint ?= null

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                requestLocationUpdates()
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show()
                // Handle denied permission
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize osmdroid configuration
        Configuration.getInstance().load(applicationContext, getPreferences(MODE_PRIVATE))

        // Find the MapView
        mapView = binding.streetMapView


        // Set the center of the map
        initializePoint = GeoPoint(19.122104, 96.009651) // Example: Monywa
        mapView.controller.setCenter(initializePoint)
        mapView.controller.setZoom(10.0)

        // Add a marker for the initial location
        val initialMarker = Marker(mapView)
        initialMarker.position = initializePoint
        initialMarker.title = "Initial Location"
        mapView.setMultiTouchControls(true)
        mapView.overlays.add(initialMarker)


        // Add a marker for the current location
        currentLocationMarker = Marker(mapView)
        currentLocationMarker.title = "Current Location"
        mapView.overlays.add(currentLocationMarker)

        // Initialize fused location provider client
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Check and request location permissions
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationUpdates()
        } else {
            // Request permission
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        onClickListener()

        // Set a map events receiver to capture map clicks
        mapView.overlays.add(MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                p?.let {
                    handleMapClick(it)
                }
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                return false
            }
        }))
    }

//    private fun handleMarkerClick(marker: Marker) {
//        // Handle marker click event
//        Toast.makeText(
//            this,
//            "Marker Clicked - ${marker.title}",
//            Toast.LENGTH_SHORT
//        ).show()
//    }

    private fun handleMapClick(clickedPoint: GeoPoint) {
        // Do something with the clicked point
        Toast.makeText(
            this,
            "Map Clicked - Latitude: ${clickedPoint.latitude}, Longitude: ${clickedPoint.longitude}",
            Toast.LENGTH_SHORT
        ).show()
    }


    private fun onClickListener()
    {
        binding.btnCurrent.setOnClickListener {
            mapView.controller.animateTo(currentPoint)
            mapView.controller.setZoom(19.0) // Set an appropriate zoom level
        }

        binding.btnRouteWay.setOnClickListener {
            if (currentPoint != null && initializePoint != null)
            {
                // Calculate and display the route between the two points
                val routePoints = calculateRoute(currentPoint!!, initializePoint!!)
                displayRoute(routePoints)
            }

        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            object : com.google.android.gms.location.LocationCallback() {
                override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                    val location = locationResult.lastLocation
                    // Update the marker position with the new location
                    currentPoint = GeoPoint(location!!.latitude, location.longitude)
                    currentLocationMarker.position = currentPoint

                    // Change the marker icon dynamically (replace R.drawable.new_marker_icon with your custom marker icon)
                    val newMarkerDrawable = resources.getDrawable(R.drawable.icon_marker_line_map_svgrepo_com)
                    currentLocationMarker.icon = newMarkerDrawable
                    mapView.controller.animateTo(currentPoint)
                    updateTextView(currentPoint!!)
                    mapView.controller.setZoom(19.0) // Set an appropriate zoom level
                    mapView.invalidate() // Force redraw
                }
            },
            null
        )
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    private fun calculateRoute(startPoint: GeoPoint, endPoint: GeoPoint): List<GeoPoint> {
        return listOf(startPoint,endPoint)
    }

    private fun displayRoute(routePoints: List<GeoPoint>) {
        val routeOverlay = Polyline()
        routeOverlay.color = getColor(R.color.red)
        routeOverlay.width = 5.0f
        routeOverlay.setPoints(routePoints)

        binding.streetMapView.overlays.add(routeOverlay)
        binding.streetMapView.invalidate()
    }
    private fun updateTextView(geoPoint: GeoPoint) {
        val latitude = geoPoint.latitude
        val longitude = geoPoint.longitude
        binding.tvLatLng.text = "Latitude: $latitude, Longitude: $longitude"
    }
}