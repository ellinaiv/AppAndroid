package com.example.team11.ui.placesList

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.team11.Place
import com.example.team11.R
import com.example.team11.ui.filter.FilterActivity
import kotlinx.android.synthetic.main.fragment_places_list.*
import kotlinx.android.synthetic.main.fragment_places_list.filterButton
import kotlinx.android.synthetic.main.fragment_places_list.searchText

class PlacesListFragment : Fragment() {

    private lateinit var placesListViewModel: PlacesListFragmentViewModel
    private lateinit var filterPlaces: List<Place>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        placesListViewModel =
            ViewModelProvider(this).get(PlacesListFragmentViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_places_list, container, false)
        val layoutManager = LinearLayoutManager(context)

        placesListViewModel.places!!.observe(viewLifecycleOwner, Observer { places ->
            recycler_viewPlaces.layoutManager = layoutManager
            recycler_viewPlaces.adapter =
                ListAdapter(
                    places,
                    context!!,
                    placesListViewModel,
                    false
                )
            searchText.doOnTextChanged { text, _, _, _ ->
                search(text.toString(), places)
            }
            parent_layout.setOnClickListener {
                hideKeyboard()
            }
            recycler_viewPlaces.setOnClickListener{
                hideKeyboard()
            }
        })

        val filterButton = root.findViewById<ImageButton>(R.id.filterButton)
        filterButton.setOnClickListener {
            startActivity(Intent(this.context!!, FilterActivity::class.java))
        }
        return root
    }
    /**
     * Søkefunksjonen filtrerer places etter navn og oppdaterer listen som vises på skjermen
     * @param name: en input-streng som skal brukes for å filtrere places
     * @param places: en liste med badesteder som skal filtreres
     */
    private fun search(name: String, places: List<Place>){
        filterPlaces = places.filter{ it.name.contains(name.toString(), ignoreCase = true)}
        recycler_viewPlaces.adapter =
            ListAdapter(
                filterPlaces,
                context!!,
                placesListViewModel,
                false
            )
    }

    fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

}
