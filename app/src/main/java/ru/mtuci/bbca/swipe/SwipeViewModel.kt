package ru.mtuci.bbca.swipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SwipeViewModel : ViewModel() {
    private val _progress = MutableStateFlow(1)
    val progress = _progress.asStateFlow()

    private val _taskDoneSideEffect = Channel<Unit>(Channel.BUFFERED)
    val taskDoneSideEffect = _taskDoneSideEffect.receiveAsFlow()

    fun onPageSelected(position: Int) {
        if (position + 1 <= progress.value || progress.value == 50) {
            return
        }

        _progress.value += 1

        if (progress.value == 50) {
            viewModelScope.launch {
                _taskDoneSideEffect.send(Unit)
            }
        }
    }
}