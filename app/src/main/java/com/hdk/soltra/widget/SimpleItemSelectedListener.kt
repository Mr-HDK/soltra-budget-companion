package com.hdk.soltra.widget

import android.view.View
import android.widget.AdapterView

internal class SimpleItemSelectedListener(
    private val onSelected: (Int) -> Unit,
) : AdapterView.OnItemSelectedListener {
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        onSelected(position)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        // no-op
    }
}
