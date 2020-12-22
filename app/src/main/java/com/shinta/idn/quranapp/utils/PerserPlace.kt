package com.shinta.idn.quranapp.utils

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class PerserPlace {

    fun parse(jObject: JSONObject)
            : List<HashMap<String, String>> {
        var jPlaces: JSONArray? = null

        try {
            jPlaces = jObject.getJSONArray("result")
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return getPlaces(jPlaces)
    }

    private fun getPlaces(jPlaces: JSONArray?)
            : List<java.util.HashMap<String, String>> {

        val placesCount = jPlaces!!.length()
        val placeList: MutableList<HashMap<String,
                String>> = ArrayList()
        var place: HashMap<String, String>
        for (i in 0 until placesCount) {
            try {
                place = getPlace(jPlaces[i] as JSONObject)
                placeList.add(place)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        return placeList


    }

    private fun getPlace(jPlace: JSONObject): HashMap<String, String> {
        val place = HashMap<String, String>()
        var placeName = "-NA-"
        var vicinity = "-NA-"
        var address = "-NA-"
        var latitude = ""
        var longitude = ""

        try {
            if (!jPlace.isNull("name")) {
                placeName = jPlace.getString("name")
            }
            if (!jPlace.isNull("vicinity")) {
                vicinity = jPlace.getString("vicinity")
            }
            if (!jPlace.isNull("formatted_address")) {
                address = jPlace.getString("formatted_address")
            }

            latitude = jPlace.getJSONObject("geometry")
                .getJSONObject("location").getString("lat")
            longitude = jPlace.getJSONObject("geometry")
                .getJSONObject("location").getString("lng")

            place["place_name"] = placeName
            place["vicinity"] = vicinity
            place["Address"] = address
            place["lat"] = latitude
            place["lng"] = longitude
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return place
    }

}