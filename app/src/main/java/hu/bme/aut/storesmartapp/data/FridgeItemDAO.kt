package hu.bme.aut.storesmartapp.data

import androidx.room.*

@Dao
interface FridgeItemDAO {
    @Query("SELECT * FROM FridgeItem")
    fun getAllItem():List<FridgeItem>

    @Query("SELECT * FROM FridgeItem WHERE depraved = 1")
    fun getAllExpired():List<FridgeItem>

    @Insert
    fun insertItem(fridgeItem: FridgeItem): Long

    @Insert
    fun insertAll(list : List<FridgeItem>)

    @Update
    fun updateItem(fridgeItem: FridgeItem)

    @Delete
    fun deleteItem(fridgeItem: FridgeItem)

    @Query("DElETE FROM FridgeItem")
    fun deleteAll()

    @Query("DElETE FROM FridgeItem WHERE depraved = 1")
    fun deleteExpired()

    @Query("SELECT EXISTS (SELECT 1 FROM FridgeItem WHERE depraved = 1 OR best_before < :time)")
    fun hasExpiredItem(time:Long?):Boolean

    @Query("SELECT EXISTS (SELECT 1 FROM FridgeItem Where name LIKE :shopName AND category = :shopCategory)")
    fun hasSimilar(shopName:String, shopCategory : FridgeItem.Category) : Boolean

    @Query("SELECT * FROM FridgeItem ORDER BY name ASC")
    fun getAllOrderByNameASC():List<FridgeItem>

    @Query("SELECT * FROM FridgeItem ORDER BY name DESC")
    fun getAllOrderByNameDESC():List<FridgeItem>

    @Query("SELECT * FROM FridgeItem ORDER BY category ASC")
    fun getAllOrderByTypeASC():List<FridgeItem>

    @Query("SELECT * FROM FridgeItem ORDER BY category DESC")
    fun getAllOrderByTypeDESC():List<FridgeItem>

}