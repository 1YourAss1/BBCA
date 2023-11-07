package ru.mtuci.bbca.app_logger

class PayloadException(
    throwable: Throwable,
    payload: String
) : Throwable(
    message = "Exception with payload: $payload",
    cause = throwable
)

inline fun <T> catchWithPayload(payload: String, block: () -> T): T {
    try {
        return block()
    } catch (throwable: Throwable) {
        throw PayloadException(throwable, payload)
    }
}

