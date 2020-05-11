package com.example.team11.ui.place

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.team11.PersonalPreference
import com.example.team11.Place
import com.example.team11.Repository.PlaceRepository
import com.example.team11.Transportation
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point

class PlaceActivityViewModel: ViewModel() {

    var place: MutableLiveData<Place>? = null
    private var placeRepository: PlaceRepository? = null
    private var personalPreference: MutableLiveData<PersonalPreference>? = null

    /**
     * Setter verdier
     */
    init {
        if(place == null){
            placeRepository = PlaceRepository.getInstance()
            place = placeRepository!!.getCurrentPlace()
            personalPreference = placeRepository!!.getPersonalPreferences()
        }
    }


    class InstanceCreator : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return modelClass.getConstructor().newInstance()
        }
    }

    /**
     * Funksjonen forteller videre til place repository at det har blitt gjort endringer på
     * hvilke steder som er favoirutter og ber den om å oppdatere det. Dette må gjøres hver gang
     * hjertetoggelknappen blir trykket på
     */
    fun updateFavoritePlaces(){
        placeRepository!!.updateFavoritePlaces()
    }

    /**
     * Endrer måten brukeren ønsker å komme seg til en strand i repository
     * @param way: måten brukeren ønsker å komme seg til stranden
     */
    fun changeWayOfTransportation(way: Transportation){
        placeRepository!!.changeWayOfTransportation(way)
    }

    /**
     * Gir featuren til en Place sin lokasjon (en feature er det som trengs for å vise noe
     * på kartet)
     * @param place: en strand
     * @return en Feature verdi basert på lokasjonen til place
     */
    fun getFeature(place: Place) = Feature.fromGeometry(Point.fromLngLat(place.lng, place.lat))!!

    /**
     * Sjekker om et sted skal ha rød eller blaa boolge
     * @param place: Stedet man vil sjekke
     */
    fun redWave(place: Place): Boolean{
        if(personalPreference!!.value!!.waterTempMid <= place.tempWater) return true
        return false
    }
}