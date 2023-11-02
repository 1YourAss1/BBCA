package ru.mtuci.bbca.main

data class TasksState(
    val isKeyStrokeDone: Boolean = false,
    val isScrollDone: Boolean = false,
    val isSwipeDone: Boolean = false,
    val isScaleDone: Boolean = false,
    val isClicksDone: Boolean = false,
    val isVideoDone: Boolean = false,
    val isLongClicksDone: Boolean = false,
    val isPaintDone: Boolean = false,
)
