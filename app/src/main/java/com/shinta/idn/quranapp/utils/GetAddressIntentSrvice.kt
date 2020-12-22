package com.shinta.idn.quranapp.utils

import android.app.IntentService
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.ResultReceiver
import java.lang.Exception
import java.util.*

class GetAddressIntentSrvice : IntentService(IDENTIFIER) {

    private var addressResultReceiver: ResultReceiver? = null

    override fun onHandleIntent(intent: Intent?) {
        var msg = ""

        //get result receiver from intent
        addressResultReceiver = intent!!
            .getParcelableExtra("add_receiver")
        if (addressResultReceiver == null){
            return
        }

        val location = intent
            .getParcelableExtra<Location>("add_location")

        //send no locartion error to results receiver
        if (location == null){
            msg = "No Location, cant go further without location"
            sendResultsToReceiver(0, msg)
            return
        }
        val geocoder = Geocoder(this, Locale.getDefault())
        var addresses: List<Address>? = null
        try {
            addresses = geocoder.getFromLocation(
                location.latitude,
                location.longitude, 1
            )
        }catch (ignored: Exception){

        }

        if (addresses ==  null || addresses.size == 0){
            msg = "No address found for the location"
            sendResultsToReceiver(1, msg)
        }
        else{
            val address = addresses[0]
            val addressDetails = StringBuffer()

            addressDetails.append(address.adminArea)
            addressDetails.append("\n")
            sendResultsToReceiver(2, addressDetails.toString())
        }
    }

    private fun sendResultsToReceiver(resultCode: Int, msg: String) {
        val bundle = Bundle()
        bundle.putString("address_result", msg)
        addressResultReceiver!!.send(resultCode, bundle)
    }

    companion object{
        private const val IDENTIFIER = "GetAddressIntentService"
    }

}