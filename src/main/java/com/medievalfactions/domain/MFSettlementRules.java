package com.medievalfactions.domain;

import java.util.EnumMap;
import java.util.Map;
import static com.medievalfactions.domain.MFGameCatalog.SettlementTier;

/** Default upgrade gates. Values are configuration points for balance, not hidden free progression. */
public final class MFSettlementRules {
    private static final Map<SettlementTier, Requirements> REQUIREMENTS = new EnumMap<>(SettlementTier.class);
    static {
        REQUIREMENTS.put(SettlementTier.CAMP, new Requirements(0, 1, 0, 0, 0, 0, 0L));
        REQUIREMENTS.put(SettlementTier.VILLAGE, new Requirements(10, 1, 10, 20, 10, 7, 500L));
        REQUIREMENTS.put(SettlementTier.TOWN, new Requirements(30, 2, 30, 30, 20, 7, 2_500L));
        REQUIREMENTS.put(SettlementTier.VILLA, new Requirements(80, 3, 80, 40, 30, 7, 10_000L));
        REQUIREMENTS.put(SettlementTier.CITY, new Requirements(200, 4, 200, 55, 45, 7, 50_000L));
        REQUIREMENTS.put(SettlementTier.CAPITAL, new Requirements(500, 5, 500, 70, 60, 14, 250_000L));
    }

    private MFSettlementRules() {}

    public static Requirements requirements(SettlementTier tier) { return REQUIREMENTS.get(tier); }

    public static SettlementTier next(SettlementTier current) {
        int next = current.ordinal() + 1;
        return next >= SettlementTier.values().length ? null : SettlementTier.values()[next];
    }

    public record Requirements(int minimumPopulation, int minimumHallLevel, int minimumHousing,
                               int minimumSecurity, int minimumInfrastructure, int minimumFoodDays,
                               long minimumTreasuryCopper) {}
}
