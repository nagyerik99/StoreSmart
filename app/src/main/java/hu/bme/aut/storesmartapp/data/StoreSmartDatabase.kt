package hu.bme.aut.storesmartapp.data

import android.content.Context
import androidx.room.*
import androidx.room.Room.databaseBuilder
import java.util.*

@Database(entities = [FridgeItem::class,ShoppingItem::class], version = 2)
@TypeConverters(value = [FridgeItem.Category::class,DateConverters::class,FridgeItem.Measurement::class])
abstract class StoreSmartDatabase : RoomDatabase(){
    abstract fun fridgeItemDao(): FridgeItemDAO
    abstract fun shopItemDao():ShoppingItemDao

    companion object {
        fun getDatabase(applicationContext: Context): StoreSmartDatabase {
            return databaseBuilder(
                applicationContext,
                StoreSmartDatabase::class.java,
                "store-smart"
            ).fallbackToDestructiveMigration()
                .build()
        }
    }
}


class DateConverters {
    @TypeConverter
    fun toCalendar(l: Long?): Calendar {
        val c: Calendar = Calendar.getInstance()
        c.timeInMillis = l?:c.timeInMillis
        return c
    }

    @TypeConverter
    fun fromCalendar(c: Calendar?): Long? {
        return c?.time?.time
    }
}