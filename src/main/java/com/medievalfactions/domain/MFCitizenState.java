package com.medievalfactions.domain;

import java.util.UUID;
import static com.medievalfactions.domain.MFGameCatalog.*;

/** Individual citizen record used by the settlement population simulation. */
public record MFCitizenState(
        UUID id,
        UUID factionId,
        UUID settlementId,
        String name,
        RaceFamily race,
        Profession profession,
        AgeStage ageStage,
        CitizenStatus status,
        int health,
        int hunger,
        int satisfaction,
        long dailyWageCopper,
        long purseCopper,
        int unpaidDays,
        long ageDays,
        int professionLevel,
        long professionXp,
        int professionPotential) {

    public MFCitizenState {
        if (id == null || factionId == null || settlementId == null) throw new IllegalArgumentException("Citizen identifiers are required");
        if (name == null || name.isBlank() || name.length() > 48) throw new IllegalArgumentException("Invalid citizen name");
        if (race == null || profession == null || ageStage == null || status == null) throw new IllegalArgumentException("Citizen identity fields are required");
        if (ageStage != MFRaceBiology.stageAt(race, ageDays)) throw new IllegalArgumentException("Age stage does not match race and age");
        if (health < 0 || health > 100 || hunger < 0 || hunger > 100 || satisfaction < 0 || satisfaction > 100
                || dailyWageCopper < 0 || purseCopper < 0 || unpaidDays < 0 || ageDays < 0 || professionLevel < 1 || professionLevel > 50
                || professionPotential < 1 || professionPotential > 50 || professionLevel > professionPotential)
            throw new IllegalArgumentException("Invalid citizen statistics");
    }

    public static MFCitizenState create(UUID factionId, UUID settlementId, String name, RaceFamily race,
                                        Profession profession, long wageCopper, int potential, long ageDays) {
        return createWithId(UUID.randomUUID(), factionId, settlementId, name, race, profession,
                wageCopper, potential, ageDays);
    }

    /** Creates a citizen using an externally-owned identity (for example, a newborn already in the family registry). */
    public static MFCitizenState createWithId(UUID id, UUID factionId, UUID settlementId, String name, RaceFamily race,
                                              Profession profession, long wageCopper, int potential, long ageDays) {
        long safeAge = Math.max(0L, ageDays);
        return new MFCitizenState(id, factionId, settlementId, name, race, profession,
                MFRaceBiology.stageAt(race, safeAge), CitizenStatus.ACTIVE, 100, 0, 65, wageCopper, 0L, 0, safeAge, 1, 0L, potential);
    }

    /** One settlement day. Hunger and unpaid wages have real effects; no food or money is fabricated. */
    public MFCitizenState advanceDay(boolean fed, boolean paid, boolean safe) {
        return advanceDay(fed, paid ? dailyWageCopper : 0L, safe);
    }

    public boolean canReceiveWage(long wageCopper) {
        if (wageCopper < 0L) return false;
        try { Math.addExact(purseCopper, wageCopper); return true; }
        catch (ArithmeticException ex) { return false; }
    }

    public MFCitizenState advanceDay(boolean fed, long wagePaidCopper, boolean safe) {
        if (status != CitizenStatus.ACTIVE) return this;
        if (wagePaidCopper < 0L || wagePaidCopper > dailyWageCopper || !canReceiveWage(wagePaidCopper)) throw new IllegalArgumentException("Invalid wage payment");
        boolean dependentChild = ageStage == AgeStage.BABY || ageStage == AgeStage.CHILD;
        boolean paid = dailyWageCopper == 0L || dependentChild || wagePaidCopper == dailyWageCopper;
        int nextHunger = clamp(hunger + (fed ? -35 : 18));
        long nextAge = ageDays == Long.MAX_VALUE ? Long.MAX_VALUE : ageDays + 1L;
        int nextHealth = health;
        if (nextHunger >= 90) nextHealth -= 8;
        if (!safe) nextHealth -= 3;
        if (fed && nextHunger <= 25 && safe) nextHealth = Math.min(100, nextHealth + 1);
        MFRaceBiology.Profile biology = MFRaceBiology.profile(race);
        if (nextAge >= biology.lifeExpectancyDays() && nextAge % 7L == 0L) nextHealth -= 2;
        boolean reachedMaximumAge = nextAge >= biology.maximumAgeDays();
        int nextUnpaid = paid ? 0 : Math.min(3650, unpaidDays + 1);
        int nextSatisfaction = clamp(satisfaction + (fed ? 2 : -7) + (paid ? 2 : -8) + (safe ? 1 : -8));
        CitizenStatus nextStatus = nextHealth <= 0 || reachedMaximumAge ? CitizenStatus.DEAD
                : nextUnpaid >= 3 && nextSatisfaction <= 15 ? CitizenStatus.LEFT : CitizenStatus.ACTIVE;
        AgeStage nextStage = MFRaceBiology.stageAt(race, nextAge);
        if (nextStatus == CitizenStatus.DEAD) nextHealth = 0;
        return new MFCitizenState(id, factionId, settlementId, name, race, profession, nextStage, nextStatus,
                clamp(nextHealth), nextHunger, nextSatisfaction, dailyWageCopper,
                Math.addExact(purseCopper, wagePaidCopper), nextUnpaid, nextAge, professionLevel, professionXp, professionPotential);
    }

    /** Replaces health/food needs without changing identity, employment, wages or progression. */
    public MFCitizenState withHealthAndHunger(int nextHealth, int nextHunger) {
        int healthValue = Math.max(0, Math.min(100, nextHealth));
        int hungerValue = Math.max(0, Math.min(100, nextHunger));
        CitizenStatus nextStatus = healthValue == 0 ? CitizenStatus.DEAD : status;
        return new MFCitizenState(id, factionId, settlementId, name, race, profession, ageStage, nextStatus,
                healthValue, hungerValue, satisfaction, dailyWageCopper, purseCopper, unpaidDays, ageDays,
                professionLevel, professionXp, professionPotential);
    }

    public MFCitizenState awardProfessionXp(long amount) {
        if (amount <= 0L || professionLevel >= professionPotential || status != CitizenStatus.ACTIVE
                || ageStage == AgeStage.BABY || ageStage == AgeStage.CHILD) return this;
        if (ageStage == AgeStage.ADOLESCENT) amount = Math.max(1L, amount / 2L);
        long xp;
        try { xp = Math.addExact(professionXp, amount); }
        catch (ArithmeticException ex) { xp = Long.MAX_VALUE; }
        int level = professionLevel;
        while (level < professionPotential) {
            long required = 400L + 75L * level;
            if (xp < required) break;
            xp -= required;
            level++;
        }
        return new MFCitizenState(id, factionId, settlementId, name, race, profession, ageStage, status,
                health, hunger, satisfaction, dailyWageCopper, purseCopper, unpaidDays, ageDays,
                level, xp, professionPotential);
    }

    public MFCitizenState withProfessionXpLevel(int newLevel) {
        int bounded = Math.max(1, Math.min(professionPotential, Math.min(50, newLevel)));
        return new MFCitizenState(id, factionId, settlementId, name, race, profession, ageStage, status,
                health, hunger, satisfaction, dailyWageCopper, purseCopper, unpaidDays, ageDays, bounded, professionXp, professionPotential);
    }

    private static int clamp(int value) { return Math.max(0, Math.min(100, value)); }
}
