package com.vardemin.verticalpagerindicatorexample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnMove.setOnClickListener {
            pagerIndicator.moveTo(editPage.text.toString().toInt())
        }
        pagerIndicator.numPages = 20
        pagerIndicator.indicatorClickListener = {
            pagerIndicator.moveTo(it)
        }
        pagerIndicator.indicatorAnimationListener = {
            Toast.makeText(this, "Indicator $it animated", Toast.LENGTH_SHORT).show()
        }


    }
}
