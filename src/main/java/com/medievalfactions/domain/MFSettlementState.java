package com.medievalfactions.domain;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import static com.medievalfactions.domain.MFGameCatalog.SettlementTier;

/** Pure domain state of a settlement; inventory and treasury are finite and explicitly owned. */
public record MFSettlementState(
        UUID id,
        UUID factionId,
        String name,
        SettlementTier tier,
        int townHallLevel,
        int population,
        int housingCapacity,
        long foodRations,
        long treasuryCopper,
        int security,
        int infrastructure,
        long dailyMaintenanceCopper,
        Map<String, Long> inventory) {

    public MFSettlementState {
        if (id == null || factionId == null) throw new IllegalArgumentException("Settlement and faction ids are required");
        if (name == null || name.trim().length() < 3 || name.trim().length() > 32) throw new IllegalArgumentException("Settlement name must be 3-32 characters");
        name = name.trim();
        if (tier == null || townHallLevel < 1 || population < 0 || housingCapacity < 0 || foodRations < 0
                || treasuryCopper < 0 || security < 0 || security > 100 || infrastructure < 0
                || infrastructure > 100 || dailyMaintenanceCopper < 0) throw new IllegalArgumentException("Invalid settlement values");
        LinkedHashMap<String, Long> copy = new LinkedHashMap<>();
        if (inventory != null) inventory.forEach((item, amount) -> {
            if (!validItem(item) || amount == null || amount <= 0) throw new IllegalArgumentException("Invalid inventory entry");
            copy.put(item, amount);
        });
        inventory = Collections.unmodifiableMap(copy);
    }

    public static MFSettlementState found(UUID factionId, String name) {
        return new MFSettlementState(UUID.randomUUID(), factionId, name, SettlementTier.CAMP,
                1, 0, 5, 0L, 0L, 0, 0, 0L, Map.of());
    }

    public int foodDays() {
        return population == 0 ? Integer.MAX_VALUE : (int) Math.min(Integer.MAX_VALUE, foodRations / population);
    }

    public MFSettlementState withPopulation(int value) {
        return new MFSettlementState(id, factionId, name, tier, townHallLevel, value, housingCapacity,
                foodRations, treasuryCopper, security, infrastructure, dailyMaintenanceCopper, inventory);
    }

    public MFSettlementState withFoodRations(long value) {
        return new MFSettlementState(id, factionId, name, tier, townHallLevel, population, housingCapacity,
                value, treasuryCopper, security, infrastructure, dailyMaintenanceCopper, inventory);
    }

    public MFSettlementState withTreasury(long value) {
        return new MFSettlementState(id, factionId, name, tier, townHallLevel, population, housingCapacity,
                foodRations, value, security, infrastructure, dailyMaintenanceCopper, inventory);
    }

    public MFSettlementState withDailyMaintenanceCopper(long value) {
        return new MFSettlementState(id, factionId, name, tier, townHallLevel, population, housingCapacity,
                foodRations, treasuryCopper, security, infrastructure, value, inventory);
    }

    public MFSettlementState withInventory(Map<String, Long> value) {
        return new MFSettlementState(id, factionId, name, tier, townHallLevel, population, housingCapacity,
                foodRations, treasuryCopper, security, infrastructure, dailyMaintenanceCopper, value);
    }

    public MFSettlementState withHousingCapacity(int value) {
        return new MFSettlementState(id, factionId, name, tier, townHallLevel, population, value,
                foodRations, treasuryCopper, security, infrastructure, dailyMaintenanceCopper, inventory);
    }

    public MFSettlementState withTownHallLevel(int value) {
        return new MFSettlementState(id, factionId, name, tier, value, population, housingCapacity,
                foodRations, treasuryCopper, security, infrastructure, dailyMaintenanceCopper, inventory);
    }

    public MFSettlementState withSecurityAndInfrastructure(int nextSecurity, int nextInfrastructure) {
        return new MFSettlementState(id, factionId, name, tier, townHallLevel, population, housingCapacity,
                foodRations, treasuryCopper, nextSecurity, nextInfrastructure, dailyMaintenanceCopper, inventory);
    }

    public MFSettlementState upgrade(SettlementTier target) {
        if (target == null || target.ordinal() != tier.ordinal() + 1) throw new IllegalArgumentException("Only the next settlement tier may be selected");
        return new MFSettlementState(id, factionId, name, target, townHallLevel, population, housingCapacity,
                foodRations, treasuryCopper, security, infrastructure, dailyMaintenanceCopper, inventory);
    }

    public boolean canAccept(String itemId, long amount) {
        if (!validItem(itemId) || amount <= 0) return false;
        try { Math.addExact(inventory.getOrDefault(itemId, 0L), amount); return true; }
        catch (ArithmeticException ex) { return false; }
    }

    private static boolean validItem(String item) {
        return item != null && item.matches("[a-z0-9_.-]+:[a-z0-9_./-]+");
    }
}
