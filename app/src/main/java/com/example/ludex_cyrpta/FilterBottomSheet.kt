package com.example.ludex_cyrpta

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.ChipGroup

class FilterBottomSheet : BottomSheetDialogFragment() {
    private var filterListener: FilterSelectionListener? = null
    private lateinit var otherServicesCG: ChipGroup
    private lateinit var applyFilters: Button
    private lateinit var clearFilters: Button

    fun setFilterSelectionListener(listener: FilterSelectionListener) {
        filterListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.filter_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        otherServicesCG = view.findViewById(R.id.servicesCG)
        applyFilters = view.findViewById(R.id.applyFiltersBtn)
        clearFilters = view.findViewById(R.id.clearFiltersBtn)

        applyFilters.setOnClickListener { applyAllFilters() }
        clearFilters.setOnClickListener { clearAllFilters() }
    }

    private fun applyAllFilters() {
        val selectedFilter = if (otherServicesCG.checkedChipId != View.NO_ID) {
            val chip = requireView().findViewById<View>(otherServicesCG.checkedChipId)
            chip?.tag?.toString() //apply filter
        } else null //no filters applied

        Log.d(TAG, "Filter Applied: $selectedFilter")
        filterListener?.onFilterApplied(selectedFilter)
        dismiss() //close after applying
    }

    private fun clearAllFilters() {
        Log.d(TAG, "Filters Cleared")
        otherServicesCG.clearCheck()
        filterListener?.onFilterApplied(null) //to send null back to Fragment
        dismiss() //close the bottom sheet
    }

    companion object {
        const val TAG = "FilterBottomSheet"
    }
}