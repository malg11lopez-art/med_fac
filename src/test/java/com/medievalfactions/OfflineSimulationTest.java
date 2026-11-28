package com.medievalfactions;

import com.medievalfactions.simulation.MFOfflineSimulation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OfflineSimulationTest {
    @Test void catchUpIsBoundedAndClockRollbackIsIgnored() {
        assertEquals(21_600L, MFOfflineSimulation.boundedElapsedSeconds(100L, 100_000L, 21_600L));
        assertEquals(MFOfflineSimulation.DEFAULT_MAX_CATCH_UP_SECONDS, MFOfflineSimulation.boundedElapsedSeconds(100L, 100_000L));
        assertEquals(0L, MFOfflineSimulation.boundedElapsedSeconds(500L, 400L, 21_600L));
    }

    @Test void stepCountIsCapped() {
        assertEquals(6L, MFOfflineSimulation.simulationSteps(100L, 10L, 6L));
        assertThrows(IllegalArgumentException.class, () -> MFOfflineSimulation.simulationSteps(-1L, 10L, 6L));
    }
}
