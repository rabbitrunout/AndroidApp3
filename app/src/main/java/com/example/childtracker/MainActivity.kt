package com.example.childtracker

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.*
import java.util.Locale
import android.view.animation.AnimationUtils

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var latlngText: TextView
    private lateinit var addressText: TextView
    private lateinit var shareButton: Button
    private lateinit var historyButton: Button
    private lateinit var clearHistoryButton: Button
    private lateinit var sosButton: Button

    private val LOCATION_PERMISSION_REQUEST = 100

    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var lastLocation: Location? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Insets for notch / system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayout)) { v, insets ->
            val sb = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom)
            insets
        }

        // UI elements
        latlngText = findViewById(R.id.latlngText)
        addressText = findViewById(R.id.addressText)
        shareButton = findViewById(R.id.shareButton)
        historyButton = findViewById(R.id.historyButton)
        clearHistoryButton = findViewById(R.id.clearHistoryButton)
        sosButton = findViewById(R.id.sosButton)

        // SOS Pulse Animation
        sosButton.startAnimation(AnimationUtils.loadAnimation(this, R.anim.sos_pulse))

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            2000L
        )
            .setMinUpdateIntervalMillis(1000)
            .setMaxUpdateDelayMillis(3000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { updateLocationUI(it) }
            }
        }

        checkLocationPermission()

        shareButton.setOnClickListener { shareLocation() }
        historyButton.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        clearHistoryButton.setOnClickListener {
            getSharedPreferences("history", MODE_PRIVATE).edit().clear().apply()
            Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show()
        }
        sosButton.setOnClickListener { sendSOS() }
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
        } else {
            startLocationUpdates()
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            mainLooper
        )
    }

    @SuppressLint("SetTextI18n")
    private fun updateLocationUI(location: Location) {

        // Anti-spam: ignore movements < 10m
        lastLocation?.let { prev ->
            if (prev.distanceTo(location) < 10) return
        }

        lastLocation = location

        val lat = "%.6f".format(location.latitude)
        val lon = "%.6f".format(location.longitude)

        latlngText.text = "Latitude: $lat\nLongitude: $lon"

        saveLocation(lat, lon)

        val geocoder = Geocoder(this, Locale.getDefault())
        val addr = geocoder.getFromLocation(location.latitude, location.longitude, 1)

        addressText.text =
            if (!addr.isNullOrEmpty()) addr[0].getAddressLine(0)
            else "Address not found"
    }

    private fun saveLocation(lat: String, lon: String) {
        val timestamp = java.text.SimpleDateFormat(
            "MMM dd, yyyy  h:mm a",
            Locale.getDefault()
        ).format(System.currentTimeMillis())

        val entry = "$lat, $lon — $timestamp"

        val prefs = getSharedPreferences("history", MODE_PRIVATE)
        val old = prefs.getString("locations", "") ?: ""
        val updated = if (old.isBlank()) entry else "$old\n$entry"

        prefs.edit().putString("locations", updated).apply()
    }

    private fun shareLocation() {
        val msg = "${latlngText.text}\n${addressText.text}"

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, msg)

        startActivity(Intent.createChooser(intent, "Share Location"))
    }

    override fun onResume() {
        super.onResume()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun sendSOS() {
        val msg =
            "🚨 SOS! I need help!\n\n${latlngText.text}\n${addressText.text}\n\nSent from ChildTracker App"

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, msg)
        }

        startActivity(Intent.createChooser(intent, "Send SOS"))
    }
}
