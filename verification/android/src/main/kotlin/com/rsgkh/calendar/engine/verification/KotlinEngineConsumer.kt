package com.rsgkh.calendar.engine.verification

import com.rsgkh.calendar.engine.KhmerCalendarEngine

/** Compiling this call checks that Android can read the engine's Kotlin metadata. */
object KotlinEngineConsumer {
    @JvmStatic
    fun newYear2012(): String = KhmerCalendarEngine().newYear(2012).start.iso
}
