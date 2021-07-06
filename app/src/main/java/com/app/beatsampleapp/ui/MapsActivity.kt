package com.app.beatsampleapp.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.app.beatsampleapp.R
import com.app.beatsampleapp.databinding.ActivityMainBinding
import com.app.beatsampleapp.viewmodels.MainActivityViewModel
import com.app.classify.ui.list.view.VenueList
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    companion object {
        private const val PERMISSION_LOCATION_REQUEST_CODE = 579
    }

    private val viewModel: MainActivityViewModel by lazy { ViewModelProvider(this)[MainActivityViewModel::class.java] }
    private lateinit var cancellationTokenSource: CancellationTokenSource
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMainBinding
    private val locationProviderClient: FusedLocationProviderClient by lazy {
        FusedLocationProviderClient(
            this
        )
    }
    private var currentLocation: Location? = null
    private var marker: Marker? = null
    var isExplorationModeEnabled: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //Added Fragments for Nearby Venue list and selected venue
        supportFragmentManager.beginTransaction().replace(R.id.venueList, VenueList()).commit()
        supportFragmentManager.beginTransaction().replace(R.id.selectedItem, SelectedItemFragment())
            .commit()
        //Added Observer for selected venue
        viewModel.selectedResult.observe(this, {
            if (it != null) {
                val lat: Double? = it.lat
                val lng: Double? = it.lng
                val current = LatLng(lat!!, lng!!)
                addMarkerAtLocation(current)
            }
        })

        //Set initial mode to exploration
        viewModel.changeModeToExploration()
        //Added Observer for Mode Change
        viewModel.mode.observe(this, {
            isExplorationModeEnabled = (it.name == "Exploration")
            if (it.name == "Exploration") {
                val builder = LocationSettingsRequest.Builder()
                val client: SettingsClient = LocationServices.getSettingsClient(this)
                val task: Task<LocationSettingsResponse> =
                    client.checkLocationSettings(builder.build())
                task.addOnSuccessListener {
                    // All location settings are satisfied. The client can initialize
                    // location requests here.
                    handleGetLocation()
                }

                task.addOnFailureListener { exception ->
                    if (exception is ResolvableApiException) {
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        Toast.makeText(this, "Location settings not satisfied", Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }
        })

    }

    //Below code is to ask for required permission for location update
    private fun handleGetLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_LOCATION_REQUEST_CODE
            )
            return
        }

        cancellationTokenSource = CancellationTokenSource()
        //Below function will collect location only once
        if (currentLocation == null) {
            locationProviderClient.getCurrentLocation(
                LocationRequest.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener {
                currentLocation = it
                getNearByFoodLocations(currentLocation!!)
            }.addOnFailureListener {
                Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show()
            }
        } else {
            getNearByFoodLocations(currentLocation!!)
        }
    }

    private fun getNearByFoodLocations(currentLocation: Location) {
        val current = LatLng(currentLocation.latitude, currentLocation.longitude)
        //Request for nearby locations
        viewModel.refresh(currentLocation)
        setMarkerAtCurrentLocation(current)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_LOCATION_REQUEST_CODE -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    //permission denied by user
                    AlertDialog.Builder(this)
                        .setMessage(getString(R.string.permission_string))
                        .setPositiveButton(getString(R.string.close)) { _, _ -> finish() }
                        .setNegativeButton(getString(R.string.allow)) { _, _ ->
                            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            startActivity(intent)
                        }
                        .setCancelable(false)
                        .create()
                        .show()
                } else {
                    //permission granted
                    handleGetLocation()
                }
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        //Below code will move marker position on map and request to api will be initiated
        mMap.setOnCameraMoveListener {
            if (marker != null) {
                marker!!.position = mMap.cameraPosition.target
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLng(mMap.cameraPosition.target))
        }

        mMap.setOnCameraIdleListener {
            if (marker != null && isExplorationModeEnabled) {
                val location = Location(LocationManager.GPS_PROVIDER)
                location.time = 2000
                location.apply {
                    latitude = mMap.cameraPosition.target.latitude
                    longitude = mMap.cameraPosition.target.longitude
                }
                if (this.currentLocation?.latitude ?: Double != location.latitude && this.currentLocation?.longitude ?: Double != location.longitude) {
                    this.currentLocation = location
                    getNearByFoodLocations(currentLocation!!)
                }
            }
        }
    }

    private fun setMarkerAtCurrentLocation(current: LatLng) {
        mMap.clear()
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 17.0f))
        val options = MarkerOptions().position(mMap.cameraPosition.target)
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
            .title("Exploration Mode")
        marker = mMap.addMarker(options)
    }

    private fun addMarkerAtLocation(current: LatLng) {
        // Creating a marker
        val markerOptions = MarkerOptions()
        // Setting the position for the marker
        markerOptions.position(current)
        // Setting the title for the marker.
        // This will be displayed on taping the marker
        markerOptions.title("Navigation Mode Marker")
        // Clears the previously touched position
        mMap.clear()
        // Moving to the touched position
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 17.0f))
        // Placing a marker on the touched position
        mMap.addMarker(markerOptions)

    }

    override fun onStop() {
        super.onStop()
        cancellationTokenSource.cancel()
    }

    override fun onBackPressed() {
        if (isExplorationModeEnabled) {
            finish()
        } else {
            viewModel.changeModeToExploration()
        }
    }
}