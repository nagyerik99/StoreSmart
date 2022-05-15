package hu.bme.aut.storesmartapp.shop

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import hu.bme.aut.storesmartapp.R
import hu.bme.aut.storesmartapp.data.FridgeItem
import hu.bme.aut.storesmartapp.data.ShoppingItem
import hu.bme.aut.storesmartapp.databinding.ShopItemListBinding

class ShopAdapter(private val listener: ShopItemClickListener) :
    RecyclerView.Adapter<ShopAdapter.ShopViewHolder>() {

    private val items = mutableListOf<ShoppingItem>()

    fun getItem(i:Int) = items[i]

    fun addItem(item: ShoppingItem) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    fun deleteItem(i:Int){
        items.removeAt(i)
        notifyItemRemoved(i)
    }

    fun deleteAll(){
        val size = items.size
        items.clear()
        notifyItemRangeRemoved(0,size)
    }

    fun deleteBought(shoppingItems: List<ShoppingItem>){
        for (item: ShoppingItem in shoppingItems){
            val index =items.indexOf(item)
            deleteItem(index)
        }
    }

    fun update(shoppingItems: List<ShoppingItem>) {
        val previousSize = items.size

        items.clear()

        items.addAll(shoppingItems)
        val newSize = items.size
        notifyItemRangeRemoved(0,previousSize)

        notifyItemRangeInserted(0, newSize)
    }

    fun updateItem(shoppingItem: ShoppingItem){
        val i = items.indexOfFirst {
            x->x.id == shoppingItem.id
        }
        items[i] = shoppingItem
        notifyItemChanged(i)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ShopViewHolder(
        ShopItemListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ShopViewHolder, position: Int) {
        if(position != RecyclerView.NO_POSITION){
            val shopItem = items[position]
            holder.binding.rlItem.setOnClickListener{
                listener.onItemClicked(shopItem)
            }
            holder.binding.ivIcon.setImageResource(getImageResource(shopItem.category))
            holder.binding.tvShopItemName.text = shopItem.name
            holder.binding.tvCategory.text = shopItem.category.name
            holder.binding.tvnumofItem.text = shopItem.numofItem.toString() +" "+ shopItem.unitOfMeasure.name
            holder.binding.tvPrice.text = shopItem.estimatedPrice.toString()+" Ft"

            if(shopItem.isBought){
                holder.binding.tvBoughtText.setText(R.string.bought)
                holder.binding.tvBoughtText.setBackgroundResource(R.color.expired_color)
            }else{
                holder.binding.tvBoughtText.text=null
                holder.binding.tvBoughtText.background = null
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

    override fun getItemCount(): Int = items.size

    interface ShopItemClickListener {
        fun onItemClicked(item: ShoppingItem)
    }

    inner class ShopViewHolder(val binding: ShopItemListBinding) :
        RecyclerView.ViewHolder(binding.root)
}