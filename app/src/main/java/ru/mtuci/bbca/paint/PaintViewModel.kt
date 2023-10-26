package ru.mtuci.bbca.paint

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class PaintViewModel : ViewModel() {
    private val _progress = MutableStateFlow(0)
    val progress = _progress.asStateFlow()

    private val _taskDoneSideEffect = Channel<Unit>(Channel.BUFFERED)
    val taskDoneSideEffect = _taskDoneSideEffect.receiveAsFlow()

    fun onNextClick() {
        if (progress.value == 10) {
            return
        }

        _progress.value += 1

        if (progress.value == 10) {
            viewModelScope.launch {
                _taskDoneSideEffect.send(Unit)
            }
        }
    }
}