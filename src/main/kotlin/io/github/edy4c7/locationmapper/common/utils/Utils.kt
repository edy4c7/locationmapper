package io.github.edy4c7.locationmapper.common.utils

import java.util.*

internal fun <T> Optional<T>.unwrap(): T? = orElse(null)
