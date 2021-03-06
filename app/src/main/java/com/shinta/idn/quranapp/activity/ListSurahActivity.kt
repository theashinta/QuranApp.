package com.shinta.idn.quranapp.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.text.format.DateFormat
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.google.android.gms.location.*
import com.shinta.idn.quranapp.R
import com.shinta.idn.quranapp.adapter.SurahAdapter
import com.shinta.idn.quranapp.detail.DetailSurahActivity
import com.shinta.idn.quranapp.fragment.JadwalSholatFragment
import com.shinta.idn.quranapp.model.ModelSurah
import com.shinta.idn.quranapp.network.Api
import com.shinta.idn.quranapp.utils.GetAddressIntentSrvice
import kotlinx.android.synthetic.main.activity_list_surah.*
import org.json.JSONArray
import org.json.JSONException
import java.util.*
import kotlin.collections.ArrayList

class ListSurahActivity : AppCompatActivity(), SurahAdapter.onSelectDataa {

    var surahAdapter: SurahAdapter? = null
    var progressDialog: ProgressDialog? = null
    var modelSurah: MutableList<ModelSurah> = ArrayList()
    var hariIni: String? = null
    var tanggal: String? = null
    private var fussedLocationClient: FusedLocationProviderClient? = null
    private var addressResultReciever: LocationAddressResultReciever? = null
    private var currentLocation: Location? = null
    var locationCallback: LocationCallback? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_surah)

        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Mohon Tunggu")
        progressDialog!!.setCancelable(false)
        progressDialog!!.setMessage("Sedang menampilkan data")

        addressResultReciever = LocationAddressResultReciever(Handler())

        val dateNow = Calendar.getInstance().time
        hariIni = DateFormat.format("EEEE", dateNow) as String
        tanggal = DateFormat.format("d MMMM yyyy", dateNow) as String
        tvToday.text = hariIni
        tvDate.text = tanggal

        val sendDetail = JadwalSholatFragment.newInstance("detail")
        llTime.setOnClickListener(View.OnClickListener {
            sendDetail.show(supportFragmentManager, sendDetail.tag)
        })

        llMosque.setOnClickListener(View.OnClickListener {
            startActivity(
                Intent(this@ListSurahActivity,
                    MasjidActivity::class.java)
            )
        })

        rvSurah.layoutManager = LinearLayoutManager(this)
        rvSurah.setHasFixedSize(true)

        fussedLocationClient = LocationServices
            .getFusedLocationProviderClient(this)
        locationCallback = object :LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult) {
                currentLocation = locationResult.locations[0]
                address
            }
        }
        startLocationUpdates()
        listSurah()
    }

    private val address: Unit
        get() {
            if (!Geocoder.isPresent()){
                Toast.makeText(this@ListSurahActivity,
                    "can't find current address, ",
                    Toast.LENGTH_SHORT).show()
                return
            }
            val intent = Intent(this, GetAddressIntentSrvice::class.java)
            intent.putExtra("add_receiver", addressResultReciever)
            intent.putExtra("add_location", currentLocation)
            startService(intent)
        }

    private fun listSurah() {
        progressDialog!!.show()
        AndroidNetworking.get(Api.URL_LIST_SURAH)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONArray(object : JSONArrayRequestListener {
                override fun onResponse(response: JSONArray) {
                    for (i in 0 until response.length()){
                        try {
                            progressDialog!!.dismiss()
                            val dataApi = ModelSurah()
                            val jsonObject = response.getJSONObject(i)
                            dataApi.nomor = jsonObject.getString("nomor")
                            dataApi.nama = jsonObject.getString("nama")
                            dataApi.type = jsonObject.getString("type")
                            dataApi.ayat = jsonObject.getString("ayat")
                            dataApi.asma = jsonObject.getString("asma")
                            dataApi.arti = jsonObject.getString("arti")
                            dataApi.audio = jsonObject.getString("audio")
                            dataApi.keterangan = jsonObject.getString("keterangan")
                            modelSurah.add(dataApi)
                            showListSurah()
                        } catch (e: JSONException){
                            e.printStackTrace()
                            Toast.makeText(this@ListSurahActivity
                                , "Gagal menampilkan data", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onError(anError: ANError?) {
                    progressDialog!!.dismiss()
                    Toast.makeText(this@ListSurahActivity, "Tidak ada jaringan",
                        Toast.LENGTH_SHORT).show()
                }

            })
    }

    private fun showListSurah() {
        surahAdapter = SurahAdapter(
            this@ListSurahActivity,
            modelSurah, this
        )

        rvSurah!!.adapter = surahAdapter

    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }else{
            val locationRequest = LocationRequest()
            locationRequest.interval = 1000
            locationRequest.fastestInterval = 1000
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            fussedLocationClient!!.requestLocationUpdates(
                locationRequest, locationCallback, null)

        }
    }


    override fun onSelected(modelSurah: ModelSurah?) {
        val intent = Intent(this@ListSurahActivity, DetailSurahActivity::class.java)
        intent.putExtra("detailSurah", modelSurah)
        startActivity(intent)

    }

    private inner class LocationAddressResultReciever
    internal constructor(handler: Handler?): ResultReceiver(handler){
        override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
            if (resultCode == 0) {
                address
            }
            if (resultCode == 1){
                Toast.makeText(this@ListSurahActivity,
                    "Address not found", Toast.LENGTH_SHORT).show()
            }
            val currentAdd = resultData.getString("address_result")
            showResults(currentAdd)
        }
    }

    private fun showResults(currentAdd: String?) {
        txtLocation!!.text = currentAdd
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        fussedLocationClient!!.removeLocationUpdates(locationCallback)
    }

    companion object{
        private const val LOCATION_PERMISSION_REQUEST_CODE = 2
    }

  }
