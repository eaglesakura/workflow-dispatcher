package com.eaglesakura.firearm.experimental.workflow.internal

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders

internal class WorkflowStateHolder internal constructor(
    val handle: SavedStateHandle
) : ViewModel() {

    init {
        Log.d("WorkflowStateHolder", "newInstance")
    }

    companion object {
        fun from(fragment: Fragment): WorkflowStateHolder = ViewModelProviders
            .of(fragment, SavedStateViewModelFactory(fragment))
            .get(WorkflowStateHolder::class.java)

        fun from(activity: FragmentActivity): WorkflowStateHolder =
            ViewModelProviders
                .of(activity, SavedStateViewModelFactory(activity))
                .get(WorkflowStateHolder::class.java)
    }
}