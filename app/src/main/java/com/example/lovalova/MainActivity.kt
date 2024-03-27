package com.example.lovalova


import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat


import android.Manifest
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.lovalova.databinding.ActivityMainBinding
import java.text.DecimalFormat


class MainActivity : AppCompatActivity(), SensorEventListener {


    private lateinit var binding: ActivityMainBinding
    private lateinit var sensorManager: SensorManager
    private var mLight: Sensor? = null


    private var running = false
    private var totalSteps = 0f
    private var previousTotalSteps = 0f

    private val stepOne = 0.726

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }
        loadData()
        resetSteps(this)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mLight = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_DENIED
        ) {
            //ask for permission
            requestPermissions(arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), 102);
        }
    }

    override fun onResume() {
        super.onResume()
        running = true

        mLight?.also {
            sensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_FASTEST)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {

            if (running) {
                totalSteps = event.values[0]
                val currentSteps = totalSteps.toInt() - previousTotalSteps.toInt()
                with(binding) {
                    tvStepsTaken.text = currentSteps.toString()
                    progress.apply {
                        setProgressWithAnimation(currentSteps.toFloat())
                        val formatNumber = DecimalFormat("#.###")
                        val km = (currentSteps * stepOne) / 1000
                        tvStepsInKm.text = formatNumber.format(km).toString()
                    }
                }

            }
        }
    }

    private fun resetSteps(context: Context) {
        with(binding) {
            tvStepsTaken.setOnClickListener {
                Toast.makeText(
                    context,
                    "Зажми, чтобы запустить заново",
                    Toast.LENGTH_SHORT
                ).show()
            }
            tvStepsTaken.setOnLongClickListener {
                previousTotalSteps = totalSteps
                tvStepsTaken.text = 0.toString()
                tvStepsInKm.text = 0.toString()
                progress.setProgressWithAnimation(0f)
                saveData()
                true
            }
        }

    }


    private fun saveData() {
        //save data
        val sharedPreferences = getSharedPreferences("myPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putFloat("key1", previousTotalSteps)
        editor.apply()
    }

    private fun loadData() {
        // retrieves data
        val sharedPreferences = getSharedPreferences("myPreferences", Context.MODE_PRIVATE)
        val savedNumber = sharedPreferences.getFloat("key1", 0f)

        Log.d("MainActivity", "$savedNumber")

        previousTotalSteps = savedNumber
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}

//if(ContextCompat.checkSelfPermission(this,
//Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED){
//    //ask for permission
//    requestPermissions(arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), 102);
//}

