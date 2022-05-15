package hu.bme.aut.storesmartapp.fridge

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import fr.tvbarthel.lib.blurdialogfragment.BlurDialogEngine
import hu.bme.aut.storesmartapp.R
import hu.bme.aut.storesmartapp.databinding.AlertDialogBaseBinding

class AlertBlurDialogFragment(alert: AlertType) : DialogFragment() {
    private lateinit var blurDialog : BlurDialogEngine
    private val alertType =alert

    interface AlertDialogOkInterface {
        fun onOkClicked(alert:AlertType)
    }

    enum class AlertType{
        AddToCart,DeleteAll,AddToFridge
    }

    private lateinit var listener: AlertDialogOkInterface
    private lateinit var binding: AlertDialogBaseBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? AlertDialogOkInterface
            ?: throw RuntimeException("Activity must implement the AlertDialogOkInterface interface!")

        blurDialog = BlurDialogEngine(activity)
        blurDialog.setBlurRadius(8)
        blurDialog.setDownScaleFactor(8f)
        blurDialog.debug(false)
        blurDialog.setBlurActionBar(false)
        blurDialog.setUseRenderScript(true)
        blurDialog.onAttach(activity)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = AlertDialogBaseBinding.inflate(LayoutInflater.from(context))

        when(alertType){
            AlertType.AddToCart ->{
                binding.alertText.text = getString(R.string.add_to_cart_title)
                binding.alertTodo.text = getString(R.string.add_to_cart_alert)
            }
            AlertType.DeleteAll ->{
                binding.alertText.text = getString(R.string.alert_delete_all_frdigeitem)
                binding.alertTodo.text = getString(R.string.alert_dialog_continue_text)
            }
            AlertType.AddToFridge ->{
                binding.alertText.text = getString(R.string.add_to_fridge_title)
                binding.alertTodo.text = getString(R.string.alert_dialog_continue_text)
            }
        }
        this.setStyle(STYLE_NO_FRAME,theme)

        return androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setPositiveButton(R.string.Yes) { _, _ ->
                run {
                    listener.onOkClicked(alertType)
                }
            }
            .setNegativeButton(R.string.button_cancel, null)
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

    companion object {
        const val TAG = "AlertDeleteAllFragment"
    }
}