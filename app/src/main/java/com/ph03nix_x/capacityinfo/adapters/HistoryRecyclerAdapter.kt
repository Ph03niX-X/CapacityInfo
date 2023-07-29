package com.ph03nix_x.capacityinfo.adapters

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.interfaces.PremiumInterface.Companion.isPremium
import com.ph03nix_x.capacityinfo.databases.History
import com.ph03nix_x.capacityinfo.databases.HistoryDB
import com.ph03nix_x.capacityinfo.databinding.HistoryRecyclerListItemBinding
import com.ph03nix_x.capacityinfo.fragments.HistoryFragment
import com.ph03nix_x.capacityinfo.helpers.HistoryHelper
import com.ph03nix_x.capacityinfo.helpers.TextAppearanceHelper
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface
import com.ph03nix_x.capacityinfo.interfaces.PremiumInterface
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys
import java.text.DecimalFormat

class HistoryAdapter (private var historyList: MutableList<History>) :
    RecyclerView.Adapter<HistoryViewHolder>(), PremiumInterface, BatteryInfoInterface {

    private lateinit var binding: HistoryRecyclerListItemBinding

    private lateinit var pref: SharedPreferences

    companion object {

        var instance: HistoryAdapter? = null
    }

    override fun getItemCount() = historyList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {

        instance = this

        binding = HistoryRecyclerListItemBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)


        pref = PreferenceManager.getDefaultSharedPreferences(parent.context)

        return HistoryViewHolder(binding.root.rootView)
    }

    override fun onBindViewHolder(holderHistory: HistoryViewHolder, position: Int) {
        updateTextAppearance(holderHistory)

        if(isPremium) {
            binding.historyDate.text = historyList[itemCount - 1 - position].date
            binding.historyResidualCapacity.text = getResidualCapacity(holderHistory.itemView.context,
                historyList[itemCount - 1 - position].residualCapacity)
            binding.historyBatteryWear.text = getBatteryWear(holderHistory.itemView.context,
                historyList[itemCount - 1 - position].residualCapacity)
        }
    }

    fun getHistoryList() = historyList

    private fun updateTextAppearance(holderHistory: HistoryViewHolder) {

        TextAppearanceHelper.setTextAppearance(holderHistory.itemView.context, binding.historyDate,
            pref.getString(PreferencesKeys.TEXT_STYLE, "0"),
            pref.getString(PreferencesKeys.TEXT_FONT, "6"),
            pref.getString(PreferencesKeys.TEXT_SIZE, "2"))

        TextAppearanceHelper.setTextAppearance(holderHistory.itemView.context,
            binding.historyResidualCapacity, pref.getString(PreferencesKeys.TEXT_STYLE, "0"),
            pref.getString(PreferencesKeys.TEXT_FONT, "6"),
            pref.getString(PreferencesKeys.TEXT_SIZE, "2"))

        TextAppearanceHelper.setTextAppearance(holderHistory.itemView.context,
            binding.historyBatteryWear, pref.getString(PreferencesKeys.TEXT_STYLE, "0"),
            pref.getString(PreferencesKeys.TEXT_FONT, "6"),
            pref.getString(PreferencesKeys.TEXT_SIZE, "2"))
    }

    private fun getResidualCapacity(context: Context, residualCapacity: Int): String {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val isCapacityInWh = pref.getBoolean(PreferencesKeys.IS_CAPACITY_IN_WH,
            context.resources.getBoolean(R.bool.is_capacity_in_wh))

        val designCapacity = pref.getInt(PreferencesKeys.DESIGN_CAPACITY,
            context.resources.getInteger(R.integer.min_design_capacity))

        var newResidualCapacity = residualCapacity / if(pref.getString(PreferencesKeys
                .UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY, "μAh") == "μAh") 1000.0
        else 100.0

        if(newResidualCapacity < 0.0) newResidualCapacity /= -1.0

        return if(isCapacityInWh) context.getString(R.string.residual_capacity_wh,
            DecimalFormat("#.#").format(getCapacityInWh(newResidualCapacity)),
            "${DecimalFormat("#.#").format(
                ((newResidualCapacity / designCapacity)) * 100.0)}%")

        else context.getString(R.string.residual_capacity, DecimalFormat("#.#").format(
            newResidualCapacity), "${DecimalFormat("#.#").format((
                (newResidualCapacity / designCapacity)) * 100.0)}%")
    }

    private fun getBatteryWear(context: Context, residualCapacity: Int): String {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val designCapacity = pref.getInt(PreferencesKeys.DESIGN_CAPACITY, context.resources
            .getInteger(R.integer.min_design_capacity)).toDouble()

        val isCapacityInWh = pref.getBoolean(PreferencesKeys.IS_CAPACITY_IN_WH,
            context.resources.getBoolean(R.bool.is_capacity_in_wh))

        var newResidualCapacity = residualCapacity / if(pref.getString(PreferencesKeys
                .UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY, "μAh") == "μAh") 1000.0
        else 100.0

        if(newResidualCapacity < 0.0) newResidualCapacity /= -1.0

        return if(isCapacityInWh) context.getString(R.string.battery_wear_wh,
            if(newResidualCapacity > 0 && newResidualCapacity < designCapacity)
                "${DecimalFormat("#.#").format(
                    100 - (newResidualCapacity / designCapacity) * 100)}%" else "0%",
            if(newResidualCapacity > 0 && newResidualCapacity < designCapacity)
                DecimalFormat("#.#").format(
                    getCapacityInWh(designCapacity - newResidualCapacity)) else "0")

        else context.getString(R.string.battery_wear, if (newResidualCapacity > 0 &&
            newResidualCapacity < designCapacity) "${DecimalFormat("#.#").format(
            100 - ((newResidualCapacity / designCapacity) * 100))}%" else "0%",
            if (newResidualCapacity > 0 && newResidualCapacity < designCapacity) DecimalFormat(
                "#.#").format(designCapacity - newResidualCapacity) else "0"
        )
    }

    fun update(context: Context) {

        pref = PreferenceManager.getDefaultSharedPreferences(context)

        if(HistoryHelper.getHistoryCount(context) > historyList.count()) {
            historyList = HistoryDB(context).readDB()
            notifyItemInserted(0)
        }
        else if(HistoryHelper.isHistoryEmpty(context) ||
            HistoryHelper.getHistoryCount(context) < historyList.count()) {
            historyList = HistoryDB(context).readDB()
            notifyItemRangeChanged(0, itemCount - 1)
        }
    }

    fun remove(context: Context, position: Int) {
        if(position >= 0) {
            historyList.removeAt(itemCount - 1 - position)
            notifyItemRemoved(position)
            if(HistoryHelper.isHistoryEmpty(context)) {
                HistoryFragment.instance?.emptyHistory()
            }
        }
    }

    fun undoRemoving(context: Context, position: Int) {
        if(position >= 0) {
            historyList = HistoryDB(context).readDB()
            notifyItemInserted(position)
        }
    }
}