package com.example.team11.depricated

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.example.team11.PersonalPreference
import com.example.team11.R
import kotlinx.android.synthetic.main.fragment_more.*


class MoreActivity : AppCompatActivity() {
    var aboutAppClicked = false
    var aboutAPIClicked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_more)

        aboutAppTitle.setOnClickListener {
            makeViewsDisappear()
            if (aboutAppClicked) {
                aboutAppClicked = false
            } else {
                aboutAppText.visibility = View.VISIBLE
                aboutAppTitle.background = resources.getDrawable(R.drawable.about_rectangle_clicked, null)
                aboutAppClicked = true;
            }
        }

        aboutAPITitle.setOnClickListener {
            makeViewsDisappear()
            if (aboutAPIClicked) {
                aboutAPIClicked = false
            }else{
                aboutAPIText.visibility = View.VISIBLE
                aboutAPITitle.background = resources.getDrawable(R.drawable.about_rectangle_clicked, null)
                aboutAPIClicked = true
            }
        }
    }

    /**
     * Nullstriller hele activiten (med tanke på viewene).
     */
    private fun makeViewsDisappear(){
        aboutAppText.visibility = View.GONE
        aboutAPIText.visibility = View.GONE

        aboutAppTitle.background = resources.getDrawable(R.drawable.about_rectangle, null)
        aboutAPITitle.background = resources.getDrawable(R.drawable.about_rectangle, null)


    }


}