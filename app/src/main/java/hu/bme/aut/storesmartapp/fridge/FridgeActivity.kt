package hu.bme.aut.storesmartapp.fridge

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.VisibleForTesting
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import hu.bme.aut.storesmartapp.R
import hu.bme.aut.storesmartapp.data.FridgeItem
import hu.bme.aut.storesmartapp.data.ShoppingItem
import hu.bme.aut.storesmartapp.data.StoreSmartDatabase
import hu.bme.aut.storesmartapp.data.SwipeGesture
import hu.bme.aut.storesmartapp.databinding.ActivityFridgeBinding
import kotlin.concurrent.thread


class FridgeActivity : AppCompatActivity(), FridgeAdapter.FridgeItemClickListener,
    AddFridgeItemFragment.NewFridgeItemDialogListener,
    AlertBlurDialogFragment.AlertDialogOkInterface{
    private lateinit var binding: ActivityFridgeBinding

    private lateinit var database: StoreSmartDatabase
    @VisibleForTesting()
    internal lateinit var adapter: FridgeAdapter

    private lateinit var  fragmentState: FragmentState
    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent
    private var nameOrder = OrderType.ASC
    private var typeOrder = OrderType.ASC

    private var editedItemPos : Int = -1

    enum class FragmentState{
        Edit,Add;
    }

    enum class OrderType{
        ASC,DESC
    }

    fun updateFailed(){
        Snackbar.make(binding.root,"\t\t\tCan't modify expired items!", Snackbar.LENGTH_LONG).show()
        adapter.notifyItemChanged(editedItemPos)
    }


    private var swipeGesture = object : SwipeGesture() {
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val item = adapter.getItem(viewHolder.adapterPosition)
            when(direction){
                ItemTouchHelper.LEFT ->{
                    thread {
                        database.fridgeItemDao().deleteItem(item)
                        runOnUiThread{
                            adapter.deleteItem(viewHolder.adapterPosition)
                        }
                    }
                }
                ItemTouchHelper.RIGHT ->{
                    fragmentState = FragmentState.Edit
                    editedItemPos = viewHolder.adapterPosition
                    val item = adapter.getItem(editedItemPos)
                    if(item.depraved){
                        updateFailed()
                        return
                    }
                    val fragment = AddFridgeItemFragment(FragmentState.Edit,item)
                    fragment.show(supportFragmentManager,AddFridgeItemFragment.TAG)
                }
            }
        }
    }

    private val touchHelper = ItemTouchHelper(swipeGesture)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFridgeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = StoreSmartDatabase.getDatabase(applicationContext)

        binding.btnAdd.setOnClickListener {
            fragmentState = FragmentState.Add
            AddFridgeItemFragment(fragmentState).show(supportFragmentManager,AddFridgeItemFragment.TAG)
        }

        binding.btnDeleteAll.setOnClickListener{
            AlertBlurDialogFragment(AlertBlurDialogFragment.AlertType.DeleteAll)
                .show(supportFragmentManager,AlertBlurDialogFragment.TAG)
        }

        binding.btnAddToCart.setOnClickListener{
            AlertBlurDialogFragment(AlertBlurDialogFragment.AlertType.AddToCart)
                .show(supportFragmentManager,AlertBlurDialogFragment.TAG)
        }

        setSupportActionBar(binding.toolbar)

        createNotificationChannel()
        initRecyclerView()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
       menuInflater.inflate(R.menu.menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.orderByName ->{
                orderByName(nameOrder)
                nameOrder = if(nameOrder == OrderType.ASC)
                    OrderType.DESC
                else
                    OrderType.ASC
                return true
            }
            R.id.orderByType ->{
                orderByType(typeOrder)

                typeOrder = if(nameOrder == OrderType.ASC)
                    OrderType.DESC
                else
                    OrderType.ASC
                return true
            }
            else ->{
                return super.onOptionsItemSelected(item)
            }
        }
    }

    private fun orderByName(orderType: OrderType){
        thread {
            val orderedItems : List<FridgeItem> = if(orderType == OrderType.ASC)
                database.fridgeItemDao().getAllOrderByNameASC()
            else
                database.fridgeItemDao().getAllOrderByNameDESC()

            runOnUiThread{
                adapter.update(orderedItems)
            }
        }
    }

    private fun orderByType(orderType: OrderType){
        thread {
            val orderedItems : List<FridgeItem> = if(orderType == OrderType.ASC)
                database.fridgeItemDao().getAllOrderByTypeASC()
            else
                database.fridgeItemDao().getAllOrderByTypeDESC()

            runOnUiThread{
                adapter.update(orderedItems)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        touchHelper.attachToRecyclerView(binding.rvFridge)
    }

    private fun createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name :CharSequence = getString(R.string.notification_channel_title)
            val description = getString(R.string.notification_channel_desc)

            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(getString(R.string.expirechannel),name,importance)
            channel.description = description
            val notificationManager = getSystemService(
                NotificationManager::class.java
            )

            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun initRecyclerView() {
        adapter = FridgeAdapter(this)
        binding.rvFridge.layoutManager = LinearLayoutManager(this)
        binding.rvFridge.adapter = adapter
        loadItemsInBackground()
    }

    private fun loadItemsInBackground() {
        thread {
            val items = database.fridgeItemDao().getAllItem()
            runOnUiThread {
                adapter.update(items)
            }
        }
    }



    override fun onItemChanged(item: FridgeItem) {
        thread {
            database.fridgeItemDao().updateItem(item)
        }

    }

    override fun onFridgeItemCreatedOrEdited(newItem: FridgeItem) {
        when(fragmentState){
            FragmentState.Add->{
                thread {
                    val insertId = database.fridgeItemDao().insertItem(newItem)
                    newItem.id = insertId
                    runOnUiThread {
                        adapter.addItem(newItem)
                    }
                }
            }
            FragmentState.Edit->{
                thread {
                    database.fridgeItemDao().updateItem(newItem)
                    runOnUiThread {
                        adapter.updateItem(editedItemPos,newItem)
                    }
                }
            }
        }
    }

    override fun onFridgeItemEditCanceled() {
        adapter.notifyItemChanged(editedItemPos)
    }

    //In case we want to cancel alarm implementation
    private fun cancelAlarm(){
            alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            val intent = Intent(this,ExpireReciever::class.java)
            pendingIntent = PendingIntent.getBroadcast(this,100,intent, PendingIntent.FLAG_UPDATE_CURRENT)

            alarmManager.cancel(pendingIntent)
    }

    override fun onOkClicked(alert: AlertBlurDialogFragment.AlertType) {
        when(alert){
            AlertBlurDialogFragment.AlertType.DeleteAll ->{
                thread{
                    database.fridgeItemDao().deleteAll()
                    runOnUiThread{
                        adapter.deleteAllItem()
                    }
                }
            }

            AlertBlurDialogFragment.AlertType.AddToCart->{
                thread{
                    //delete all previous shopItem
                    database.shopItemDao().deleteAll()
                    //get all expired item
                    val expiredItems = database.fridgeItemDao().getAllExpired()
                    //remove all expired item from fridgeTable
                    database.fridgeItemDao().deleteExpired()
                    //add to shoppingTable

                    thread {
                        insertShoppingList(expiredItems)
                    }

                    runOnUiThread{
                        //TODO in case more items we should use a waiting animation
                        adapter.deleteAllExpired(expiredItems)
                    }
                }
            }
            else -> return
        }

    }

    private fun insertShoppingList(expiredItems:List<FridgeItem>){
        val list  = mutableListOf<ShoppingItem>()
        for(item in expiredItems){
            val shopItem = ShoppingItem(
                null,
                name = item.name,
                category = item.category,
                numofItem = item.numofItem,
                unitOfMeasure = item.unitOfMeasure,
                estimatedPrice = 0,
                false
            )
            list.add(shopItem)
        }
            database.shopItemDao().insertAll(list)
    }
}