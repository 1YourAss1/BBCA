package ru.mtuci.bbca.swipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow

class SwipeViewModel : ViewModel() {
    private val _progress = MutableStateFlow(1)
    val progress = _progress.asStateFlow()

    private val _likedPositions = MutableStateFlow(emptySet<Int>())
    val likedPositions = _likedPositions.asStateFlow()

    private val _taskDoneSideEffect = Channel<Unit>(Channel.BUFFERED)
    val taskDoneSideEffect = _taskDoneSideEffect.receiveAsFlow()

    init {
        progress.combine(likedPositions) { progress, likedPositions ->
            progress == 50 && likedPositions.size >= 20
        }.onEach { isDone ->
            if (isDone) {
                _taskDoneSideEffect.send(Unit)
            }
        }.launchIn(viewModelScope)
    }

    fun onPageSelected(position: Int) {
        if (position + 1 <= progress.value || progress.value == 50) {
            return
        }

        _progress.value += 1
    }

    fun onPageLiked(position: Int) {
        _likedPositions.value += position
    }
}