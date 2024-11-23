package com.example.termproject

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val TAG = "MapFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        // Initialize map
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Initialize UI elements
        searchEditText = view.findViewById(R.id.searchEditText)
        searchButton = view.findViewById(R.id.searchButton)

        setFragmentResultListener("randomChoice") { randomChoice, bundle ->
            //결과 값을 받는 곳입니다.
            val food = bundle.getString("bundleFood")
            searchEditText.setText(food)
            val query = searchEditText.text.toString()
            if (query.isNotEmpty()) {
                searchNearbyPlaces(query)
            } else {
                Toast.makeText(activity, "Please enter a search term", Toast.LENGTH_SHORT).show()
            }
        }

        // Set search button click listener
        searchButton.setOnClickListener {
            val query = searchEditText.text.toString()
            if (query.isNotEmpty()) {
                searchNearbyPlaces(query)
            } else {
                Toast.makeText(activity, "Please enter a search term", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val pusanNationalUniversity = LatLng(35.2339681, 129.0806855) // 부산대학교의 좌표
        mMap.addMarker(MarkerOptions().position(pusanNationalUniversity).title("Pusan National University"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pusanNationalUniversity, 15F))
    }

    private fun getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener { location: Location? ->
            location?.let {
                val currentLocation = LatLng(it.latitude, it.longitude)
                mMap.addMarker(MarkerOptions().position(currentLocation).title("Current Location"))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15F))
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastKnownLocation()
            } else {
                Toast.makeText(activity, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun searchNearbyPlaces(query: String) {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val currentLocation = LatLng(it.latitude, it.longitude)
                val apiKey = getString(R.string.google_maps_key)
                val radius = 5000

                val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" +
                        "${currentLocation.latitude},${currentLocation.longitude}" +
                        "&radius=$radius" +
                        "&type=restaurant" +
                        "&keyword=$query" +
                        "&key=$apiKey"

                Log.d(TAG, "URL: $url")  // URL을 로그에 출력

                PlaceTask(mMap).execute(url)
            }
        }
    }
}


