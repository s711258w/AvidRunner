package edu.uw.s711258w.avidrunner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View


import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import kotlin.math.round

const val REQUEST_PERMISSIONS_CODE = 1
const val LAST_LOCATION_REQUEST_CODE = 1
const val ONGOING_LOCATION_REQUEST_CODE = 2


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private val TAG = "MapsActivity"

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var linesList: MutableList<Polyline> = mutableListOf()
    private var polyline: PolylineOptions? = null
    private var line: Polyline? = null
    private var milesTraveled = 0.0
    private var from: LatLng? = null
    private lateinit var locationCallback: LocationCallback

    val RequiredPermissions = listOf<String>(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(checkPermissions()) {
            initializeMap()
        } else {
            getPermissions()
        }
        milesTraveled = 0.0

        startService(Intent(this@MapsActivity, CountingService::class.java))

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation()

    }

    override fun onStart() {
        super.onStart()
        startLocationUpdates()
    }

    //when "Stop" button is pressed
    fun handleStop(v: View) {
        Log.i(TAG, "Stop pressed")
        stopService(Intent(this@MapsActivity, CountingService::class.java))

    }

    // Returns whether the permissions have been granted
    fun checkPermissions(): Boolean {
        for(permission: String in RequiredPermissions) {
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    // Asks for permissions
    fun getPermissions() {
        for(permission: String in RequiredPermissions) {
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(permission), REQUEST_PERMISSIONS_CODE)
            }
        }
    }

    fun initializeMap() {
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.retainInstance = true
        mapFragment.getMapAsync(this)
    }

    fun getLastLocation() {
        val permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            //access last location, asynchronously!
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                Log.v(TAG, "$location")
                displayLocation(location)
            }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LAST_LOCATION_REQUEST_CODE)
        }
    }

    fun displayLocation(location: Location?) {
        if (location != null) {
            val lat = location.latitude
            val lng = location.longitude
            Log.v(TAG, "Lat: " + lat)
            Log.v(TAG, "Lng: " + lng)

            // Make new latlng
            val latLng: LatLng = LatLng(lat, lng)

            // move camera to current pos
            //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            val zoomLevel = 16.0f //This goes up to 21
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel))

            // Set polyline options
            if (polyline == null) {
                polyline = PolylineOptions().width(25f).color(Color.parseColor("#636161"))
            }

            polyline!!.add(latLng)
            line = mMap.addPolyline(polyline)
            if(line!!.points.size > 1) {
                linesList.add(line!!)

                val geoJson = convertToGeoJson(linesList)
                Log.v(TAG, geoJson)
                val intent = Intent(this, MapSavingService::class.java)
                intent.putExtra("data", geoJson)
                startService(intent)
            }

            milesTraveled += calculateDistance(latLng)
            Log.v(TAG, "" + milesTraveled)
        }
    }

    fun startLocationUpdates() {
        val permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            val locationRequest = LocationRequest().apply {
                interval = 20000
                fastestInterval = 15000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    locationResult ?: return
                    displayLocation(locationResult.locations[0])
                }
            }
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), ONGOING_LOCATION_REQUEST_CODE)
        }
    }

    // Method based off original work by GH user xd6
    // https://github.com/xd6/GoogleMapDistanceMeasure/blob/master/app/src/main/java/com/xd6/googlemapstoy/MapsActivity.java
    fun calculateDistance(to: LatLng?): Double {
        if (from == null || to == null) {
            from = to
            return 0.0
        }

        val R = 6372.8

        var lat1 = from!!.latitude
        var lat2 = to.latitude

        val lon1 = from!!.longitude
        val lon2 = to.longitude

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        lat1 = Math.toRadians(lat1)
        lat2 = Math.toRadians(lat2)

        val a = Math.pow(Math.sin(dLat / 2), 2.0) + Math.pow(Math.sin(dLon / 2), 2.0) * Math.cos(lat1) * Math.cos(lat2)
        val c = 2 * Math.asin(Math.sqrt(a))

        from = to
        val roundOff = (Math.round(kmToMi(R * c) * 1000.0) / 1000.0).toDouble()
        Log.v(TAG, "Distance: " + roundOff)
        return roundOff

    }

    fun kmToMi(km: Double): Double {
        return km * 0.621371
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
    }
}
