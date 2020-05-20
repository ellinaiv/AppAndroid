package com.example.team11.ui.placesList

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.team11.database.entity.Place
import com.example.team11.database.entity.PersonalPreference
import com.example.team11.Repository.PlaceRepository
import com.example.team11.database.entity.WeatherForecastDb


class PlacesListFragmentViewModel(context: Context) : ViewModel() {
    var places: LiveData<List<Place>>? = null
    var forecasts: LiveData<List<WeatherForecastDb>>? = null
    lateinit var personalPreference: LiveData<List<PersonalPreference>>
    private var placeRepository: PlaceRepository? = null

    init {
        if(places == null){
            placeRepository = PlaceRepository.getInstance(context)
            places = placeRepository!!.getPlaces()
            personalPreference = placeRepository!!.getPersonalPreferences()
        }
    }
    class InstanceCreator(val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T  {
            return modelClass.getConstructor(Context::class.java).newInstance(context)
        }
    }

    fun changeCurrentPlace(place: Place){
        placeRepository?.changeCurrentPlace(place)
    }

    fun getForecasts(placesIn: List<Place>) = placeRepository!!.getNowForecastsList(placesIn)

    /**
     * Sjekker om et sted skal ha rød eller blaa boolge
     * @param place: Stedet man vil sjekke
     */
    fun redWave(place: Place): Boolean{
        if(personalPreference.value!![0].waterTempMid <= place.tempWater) return true
        return false
    }


}