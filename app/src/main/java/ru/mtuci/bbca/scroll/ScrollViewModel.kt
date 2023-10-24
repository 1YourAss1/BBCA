package ru.mtuci.bbca.scroll

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ScrollViewModel : ViewModel() {
    private var isTaskDone = false

    private val _taskDoneSideEffect = Channel<Unit>(Channel.BUFFERED)
    val taskDoneSideEffect = _taskDoneSideEffect.receiveAsFlow()

    fun onScroll(isScrolledToBottom: Boolean) = viewModelScope.launch {
        if (!isTaskDone && isScrolledToBottom) {
            isTaskDone = true
            _taskDoneSideEffect.send(Unit)
        }
    }
}