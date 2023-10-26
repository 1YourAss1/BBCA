package ru.mtuci.bbca.long_click

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LongClickViewModel : ViewModel() {
    private val _items = MutableStateFlow(List(10) { index -> AdapterItem(id = index + 1) })
    val items = _items.asStateFlow()

    private val _progress = MutableStateFlow(0)
    val progress = _progress.asStateFlow()

    private val _taskDoneSideEffect = Channel<Unit>(Channel.BUFFERED)
    val taskDoneSideEffect = _taskDoneSideEffect.receiveAsFlow()

    fun onItemLongClick(item: AdapterItem) {
        _items.update { items ->
            items.map { foundItem ->
                if (foundItem.id == item.id) {
                    foundItem.copy(isDeleted = true)
                } else {
                    foundItem
                }
            }
        }

        if (items.value.all { it.isDeleted }) {
            _progress.value += 1

            if (progress.value == 10) {
                viewModelScope.launch {
                    _taskDoneSideEffect.send(Unit)
                }
            } else {
                _items.update { items ->
                    items.map { foundItem ->
                        foundItem.copy(isDeleted = false)
                    }
                }
            }
        }
    }
}