package com.edblueprint.app.data.remote

import retrofit2.HttpException
import java.io.IOException

/** Turns any failure into a sentence a student can understand. */
fun Throwable.userMessage(): String = when (this) {
    is HttpException -> when (code()) {
        401 -> "Your session has expired. Please log in again."
        403 -> "This content needs an active subscription."
        404 -> "This item is no longer available."
        429 -> "Too many requests. Please wait a moment and try again."
        else -> "The server had a problem (${code()}). Please try again."
    }
    is IOException -> "Cannot reach the server. Check your internet connection and try again."
    else -> "Something went wrong. Please try again."
}

/** True when the server refused because the subscription is missing or expired. */
fun Throwable.isForbidden(): Boolean = this is HttpException && code() == 403
