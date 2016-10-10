package it.facile.form.ui.adapters.FieldViewHolders

import android.view.View
import it.facile.form.ui.CanBeHidden
import it.facile.form.ui.viewmodel.FieldViewModel
import it.facile.form.ui.viewmodel.FieldViewModelStyle
import kotlinx.android.synthetic.main.form_field_exception_text.view.*

class FieldViewHolderException(itemView: View) : FieldViewHolderBase(itemView), CanBeHidden {
    override fun bind(viewModel: FieldViewModel, position: Int, errorsShouldBeVisible: Boolean) {
        super.bind(viewModel, position, errorsShouldBeVisible)
        itemView.exceptionLabel.text = viewModel.label
        val style = viewModel.style
        when (style) {
            is FieldViewModelStyle.ExceptionText -> itemView.exceptionTextView.text = style.text
        }
    }
}