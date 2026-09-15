package com.rsgkh.calendar.engine.verification;

import com.rsgkh.calendar.engine.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class EngineConsumerTest {
    @Test public void consumesThePublishedJvmApi() {
        KhmerCalendarEngine engine = new KhmerCalendarEngine();
        assertEquals("2012-04-13", EngineConsumer.newYear2012());
        LunarDate lunar = engine.fromGregorian(2026, 7, 30).getLunar();
        assertEquals(13, lunar.getMonth());
        assertEquals("2026-07-30", engine.toGregorian(lunar.getBuddhistYear(), lunar.getMonth(), lunar.getDay(), lunar.getWaxing()).getIso());
        RecurrenceRule rule = new RecurrenceRule("lent", "khmer_lunar", 7, 1, false, 0, 1, 1800, 2200, "ordinary_or_second_asadh", 1);
        assertEquals("2026-07-30", engine.evaluateRule(2026, rule, null)[0].getDate().getIso());
    }
}
