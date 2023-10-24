package ru.mtuci.bbca.clicks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ClicksViewModel : ViewModel() {
    private val _visibleButtonsIndexes = MutableStateFlow(emptySet<Int>())
    val visibleButtonsIndexes = _visibleButtonsIndexes.asStateFlow()

    private val _progress = MutableStateFlow(0)
    val progress = _progress.asStateFlow()

    private val _taskDoneSideEffect = Channel<Unit>(Channel.BUFFERED)
    val taskDoneSideEffect = _taskDoneSideEffect.receiveAsFlow()

    val startButtonState = visibleButtonsIndexes
        .combine(progress) { visibleButtonsIndexes, progress ->
            when {
                progress == 10 -> ClicksStartButtonState.GONE
                visibleButtonsIndexes.isEmpty() -> ClicksStartButtonState.IDLE
                else -> ClicksStartButtonState.IN_PROGRESS
            }
        }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = when {
                progress.value == 10 -> ClicksStartButtonState.GONE
                visibleButtonsIndexes.value.isEmpty() -> ClicksStartButtonState.IDLE
                else -> ClicksStartButtonState.IN_PROGRESS
            }
        )

    fun onStartClick() {
        _visibleButtonsIndexes.value = (0 until BUTTONS_COUNT)
            .shuffled()
            .subList(0, VISIBLE_COUNT)
            .toSet()
    }

    fun onButtonClick(index: Int) {
        _visibleButtonsIndexes.value -= index

        if (visibleButtonsIndexes.value.isEmpty()) {
            _progress.value += 1
        }

        if (progress.value == 10) {
            viewModelScope.launch {
                _taskDoneSideEffect.send(Unit)
            }
        }
    }

    companion object {
        private const val BUTTONS_COUNT = 25
        private const val VISIBLE_COUNT = 10
    }
}