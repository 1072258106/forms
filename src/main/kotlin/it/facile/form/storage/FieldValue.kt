package it.facile.form.storage

import java.util.*

sealed class FieldValue {
    object Missing : FieldValue()
    class Text(val text: String = "") : FieldValue()
    class Bool(val bool: Boolean = false) : FieldValue()
    class DateValue(val date: Date) : FieldValue()
    class Object(val value: DescribableWithKey) : FieldValue()

    override fun toString(): String = when (this) {
        is Text -> text.toString()
        is Bool -> bool.toString()
        is Object -> value.describe()
        is DateValue -> date.toString()
        is Missing -> "Missing value"
    }

    override fun equals(other: Any?): Boolean {
        return if (other == null) false
        else when (this) {
            is Missing -> true
            is Text -> other is Text && text == other.text
            is Bool -> other is Bool && bool == other.bool
            is DateValue -> other is Object && date == other.value
            is Object -> other is Object && value == other.value
        }
    }

    override fun hashCode(): Int = when (this) {
        is Missing -> 31
        is Text -> text.hashCode()
        is Bool -> bool.hashCode() * 31
        is DateValue -> date.hashCode() * 31
        is Object -> value.hashCode()
    }
}