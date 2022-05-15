package hu.bme.aut.storesmartapp.fridge

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
import hu.bme.aut.storesmartapp.data.DateConverters
import hu.bme.aut.storesmartapp.data.FridgeItem
import hu.bme.aut.storesmartapp.databinding.DialogNewFridgeItemBinding
import java.util.*

class AddFridgeItemFragment(type :FridgeActivity.FragmentState) : DialogFragment(){
    interface NewFridgeItemDialogListener {
        fun onFridgeItemCreatedOrEdited(newItem: FridgeItem)
        fun onFridgeItemEditCanceled()
    }

    private lateinit var editItem:FridgeItem

    private lateinit var listener: NewFridgeItemDialogListener
    private lateinit var binding: DialogNewFridgeItemBinding
    private lateinit var blurDialog: BlurDialogEngine
    private val addOrEdit:FridgeActivity.FragmentState = type
    private lateinit var alertText:AlertText

    enum class AlertText{
        InvalidDate,InvalidName
    }

    constructor(type:FridgeActivity.FragmentState, fridgeItem: FridgeItem) : this(type) {
        editItem = fridgeItem
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? NewFridgeItemDialogListener
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
        binding = DialogNewFridgeItemBinding.inflate(LayoutInflater.from(context))
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

        if(addOrEdit == FridgeActivity.FragmentState.Edit){
            loadEditItem()
        }

        val text = if(addOrEdit == FridgeActivity.FragmentState.Edit)
            R.string.edit_fridge_item
        else
            R.string.new_fridge_item

        binding.titleText.setText(text)

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setPositiveButton(R.string.button_ok) { _, _ ->
                if (isValid()) {
                    listener.onFridgeItemCreatedOrEdited(getShoppingItem())
                }else{
                    if (addOrEdit == FridgeActivity.FragmentState.Edit){
                        listener.onFridgeItemEditCanceled()
                    }
                    makeToastAlert()
                }
            }
            .setNegativeButton(R.string.button_cancel){
                    _, _ ->
                if(addOrEdit == FridgeActivity.FragmentState.Edit){
                    listener.onFridgeItemEditCanceled()
                }
            }
            .create()
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
        listener.onFridgeItemEditCanceled()
        super.onCancel(dialog)
    }

    private fun makeToastAlert(){
        val toastText = when(alertText){
            AlertText.InvalidName -> "Invalid Name!"
            AlertText.InvalidDate -> "Invalid Expiry Date !"
        }

        Toast.makeText(requireContext(),toastText,Toast.LENGTH_LONG).show()

    }

    private fun isValid() : Boolean{
        if(binding.etName.text.isEmpty()){
            alertText = AlertText.InvalidName
            return false
        }
        if (isExpired()){
            alertText = AlertText.InvalidDate
            return false
        }

        return true
    }

    private fun getShoppingItem() :FridgeItem{
        val cal = Calendar.getInstance()
            cal.set(binding.bestBefore.year,binding.bestBefore.month,binding.bestBefore.dayOfMonth)

        return FridgeItem(
            if(addOrEdit == FridgeActivity.FragmentState.Edit)
                editItem.id
            else
                null,
            name = binding.etName.text.toString(),
            category = FridgeItem.Category.getByOrdinal(binding.spCategory.selectedItemPosition)
                ?: FridgeItem.Category.FROZEN,
            numofItem = binding.numofItem.value,
            unitOfMeasure = FridgeItem.Measurement.getByOrdinal(binding.uom.selectedItemPosition)
                ?:FridgeItem.Measurement.DB,
            bestBefore = DateConverters().fromCalendar(cal),
            depraved = isExpired()
        )
    }

    private fun isExpired():Boolean{
        val cal = Calendar.getInstance()
        cal.set(binding.bestBefore.year,binding.bestBefore.month,binding.bestBefore.dayOfMonth)
        val dateNow = Calendar.getInstance()
        dateNow.set(Calendar.DAY_OF_MONTH,dateNow.get(Calendar.DAY_OF_MONTH)-1)

        return cal.before(dateNow)
    }
    companion object {
        const val TAG = "AddFridgeItemFragment"
    }

    private fun loadEditItem(){
        binding.etName.setText(editItem.name,TextView.BufferType.EDITABLE)
        binding.numofItem.value = editItem.numofItem
        binding.uom.setSelection(FridgeItem.Measurement.toInt(editItem.unitOfMeasure),true)
        binding.spCategory.setSelection(FridgeItem.Category.toInt(editItem.category))
        if (editItem.bestBefore != null) {
            val calendar = DateConverters().toCalendar(editItem.bestBefore)
            binding.bestBefore.updateDate(calendar.get(Calendar.YEAR),
                                         calendar.get(Calendar.MONTH),
                                         calendar.get(Calendar.DAY_OF_MONTH))
        }
    }

}