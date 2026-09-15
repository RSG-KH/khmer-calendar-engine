package com.rsgkh.calendar.engine

internal actual fun freezeValue(value: Any) {
    js("Object.freeze")(value)
}
