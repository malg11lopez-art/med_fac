package com.medievalfactions.tools;

import com.medievalfactions.network.MFPlayerSkinRules;

/** Boundary regression tests for the server-authoritative 86-entry cosmetic catalogue. */
public final class MFPlayerSkinRulesSelfTest {
    private MFPlayerSkinRulesSelfTest() {}

    public static void main(String[] args) {
        int assertions = 0;
        assertTrue(MFPlayerSkinRules.isValidIndex(0), "first skin is valid"); assertions++;
        assertTrue(MFPlayerSkinRules.isValidIndex(85), "last skin is valid"); assertions++;
        assertFalse(MFPlayerSkinRules.isValidIndex(-1), "negative skin is rejected"); assertions++;
        assertFalse(MFPlayerSkinRules.isValidIndex(86), "out-of-range skin is rejected"); assertions++;
        assertFalse(MFPlayerSkinRules.isValidIndex(Integer.MAX_VALUE), "large payload index is rejected"); assertions++;
        System.out.println("Player skin rules tests passed: " + assertions + " assertions.");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void assertFalse(boolean condition, String message) {
        assertTrue(!condition, message);
    }
}
