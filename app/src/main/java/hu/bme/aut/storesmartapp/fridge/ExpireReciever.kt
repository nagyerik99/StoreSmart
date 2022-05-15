package hu.bme.aut.storesmartapp.fridge

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import hu.bme.aut.storesmartapp.R
import hu.bme.aut.storesmartapp.data.DateConverters
import hu.bme.aut.storesmartapp.data.StoreSmartDatabase
import java.util.*
import kotlin.concurrent.thread

class ExpireReciever : BroadcastReceiver() {
    private val intentCode = 10001

    private lateinit var database : StoreSmartDatabase

    override fun onReceive(context: Context?, intent: Intent?) {
        thread {
            database = StoreSmartDatabase.getDatabase(context!!)
            val actualTime = Calendar.getInstance()
            actualTime.add(Calendar.DAY_OF_MONTH,2) //alert 2 days before if some item will or expired already
            if(database.fridgeItemDao().hasExpiredItem(DateConverters().fromCalendar(actualTime)))
                SendNotification(context,intent)
        }
    }

    private fun SendNotification(context: Context?,intent: Intent?){
        val i = Intent(context,FridgeActivity::class.java)

        intent!!.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(context,intentCode,i,PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(context!!,context.getString(R.string.expirechannel))
            .setSmallIcon(R.drawable.alert_icon)
            .setContentTitle("StoreSmart")
            .setContentText("Some of the items, might have expire soon!")
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(1111,builder.build())
    }
}