package ru.nurdaulet.clockview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ru.nurdaulet.clockview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var batteryPercent: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }

    /*private fun parsePercent() {
        val percentText = binding.editTextNumberDecimal.text.toString()
        batteryPercent = try {
            percentText.trim().toInt()
        } catch (nfe: NumberFormatException) {
            0
        }
    }*/
}