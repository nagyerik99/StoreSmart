package hu.bme.aut.storesmartapp.shop

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import fr.tvbarthel.lib.blurdialogfragment.BlurDialogEngine
import hu.bme.aut.storesmartapp.R
import hu.bme.aut.storesmartapp.data.FridgeItem
import hu.bme.aut.storesmartapp.data.ShoppingItem
import hu.bme.aut.storesmartapp.databinding.DialogAddShopItemBinding

class AddShopItemFragment(type: ShopActivity.FragmentState) : DialogFragment() {
    interface ShopItemCreatedListener {
        fun onShopItemItemCreatedOrEdited(newItem: ShoppingItem)
        fun onShopItemEditCanceled()
    }

    private lateinit var editItem: ShoppingItem

    private lateinit var listener: ShopItemCreatedListener
    private lateinit var binding: DialogAddShopItemBinding
    private lateinit var blurDialog: BlurDialogEngine
    private val addOrEdit: ShopActivity.FragmentState = type
    private lateinit var alertText: AlertText

    enum class AlertText{
        InvalidPrice,InvalidName
    }

    constructor(type: ShopActivity.FragmentState, shoppingItem: ShoppingItem) : this(type) {
        editItem = shoppingItem
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? ShopItemCreatedListener
            ?: throw RuntimeException("Activity must implement the NewFridgeItemDialogListener interface!")

        blurDialog = BlurDialogEngine(activity)
        blurDialog.setBlurRadius(8)
        blurDialog.setDownScaleFactor(8f)
        blurDialog.debug(false)
        blurDialog.setBlurActionBar(false)
        blurDialog.setUseRenderScript(true)
        blurDialog.onAttach(activity)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogAddShopItemBinding.inflate(LayoutInflater.from(context))
        binding.spCategory.adapter = ArrayAdapter(
            requireContext(),
            R.layout.support_simple_spinner_dropdown_item,
            resources.getStringArray(R.array.category_items)
        )
        binding.uom.adapter = ArrayAdapter(
            requireContext(),
            R.layout.support_simple_spinner_dropdown_item,
            resources.getStringArray(R.array.uom_items)
        )
        binding.numofItem.minValue = 1
        binding.numofItem.maxValue = 50

        if(addOrEdit == ShopActivity.FragmentState.Edit){
            loadEditItem()
        }

        val text = if(addOrEdit == ShopActivity.FragmentState.Edit)
            getString(R.string.edit_grocery)
        else
            getString(R.string.new_grocery)

        binding.titleText.text = text

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setPositiveButton(R.string.button_ok) { _, _ ->
                if (isValid()) {
                    listener.onShopItemItemCreatedOrEdited(getShoppingItem())
                }else{
                    if (addOrEdit == ShopActivity.FragmentState.Edit){
                        listener.onShopItemEditCanceled()
                    }
                    makeToastAlert()
                }
            }
            .setNegativeButton(R.string.button_cancel){
                    _,_->
                if(addOrEdit == ShopActivity.FragmentState.Edit){
                    listener.onShopItemEditCanceled()
                }
            }
            .create()
    }

    private fun makeToastAlert(){
        val toastText = when(alertText){
            AlertText.InvalidName -> "Invalid Name!"
            AlertText.InvalidPrice -> "Invalid Price!"
        }

        Toast.makeText(requireContext(),toastText, Toast.LENGTH_LONG).show()

    }

    override fun onDismiss(dialog: DialogInterface) {
        blurDialog.onDismiss()
        super.onDismiss(dialog)
    }

    override fun onDetach() {
        super.onDetach()
        blurDialog.onDetach()
    }

    override fun onResume() {
        super.onResume()
        blurDialog.onResume(false)
    }

    override fun onCancel(dialog: DialogInterface) {
        listener.onShopItemEditCanceled()
        super.onCancel(dialog)
    }

    private fun isValid() : Boolean{
        if(binding.etName.text.isEmpty()){
            alertText = AlertText.InvalidName
            return false
        }

        if(binding.etPrice.text.isEmpty()){
            alertText = AlertText.InvalidPrice
            return false
        }

        return true
    }

    private fun getShoppingItem() :ShoppingItem {
        val price = binding.etPrice.text.toString()
        var id :Long? = null
        if(addOrEdit == ShopActivity.FragmentState.Edit){
            id = editItem.id
        }

        return ShoppingItem(
            id,
            name = binding.etName.text.toString(),
            category = FridgeItem.Category.getByOrdinal(binding.spCategory.selectedItemPosition)
                ?: FridgeItem.Category.FROZEN,
            numofItem = binding.numofItem.value,
            unitOfMeasure = FridgeItem.Measurement.getByOrdinal(binding.uom.selectedItemPosition)
                ?: FridgeItem.Measurement.DB,
            estimatedPrice = price.toInt(),
            isBought = false
        )
    }

    companion object {
        const val TAG = "AddFridgeItemFragment"
    }

    private fun loadEditItem(){
        binding.etName.setText(editItem.name, TextView.BufferType.EDITABLE)
        binding.numofItem.value = editItem.numofItem
        binding.uom.setSelection(FridgeItem.Measurement.toInt(editItem.unitOfMeasure),false)
        binding.spCategory.setSelection(FridgeItem.Category.toInt(editItem.category))
        binding.etPrice.setText(editItem.estimatedPrice.toString(),TextView.BufferType.EDITABLE)
    }}