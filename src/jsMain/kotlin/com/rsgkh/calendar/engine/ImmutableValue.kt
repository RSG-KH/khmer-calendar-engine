package com.rsgkh.calendar.engine

internal actual fun freezeValue(value: Any) {
    js("Object.freeze")(value)
}

internal actual fun requireInteger(value: Int) {
    val ok: Boolean = js("typeof value === 'number' && Number.isInteger(value) && value >= -2147483648 && value <= 2147483647")
    require(ok) { "Expected an integer in 32-bit range, received: $value" }
}
