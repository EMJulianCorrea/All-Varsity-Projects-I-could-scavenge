package com.example.googlemapsdemo

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.googlemapsdemo.ui.theme.GoogleMapsDemoTheme
import com.example.googlemapsdemo.ui.screens.MapScreen
import com.example.googlemapsdemo.ui.screens.SearchResult
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient

private const val TAG = "GoogleMapsDemo"

class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var placesClient: PlacesClient

    private var hasLocationPermission by mutableStateOf(false)

    @SuppressLint("MissingPermission")
    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasLocationPermission = granted
        if (granted) {
            fetchLastKnownLocation()
        } else {
            errorMessage = "Location permission was denied, so your current location can't be shown."
        }
    }

    private var currentLocation by mutableStateOf<LatLng?>(null)

    private var searchQuery by mutableStateOf("")

    private var searchResult by mutableStateOf<SearchResult?>(null)

    private var isLoading by mutableStateOf(false)

    private var errorMessage by mutableStateOf<String?>(null)

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "?{MAPS_API_KEY}")
        }
        placesClient = Places.createClient(this)

        requestLocationPermissionIfNeeded()

        setContent {
            GoogleMapsDemoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MapScreen(
                        modifier = Modifier.padding(innerPadding),
                        currentLocation = currentLocation,
                        searchResult = searchResult,
                        searchQuery = searchQuery,
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        onSearchQueryChange = { query ->
                            searchQuery = query
                        },
                        onSearchClick = {
                            searchPlace(searchQuery)
                        },
                        onMyLocationClick = {
                            onMyLocationClick()
                        }
                    )
                }
            }
        }
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun requestLocationPermissionIfNeeded() {
        val alreadyGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (alreadyGranted) {
            hasLocationPermission = true
            fetchLastKnownLocation()
        } else {
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun fetchLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        isLoading = true
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                isLoading = false
                if (location != null) {
                    currentLocation = LatLng(location.latitude, location.longitude)
                    errorMessage = null
                } else {
                    errorMessage = "Your current location isn't available yet. Try again shortly."
                }
            }
            .addOnFailureListener { e ->
                isLoading = false
                errorMessage = "Couldn't get your current location."
                Log.e(TAG, "Failed to fetch last known location", e)
            }
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun onMyLocationClick() {
        if (hasLocationPermission) {
            fetchLastKnownLocation()
        } else {
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
    private fun searchPlace(query: String) {
        if (query.isBlank()) {
            errorMessage = "Enter a place to search for."
            return
        }

        isLoading = true
        errorMessage = null
        searchResult = searchResult

        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                val topPrediction = response.autocompletePredictions.firstOrNull()
                if (topPrediction == null) {
                    isLoading = false
                    errorMessage = "No results found for \"$query\"."
                    return@addOnSuccessListener
                }
                resolvePlace(topPrediction.placeId)
            }
            .addOnFailureListener { e ->
                isLoading = false
                errorMessage = "Search failed. Please try again."
                Log.e(TAG, "Autocomplete search failed for query: $query", e)
            }
    }

    private fun resolvePlace(placeId: String) {
        val placeFields: List<Place.Field> = listOf(
            Place.Field.LOCATION,
            Place.Field.DISPLAY_NAME,
            Place.Field.FORMATTED_ADDRESS
        )

        val request = FetchPlaceRequest.newInstance(
            placeId,
            placeFields
        )

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                isLoading = false

                val place = response.place
                val latLng = place.location

                if (latLng != null) {
                    searchResult = SearchResult(
                        name = place.displayName ?: "Unknown place",
                        address = place.formattedAddress,
                        latLng = latLng
                    )

                    errorMessage = null
                } else {
                    errorMessage = "That place doesn't have a location to show."
                }
            }
            .addOnFailureListener { e ->
                isLoading = false
                errorMessage = "Couldn't load details for that place."

                Log.e(
                    TAG,
                    "Failed to fetch place details for $placeId",
                    e
                )
            }
    }
}