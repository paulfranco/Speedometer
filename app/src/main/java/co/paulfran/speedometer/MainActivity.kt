package co.paulfran.speedometer

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import co.paulfran.speedometer.databinding.ActivityMainBinding
import com.google.android.gms.location.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationProvideClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var kph: Float? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //binding = ActivityMainBinding.inflate(layoutInflater)
        //setContentView(binding.root)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val font = Typeface.createFromAsset(this.assets, "font/digital_7_mono.ttf")
        binding.speed.typeface = font
        binding.latitude.typeface = font
        binding.longLatitude.typeface = font

        binding.stopButton.isEnabled = false
        binding.startButton.isEnabled = true

        fusedLocationProvideClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                binding.latitude.text = location.latitude.toString()
                binding.longLatitude.text = location.longitude.toString()
                //binding.speed.text = (location.speed * 3600 / 1000).roundToInt().toString()
                kph = location.speed * 3600 / 1000
                val mph = kph!! * 0.62137119223733
                binding.speed.text = mph.roundToInt().toString()
            }
        }

        binding.startButton.setOnClickListener {
            startLocationUpdates()
            it.isEnabled = false
            binding.stopButton.isEnabled = true
        }

        binding.stopButton.setOnClickListener {
            stopLocationUpdates()
            it.isEnabled = false
            binding.startButton.isEnabled = true
            binding.speed.text = "0"
        }

    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun startLocationUpdates() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                requestNewLocationData()
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 1000
        locationRequest.fastestInterval = 500
        fusedLocationProvideClient.requestLocationUpdates(
                locationRequest, locationCallback,
                Looper.myLooper()
        )
    }

    private fun stopLocationUpdates() {
        fusedLocationProvideClient.removeLocationUpdates(locationCallback)
    }

    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ),
                44
        )
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        )
    }

}