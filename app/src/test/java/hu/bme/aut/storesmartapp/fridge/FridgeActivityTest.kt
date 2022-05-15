package hu.bme.aut.storesmartapp.fridge

import hu.bme.aut.storesmartapp.data.FridgeItem
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class FridgeActivityTest{
    @Test
    fun testAdd(){
        val activity = FridgeActivity()
        assertEquals(0,activity.adapter.itemCount)
        activity.adapter.addItem(FridgeItem(null,"test",
            FridgeItem.Category.MEAT,1,FridgeItem.Measurement.DB,null,true))
        assertEquals(1,activity.adapter.itemCount)
    }
}