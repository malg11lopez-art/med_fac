package com.medievalfactions.domain;

import java.util.LinkedHashMap;
import java.util.Optional;

/** Atomic settlement-to-settlement goods trade; money moves and goods move, neither is generated. */
public final class MFTradeEngine {
    private MFTradeEngine() {}

    public static Optional<TradeResult> execute(MFSettlementState seller, MFSettlementState buyer,
                                                String itemId, long quantity, long unitPriceCopper) {
        if (seller == null || buyer == null || seller.id().equals(buyer.id()) || quantity <= 0 || unitPriceCopper < 0
                || itemId == null || !itemId.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) return Optional.empty();
        long totalPrice;
        try { totalPrice = Math.multiplyExact(quantity, unitPriceCopper); }
        catch (ArithmeticException ex) { return Optional.empty(); }
        long sellerStock = seller.inventory().getOrDefault(itemId, 0L);
        if (sellerStock < quantity || buyer.treasuryCopper() < totalPrice) return Optional.empty();
        long nextSellerTreasury;
        long nextBuyerTreasury;
        long buyerStock;
        try {
            nextSellerTreasury = Math.addExact(seller.treasuryCopper(), totalPrice);
            nextBuyerTreasury = sellerSafeSubtract(buyer.treasuryCopper(), totalPrice);
            buyerStock = Math.addExact(buyer.inventory().getOrDefault(itemId, 0L), quantity);
        } catch (ArithmeticException ex) { return Optional.empty(); }
        LinkedHashMap<String, Long> sellerItems = new LinkedHashMap<>(seller.inventory());
        long remaining = sellerStock - quantity;
        if (remaining == 0) sellerItems.remove(itemId); else sellerItems.put(itemId, remaining);
        LinkedHashMap<String, Long> buyerItems = new LinkedHashMap<>(buyer.inventory());
        if (quantity > 0) buyerItems.put(itemId, buyerStock);
        MFSettlementState updatedSeller = seller.withTreasury(nextSellerTreasury).withInventory(sellerItems);
        MFSettlementState updatedBuyer = buyer.withTreasury(nextBuyerTreasury).withInventory(buyerItems);
        return Optional.of(new TradeResult(updatedSeller, updatedBuyer, itemId, quantity, totalPrice));
    }

    private static long sellerSafeSubtract(long value, long amount) {
        if (amount > value) throw new ArithmeticException("insufficient funds");
        return value - amount;
    }

    public record TradeResult(MFSettlementState seller, MFSettlementState buyer, String itemId,
                              long quantity, long totalPriceCopper) {}
}
