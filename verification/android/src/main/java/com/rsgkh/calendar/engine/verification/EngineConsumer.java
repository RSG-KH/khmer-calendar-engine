package com.rsgkh.calendar.engine.verification;

import com.rsgkh.calendar.engine.KhmerCalendarEngine;

/** Ensures the actual JVM artifact is included in Android's compilation and DEX packaging. */
public final class EngineConsumer {
    public static String newYear2012() {
        return new KhmerCalendarEngine().newYear(2012).getStart().getIso();
    }
}
