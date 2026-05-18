package com.example.locationmanager3

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity(), LocationListener {

    private lateinit var locationManager: LocationManager

    // UI state variables
    private var latitude by mutableStateOf("--")
    private var longitude by mutableStateOf("--")
    private var altitude by mutableStateOf("--")
    private var accuracy by mutableStateOf("--")
    private var providerName by mutableStateOf("--")
    private var time by mutableStateOf("--")

    private var isGPSEnabled by mutableStateOf(false)
    private var isLocationActive by mutableStateOf(false)  // Toggle ON/OFF
    private var isLoading by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        // Request Permission
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }

        // Check GPS status
        checkGPSEnabledStatus()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF5F5F5)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header Section
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2196F3))
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "📍 Location Manager",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tasks 5.1 & 5.2",
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // GPS Status Card (Task 5.1)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isGPSEnabled) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "GPS Status",
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = if (isGPSEnabled) "✅ ACTIVE" else "❌ DISABLED",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isGPSEnabled) Color(0xFF2E7D32) else Color(0xFFC62828)
                                    )
                                }
                                if (!isGPSEnabled) {
                                    Button(
                                        onClick = { showEnableGPSDialog() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00))
                                    ) {
                                        Text("Enable GPS")
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Toggle Switch Card - Turn Location ON/OFF
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isLocationActive) Color(0xFFE3F2FD) else Color.White
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "🔘 Location Service",
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = if (isLocationActive) "ACTIVE" else "INACTIVE",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isLocationActive) Color(0xFF1976D2) else Color(0xFF757575)
                                    )
                                }
                                Switch(
                                    checked = isLocationActive,
                                    onCheckedChange = { isChecked ->
                                        if (isChecked) {
                                            startLocationUpdates()
                                        } else {
                                            stopLocationUpdates()
                                        }
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color(0xFF2196F3),
                                        checkedTrackColor = Color(0xFF90CAF9),
                                        uncheckedThumbColor = Color(0xFFBDBDBD),
                                        uncheckedTrackColor = Color(0xFFE0E0E0)
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Location Info Card (Task 5.2)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "📍 Location Information",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2196F3)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Divider()

                                // Location details grid
                                LocationDetailRow("Latitude:", latitude)
                                LocationDetailRow("Longitude:", longitude)
                                LocationDetailRow("Altitude:", if (altitude != "--") "$altitude m" else altitude)
                                LocationDetailRow("Accuracy:", if (accuracy != "--") "±$accuracy m" else accuracy)
                                LocationDetailRow("Provider:", providerName)
                                LocationDetailRow("Time:", time)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Manual Refresh Button (shows only when location is ON)
                        if (isLocationActive) {
                            Button(
                                onClick = { refreshLocation() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4CAF50)
                                ),
                                enabled = !isLoading && isGPSEnabled
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Getting Location...")
                                } else {
                                    Text("🔄 REFRESH LOCATION", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Status message
                        when {
                            !isGPSEnabled -> {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                                ) {
                                    Text(
                                        text = "⚠️ Please enable GPS to get your location",
                                        modifier = Modifier.padding(12.dp),
                                        fontSize = 12.sp,
                                        color = Color(0xFFE65100)
                                    )
                                }
                            }
                            !isLocationActive -> {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EAF6))
                                ) {
                                    Text(
                                        text = "💡 Toggle the switch ON to start getting location",
                                        modifier = Modifier.padding(12.dp),
                                        fontSize = 12.sp,
                                        color = Color(0xFF3949AB)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun LocationDetailRow(label: String, value: String) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black
            )
        }
    }

    // TASK 5.1: Check GPS status
    private fun checkGPSEnabledStatus() {
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    // TASK 5.1: Show dialog to enable GPS
    private fun showEnableGPSDialog() {
        AlertDialog.Builder(this)
            .setTitle("📡 Enable GPS")
            .setMessage("GPS is required to get your current location. Do you want to enable it now?")
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Start location updates when toggle is turned ON
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show()
            isLocationActive = false
            return
        }

        if (!isGPSEnabled) {
            Toast.makeText(this, "Please enable GPS first", Toast.LENGTH_LONG).show()
            isLocationActive = false
            showEnableGPSDialog()
            return
        }

        isLocationActive = true
        Toast.makeText(this, "📍 Location service activated", Toast.LENGTH_SHORT).show()

        // Get location immediately when turned on
        refreshLocation()
    }

    // Stop location updates when toggle is turned OFF
    private fun stopLocationUpdates() {
        isLocationActive = false
        // Clear location display
        latitude = "--"
        longitude = "--"
        altitude = "--"
        accuracy = "--"
        providerName = "--"
        time = "--"

        Toast.makeText(this, "🔴 Location service deactivated", Toast.LENGTH_SHORT).show()
    }

    // TASK 5.2: Refresh location manually
    private fun refreshLocation() {
        if (!isLocationActive) {
            Toast.makeText(this, "Please turn ON location service first", Toast.LENGTH_SHORT).show()
            return
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show()
            return
        }

        // Check GPS status again
        checkGPSEnabledStatus()

        if (!isGPSEnabled) {
            Toast.makeText(this, "Please enable GPS first", Toast.LENGTH_LONG).show()
            showEnableGPSDialog()
            return
        }

        isLoading = true

        var location: Location? = null

        // Try GPS first
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if (location != null) {
            providerName = "GPS"
        }

        // If GPS fails, try Network
        if (location == null) {
            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            if (isNetworkEnabled) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (location != null) {
                    providerName = "NETWORK"
                }
            }
        }

        // Also request fresh updates
        if (isLocationActive) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000,  // 5 seconds
                1f,    // 1 meter
                this
            )
        }

        // Display results
        if (location != null) {
            updateUIDisplay(location)
            Toast.makeText(this, "✅ Location obtained from $providerName", Toast.LENGTH_SHORT).show()
        } else {
            latitude = "--"
            longitude = "--"
            altitude = "--"
            accuracy = "--"
            providerName = "--"
            time = "--"

            Toast.makeText(this, "❌ Unable to get location. Try moving to an open area.", Toast.LENGTH_LONG).show()
        }

        isLoading = false
    }

    private fun updateUIDisplay(location: Location) {
        latitude = String.format("%.6f", location.latitude)
        longitude = String.format("%.6f", location.longitude)
        altitude = String.format("%.2f", location.altitude)
        accuracy = String.format("%.2f", location.accuracy)

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        time = sdf.format(Date(location.time))
    }

    // LocationListener methods
    override fun onLocationChanged(location: Location) {
        if (isLocationActive) {
            updateUIDisplay(location)
            providerName = location.provider ?: "GPS"
        }
    }

    override fun onProviderDisabled(provider: String) {
        if (provider == LocationManager.GPS_PROVIDER) {
            isGPSEnabled = false
            if (isLocationActive) {
                Toast.makeText(this, "GPS Disabled! Turning off location service.", Toast.LENGTH_LONG).show()
                stopLocationUpdates()
            }
        }
    }

    override fun onProviderEnabled(provider: String) {
        if (provider == LocationManager.GPS_PROVIDER) {
            isGPSEnabled = true
            Toast.makeText(this, "GPS Enabled!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        checkGPSEnabledStatus()
    }

    override fun onPause() {
        super.onPause()
        // Stop location updates when app is paused
        locationManager.removeUpdates(this)
    }
}