package hu.bme.aut.storesmartapp.data

import androidx.room.*

@Dao
interface ShoppingItemDao {
    @Query("SELECT * FROM shoppingItem")
    fun getAll(): List<ShoppingItem>

    @Query("SELECT * FROM shoppingItem WHERE is_bought=1")
    fun getAllBought(): List<ShoppingItem>

    @Insert
    fun insert(shoppingItems: ShoppingItem): Long

    @Insert
    fun insertAll(list: List<ShoppingItem>)

    @Update
    fun update(shoppingItem: ShoppingItem)

    @Delete
    fun delete(shoppingItem: ShoppingItem)

    @Query("DElETE FROM shoppingItem")
    fun deleteAll()

    @Query("DElETE FROM shoppingItem WHERE is_bought = 1")
    fun deleteAllBought()

    @Query("SELECT * FROM shoppingItem ORDER BY name ASC")
    fun getAllOrderByNameASC():List<ShoppingItem>

    @Query("SELECT * FROM shoppingItem ORDER BY name DESC")
    fun getAllOrderByNameDESC():List<ShoppingItem>

    @Query("SELECT * FROM shoppingItem ORDER BY category ASC")
    fun getAllOrderByTypeASC():List<ShoppingItem>

    @Query("SELECT * FROM shoppingItem ORDER BY category DESC")
    fun getAllOrderByTypeDESC():List<ShoppingItem>
}