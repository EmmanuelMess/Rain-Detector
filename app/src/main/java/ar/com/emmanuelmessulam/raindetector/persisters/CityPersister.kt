package ar.com.emmanuelmessulam.raindetector.persisters

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import ar.com.emmanuelmessulam.raindetector.dataclasses.City

object CityPersister {
    private val PREFERENCE_KEY = "city preference"
    private val CURRENT_SELECTED_CITY = "current city"

    fun setCity(context: Context, city: City) {
        getSharedPreferences(context)
            .edit()
            .putCity(CURRENT_SELECTED_CITY, city)
            .apply()
    }

    fun getCity(context: Context): City? {
        return getSharedPreferences(context).getCity(CURRENT_SELECTED_CITY)
    }

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFERENCE_KEY, AppCompatActivity.MODE_PRIVATE)
    }

    private fun SharedPreferences.getCity(key: String): City? {
        val readableName = getString(getKeyReadable(key), null) ?: return null
        val id = getString(getKeId(key), null) ?: return null

        return City(readableName, id)
    }

    private fun SharedPreferences.Editor.putCity(key: String, city: City): SharedPreferences.Editor {
        putString(getKeyReadable(key), city.readableName)
        putString(getKeId(key), city.id)
        return this
    }

    private fun getKeyReadable(key: String) = key + "1"

    private fun getKeId(key: String) = key + "2"
}