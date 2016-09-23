package it.facile.form.model.configurations

import it.facile.form.Dates
import it.facile.form.format
import it.facile.form.model.FieldConfig
import it.facile.form.model.FieldRule
import it.facile.form.model.FieldRulesValidator
import it.facile.form.storage.FieldValue.DateValue
import it.facile.form.storage.FieldValue.Missing
import it.facile.form.storage.FormStorage
import it.facile.form.ui.viewmodel.FieldViewModel
import it.facile.form.ui.viewmodel.FieldViewModelStyle
import it.facile.form.ui.viewmodel.FieldViewModelStyle.DatePicker
import java.text.DateFormat
import java.util.*

class FieldConfigPickerDate(label: String,
                            val minDate: Date = Dates.create(1900, 0, 1),
                            val maxDate: Date = Dates.create(2100, 11, 31),
                            val dateFormatter: DateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM),
                            val placeholder: String = "Select a date",
                            override val rules: List<FieldRule> = emptyList()) : FieldConfig(label), FieldRulesValidator {

    override fun getViewModel(key: Int, storage: FormStorage): FieldViewModel {
        val value = storage.getValue(key)
        return FieldViewModel(
                label,
                getViewModelStyle(key, storage),
                storage.isHidden(key),
                isValid(value, storage))
    }

    override fun getViewModelStyle(key: Int, storage: FormStorage): FieldViewModelStyle {
        val value = storage.getValue(key)
        return when (value) {
            is DateValue -> DatePicker(minDate, maxDate,
                        selectedDate = value.date,
                        dateText = value.date.format(dateFormatter))
            is Missing -> DatePicker(minDate, maxDate,
                    selectedDate = Dates.today(),
                    dateText = placeholder)
            else -> FieldViewModelStyle.ExceptionText(FieldViewModelStyle.INVALID_TYPE)
        }
    }
}