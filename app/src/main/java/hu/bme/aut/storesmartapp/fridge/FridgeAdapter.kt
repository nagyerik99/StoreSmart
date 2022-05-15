package hu.bme.aut.storesmartapp.fridge

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import hu.bme.aut.storesmartapp.R
import hu.bme.aut.storesmartapp.data.DateConverters
import hu.bme.aut.storesmartapp.data.FridgeItem
import hu.bme.aut.storesmartapp.databinding.FridgeItemListBinding
import java.text.SimpleDateFormat
import java.util.*

class FridgeAdapter(private val listener: FridgeItemClickListener) :
RecyclerView.Adapter<FridgeAdapter.FridgeViewHolder>() {

    private val items = mutableListOf<FridgeItem>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = FridgeViewHolder(
        FridgeItemListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: FridgeViewHolder, position: Int) {
        if(position != RecyclerView.NO_POSITION){

            val fridgeItem = items[position]
            val formatter = SimpleDateFormat("yyyy. MM. dd")
            val expired = isExpired(fridgeItem)
            holder.binding.ivIcon.setImageResource(getImageResource(fridgeItem.category))
            holder.binding.tvName.text = fridgeItem.name
            holder.binding.tvCategory.text = fridgeItem.category.name
            holder.binding.tvBestBefore.text =
                "BestBefore: "+formatter.format(DateConverters().toCalendar(fridgeItem.bestBefore).time)
            holder.binding.numofItem.text = fridgeItem.numofItem.toString() +" "+fridgeItem.unitOfMeasure.name
            if(expired){
                holder.binding.tvExpiredText.setText(R.string.expired)
                holder.binding.tvExpiredText.setBackgroundResource(R.color.expired_color)

                if(!fridgeItem.depraved){
                    fridgeItem.depraved = expired
                    listener.onItemChanged(fridgeItem)
                }
            }else{
                holder.binding.tvExpiredText.text = null
                holder.binding.tvExpiredText.background = null

            }
        }
    }

    @DrawableRes
    private fun getImageResource(category: FridgeItem.Category): Int {
        return when (category) {
            FridgeItem.Category.DAIRY -> R.drawable.dairy_type
            FridgeItem.Category.MEAT -> R.drawable.meat_type
            FridgeItem.Category.FRUIT -> R.drawable.fruit_type
            FridgeItem.Category.VEGETABLE -> R.drawable.vegetable_type
            FridgeItem.Category.FROZEN -> R.drawable.frozen_type
        }
    }

    private fun isExpired(fridgeItem: FridgeItem):Boolean{
        val dateNow = Calendar.getInstance()
        //dateNow.set(Calendar.DAY_OF_MONTH,dateNow.get(Calendar.DAY_OF_MONTH)-1)
        val calendarItem = DateConverters().toCalendar(fridgeItem.bestBefore)
        return calendarItem.before(dateNow)
    }

    override fun getItemCount(): Int = items.size

    fun addItem(item: FridgeItem) {
        items.add(item)

        notifyItemInserted(items.size - 1)
    }

    fun deleteItem(i:Int){
        items.removeAt(i)
        notifyItemRemoved(i)
    }

    fun deleteAllItem(){
        val size = items.size
        items.clear()
        notifyItemRangeRemoved(0,size)
    }

    fun deleteAllExpired(fridgeItems: List<FridgeItem>){
        for (item: FridgeItem in fridgeItems){
            val index =items.indexOf(item)
            deleteItem(index)
        }
    }

    fun getItem(i:Int) = items[i]

    fun update(fridgeItems: List<FridgeItem>) {
        val previousSize = items.size

        items.clear()

        items.addAll(fridgeItems)
        val newSize = items.size
        notifyItemRangeRemoved(0,previousSize)

        notifyItemRangeInserted(0, newSize)
    }

    fun updateItem(i: Int,fridgeItem:FridgeItem){
        items[i] = fridgeItem
        notifyItemChanged(i)
    }

    interface FridgeItemClickListener {
        fun onItemChanged(item: FridgeItem)
    }

    inner class FridgeViewHolder(val binding: FridgeItemListBinding) :RecyclerView.ViewHolder(binding.root)
}