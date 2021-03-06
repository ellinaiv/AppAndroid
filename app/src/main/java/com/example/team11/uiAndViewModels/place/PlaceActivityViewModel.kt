package com.example.team11.uiAndViewModels.place

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.team11.Color
import com.example.team11.database.entity.Place
import com.example.team11.database.entity.PersonalPreference
import com.example.team11.repository.PlaceRepository
import com.example.team11.Transportation
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point

class PlaceActivityViewModel(context: Context): ViewModel() {

    var place: MutableLiveData<Place>? = null
    lateinit var isFavorite: LiveData<Boolean>
    lateinit var placeRepository: PlaceRepository
    lateinit var personalPreference: LiveData<List<PersonalPreference>>

    /**
     * Setter verdier
     */
    init {
        if(place == null){
            placeRepository = PlaceRepository.getInstance(context)
            place = placeRepository.getCurrentPlace()
            isFavorite = placeRepository.isPlaceFavorite(place!!.value!!)
            personalPreference = placeRepository.getPersonalPreferences()
        }
    }

    class InstanceCreator(val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T  {
            return modelClass.getConstructor(Context::class.java).newInstance(context)
        }
    }

    /**
     * Markerer stedet som favoritt i databasen
     */
    fun addFavoritePlace() = placeRepository.addFavoritePlace(place!!.value!!)

    /**
     * Markerer stedet som ikke-favoritt i databasen
     */
    fun removeFavoritePlace() = placeRepository.removeFavoritePlace(place!!.value!!)

    /**
     * Henter forecast for de neste timene fra database
     * @return Livedata<List<HourForecast>>
     */
    fun getHourForecast() = placeRepository.getForecast(place!!.value!!, true)
    /**
     * Henter forecast for de neste dagene fra database
     * @return Livedata<List<HourForecast>>
     */
    fun getDayForecast() = placeRepository.getForecast(place!!.value!!, false)


    /**
     * Endrer måten brukeren ønsker å komme seg til en strand i repository
     * @param way: måten brukeren ønsker å komme seg til stranden
     */
    fun changeWayOfTransportation(way: Transportation){
        placeRepository.changeWayOfTransportation(way)
    }

    /**
     * Gir featuren til en Place sin lokasjon (en feature er det som trengs for å vise noe
     * på kartet)
     * @param place: en strand
     * @return en Feature verdi basert på lokasjonen til place
     */
    fun getFeature(place: Place) = Feature.fromGeometry(Point.fromLngLat(place.lng, place.lat))!!

    /**
     * Sjekker om et sted skal ha graa, rood eller blaa boolge
     * @param place: Stedet man vil sjekke
     * @return fargen boolgen skal veare
     */
    fun colorWave(place: Place): Color {
        if(place.tempWater == Int.MAX_VALUE) return Color.GRAY
        val waterTempPp = personalPreference.value?.get(0)?.waterTempMid ?: return Color.GRAY
        if(waterTempPp <= place.tempWater) return Color.RED
        return Color.BLUE
    }
}