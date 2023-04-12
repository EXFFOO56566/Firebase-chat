package com.devlomi.fireapp.common

import android.arch.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

abstract class ScopedViewModel(protected val uiContext: CoroutineContext): ViewModel(), CoroutineScope {

    protected var jobTracker: Job = Job()


    override val coroutineContext: CoroutineContext
        get() = uiContext + jobTracker

}
