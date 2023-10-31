package ru.mtuci.bbca.identifier

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import ru.mtuci.bbca.R

class IdentifierViewModel : ViewModel() {
    private val _identifierError = MutableStateFlow<Int?>(null)
    val identifierError = _identifierError.asStateFlow()

    private val _repeatedIdentifierError = MutableStateFlow<Int?>(null)
    val repeatedIdentifierError = _repeatedIdentifierError.asStateFlow()

    private val _doneSideEffect = Channel<Unit>(Channel.BUFFERED)
    val doneSideEffect = _doneSideEffect.receiveAsFlow()

    var identifier = ""
        private set

    var repeatedIdentifier = ""
        private set

    fun onIdentifierChange(value: String) {
        identifier = value
        _identifierError.value = null
    }

    fun onRepeatedIdentifierChange(value: String) {
        repeatedIdentifier = value
        _repeatedIdentifierError.value = null
    }

    fun identify() {
        viewModelScope.launch {
            when {
                identifier.length < MINIMUM_IDENTIFIER_LENGTH -> {
                    _identifierError.value = R.string.identifier_too_short
                }

                identifier != repeatedIdentifier -> {
                    _repeatedIdentifierError.value = R.string.identifiers_matching_error
                }

                else -> {
                    _doneSideEffect.send(Unit)
                }
            }
        }
    }

    companion object {
        private const val MINIMUM_IDENTIFIER_LENGTH = 6
    }
}