package com.medievalfactions.economy;

import java.util.Optional;
import java.util.OptionalLong;

/** Canonical money arithmetic. Copper is the storage unit; conversions never use floating point. */
public final class MFCurrency {
    public static final long COPPER_PER_SILVER = 10L;
    public static final long SILVER_PER_GOLD = 10L;
    public static final long COPPER_PER_GOLD = COPPER_PER_SILVER * SILVER_PER_GOLD;

    private MFCurrency() {}

    /** Exact physical change represented by the three registered coin denominations. */
    public record CoinBreakdown(long gold, long silver, long copper) {
        public CoinBreakdown {
            if (gold < 0L || silver < 0L || copper < 0L) throw new IllegalArgumentException("Coin counts cannot be negative");
        }
        public long totalCopper() {
            return Math.addExact(Math.addExact(Math.multiplyExact(gold, COPPER_PER_GOLD),
                    Math.multiplyExact(silver, COPPER_PER_SILVER)), copper);
        }
    }

    /** Converts a positive copper amount into the minimum canonical denominations (without rounding). */
    public static Optional<CoinBreakdown> breakdown(long amountCopper) {
        if (amountCopper < 0L) return Optional.empty();
        long gold = amountCopper / COPPER_PER_GOLD;
        long afterGold = amountCopper % COPPER_PER_GOLD;
        long silver = afterGold / COPPER_PER_SILVER;
        long copper = afterGold % COPPER_PER_SILVER;
        return Optional.of(new CoinBreakdown(gold, silver, copper));
    }

    /**
     * Selects exact change from a player's inventory. Higher denominations are consumed first,
     * but a coin is never silently overpaid or converted into fabricated change.
     */
    public static Optional<CoinBreakdown> payment(long amountCopper, long goldAvailable,
                                                   long silverAvailable, long copperAvailable) {
        if (amountCopper <= 0L || goldAvailable < 0L || silverAvailable < 0L || copperAvailable < 0L)
            return Optional.empty();
        long gold = Math.min(goldAvailable, amountCopper / COPPER_PER_GOLD);
        long remaining = amountCopper - gold * COPPER_PER_GOLD;
        long silver = Math.min(silverAvailable, remaining / COPPER_PER_SILVER);
        remaining -= silver * COPPER_PER_SILVER;
        if (copperAvailable < remaining) return Optional.empty();
        return Optional.of(new CoinBreakdown(gold, silver, remaining));
    }

    /** Applies a signed delta without allowing negative balances or integer overflow. */
    public static OptionalLong adjust(long currentCopper, long deltaCopper) {
        if (currentCopper < 0L) return OptionalLong.empty();
        try {
            long result = Math.addExact(currentCopper, deltaCopper);
            return result < 0L ? OptionalLong.empty() : OptionalLong.of(result);
        } catch (ArithmeticException ex) {
            return OptionalLong.empty();
        }
    }

    /** Calculates both sides before the caller mutates either account. */
    public static Optional<TransferResult> transfer(long sourceCopper, long targetCopper, long amountCopper) {
        if (sourceCopper < 0L || targetCopper < 0L || amountCopper <= 0L || sourceCopper < amountCopper)
            return Optional.empty();
        try {
            long newTarget = Math.addExact(targetCopper, amountCopper);
            return Optional.of(new TransferResult(sourceCopper - amountCopper, newTarget));
        } catch (ArithmeticException ex) {
            return Optional.empty();
        }
    }

    public record TransferResult(long sourceCopper, long targetCopper) {}
}
