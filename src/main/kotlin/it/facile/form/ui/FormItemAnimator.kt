package it.facile.form.ui

import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.RecyclerView
import it.facile.form.ui.adapters.FieldViewHolders.FieldViewHolderInputText

open class FormItemAnimator : DefaultItemAnimator() {
    override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean {
        return viewHolder is FieldViewHolderInputText
    }
}