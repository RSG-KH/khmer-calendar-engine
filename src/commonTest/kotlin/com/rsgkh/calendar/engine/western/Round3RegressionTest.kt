package com.rsgkh.calendar.engine.western

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.math.abs

/** Run in the real repository's commonTest. Some methods intentionally fail on PROPOSAL(2).md. */
class Round3RegressionTest {
    @Test fun correctedDeltaTUsesDecimalYears() {
        assertEquals(13.72, MeeusEngine.deltaTSeconds(1800.0), 1e-9)
        assertEquals(-2.79, MeeusEngine.deltaTSeconds(1900.0), 1e-9)
        assertEquals(63.86, MeeusEngine.deltaTSeconds(2000.0), 1e-9)
        assertEquals(202.74, MeeusEngine.deltaTSeconds(2100.0), 1e-9)
        assertEquals(442.08, MeeusEngine.deltaTSeconds(2200.0), 1e-9)
    }
    @Test fun bothCoincidentPlaneConfigurationsAreNullable() {
        val eps=23.43929111
        for ((ramc,lat) in listOf(270.0 to (90.0-eps),90.0 to -(90.0-eps))) {
            val (longitude,status)=MeeusEngine.ascendant(ramc,eps,lat)
            assertNull(longitude)
            assertEquals(AscendantStatus.COINCIDENT_PLANES,status)
        }
        assertNotNull(MeeusEngine.ascendant(270.0,eps,-(90.0-eps)).first)
    }
    @Test fun publicPositionConstructorEnforcesHalfOpenRanges() {
        assertFailsWith<IllegalArgumentException> {
            ZodiacPosition(WesternZodiacSign.ARIES,-0.000001,0,0,-0.00001,0.0)
        }
        val d=ZodiacPosition.fromLongitude(29.999999)
        assertFailsWith<IllegalArgumentException> {d.copy(degreeInSign=30.0)}
        val s=ZodiacPosition.fromLongitude(59.99995/3600.0)
        assertFailsWith<IllegalArgumentException> {s.copy(second=60.0)}
    }
    @Test fun decimalOffsetRoundingCannotMoveChartAnHour() {
        val a=WesternZodiacCalculator.calculateHoroscope(2026,1,2,0,0,0.0,0.0,0.0,0.0)
        val b=WesternZodiacCalculator.calculateHoroscope(2026,1,2,1,6,0.0,1.1,0.0,0.0)
        val x=assertNotNull(a.ascendant).totalLongitude
        val y=assertNotNull(b.ascendant).totalLongitude
        val distance=abs(MeeusEngine.normalizeDegrees(x-y+180.0)-180.0)
        assertTrue(distance < 1e-6, "UTC+1.1 normalization caused $distance degrees of Ascendant change")
    }
}
