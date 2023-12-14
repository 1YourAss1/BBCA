package ru.mtuci.bbca.scale

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.mtuci.bbca.scale.overlay_image_view.OverlayItem

class ScaleViewModel(
    val characters: List<OverlayItem>
) : ViewModel() {
    private val _progress = MutableStateFlow(0)

    private val _taskDoneSideEffect = Channel<Unit>(Channel.BUFFERED)
    val taskDoneSideEffect = _taskDoneSideEffect.receiveAsFlow()

    val progress = _progress.map { index ->
        index to characters.getOrNull(index)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = _progress.value to characters.getOrNull(_progress.value)
    )

    fun onCharacterClick(items: List<OverlayItem>) {
        if (!items.contains(characters.getOrNull(_progress.value))) {
            return
        }

        if (_progress.value == characters.size) {
            return
        }

        _progress.value += 1

        if (_progress.value == characters.size) {
            viewModelScope.launch {
                _taskDoneSideEffect.send(Unit)
            }
        }
    }

    object CharactersKey : CreationExtras.Key<List<OverlayItem>>

    companion object {
        val factory get() = viewModelFactory {
            initializer {
                ScaleViewModel(
                    characters = requireNotNull(get(CharactersKey))
                )
            }
        }
    }
}