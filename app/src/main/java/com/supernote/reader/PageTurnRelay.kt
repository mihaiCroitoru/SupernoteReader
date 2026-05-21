package com.supernote.reader

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object PageTurnRelay {
    private val _flow = MutableSharedFlow<Int>(extraBufferCapacity = 8)
    val flow: SharedFlow<Int> = _flow.asSharedFlow()
    fun emit(keyCode: Int) { _flow.tryEmit(keyCode) }
}
