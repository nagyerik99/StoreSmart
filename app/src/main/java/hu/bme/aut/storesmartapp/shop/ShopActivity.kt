package hu.bme.aut.storesmartapp.shop

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import hu.bme.aut.storesmartapp.R
import hu.bme.aut.storesmartapp.data.*
import hu.bme.aut.storesmartapp.databinding.ActivityShopBinding
import hu.bme.aut.storesmartapp.fridge.AlertBlurDialogFragment
import java.util.*
import kotlin.concurrent.thread

class ShopActivity : AppCompatActivity(),
    ShopAdapter.ShopItemClickListener,
    AddShopItemFragment.ShopItemCreatedListener,
    AlertBlurDialogFragment.AlertDialogOkInterface {
    private lateinit var binding: ActivityShopBinding
    private lateinit var database: StoreSmartDatabase
    private lateinit var adapter: ShopAdapter
    private var nameOrder = OrderType.ASC
    private var typeOrder = OrderType.ASC


    private lateinit var  fragmentState: FragmentState
    private var editedItemPos : Int = -1
    private var askedAlready :String = "askedAlready"
    private var checkFridge :String = "checkFridge"


    enum class OrderType{
        ASC,DESC
    }

    enum class FragmentState{
        Edit,Add;
    }

    fun updateFailed(){
        Snackbar.make(binding.root,"\t\t\tCan't modify bought items!",Snackbar.LENGTH_LONG).show()
        adapter.notifyItemChanged(editedItemPos)
    }

    private var swipeGesture = object : SwipeGesture() {
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val item = adapter.getItem(viewHolder.adapterPosition)
            when(direction){
                ItemTouchHelper.LEFT ->{
                   thread{
                       database.shopItemDao().delete(item)
                        runOnUiThread{
                            adapter.deleteItem(viewHolder.adapterPosition)
                        }
                    }
                }
                ItemTouchHelper.RIGHT ->{
                    editedItemPos = viewHolder.adapterPosition
                    fragmentState = FragmentState.Edit
                    val item = adapter.getItem(editedItemPos)
                    if(item.isBought){
                        updateFailed()
                        return
                    }
                    val fragment = AddShopItemFragment(FragmentState.Edit,item)
                    fragment.show(supportFragmentManager,AddShopItemFragment.TAG)
                }
            }
        }
    }

    private val touchHelper = ItemTouchHelper(swipeGesture)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityShopBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = StoreSmartDatabase.getDatabase(applicationContext)

        touchHelper.attachToRecyclerView(binding.rvShop)

        binding.btnAdd.setOnClickListener {
            fragmentState = FragmentState.Add
            AddShopItemFragment(fragmentState)
                .show(supportFragmentManager,AddShopItemFragment.TAG)
        }

        binding.btnDeleteAll.setOnClickListener{
            AlertBlurDialogFragment(AlertBlurDialogFragment.AlertType.DeleteAll)
                .show(supportFragmentManager,AlertBlurDialogFragment.TAG)
        }

        binding.btnAddToFridge.setOnClickListener{
            AlertBlurDialogFragment(AlertBlurDialogFragment.AlertType.AddToFridge)
                .show(supportFragmentManager,AlertBlurDialogFragment.TAG)
        }

        setSupportActionBar(binding.toolbar)

        initRecyclerView()

        val sharedPreferences = getPreferences(MODE_PRIVATE)

        if(!sharedPreferences.getBoolean(askedAlready,false)){
            val editor = sharedPreferences.edit()
            editor.putBoolean(askedAlready,true)

            val alertDialog = AlertDialog.Builder(this@ShopActivity).create()
            alertDialog.setTitle("CheckFridge Permission")
            alertDialog.setMessage("Do You want to check your fridge when adding new Item ?")
            alertDialog.setButton(
                AlertDialog.BUTTON_NEGATIVE, "No"
            ) { _, _ ->
                editor.putBoolean(checkFridge,false)
                editor.apply()
            }
            alertDialog.setButton(
                AlertDialog.BUTTON_POSITIVE, "Yes"
            ) { _,_->
                editor.putBoolean(checkFridge,true)
                editor.apply()
            }
            alertDialog.setCanceledOnTouchOutside(false)
            alertDialog.setCancelable(false)
            alertDialog.show()
        }
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
            val orderedItems : List<ShoppingItem> = if(orderType == OrderType.ASC)
                database.shopItemDao().getAllOrderByNameASC()
            else
                database.shopItemDao().getAllOrderByNameDESC()

            runOnUiThread{
                adapter.update(orderedItems)
            }
        }
    }

    private fun orderByType(orderType: OrderType){
        thread {
            val orderedItems : List<ShoppingItem> = if(orderType == OrderType.ASC)
                database.shopItemDao().getAllOrderByTypeASC()
            else
                database.shopItemDao().getAllOrderByTypeDESC()

            runOnUiThread{
                adapter.update(orderedItems)
            }
        }
    }

    private fun initRecyclerView() {
        adapter = ShopAdapter(this)
        binding.rvShop.layoutManager = LinearLayoutManager(this)
        binding.rvShop.adapter = adapter
        loadItemsInBackground()
    }

    private fun loadItemsInBackground() {
        thread {
            val items = database.shopItemDao().getAll()
            runOnUiThread {
                adapter.update(items)
            }
        }
    }

    override fun onItemClicked(item: ShoppingItem) {
        item.isBought = !item.isBought

        thread {
            database.shopItemDao().update(item)
            runOnUiThread{
                adapter.updateItem(item)
            }
        }
    }

    private fun alertItemAlreadyInFridge(item: ShoppingItem){

        val alertDialog = AlertDialog.Builder(this@ShopActivity).create()
        alertDialog.setTitle("Multiple Similar Item")
        alertDialog.setMessage("Similar item is already in fridge, do you wish to buy anyway?")
      /*  alertDialog.setButton(
            AlertDialog.BUTTON_NEGATIVE, "No"
        ) { _, _ ->

        }*/
        alertDialog.setButton(
            AlertDialog.BUTTON_POSITIVE, "Yes"
        ) { _,_->
            insertItem(item)
        }
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    override fun onShopItemItemCreatedOrEdited(newItem: ShoppingItem) {
        when(fragmentState){
            FragmentState.Add ->{
                thread {
                    val sharedPreferences = getPreferences(MODE_PRIVATE)

                    if(sharedPreferences.getBoolean(checkFridge,true)){
                        val likeName = '%'+newItem.name+'%'
                        val hasInFridge = database.fridgeItemDao().hasSimilar(likeName, newItem.category)
                        if(hasInFridge) {
                            runOnUiThread{
                                alertItemAlreadyInFridge(newItem)
                            }
                        }else{
                            insertItem(newItem)
                        }
                    }else{
                            insertItem(newItem)
                    }
                }
            }

            FragmentState.Edit ->{
                thread {
                    database.shopItemDao().update(newItem)
                    runOnUiThread {
                        adapter.updateItem(newItem)
                    }
                }
            }
        }
    }

    private fun insertItem(newItem:ShoppingItem){
        thread{
            val insertId = database.shopItemDao().insert(newItem)
            newItem.id = insertId
            runOnUiThread {
                adapter.addItem(newItem)
            }
        }
    }

    override fun onShopItemEditCanceled() {
        adapter.notifyItemChanged(editedItemPos)
    }

    override fun onOkClicked(alert: AlertBlurDialogFragment.AlertType) {
        when(alert){
            AlertBlurDialogFragment.AlertType.DeleteAll->{
                thread{
                    database.shopItemDao().deleteAll()
                    runOnUiThread{
                        adapter.deleteAll()
                    }
                }
            }

            AlertBlurDialogFragment.AlertType.AddToFridge->{
                thread{
                    //get all bought item
                    val boughtItems = database.shopItemDao().getAllBought()
                    //remove all bought item
                    database.shopItemDao().deleteAllBought()
                    //add to fridgeTable
                    thread {
                        importToFridge(boughtItems)
                    }

                    runOnUiThread{
                        //TODO waiting animation just like fridgeActivity
                        adapter.deleteBought(boughtItems)
                    }
                }
            }
            else -> return
        }
    }

    //TODO for now constant values for the product Types
    private fun calcBestBefore(category: FridgeItem.Category):Long?{
        val calendar = Calendar.getInstance()
        when(category){
            FridgeItem.Category.FRUIT -> calendar.add(Calendar.DAY_OF_MONTH,7)
            FridgeItem.Category.VEGETABLE -> calendar.add(Calendar.DAY_OF_MONTH,5)
            FridgeItem.Category.MEAT -> calendar.add(Calendar.DAY_OF_MONTH,3)
            FridgeItem.Category.DAIRY -> calendar.add(Calendar.DAY_OF_MONTH,7)
            FridgeItem.Category.FROZEN -> calendar.add(Calendar.MONTH,4)
        }

        return DateConverters().fromCalendar(calendar)
    }

    private fun importToFridge(boughtItems:List<ShoppingItem>){
        val list = mutableListOf<FridgeItem>()
        for(item in boughtItems){
            val fridgeItem = FridgeItem(
                null,
                name = item.name,
                category = item.category,
                numofItem = item.numofItem,
                unitOfMeasure = item.unitOfMeasure,
                calcBestBefore(item.category),
                false
            )
            list.add(fridgeItem)
        }
            database.fridgeItemDao().insertAll(list)
    }
}