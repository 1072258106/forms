package it.facile.form.model.configurations

import it.facile.form.model.CouldHaveLoadingError
import it.facile.form.model.FieldConfig
import it.facile.form.model.FieldRule
import it.facile.form.model.FieldRulesValidator
import it.facile.form.storage.FieldPossibleValues
import it.facile.form.storage.FieldPossibleValues.Available
import it.facile.form.storage.FieldPossibleValues.ToBeRetrieved
import it.facile.form.storage.FieldValue.Missing
import it.facile.form.storage.FieldValue.Object
import it.facile.form.storage.FormStorageApi
import it.facile.form.ui.viewmodel.FieldViewModel
import it.facile.form.ui.viewmodel.FieldViewModelStyle
import it.facile.form.ui.viewmodel.FieldViewModelStyle.*
import rx.Subscription

class FieldConfigPicker(label: String,
                        val possibleValues: FieldPossibleValues,
                        val placeHolder: String = "Select a value",
                        override val errorMessage: String = "Loading error",
                        override val rules: (FormStorageApi) -> List<FieldRule> = { emptyList() }) : FieldConfig(label), FieldRulesValidator, CouldHaveLoadingError {

    private var sub: Subscription? = null
    override var hasErrors = false

    override fun getViewModel(key: String, storage: FormStorageApi) =
            FieldViewModel(
                    label = label,
                    style = getViewModelStyle(key, storage),
                    hidden = storage.isHidden(key),
                    disabled = storage.isDisabled(key),
                    error = isValid(storage.getValue(key), storage))


    val possibleValuesGenerator: (FormStorageApi, String) -> FieldPossibleValues = { storage, key -> storage.getPossibleValues(key) ?: possibleValues }

    override fun getViewModelStyle(key: String, storage: FormStorageApi): FieldViewModelStyle {
        val value = storage.getValue(key)
        return if (hasErrors)
            ExceptionText(errorMessage)
        else
            when (value) {
                is Object -> chooseViewModelStyle(storage, key, value.value.textDescription)
                is Missing -> chooseViewModelStyle(storage, key, placeHolder)
                else -> ExceptionText(FieldViewModelStyle.INVALID_TYPE)
            }
    }

    private fun chooseViewModelStyle(storage: FormStorageApi, key: String, text: String): FieldViewModelStyle {
        sub?.unsubscribe()
        val possibleValues = possibleValuesGenerator(storage, key)
        return when (possibleValues) {
            is Available -> Picker(possibleValues.list, text)
            is ToBeRetrieved -> Loading()
        }
    }
}