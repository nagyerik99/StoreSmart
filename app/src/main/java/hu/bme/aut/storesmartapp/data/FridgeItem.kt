package hu.bme.aut.storesmartapp.data

import androidx.room.*

@Entity(tableName = "FridgeItem")
data class FridgeItem (
    @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) var id: Long? = null,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "category") var category: Category,
    @ColumnInfo(name = "numofitem") var numofItem: Int,
    @ColumnInfo(name = "uom") var unitOfMeasure: Measurement,
    @ColumnInfo(name = "best_before") var bestBefore: Long?,
    @ColumnInfo(name = "depraved") var depraved: Boolean
) {
    enum class Category {
        DAIRY, MEAT,VEGETABLE,FRUIT,FROZEN;
        companion object {
            @JvmStatic
            @TypeConverter
            fun getByOrdinal(ordinal: Int): Category? {
                var ret: Category? = null
                for (cat in values()) {
                    if (cat.ordinal == ordinal) {
                        ret = cat
                        break
                    }
                }
                return ret
            }

            @JvmStatic
            @TypeConverter
            fun toInt(category: Category): Int {
                return category.ordinal
            }
        }
    }
    enum class Measurement {
        KG, LBS, DB, L;

        companion object {
            @JvmStatic
            @TypeConverter
            fun getByOrdinal(ordinal: Int): Measurement? {
                var ret: Measurement? = null
                for (cat in values()) {
                    if (cat.ordinal == ordinal) {
                        ret = cat
                        break
                    }
                }
                return ret
            }

            @JvmStatic
            @TypeConverter
            fun toInt(measure: Measurement): Int {
                return measure.ordinal
            }
        }
    }
}