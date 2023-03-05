package ru.nurdaulet.clockview

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import ru.nurdaulet.clockview.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val handler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateTime()

        binding.clockView.setClockRadius(170f)
    }

    private fun updateTime() {
        handler.post(object : Runnable {
            override fun run() {
                handler.postDelayed(this, 1000)

                val calendar = Calendar.getInstance()
                binding.clockView.setClockTime(
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    calendar.get(Calendar.SECOND)
                )
            }
        })
    }
}