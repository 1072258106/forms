package it.facile.form.ui.adapters

import android.support.v7.widget.RecyclerView
import it.facile.form.SectionedRecyclerViewAdapter
import it.facile.form.deferredNotifyItemChanged
import it.facile.form.logD
import it.facile.form.model.CustomPickerId
import it.facile.form.storage.FieldValue
import it.facile.form.ui.viewmodel.FieldPath
import it.facile.form.ui.viewmodel.FieldPathSection
import it.facile.form.ui.viewmodel.FieldViewModel
import it.facile.form.ui.viewmodel.SectionViewModel
import rx.Observable
import java.util.*

class SectionsAdapter(val sectionViewModels: List<SectionViewModel>,
                      fieldsLayouts: FieldsLayouts = FieldsLayouts(),
                      customPickerActions: Map<CustomPickerId, ((FieldValue) -> Unit) -> Unit> = emptyMap(),
                      customBehaviours: Map<String, () -> Unit> = emptyMap())
: SectionedRecyclerViewAdapter(fieldsLayouts.sectionHeaders.first, fieldsLayouts.sectionHeaders.second) {
    private val fieldsAdapter: FieldsAdapter
    private val recyclerViews: MutableList<RecyclerView> = mutableListOf()

    init {
        setAwareSections(sectionViewModels.buildPositionAwareList())
        val fieldViewModels = sectionViewModels.listAllFieldsViewModel()
        fieldsAdapter = FieldsAdapter(fieldViewModels,
                customPickerActions,
                customBehaviours,
                fieldsLayouts)
        this.adapter = fieldsAdapter
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        //TODO remove the recyclerView from the adapter and move it to pageFragment
        recyclerViews.add(recyclerView)
    }

    /**
     * Updates the field at the given [FieldPath] taking care of notifying the changes when
     * appropriate using the provided [SectionViewModel].
     */
    fun updateField(path: FieldPath, sectionViewModel: SectionViewModel) {
        val absolutePosition = path.buildAbsoluteFieldPosition(sectionViewModels)
        val viewModel = sectionViewModel.fields[path.fieldIndex]
        val oldViewModel = fieldsAdapter.getViewModel(absolutePosition)
        logD("Position $absolutePosition: new fieldViewModel update request:\n" +
                "old: $oldViewModel\n" +
                "new: $viewModel\n")
        if (absolutePosition >= fieldsAdapter.itemCount) {
            logD("Not updating because position is out of bound")
            return
        } // No field at given position
        if (viewModel == oldViewModel) {
            logD("Not updating because viewModels are the same")
            return
        } // Same view model

        val sectionIndex = absolutePosition.calculateSectionIndex()
        val isSectionTitleChanged = sectionViewModel.title != sectionViewModels[sectionIndex!!].title
        val isViewModelChanged = viewModel != oldViewModel

        val sectionedPosition = positionToSectionedPosition(absolutePosition)
        fieldsAdapter.setFieldViewModel(absolutePosition, viewModel)
        setAwareSection(sectionViewModel.buildPositionAware(sectionIndex))


        recyclerViews.map {
            if (isViewModelChanged or areErrorsVisible()) {
                logD("Updating...")
                if (it.isComputingLayout) { // Defer view update if RecyclerView is computing layout
                    deferredNotifyItemChanged(sectionedPosition)
                    if (isSectionTitleChanged) deferredNotifyItemChanged(awareSections.keyAt(sectionIndex))
                } else {
                    notifyItemChanged(sectionedPosition)
                    if (isSectionTitleChanged) notifyItemChanged(awareSections.keyAt(sectionIndex))
                }
            } else {
                logD("Not updating because viewModel is the same")
            }
        }
    }

    /**
     * Returns an [Observable] emitting a [Pair] with the [FieldPathSection] of the field that has a
     * new value and the new [FieldValue]
     * */
    fun observeValueChanges(): Observable<Pair<FieldPathSection, FieldValue>> =
            fieldsAdapter.observeValueChanges()
                    .map {
                        FieldPathSection(
                                fieldIndex = it.first.calculateFieldIndex()!!,
                                sectionIndex = it.first.calculateSectionIndex()!!
                        ) to it.second
                    }

    /* ---------- Errors related methods ---------- */

    /** Toggles the errors visibility notifying the changes to the adapter */
    fun showErrors(show: Boolean) {
        fieldsAdapter.showErrors(show)
        if (show) fieldsAdapter.errorPositions().map { notifyItemChanged(positionToSectionedPosition(it)) }
    }

    /** Returns the sectioned position of the first occurred error, -1 if no errors are present */
    fun firstErrorPosition() = positionToSectionedPosition(fieldsAdapter.firstErrorPosition())

    /** Returns whether the adapter is currently showing errors */
    fun areErrorsVisible() = fieldsAdapter.areErrorsVisible()

    /** Returns whether the adapter contains at least one visible field with an error */
    fun hasErrors() = fieldsAdapter.hasErrors()


    /* ---------- Helper extension functions ---------- */

    /** Builds the section view model aware of its position considering all the fields */
    fun SectionViewModel.buildPositionAware(index: Int)
            : PositionAwareSectionViewModel {
        val offset = sectionViewModels.subList(0, index).fold(0,
                { offset, section ->
                    offset + section.fields.size
                })
        return PositionAwareSectionViewModel(
                offset,
                offset + index,
                this.title,
                this.isHidden())
    }

    fun List<SectionViewModel>.buildPositionAwareList() =
            mapIndexed { i, sectionViewModel -> sectionViewModel.buildPositionAware(i) }.toTypedArray()

    /** Returns a list of all the [FieldViewModel] contained in all the [SectionViewModel]*/
    fun List<SectionViewModel>.listAllFieldsViewModel() =
            fold(mutableListOf<FieldViewModel>(), { list, item ->
                list.addAll(item.fields)
                list
            })

    /** Calculates the section index for the absolute position index */
    fun Int.calculateSectionIndex(): Int? {
        var counter = 0
        sectionViewModels.mapIndexed { i, sectionViewModel ->
            counter += sectionViewModel.fields.size
            if (this < counter) return i
        }
        return null
    }

    /** Calculates the field index (the position inside the section) for the absolute position index */
    fun Int.calculateFieldIndex(): Int? {
        var copy = this
        for ((title, fields) in sectionViewModels) {
            for ((index, fieldViewModel) in fields.withIndex()) {
                if (copy == 0) return index
                copy--
            }
        }
        return null
    }

    fun FieldPath.buildAbsoluteFieldPosition(sectionViewModels: List<SectionViewModel>): Int {
        var absolutePosition = 0
        for (i in 0..sectionViewModels.size - 1) {
            for (j in 0..sectionViewModels[i].fields.size - 1) {
                if (i == sectionIndex && j == fieldIndex) return absolutePosition
                absolutePosition++
            }
        }
        return -1
    }
}
