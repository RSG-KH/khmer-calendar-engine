package com.rsgkh.calendar.engine

internal actual fun requireFiniteDouble(value: Double, name: String) {
    require(value.isFinite()) { "$name must be finite" }
}
