package com.shinta.idn.quranapp.detail

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.shinta.idn.quranapp.R
import com.shinta.idn.quranapp.adapter.AyatAdapter
import com.shinta.idn.quranapp.model.ModelAyat
import com.shinta.idn.quranapp.model.ModelSurah
import com.shinta.idn.quranapp.network.Api
import kotlinx.android.synthetic.main.activity_detail_surah.*
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException

class DetailSurahActivity : AppCompatActivity() {

    var nomor : String? = null
    var nama : String? = null
    var arti : String? = null
    var type : String? = null
    var ayat : String? = null
    var keterangan : String? = null
    var audio : String? = null
    var modelSurah : ModelSurah? = null
    var ayatAdapter: AyatAdapter? = null
    var proggressDialog: ProgressDialog? = null
    var modelAyat : MutableList<ModelAyat> = ArrayList()
    var mHandler : Handler? = null

    @SuppressLint("")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_surah)

        toolbar_detail.setTitle(null)
        setSupportActionBar(toolbar_detail)
        assert(supportActionBar != null)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        mHandler = Handler()

        //get data dri listsurat
        modelSurah = intent
            .getSerializableExtra("detailSurah")
                as ModelSurah

        if (modelSurah != null){
            nomor = modelSurah!!.nomor
            nama = modelSurah!!.nama
            arti = modelSurah!!.arti
            type = modelSurah!!.type
            ayat = modelSurah!!.ayat
            audio = modelSurah!!.audio
            keterangan = modelSurah!!.keterangan

            fabStop.visibility = View.GONE
            fabPlay.visibility = View.VISIBLE

            //setText
            tvHeader.setText(nama)
            tvTitle.setText(nama)
            tvSubTitle.setText(arti)
            tvIndo.setText("$type - $ayat Ayat")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                tvKeterangan.setText(
                    Html.fromHtml(keterangan
                    , Html.FROM_HTML_MODE_COMPACT))
            else{
                tvKeterangan.setText(Html.fromHtml(keterangan))
            }

            playAudio()
        }
        proggressDialog = ProgressDialog(this)
        proggressDialog!!.setTitle("Mohon tunggu")
        proggressDialog!!.setCancelable(false)
        proggressDialog!!.setMessage("Sedang menampilkan data...")

        rvAyat.layoutManager = LinearLayoutManager(this)
        rvAyat.setHasFixedSize(true)

        listAyat()
    }

    private fun listAyat() {
        proggressDialog!!.show()
        AndroidNetworking.get(Api.URL_LIST_AYAT)
            .addPathParameter("nomor", nomor)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONArray(object : JSONArrayRequestListener {
                override fun onResponse(response: JSONArray) {
                    for (i in 0 until response.length()) {
                        try {
                            proggressDialog!!.dismiss()
                            val dataApi = ModelAyat()
                            val jsonObject = response.getJSONObject(i)
                            dataApi.nomor = jsonObject.getString("nomor")
                            dataApi.arab = jsonObject.getString("ar")
                            dataApi.indo = jsonObject.getString("id")
                            dataApi.terjemahan = jsonObject.getString("tr")
                            modelAyat.add(dataApi)
                            showListAyat()
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            Toast.makeText(
                                this@DetailSurahActivity,
                                "Gagal mengambil data", Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                override fun onError(anError: ANError?) {
                    proggressDialog!!.dismiss()
                    Toast.makeText(
                        this@DetailSurahActivity,
                        "Tidak ada jarigan internet",
                        Toast.LENGTH_SHORT).show()
                }

            })
    }

    private fun showListAyat() {
        ayatAdapter = AyatAdapter(this@DetailSurahActivity,
            modelAyat)
        rvAyat!!.adapter = ayatAdapter
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home){
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("RestrictetApi")
    private fun playAudio() {
        val mediaPlayer = MediaPlayer()
        fabPlay.setOnClickListener(View.OnClickListener {
            try {
                mediaPlayer.setAudioStreamType(
                    AudioManager
                    .STREAM_MUSIC)
                mediaPlayer.setDataSource(audio)
                mediaPlayer.prepare()
                mediaPlayer.start()
            }catch (e : IOException){
                e.printStackTrace()
            }
            fabPlay.visibility = View.GONE
            fabStop.visibility = View.VISIBLE
        })

        fabStop.setOnClickListener(View.OnClickListener {
            mediaPlayer.stop()
            mediaPlayer.reset()
            fabPlay.visibility = View.VISIBLE
            fabStop.visibility = View.GONE
        })

    }
}