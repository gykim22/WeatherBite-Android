package com.example.termproject

import android.util.Log
import org.json.JSONObject

class DataParser {
    fun parse(jsonData: String): List<HashMap<String, String>> {
        val jsonObject = JSONObject(jsonData)
        val jsonArray = jsonObject.getJSONArray("results")
        val placesList = ArrayList<HashMap<String, String>>()
        for (i in 0 until jsonArray.length()) {
            val place = jsonArray.getJSONObject(i)
            Log.d("text", place.toString())
            val placeMap = HashMap<String, String>()
            val lat = place.getJSONObject("geometry").getJSONObject("location").getString("lat")
            val lng = place.getJSONObject("geometry").getJSONObject("location").getString("lng")
            val placeName = place.getString("name")
            val placeRating = place.getString("rating")
            val placeAddress = place.getString("vicinity")
            placeMap["lat"] = lat
            placeMap["lng"] = lng
            placeMap["place_name"] = placeName
            placeMap["place_rating"] = placeRating
            placeMap["place_address"] = placeAddress
            placesList.add(placeMap)
        }
        Log.d("DataParser", "Parsed JSON data: $placesList")
        return placesList
    }
}
