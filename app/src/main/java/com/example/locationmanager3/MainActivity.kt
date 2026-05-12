package com.example.locationmanager3

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity(), LocationListener {

    private lateinit var locationManager: LocationManager

    private var latitude by mutableStateOf("Latitude: ")
    private var longitude by mutableStateOf("Longitude: ")
    private var speed by mutableStateOf("Speed: ")
    private var time by mutableStateOf("Time: ")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationManager =
            getSystemService(LOCATION_SERVICE) as LocationManager

        // Request Permission
        if (
            ActivityCompat.checkSelfPermission(
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

        setContent {

            Surface(
                modifier = Modifier.fillMaxSize()
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Button(
                        onClick = {

                            if (
                                ActivityCompat.checkSelfPermission(
                                    this@MainActivity,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {

                                locationManager.requestLocationUpdates(
                                    LocationManager.GPS_PROVIDER,
                                    1000,
                                    1f,
                                    this@MainActivity
                                )

                                Toast.makeText(
                                    this@MainActivity,
                                    "GPS Tracking Started",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    ) {

                        Text("Get Location")
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(latitude, fontSize = 20.sp)

                    Text(longitude, fontSize = 20.sp)

                    Text(speed, fontSize = 20.sp)

                    Text(time, fontSize = 20.sp)
                }
            }
        }
    }

    override fun onLocationChanged(location: Location) {

        latitude = "Latitude: ${location.latitude}"

        longitude = "Longitude: ${location.longitude}"

        speed = "Speed: ${location.speed} m/s"

        val sdf =
            SimpleDateFormat(
                "dd/MM/yyyy HH:mm:ss",
                Locale.getDefault()
            )

        time = "Time: ${sdf.format(Date(location.time))}"
    }
}