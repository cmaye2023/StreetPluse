package com.cmaye.streetpluse

import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cmaye.streetpluse.databinding.ActivityMainBinding
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline


class LocationManagerActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var locationManager: LocationManager
    private lateinit var currentLocationMarker: Marker
    private lateinit var binding: ActivityMainBinding

    private var currentPoint: GeoPoint? = null
    private var initialPoint: GeoPoint? = null

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Permission granted, request location updates
                requestLocationUpdates()
            } else {
                // Handle permission denial
                // You may want to inform the user or take other actions
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
        mapView.setMultiTouchControls(true)

        // Set the center of the map
        initialPoint = GeoPoint(17.312240, 96.516172) // Example: Bago
        mapView.controller.setCenter(initialPoint)
//        mapView.controller.animateTo(initialPoint)
        mapView.controller.setZoom(16.0)

        // Add a marker for the initial location
        val initialMarker = Marker(mapView)
        initialMarker.position = initialPoint
        initialMarker.title = "Initial Location"
        mapView.overlays.add(initialMarker)

        // Add a marker for the current location
        currentLocationMarker = Marker(mapView)
        currentLocationMarker.title = "Current Location"
        mapView.overlays.add(currentLocationMarker)

        // Initialize location manager
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        // Check location permissions
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is already granted, request location updates
            requestLocationUpdates()
        } else {
            // Permission is not granted, request it
            permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }

        onCLickListener()
    }

    private fun onCLickListener() {
        binding.btnCurrent.setOnClickListener {
            mapView.controller.animateTo(currentPoint)
            mapView.controller.setZoom(16.0)
        }

        binding.btnRouteWay.setOnClickListener {
            if (currentPoint != null && initialPoint != null) {
                // Calculate and display the route between the two points
                val routePoints = calculateRoute(currentPoint!!, initialPoint!!)
                displayRoute(routePoints)
            }
        }
    }

    private fun requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

            //for demo, getLastKnownLocation from GPS only, not from NETWORK
            val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            lastLocation?.let {location ->
                // Update the marker position with the new location
                currentPoint = GeoPoint(location.latitude, location.longitude)

                // Move the map to the current location
                mapView.controller.animateTo(currentPoint)
                mapView.controller.setZoom(16.0)

                // Update the marker position
                currentLocationMarker.position = currentPoint
                updateTextView(currentPoint!!)
                mapView.invalidate() // Force redraw
            }

        }
    }

    private fun calculateRoute(startPoint: GeoPoint, endPoint: GeoPoint): List<GeoPoint> {
        // TODO: Implement the actual route calculation using a routing service or library.
        // For now, return a direct line between the two points as a placeholder.
        return listOf(startPoint, endPoint)
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