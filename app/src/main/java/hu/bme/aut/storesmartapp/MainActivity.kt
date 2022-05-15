package hu.bme.aut.storesmartapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import hu.bme.aut.storesmartapp.databinding.ActivityMainBinding
import hu.bme.aut.storesmartapp.fridge.ExpireReciever
import hu.bme.aut.storesmartapp.fridge.FridgeActivity
import hu.bme.aut.storesmartapp.shop.ShopActivity
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var alarmManager :AlarmManager
    private lateinit var pendingIntent : PendingIntent
    private lateinit var calendar: Calendar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnFridge.setOnClickListener {
            val intent = Intent(this, FridgeActivity::class.java)
            startActivity(intent)
        }

        binding.btnShop.setOnClickListener {
            val intent = Intent(this, ShopActivity::class.java)
            startActivity(intent)
        }

        //Set notification time
        calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY,9)
        calendar.set(Calendar.MINUTE,0)
        calendar.set(Calendar.SECOND,0)

        setAlarm()

    }

    private fun setAlarm(){
        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ExpireReciever::class.java)

        pendingIntent = PendingIntent.getBroadcast(this,100,intent, PendingIntent.FLAG_UPDATE_CURRENT)

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )

        Toast.makeText(this,"Alarm set successfully", Toast.LENGTH_LONG).show()
    }
}