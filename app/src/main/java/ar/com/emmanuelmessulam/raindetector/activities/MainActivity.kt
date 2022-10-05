package ar.com.emmanuelmessulam.raindetector.activities

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import ar.com.emmanuelmessulam.raindetector.*
import ar.com.emmanuelmessulam.raindetector.R
import ar.com.emmanuelmessulam.raindetector.persisters.CityPersister
import ar.com.emmanuelmessulam.raindetector.processing.Processing
import ar.com.emmanuelmessulam.raindetector.processing.ProcessingApiJ
import ar.com.emmanuelmessulam.raindetector.processing.ProcessingApiO
import ar.com.emmanuelmessulam.raindetector.smn.SmnApi
import ar.com.emmanuelmessulam.raindetector.smn.SmnApiV1
import ar.com.emmanuelmessulam.raindetector.views.CitiesListView
import arrow.core.Either
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        CoroutineScope(Dispatchers.Main).launch {
            val smn: SmnApi = SmnApiV1()

            val process = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ProcessingApiO(smn)
            } else {
                ProcessingApiJ(smn)
            }

            loadList(smn, process)
            setTitle(process)
            loadAndNotify(process)
        }
    }

    private suspend fun loadList(smn: SmnApi, process: Processing) {
        val citiesListView: CitiesListView = view(R.id.citiesListView)

        val progressBar: ProgressBar = view(R.id.progressBar)

        val errorLoadingTextView: TextView = view(R.id.errorLoadingTextView)

        when(val cities = smn.getCities()) {
            is Either.Left -> {
                progressBar.visibility = GONE
                errorLoadingTextView.visibility = VISIBLE
                errorLoadingTextView.text = getString(R.string.error_loading, cities.value.message)
            }
            is Either.Right -> {
                citiesListView.initializeAdapter(
                    this@MainActivity,
                    cities.value
                ) { city, positions, _ ->
                    CityPersister.setCity(this@MainActivity, city)
                    citiesListView.setSelected(positions)
                    CoroutineScope(Dispatchers.Main).launch {
                        setTitle(process)
                        loadAndNotify(process)
                    }
                }

                val currentlySelectedCity = CityPersister.getCity(this)
                if(currentlySelectedCity != null) {
                    val currentlySelectedCityPosition = cities.value.indexOf(currentlySelectedCity)
                    citiesListView.setSelected(currentlySelectedCityPosition)
                }

                progressBar.visibility = GONE
            }
        }
    }

    private suspend fun setTitle(process: Processing) {
        val rainTextView: TextView = view(R.id.rainTextView)

        val city = CityPersister.getCity(this@MainActivity) ?: return

        val result = process.nextRain(city)

        rainTextView.text = TextManager.getText(this@MainActivity, result)
        rainTextView.visibility = VISIBLE
    }

    private suspend fun loadAndNotify(processing: Processing) {
        RefreshNotification.loadAndNotify(this@MainActivity, processing)
    }
}