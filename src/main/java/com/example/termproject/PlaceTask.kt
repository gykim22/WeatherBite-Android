package com.example.termproject

import android.os.AsyncTask
import android.util.Log
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class PlaceTask(private val mMap: GoogleMap) : AsyncTask<String, Int, String>() {
    override fun doInBackground(vararg params: String?): String {
        var data = ""
        try {
            data = downloadUrl(params[0]!!)
            Log.d("PlaceTask", "Downloaded data: $data")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return data
    }

    override fun onPostExecute(result: String?) {
        result?.let {
            val dataParser = DataParser()
            val nearbyPlacesList = dataParser.parse(it)
            Log.d("PlaceTask", "Parsed places: $nearbyPlacesList")
            mMap.clear()
            showNearbyPlaces(nearbyPlacesList)
        }
    }

    private fun downloadUrl(strUrl: String): String {
        var data = ""
        var iStream: BufferedReader? = null
        var urlConnection: HttpURLConnection? = null
        try {
            val url = URL(strUrl)
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.connect()

            iStream = BufferedReader(InputStreamReader(urlConnection.inputStream))
            val sb = StringBuffer()
            var line: String?
            while (iStream.readLine().also { line = it } != null) {
                sb.append(line)
            }
            data = sb.toString()
            iStream.close()
            urlConnection.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            iStream?.close()
            urlConnection?.disconnect()
        }
        return data
    }

    private fun showNearbyPlaces(nearbyPlacesList: List<HashMap<String, String>>) {
        for (i in nearbyPlacesList.indices) {
            val googlePlace = nearbyPlacesList[i]
            Log.d("text", googlePlace.toString())
            val lat = googlePlace["lat"]?.toDouble() ?: 0.0
            val lng = googlePlace["lng"]?.toDouble() ?: 0.0
            val placeName = googlePlace["place_name"]
            val placeRating = googlePlace["place_rating"]
            val placeAddress = googlePlace["place_address"]
            val latLng = LatLng(lat, lng)
            val markerOptions = MarkerOptions()
            markerOptions.position(latLng)
            markerOptions.title(placeName)
            val snippet = "주소: $placeAddress, 구글 평점: $placeRating "
            markerOptions.snippet(snippet)
            mMap.addMarker(markerOptions)
            Log.d("PlaceTask", "Added marker: $placeName at $latLng")
        }
    }
}

