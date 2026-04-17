package edu.illinois.cs.cs124.ay2024.mp.helpers

class ResultMightThrow<T> {
    private val value: T?
    private val exception: Exception?

    constructor(setValue: T) {
        value = setValue
        exception = null
    }

    constructor(setException: Exception) {
        value = null
        exception = setException
    }

    fun getValue(): T {
        if (exception != null) throw RuntimeException(exception)
        return value!!
    }

    fun getException(): Exception? = exception
}
