package com.medievalfactions;

import com.medievalfactions.economy.MFCurrency;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MoneyInvariantTest {
    @Test void currencyScaleIsIntegerAndConsistent() {
        assertEquals(10L, MFCurrency.COPPER_PER_SILVER);
        assertEquals(100L, MFCurrency.COPPER_PER_GOLD);
    }

    @Test void positiveAndNegativeAdjustmentsRespectZeroFloor() {
        assertEquals(150L, MFCurrency.adjust(100L, 50L).orElseThrow());
        assertEquals(40L, MFCurrency.adjust(100L, -60L).orElseThrow());
        assertTrue(MFCurrency.adjust(10L, -11L).isEmpty());
        assertTrue(MFCurrency.adjust(-1L, 0L).isEmpty());
    }

    @Test void additionOverflowIsRejected() {
        assertTrue(MFCurrency.adjust(Long.MAX_VALUE, 1L).isEmpty());
    }

    @Test void transferIsBalancedAndAtomicByCalculation() {
        var result = MFCurrency.transfer(500L, 200L, 125L).orElseThrow();
        assertEquals(375L, result.sourceCopper());
        assertEquals(325L, result.targetCopper());
        assertEquals(700L, result.sourceCopper() + result.targetCopper());
    }

    @Test void invalidTransfersAreRejected() {
        assertTrue(MFCurrency.transfer(50L, 30L, 0L).isEmpty());
        assertTrue(MFCurrency.transfer(50L, 30L, -1L).isEmpty());
        assertTrue(MFCurrency.transfer(50L, 30L, 51L).isEmpty());
        assertTrue(MFCurrency.transfer(-1L, 30L, 1L).isEmpty());
        assertTrue(MFCurrency.transfer(50L, -1L, 1L).isEmpty());
    }

    @Test void targetOverflowRejectsWholeTransfer() {
        assertTrue(MFCurrency.transfer(50L, Long.MAX_VALUE, 1L).isEmpty());
    }
}
