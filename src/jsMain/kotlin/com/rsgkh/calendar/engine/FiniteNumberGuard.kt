package com.rsgkh.calendar.engine

internal actual fun requireFiniteDouble(value: Double, name: String) {
    val ok: Boolean = js("typeof value === 'number' && Number.isFinite(value)")
    require(ok) { "$name must be a finite primitive number" }
}
