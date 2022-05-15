package hu.bme.aut.storesmartapp.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shoppingitem")
data class ShoppingItem(
    @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) var id: Long? = null,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "category") var category: FridgeItem.Category,
    @ColumnInfo(name = "numofitem") var numofItem: Int,
    @ColumnInfo(name = "uom") var unitOfMeasure: FridgeItem.Measurement,
    @ColumnInfo(name = "estimated_price") var estimatedPrice: Int,
    @ColumnInfo(name = "is_bought") var isBought: Boolean,
)