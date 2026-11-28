package com.medievalfactions.domain;

import java.util.ArrayList;
import java.math.BigInteger;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SplittableRandom;
import java.util.UUID;

import static com.medievalfactions.domain.MFGameCatalog.*;

/**
 * Deterministic, dependency-free rules for the second wave of MEDIEVAL FACTIONS systems.
 * Records are immutable so they can be safely persisted by SavedData and tested without a game client.
 */
public final class MFAdvancedSystems {
    private MFAdvancedSystems() {}

    // ---------------------------------------------------------------------
    // NPC needs-driven planning
    // ---------------------------------------------------------------------
    public enum NpcIntent { NONE, SEEK_FOOD, SEEK_MEDIC, SEEK_SHELTER, CONTINUE_WORK, WORK, DEFEND, FLEE, REST, HAUL }

    public record NpcDirective(UUID citizenId, NpcIntent intent, int priority, long plannedDay, String reason) {
        public NpcDirective {
            Objects.requireNonNull(citizenId); Objects.requireNonNull(intent);
            priority = clamp(priority, 0, 100);
            if (plannedDay < 0) throw new IllegalArgumentException("plannedDay must be non-negative");
            reason = reason == null ? "" : reason.substring(0, Math.min(160, reason.length()));
        }
    }

    /** One high-level intent per NPC. Urgent survival needs always outrank production and hauling. */
    public static NpcDirective planNpc(MFCitizenState citizen, int nearbyThreat, boolean hasFood,
                                       boolean hasHome, boolean hasActiveWork, long day) {
        Objects.requireNonNull(citizen);
        if (citizen.status() != CitizenStatus.ACTIVE) return new NpcDirective(citizen.id(), NpcIntent.NONE, 0, day, "No está activo");
        if (nearbyThreat >= 80 && citizen.health() < 35)
            return new NpcDirective(citizen.id(), NpcIntent.FLEE, 100, day, "Amenaza alta y salud crítica");
        if (citizen.health() <= 35)
            return new NpcDirective(citizen.id(), NpcIntent.SEEK_MEDIC, 96, day, "Necesita atención médica");
        if (nearbyThreat >= 55 && citizen.profession() != Profession.GUARD)
            return new NpcDirective(citizen.id(), NpcIntent.SEEK_SHELTER, 92, day, "Busca refugio ante una amenaza");
        if (citizen.hunger() >= 70 && hasFood)
            return new NpcDirective(citizen.id(), NpcIntent.SEEK_FOOD, 90, day, "Necesidad de alimento");
        if (!hasHome)
            return new NpcDirective(citizen.id(), NpcIntent.SEEK_SHELTER, 82, day, "No dispone de vivienda");
        if (nearbyThreat >= 25 && citizen.profession() == Profession.GUARD)
            return new NpcDirective(citizen.id(), NpcIntent.DEFEND, 85, day, "Defiende el asentamiento");
        if (citizen.ageStage() == AgeStage.BABY || citizen.ageStage() == AgeStage.CHILD)
            return new NpcDirective(citizen.id(), NpcIntent.REST, 60, day, "Menor dependiente: no se le asigna trabajo adulto");
        if (hasActiveWork)
            return new NpcDirective(citizen.id(), NpcIntent.CONTINUE_WORK, 70, day, "Continúa su encargo actual");
        if (citizen.hunger() >= 70 && !hasFood)
            return new NpcDirective(citizen.id(), NpcIntent.HAUL, 72, day, "Busca suministros fuera del almacén");
        if (citizen.satisfaction() < 25)
            return new NpcDirective(citizen.id(), NpcIntent.REST, 45, day, "Necesita descanso y recuperación");
        return new NpcDirective(citizen.id(), NpcIntent.WORK, 40, day, "Realiza su profesión asignada");
    }

    // ---------------------------------------------------------------------
    // Finite, delayed caravan logistics
    // ---------------------------------------------------------------------
    public enum CaravanStatus { IN_TRANSIT, ARRIVED, RAIDED, CANCELLED }

    public record Caravan(UUID id, UUID sellerSettlement, UUID buyerSettlement, String itemId,
                          long quantity, long totalPriceCopper, int daysRemaining, int routeRisk,
                          CaravanStatus status, long departureDay) {
        public Caravan {
            Objects.requireNonNull(id); Objects.requireNonNull(sellerSettlement); Objects.requireNonNull(buyerSettlement);
            Objects.requireNonNull(status);
            if (sellerSettlement.equals(buyerSettlement) || !validItem(itemId) || quantity <= 0 || totalPriceCopper < 0
                    || daysRemaining < 0 || routeRisk < 0 || routeRisk > 100 || departureDay < 0)
                throw new IllegalArgumentException("Invalid caravan");
        }
        public Caravan advance() {
            if (status != CaravanStatus.IN_TRANSIT || daysRemaining == 0) return this;
            return new Caravan(id, sellerSettlement, buyerSettlement, itemId, quantity, totalPriceCopper,
                    daysRemaining - 1, routeRisk, status, departureDay);
        }
        public Caravan finish(CaravanStatus result) {
            if (status != CaravanStatus.IN_TRANSIT || (result != CaravanStatus.ARRIVED && result != CaravanStatus.RAIDED && result != CaravanStatus.CANCELLED))
                throw new IllegalStateException("Caravan cannot transition to that state");
            return new Caravan(id, sellerSettlement, buyerSettlement, itemId, quantity, totalPriceCopper,
                    daysRemaining, routeRisk, result, departureDay);
        }
    }

    public record CaravanStart(MFSettlementState seller, MFSettlementState buyer, Caravan caravan) {}
    public record CaravanArrival(MFSettlementState seller, MFSettlementState buyer, Caravan caravan) {}

    /** Reserves goods and the buyer's money when the caravan departs; there is no instant delivery. */
    public static Optional<CaravanStart> startCaravan(MFSettlementState seller, MFSettlementState buyer,
                                                        String itemId, long quantity, long unitPriceCopper,
                                                        int travelDays, int routeRisk, long currentDay) {
        if (seller == null || buyer == null || seller.id().equals(buyer.id()) || quantity <= 0 || unitPriceCopper < 0 || travelDays < 1 || currentDay < 0) return Optional.empty();
        long total;
        try { total = Math.multiplyExact(quantity, unitPriceCopper); }
        catch (ArithmeticException ex) { return Optional.empty(); }
        long stock = seller.inventory().getOrDefault(itemId, 0L);
        if (!validItem(itemId) || stock < quantity || buyer.treasuryCopper() < total) return Optional.empty();
        LinkedHashMap<String, Long> sellerStock = new LinkedHashMap<>(seller.inventory());
        long rest = stock - quantity;
        if (rest == 0) sellerStock.remove(itemId); else sellerStock.put(itemId, rest);
        MFSettlementState nextSeller = seller.withInventory(sellerStock);
        MFSettlementState nextBuyer = buyer.withTreasury(buyer.treasuryCopper() - total);
        Caravan caravan = new Caravan(UUID.randomUUID(), seller.id(), buyer.id(), itemId, quantity, total,
                travelDays, clamp(routeRisk, 0, 100), CaravanStatus.IN_TRANSIT, currentDay);
        return Optional.of(new CaravanStart(nextSeller, nextBuyer, caravan));
    }

    /** Delivers the reserved cargo and releases escrow to the seller exactly once. */
    public static Optional<CaravanArrival> arriveCaravan(Caravan caravan, MFSettlementState seller, MFSettlementState buyer) {
        if (caravan == null || seller == null || buyer == null || caravan.status() != CaravanStatus.IN_TRANSIT
                || caravan.daysRemaining() > 0 || !seller.id().equals(caravan.sellerSettlement())
                || !buyer.id().equals(caravan.buyerSettlement())) return Optional.empty();
        LinkedHashMap<String, Long> stock = new LinkedHashMap<>(buyer.inventory());
        try {
            stock.merge(caravan.itemId(), caravan.quantity(), Math::addExact);
            long sellerBalance = Math.addExact(seller.treasuryCopper(), caravan.totalPriceCopper());
            MFSettlementState nextSeller = seller.withTreasury(sellerBalance);
            MFSettlementState nextBuyer = buyer.withInventory(stock);
            return Optional.of(new CaravanArrival(nextSeller, nextBuyer, caravan.finish(CaravanStatus.ARRIVED)));
        } catch (ArithmeticException ex) { return Optional.empty(); }
    }

    /**
     * Resolves an ambush against the escrowed transaction. Surviving goods are delivered and paid for;
     * the buyer receives the unused escrow for lost cargo. No money or items are created.
     */
    public static Optional<CaravanRaid> resolveCaravanRaid(Caravan caravan, MFSettlementState seller,
                                                             MFSettlementState buyer, int lossPercent) {
        if (caravan == null || seller == null || buyer == null || caravan.status() != CaravanStatus.IN_TRANSIT
                || !seller.id().equals(caravan.sellerSettlement()) || !buyer.id().equals(caravan.buyerSettlement())
                || lossPercent < 1 || lossPercent > 100) return Optional.empty();
        long lost = Math.min(caravan.quantity(), Math.max(1L,
                BigInteger.valueOf(caravan.quantity()).multiply(BigInteger.valueOf(lossPercent)).divide(BigInteger.valueOf(100L)).longValue()));
        long delivered = caravan.quantity() - lost;
        long paid = BigInteger.valueOf(caravan.totalPriceCopper()).multiply(BigInteger.valueOf(delivered))
                .divide(BigInteger.valueOf(caravan.quantity())).longValue();
        long refund = caravan.totalPriceCopper() - paid;
        try {
            LinkedHashMap<String, Long> buyerStock = new LinkedHashMap<>(buyer.inventory());
            if (delivered > 0) buyerStock.merge(caravan.itemId(), delivered, Math::addExact);
            MFSettlementState nextBuyer = buyer.withTreasury(Math.addExact(buyer.treasuryCopper(), refund)).withInventory(buyerStock);
            MFSettlementState nextSeller = seller.withTreasury(Math.addExact(seller.treasuryCopper(), paid));
            Caravan resolved = caravan.finish(CaravanStatus.RAIDED);
            return Optional.of(new CaravanRaid(nextSeller, nextBuyer, resolved, delivered, refund));
        } catch (ArithmeticException ex) { return Optional.empty(); }
    }
    /** Deterministic loss severity for an ambush: 25–100% of the reserved cargo. */
    public static int caravanAmbushLossPercent(Caravan caravan, long day) {
        if (caravan == null || day < 0) throw new IllegalArgumentException("Caravan and non-negative day required");
        long mixed = (long) caravan.id().hashCode() * 31L + day * 0x9E3779B97F4A7C15L;
        return 25 + Math.floorMod(Long.hashCode(mixed), 76);
    }

    public record CaravanRaid(MFSettlementState seller, MFSettlementState buyer, Caravan caravan,
                              long deliveredQuantity, long refundedCopper) {}

    // ---------------------------------------------------------------------
    // Bank accounts and reserve-backed lending
    // ---------------------------------------------------------------------
    public record BankAccount(UUID id, UUID ownerId, UUID branchSettlement, long balanceCopper, boolean frozen) {
        public BankAccount {
            Objects.requireNonNull(id); Objects.requireNonNull(ownerId); Objects.requireNonNull(branchSettlement);
            if (balanceCopper < 0) throw new IllegalArgumentException("Negative bank balance");
        }
        public BankAccount withBalance(long value) { return new BankAccount(id, ownerId, branchSettlement, value, frozen); }
        public BankAccount freeze(boolean value) { return new BankAccount(id, ownerId, branchSettlement, balanceCopper, value); }
    }

    public record BankTransfer(BankAccount source, BankAccount target, long amountCopper) {}
    public record BankMovement(BankAccount account, MFSettlementState branch, long amountCopper) {}

    /** A physical cash deposit increases both the customer's claim and the branch's reserve. */
    public static Optional<BankMovement> deposit(BankAccount account, MFSettlementState branch, long amount) {
        if (!validAccountBranch(account, branch) || account.frozen() || amount <= 0) return Optional.empty();
        try { return Optional.of(new BankMovement(account.withBalance(Math.addExact(account.balanceCopper(), amount)),
                branch.withTreasury(Math.addExact(branch.treasuryCopper(), amount)), amount)); }
        catch (ArithmeticException ex) { return Optional.empty(); }
    }

    /** A cash withdrawal decreases both the customer's claim and the branch's liquid reserve. */
    public static Optional<BankMovement> withdraw(BankAccount account, MFSettlementState branch, long amount) {
        if (!validAccountBranch(account, branch) || account.frozen() || amount <= 0
                || account.balanceCopper() < amount || branch.treasuryCopper() < amount) return Optional.empty();
        return Optional.of(new BankMovement(account.withBalance(account.balanceCopper() - amount),
                branch.withTreasury(branch.treasuryCopper() - amount), amount));
    }

    /** Transfers are internal to one branch; moving between branches requires a real inter-branch settlement. */
    public static Optional<BankTransfer> transfer(BankAccount source, BankAccount target, long amount) {
        if (source == null || target == null || source.id().equals(target.id())
                || !source.branchSettlement().equals(target.branchSettlement()) || source.frozen() || target.frozen()
                || amount <= 0 || source.balanceCopper() < amount) return Optional.empty();
        try {
            return Optional.of(new BankTransfer(source.withBalance(source.balanceCopper() - amount),
                    target.withBalance(Math.addExact(target.balanceCopper(), amount)), amount));
        } catch (ArithmeticException ex) { return Optional.empty(); }
    }

    public enum LoanStatus { ACTIVE, PAID, DEFAULTED }
    public record Loan(UUID id, UUID borrowerAccount, UUID lenderSettlement, long principalCopper,
                       long totalDueCopper, long repaidCopper, int termDays, int ageDays,
                       int overdueDays, LoanStatus status) {
        /** Compatibility constructor for loans saved by v0.6.0-alpha and earlier. */
        public Loan(UUID id, UUID borrowerAccount, UUID lenderSettlement, long principalCopper,
                    long totalDueCopper, long repaidCopper, int overdueDays, LoanStatus status) {
            this(id, borrowerAccount, lenderSettlement, principalCopper, totalDueCopper, repaidCopper,
                    30, overdueDays == 0 ? 0 : 30 + overdueDays, overdueDays, status);
        }
        public Loan {
            Objects.requireNonNull(id); Objects.requireNonNull(borrowerAccount); Objects.requireNonNull(lenderSettlement); Objects.requireNonNull(status);
            // Gson supplies zero for fields absent from legacy NBT; normalize those old loans to a 30-day term.
            if (termDays == 0) termDays = 30;
            if (ageDays == 0 && overdueDays > 0) ageDays = termDays + overdueDays;
            if (principalCopper <= 0 || totalDueCopper < principalCopper || repaidCopper < 0 || repaidCopper > totalDueCopper
                    || termDays < 1 || ageDays < 0 || overdueDays < 0 || overdueDays > ageDays)
                throw new IllegalArgumentException("Invalid loan");
        }
        public long outstandingCopper() { return totalDueCopper - repaidCopper; }
        public Loan advanceDay() {
            if (status != LoanStatus.ACTIVE) return this;
            int nextAge = Math.min(36_500, ageDays + 1);
            int nextOverdue = Math.max(0, nextAge - termDays);
            LoanStatus nextStatus = nextOverdue >= 30 ? LoanStatus.DEFAULTED : LoanStatus.ACTIVE;
            return new Loan(id, borrowerAccount, lenderSettlement, principalCopper, totalDueCopper, repaidCopper,
                    termDays, nextAge, nextOverdue, nextStatus);
        }
        public Loan applyRepayment(long amount) {
            if (status != LoanStatus.ACTIVE || amount <= 0 || amount > outstandingCopper()) throw new IllegalArgumentException("Invalid loan repayment");
            long paid = repaidCopper + amount;
            return new Loan(id, borrowerAccount, lenderSettlement, principalCopper, totalDueCopper, paid,
                    termDays, ageDays, overdueDays, paid == totalDueCopper ? LoanStatus.PAID : LoanStatus.ACTIVE);
        }
        public Loan defaultLoan() { return status == LoanStatus.ACTIVE ? new Loan(id, borrowerAccount, lenderSettlement,
                principalCopper, totalDueCopper, repaidCopper, termDays, ageDays, overdueDays, LoanStatus.DEFAULTED) : this; }
    }

    public record LoanDefaultSettlement(BankAccount account, MFSettlementState branch, long recoveredCopper) {}

    /**
     * Settles a default against the borrower's deposit at the lending branch.
     * Seized deposits reduce the bank's customer liability and replenish the branch reserve equally.
     */
    public static LoanDefaultSettlement settleLoanDefault(Loan loan, BankAccount account, MFSettlementState branch) {
        if (loan == null || loan.status() != LoanStatus.DEFAULTED || account == null
                || !loan.borrowerAccount().equals(account.id()))
            throw new IllegalArgumentException("Invalid default settlement");
        long recovered = 0L;
        MFSettlementState updatedBranch = branch;
        if (!account.frozen() && branch != null && account.branchSettlement().equals(loan.lenderSettlement())
                && branch.id().equals(loan.lenderSettlement())) {
            long candidate = Math.min(account.balanceCopper(), loan.outstandingCopper());
            try {
                updatedBranch = branch.withTreasury(Math.addExact(branch.treasuryCopper(), candidate));
                recovered = candidate;
            } catch (ArithmeticException ignored) {
                // Reserve overflow means no seizure can be booked safely; freeze without changing balances.
            }
        }
        BankAccount updatedAccount = account.withBalance(account.balanceCopper() - recovered).freeze(true);
        return new LoanDefaultSettlement(updatedAccount, updatedBranch, recovered);
    }

    /** Issues a fixed-interest loan only from real branch reserves. Interest is a transfer, not new money. */
    public static Optional<LoanIssue> issueLoan(BankAccount borrower, MFSettlementState branch,
                                                  long principal, int interestBasisPoints) {
        if (!validAccountBranch(borrower, branch) || borrower.frozen() || principal <= 0
                || interestBasisPoints < 0 || interestBasisPoints > 100_000 || branch.treasuryCopper() < principal) return Optional.empty();
        long interest;
        long due;
        try {
            interest = Math.addExact(Math.multiplyExact(principal, (long) interestBasisPoints), 9_999L) / 10_000L;
            due = Math.addExact(principal, interest);
        } catch (ArithmeticException ex) { return Optional.empty(); }
        // The loan is disbursed as physical coins by the command layer. Do not credit the deposit account:
        // doing both would let the borrower withdraw the same principal a second time.
        BankAccount updatedAccount = borrower;
        MFSettlementState updatedBranch = branch.withTreasury(branch.treasuryCopper() - principal);
        Loan loan = new Loan(UUID.randomUUID(), borrower.id(), branch.id(), principal, due, 0L,
                30, 0, 0, LoanStatus.ACTIVE);
        return Optional.of(new LoanIssue(updatedAccount, updatedBranch, loan));
    }
    public record LoanIssue(BankAccount account, MFSettlementState branch, Loan loan) {}

    // ---------------------------------------------------------------------
    // Siege logistics and wall morale simulation
    // ---------------------------------------------------------------------
    public enum SiegeStatus { ACTIVE, DEFENDER_VICTORY, ATTACKER_VICTORY, WITHDRAWN }
    public record Siege(UUID id, UUID attackerFaction, UUID defenderFaction, UUID targetSettlement,
                        int attackers, int defenders, long attackerSupplies, long defenderSupplies,
                        int wallIntegrity, int morale, int days, SiegeStatus status) {
        public Siege {
            Objects.requireNonNull(id); Objects.requireNonNull(attackerFaction); Objects.requireNonNull(defenderFaction);
            Objects.requireNonNull(targetSettlement); Objects.requireNonNull(status);
            if (attackerFaction.equals(defenderFaction) || attackers < 1 || defenders < 0 || attackerSupplies < 0
                    || defenderSupplies < 0 || wallIntegrity < 0 || wallIntegrity > 100 || morale < 0 || morale > 100 || days < 0)
                throw new IllegalArgumentException("Invalid siege");
        }
        public Siege advance() {
            if (status != SiegeStatus.ACTIVE) return this;
            int nextDays = days + 1;
            long aSupplies = Math.max(0L, attackerSupplies - Math.max(1, attackers / 20));
            long dSupplies = Math.max(0L, defenderSupplies - Math.max(1, defenders / 20));
            int nextMorale = clamp(morale + (aSupplies == 0 ? -8 : 1) + (dSupplies == 0 ? -6 : 0), 0, 100);
            int wallDamage = aSupplies == 0 ? Math.max(1, attackers / 12) : Math.max(1, attackers / 30);
            int nextWall = Math.max(0, wallIntegrity - wallDamage);
            int nextDefenders = Math.max(0, defenders - (aSupplies == 0 ? 0 : Math.max(1, attackers / 80)));
            int nextAttackers = Math.max(0, attackers - (dSupplies == 0 ? Math.max(1, defenders / 90) : Math.max(0, defenders / 180)));
            SiegeStatus nextStatus = nextWall == 0 || nextDefenders == 0 ? SiegeStatus.ATTACKER_VICTORY
                    : nextAttackers == 0 || nextMorale <= 10 ? SiegeStatus.DEFENDER_VICTORY : SiegeStatus.ACTIVE;
            return new Siege(id, attackerFaction, defenderFaction, targetSettlement, nextAttackers, nextDefenders,
                    aSupplies, dSupplies, nextWall, nextMorale, nextDays, nextStatus);
        }
        public Siege withdraw() { return status != SiegeStatus.ACTIVE ? this : new Siege(id, attackerFaction, defenderFaction,
                targetSettlement, attackers, defenders, attackerSupplies, defenderSupplies, wallIntegrity, morale, days, SiegeStatus.WITHDRAWN); }
    }

    // ---------------------------------------------------------------------
    // Real skill effects, with class/element/resource/cooldown gates
    // ---------------------------------------------------------------------
    public enum AbilityEffect { DAMAGE, HEAL, FIRE_DAMAGE, ROOT }
    public enum ResourceKind { MANA, STAMINA }
    public record AbilitySpec(String id, CombatClass requiredClass, Element requiredElement,
                              AbilityEffect effect, ResourceKind resource, int cost, int power, int cooldownTicks) {
        public AbilitySpec {
            if (id == null || !id.matches("[a-z0-9_.-]+") || requiredClass == null || effect == null || resource == null
                    || cost < 0 || power < 0 || cooldownTicks < 0) throw new IllegalArgumentException("Invalid ability");
        }
    }
    public record Combatant(UUID id, CombatClass combatClass, Element element, int health, int maxHealth,
                            int mana, int maxMana, int stamina, int maxStamina, Map<String, Integer> cooldowns) {
        public Combatant {
            Objects.requireNonNull(id); Objects.requireNonNull(combatClass);
            if (health < 0 || maxHealth < 1 || health > maxHealth || mana < 0 || maxMana < 0 || mana > maxMana
                    || stamina < 0 || maxStamina < 0 || stamina > maxStamina) throw new IllegalArgumentException("Invalid combatant");
            cooldowns = cooldowns == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(cooldowns));
        }
        public int resource(ResourceKind kind) { return kind == ResourceKind.MANA ? mana : stamina; }
        public Combatant withState(int hp, int nextMana, int nextStamina, Map<String, Integer> nextCooldowns) {
            return new Combatant(id, combatClass, element, clamp(hp, 0, maxHealth), maxHealth,
                    clamp(nextMana, 0, maxMana), maxMana, clamp(nextStamina, 0, maxStamina), maxStamina, nextCooldowns);
        }
        public Combatant tickCooldowns() {
            LinkedHashMap<String, Integer> next = new LinkedHashMap<>();
            cooldowns.forEach((key, value) -> { if (value > 1) next.put(key, value - 1); });
            return withState(health, mana, stamina, next);
        }
    }
    public record AbilityCast(Combatant caster, Combatant target, AbilitySpec ability, int actualEffect) {}

    public static List<AbilitySpec> starterAbilities() {
        return List.of(
                new AbilitySpec("warrior_heavy_strike", CombatClass.WARRIOR, null, AbilityEffect.DAMAGE, ResourceKind.STAMINA, 18, 12, 100),
                new AbilitySpec("berserker_rend", CombatClass.BERSERKER, null, AbilityEffect.DAMAGE, ResourceKind.STAMINA, 22, 16, 120),
                new AbilitySpec("mage_fire_bolt", CombatClass.MAGE, Element.FIRE, AbilityEffect.FIRE_DAMAGE, ResourceKind.MANA, 20, 14, 80),
                new AbilitySpec("cleric_mend", CombatClass.CLERIC, null, AbilityEffect.HEAL, ResourceKind.MANA, 18, 14, 100),
                new AbilitySpec("druid_root_snare", CombatClass.DRUID, Element.NATURE, AbilityEffect.ROOT, ResourceKind.MANA, 15, 6, 120),
                new AbilitySpec("paladin_smite", CombatClass.PALADIN, Element.LIGHT, AbilityEffect.FIRE_DAMAGE, ResourceKind.MANA, 16, 11, 100));
    }

    public static Optional<AbilityCast> cast(Combatant caster, Combatant target, AbilitySpec ability) {
        if (caster == null || ability == null || caster.health() <= 0 || caster.combatClass() != ability.requiredClass()) return Optional.empty();
        if (ability.requiredElement() != null && caster.element() != ability.requiredElement()) return Optional.empty();
        if (caster.resource(ability.resource()) < ability.cost() || caster.cooldowns().getOrDefault(ability.id(), 0) > 0) return Optional.empty();
        if (ability.effect() != AbilityEffect.HEAL && (target == null || target.health() <= 0 || target.id().equals(caster.id()))) return Optional.empty();
        int actual;
        Combatant nextTarget = target;
        switch (ability.effect()) {
            case HEAL -> { actual = Math.min(ability.power(), caster.maxHealth() - caster.health()); }
            case DAMAGE, FIRE_DAMAGE -> { actual = Math.min(target.health(), ability.power()); nextTarget = target.withState(target.health() - actual,
                    target.mana(), target.stamina(), target.cooldowns()); }
            case ROOT -> { actual = ability.power(); }
            default -> { return Optional.empty(); }
        }
        LinkedHashMap<String, Integer> nextCooldowns = new LinkedHashMap<>(caster.cooldowns());
        nextCooldowns.put(ability.id(), ability.cooldownTicks());
        int hp = caster.health() + (ability.effect() == AbilityEffect.HEAL ? actual : 0);
        int mana = caster.mana() - (ability.resource() == ResourceKind.MANA ? ability.cost() : 0);
        int stamina = caster.stamina() - (ability.resource() == ResourceKind.STAMINA ? ability.cost() : 0);
        return Optional.of(new AbilityCast(caster.withState(hp, mana, stamina, nextCooldowns), nextTarget, ability, actual));
    }

    // ---------------------------------------------------------------------
    // Mount and dragon progression / ownership
    // ---------------------------------------------------------------------
    public enum CompanionSpecies { HORSE, DONKEY, MULE, CAMEL, GIANT_WOLF, LIZARD, GRIFFIN, NIGHTMARE, PHOENIX, ROC, DRAGON }
    public enum BondLevel { NONE, FAMILIAR, LOYAL, COMPLETE_TRUST }
    public record Companion(UUID id, UUID ownerId, CompanionSpecies species, int bond, int level,
                            long xp, int health, int maxHealth, int stamina, int maxStamina,
                            int hunger, boolean alive) {
        public Companion {
            Objects.requireNonNull(id); Objects.requireNonNull(ownerId); Objects.requireNonNull(species);
            if (bond < 0 || bond > 100 || level < 1 || level > (species == CompanionSpecies.DRAGON ? 100 : 20)
                    || xp < 0 || health < 0 || maxHealth < 1 || health > maxHealth || stamina < 0 || maxStamina < 1
                    || stamina > maxStamina || hunger < 0 || hunger > 100 || (alive && health == 0)) throw new IllegalArgumentException("Invalid companion");
        }
        public boolean dragon() { return species == CompanionSpecies.DRAGON; }
        public BondLevel bondLevel() { return bond >= 80 ? BondLevel.COMPLETE_TRUST : bond >= 55 ? BondLevel.LOYAL : bond >= 25 ? BondLevel.FAMILIAR : BondLevel.NONE; }
        public boolean canRide(UUID riderId) { return alive && ownerId.equals(riderId) && hunger < 95 && stamina >= 10 && health > 0; }
        public Companion feed(int foodValue) { if (!alive || foodValue <= 0) return this; return copy(bond, level, xp, health, stamina, Math.max(0, hunger - foodValue), alive); }
        public Companion ride(int distanceUnits) { if (!alive || distanceUnits <= 0) return this; return copy(bond, level, xp, health,
                Math.max(0, stamina - Math.max(1, distanceUnits / 10)), dragon() ? Math.min(100, hunger + distanceUnits / 30) : hunger, alive); }
        /** Rest restores stamina; it does not make a stationary companion hungry. */
        public Companion rest(int ticks) { if (!alive || ticks <= 0) return this; return copy(bond, level, xp, health,
                Math.min(maxStamina, stamina + ticks / 20), hunger, alive); }
        /** Slow passive hunger is advanced by game days, not by the 20-second server upkeep pulse. */
        public Companion advanceDailyNeeds() { if (!alive || !dragon()) return this;
            return copy(bond, level, xp, health, stamina, Math.min(100, hunger + 5), alive); }
        public Companion train(long amount) {
            if (!alive || amount <= 0) return this;
            long nextXp = safeAdd(xp, amount); int cap = dragon() ? 100 : 20; int nextLevel = level;
            while (nextLevel < cap && nextXp >= nextLevel * 250L) { nextXp -= nextLevel * 250L; nextLevel++; }
            return copy(Math.min(100, bond + 1), nextLevel, nextXp, health, stamina, hunger, alive);
        }
        public Companion heal(int amount) { return amount <= 0 || !alive ? this : copy(bond, level, xp, Math.min(maxHealth, health + amount), stamina, hunger, alive); }
        /** Synchronizes the persistent ledger with the real Minecraft entity health. */
        public Companion withEntityHealth(int entityHealth) {
            if (!alive) return this;
            if (entityHealth <= 0) return die();
            return copy(bond, level, xp, Math.min(maxHealth, entityHealth), stamina, hunger, alive);
        }
        public Companion transfer(UUID newOwner) {
            if (dragon()) throw new IllegalStateException("Los dragones no se pueden vender ni transferir");
            if (newOwner == null) throw new IllegalArgumentException("Owner required");
            return new Companion(id, newOwner, species, 0, level, xp, health, maxHealth, stamina, maxStamina, hunger, alive);
        }
        public Companion die() { return copy(bond, level, xp, 0, stamina, hunger, false); }
        private Companion copy(int b, int l, long x, int hp, int st, int hu, boolean isAlive) {
            return new Companion(id, ownerId, species, b, l, x, hp, maxHealth, st, maxStamina, hu, isAlive);
        }
    }

    // ---------------------------------------------------------------------
    // Relationships, consent, pregnancy and deterministic inheritance
    // ---------------------------------------------------------------------
    public enum Sex { FEMALE, MALE }
    public enum RelationshipStatus { COURTING, MARRIED, ENDED }
    public enum PregnancyStatus { ACTIVE, COMPLETED, LOST }
    public record Person(UUID id, UUID factionId, RaceFamily race, Affinity affinity, Element element,
                         Sex sex, long ageDays, int health, String familyName) {
        public Person {
            Objects.requireNonNull(id); Objects.requireNonNull(factionId); Objects.requireNonNull(race);
            Objects.requireNonNull(affinity); Objects.requireNonNull(element); Objects.requireNonNull(sex);
            if (element.affinity() != affinity || ageDays < 0 || health < 0 || health > 100) throw new IllegalArgumentException("Invalid person");
            familyName = familyName == null ? "" : familyName.substring(0, Math.min(32, familyName.length()));
        }
        public boolean adult() { return ageDays >= MFRaceBiology.adultAgeDays(race); }
    }
    public record Relationship(UUID id, UUID personA, UUID personB, int trust, RelationshipStatus status,
                               boolean consentA, boolean consentB, long createdDay) {
        public Relationship {
            Objects.requireNonNull(id); Objects.requireNonNull(personA); Objects.requireNonNull(personB); Objects.requireNonNull(status);
            if (personA.equals(personB) || trust < 0 || trust > 100 || createdDay < 0) throw new IllegalArgumentException("Invalid relationship");
        }
        public boolean mutuallyConsenting() { return consentA && consentB && status != RelationshipStatus.ENDED; }
    }
    public record Pregnancy(UUID id, UUID motherId, UUID fatherId, long conceptionDay, int gestationDays,
                            long seed, PregnancyStatus status) {
        public Pregnancy {
            Objects.requireNonNull(id); Objects.requireNonNull(motherId); Objects.requireNonNull(fatherId); Objects.requireNonNull(status);
            if (motherId.equals(fatherId) || conceptionDay < 0 || gestationDays < 1) throw new IllegalArgumentException("Invalid pregnancy");
        }
        public boolean due(long day) { return status == PregnancyStatus.ACTIVE && day - conceptionDay >= gestationDays; }
        public Pregnancy complete() { return new Pregnancy(id, motherId, fatherId, conceptionDay, gestationDays, seed, PregnancyStatus.COMPLETED); }
        public Pregnancy lose() { return new Pregnancy(id, motherId, fatherId, conceptionDay, gestationDays, seed, PregnancyStatus.LOST); }
    }
    public record Birth(Person child, UUID motherId, UUID fatherId, String inheritedFamilyName) {}

    public static Optional<Relationship> createRelationship(Person a, Person b, int trust, boolean consentA, boolean consentB, long day) {
        if (a == null || b == null || !a.adult() || !b.adult() || a.id().equals(b.id()) || !consentA || !consentB || day < 0) return Optional.empty();
        // Cross-family hybrids require an explicitly authored compatibility entry; this build safely permits same-family unions only.
        if (a.race() != b.race()) return Optional.empty();
        return Optional.of(new Relationship(UUID.randomUUID(), a.id(), b.id(), clamp(trust, 0, 100),
                RelationshipStatus.COURTING, true, true, day));
    }

    public static Optional<Pregnancy> conceive(Relationship relationship, Person a, Person b, long day, int fertilityRoll, long seed) {
        if (relationship == null || a == null || b == null || !relationship.mutuallyConsenting()
                || relationship.status() == RelationshipStatus.ENDED || relationship.trust() < 40
                || !a.adult() || !b.adult() || day < relationship.createdDay() || fertilityRoll < 0 || fertilityRoll > 99
                || a.race() != b.race() || a.health() < 30 || b.health() < 30) return Optional.empty();
        Person mother = a.sex() == Sex.FEMALE ? a : b.sex() == Sex.FEMALE ? b : null;
        Person father = a.sex() == Sex.MALE ? a : b.sex() == Sex.MALE ? b : null;
        if (mother == null || father == null || fertilityRoll >= 45) return Optional.empty();
        int gestation = Math.max(120, (int) (MFRaceBiology.adultAgeDays(mother.race()) / 20L));
        return Optional.of(new Pregnancy(UUID.randomUUID(), mother.id(), father.id(), day, gestation, seed, PregnancyStatus.ACTIVE));
    }

    /** Creates exactly one child at term; affinity follows the parents' inheritance rule and element is rolled within it. */
    public static Optional<Birth> giveBirth(Pregnancy pregnancy, Person mother, Person father, long day) {
        if (pregnancy == null || mother == null || father == null || !pregnancy.due(day)
                || mother.health() < 20 || !mother.id().equals(pregnancy.motherId()) || !father.id().equals(pregnancy.fatherId())) return Optional.empty();
        SplittableRandom random = new SplittableRandom(pregnancy.seed());
        Affinity affinity = mother.affinity() == father.affinity() ? mother.affinity()
                : (random.nextBoolean() ? Affinity.SOLAR : Affinity.LUNAR);
        List<Element> allowed = elementsFor(affinity);
        Element element = allowed.get(random.nextInt(allowed.size()));
        Sex sex = random.nextBoolean() ? Sex.FEMALE : Sex.MALE;
        String surname = mother.familyName().isBlank() ? father.familyName() : mother.familyName();
        Person child = new Person(UUID.randomUUID(), mother.factionId(), mother.race(), affinity, element, sex, 0L, 100, surname);
        return Optional.of(new Birth(child, mother.id(), father.id(), surname));
    }

    // ---------------------------------------------------------------------
    // Deterministic dungeon generation and autonomous expedition outcomes
    // ---------------------------------------------------------------------
    public enum RoomKind { ENTRANCE, CORRIDOR, TREASURE, MONSTER, PUZZLE, SHRINE, BOSS }
    public record DungeonRoom(int index, RoomKind kind, int width, int height, int depth, int encounterPower) {}
    public record DungeonLayout(long seed, int tier, List<DungeonRoom> rooms, int totalEncounterPower, String bossId) {
        public DungeonLayout { rooms = List.copyOf(rooms); }
    }
    public enum DungeonBuildStatus { QUEUED, BUILDING, COMPLETED, BLOCKED }
    public record DungeonProject(UUID id, long seed, int tier, String dimension,
                                 int originX, int originY, int originZ, int nextPlacement,
                                 DungeonBuildStatus status) {
        public DungeonProject {
            Objects.requireNonNull(id); Objects.requireNonNull(status);
            if (tier < 1 || tier > 10 || dimension == null || dimension.isBlank() || dimension.length() > 128
                    || nextPlacement < 0) throw new IllegalArgumentException("Invalid dungeon project");
        }
        public DungeonProject advance(int amount, int total) {
            int next = Math.min(total, nextPlacement + Math.max(0, amount));
            return new DungeonProject(id, seed, tier, dimension, originX, originY, originZ, next,
                    next >= total ? DungeonBuildStatus.COMPLETED : DungeonBuildStatus.BUILDING);
        }
        public DungeonProject blocked() {
            return new DungeonProject(id, seed, tier, dimension, originX, originY, originZ, nextPlacement, DungeonBuildStatus.BLOCKED);
        }
    }
    public enum ExpeditionStatus { TRAVELLING, EXPLORING, RETURNING, COMPLETED, FAILED }
    public record Expedition(UUID id, UUID factionId, UUID settlementId, List<UUID> memberIds,
                             long dungeonSeed, int dungeonTier, int daysRemaining, long provisions, int partyPower,
                             ExpeditionStatus status, String rewardItem, long rewardAmount) {
        public Expedition {
            Objects.requireNonNull(id); Objects.requireNonNull(factionId); Objects.requireNonNull(settlementId); Objects.requireNonNull(status);
            memberIds = List.copyOf(memberIds);
            if (memberIds.isEmpty() || memberIds.size() > 8 || new HashSet<>(memberIds).size() != memberIds.size()
                    || memberIds.stream().anyMatch(Objects::isNull) || daysRemaining < 0 || provisions < 0 || partyPower < 0 || rewardAmount < 0)
                throw new IllegalArgumentException("Invalid expedition");
            if (rewardItem != null && !validItem(rewardItem)) throw new IllegalArgumentException("Invalid reward item");
        }
        public Expedition advance() {
            if (status == ExpeditionStatus.COMPLETED || status == ExpeditionStatus.FAILED || status == ExpeditionStatus.RETURNING) return this;
            if (provisions <= 0) return new Expedition(id, factionId, settlementId, memberIds, dungeonSeed, dungeonTier, daysRemaining,
                    0, partyPower, ExpeditionStatus.FAILED, null, 0);
            int nextDays = Math.max(0, daysRemaining - 1);
            long nextFood = Math.max(0L, provisions - Math.max(1, memberIds.size()));
            ExpeditionStatus nextStatus = nextDays == 0 ? ExpeditionStatus.RETURNING : ExpeditionStatus.EXPLORING;
            return new Expedition(id, factionId, settlementId, memberIds, dungeonSeed, dungeonTier, nextDays, nextFood,
                    partyPower, nextStatus, rewardItem, rewardAmount);
        }
    }
    public record ExpeditionResolution(Expedition expedition, String report) {}

    public static DungeonLayout generateDungeon(long seed, int tier) {
        int safeTier = clamp(tier, 1, 10);
        SplittableRandom random = new SplittableRandom(seed);
        int count = 5 + safeTier * 2;
        List<DungeonRoom> rooms = new ArrayList<>(count);
        int totalPower = 0;
        for (int i = 0; i < count; i++) {
            RoomKind kind = i == 0 ? RoomKind.ENTRANCE : i == count - 1 ? RoomKind.BOSS
                    : switch (random.nextInt(5)) {
                        case 0 -> RoomKind.CORRIDOR; case 1 -> RoomKind.TREASURE; case 2 -> RoomKind.MONSTER;
                        case 3 -> RoomKind.PUZZLE; default -> RoomKind.SHRINE;
                    };
            int power = kind == RoomKind.MONSTER ? safeTier * (4 + random.nextInt(8))
                    : kind == RoomKind.BOSS ? safeTier * (15 + random.nextInt(12)) : 0;
            totalPower += power;
            rooms.add(new DungeonRoom(i, kind, 7 + random.nextInt(8), 7 + random.nextInt(8), i, power));
        }
        String boss = "ancient_guardian_t" + safeTier;
        return new DungeonLayout(seed, safeTier, rooms, totalPower, boss);
    }

    /** Builds each seeded dungeon as a chain of sealed stone rooms with a walkable connector. */
    public static List<BlockPlacement> dungeonWorldPlacements(long seed, int tier, int originX, int originY, int originZ) {
        DungeonLayout layout = generateDungeon(seed, tier);
        LinkedHashMap<String, BlockPlacement> unique = new LinkedHashMap<>();
        int cursorX = 0;
        for (DungeonRoom room : layout.rooms()) {
            int width = room.width();
            int depth = Math.max(7, room.height());
            int roomMinX = cursorX;
            int roomMinZ = -depth / 2;
            int floorY = 0;
            for (int x = 0; x < width; x++) for (int z = 0; z < depth; z++) {
                addDungeonPlacement(unique, originX + roomMinX + x, originY + floorY, originZ + roomMinZ + z, "minecraft:stone_bricks");
                addDungeonPlacement(unique, originX + roomMinX + x, originY + 5, originZ + roomMinZ + z, "minecraft:stone_bricks");
                for (int y = 1; y <= 4; y++) {
                    boolean edge = x == 0 || x == width - 1 || z == 0 || z == depth - 1;
                    addDungeonPlacement(unique, originX + roomMinX + x, originY + y, originZ + roomMinZ + z,
                            edge ? "minecraft:stone_bricks" : "minecraft:air");
                }
            }
            // Center lights and a visible room marker make rooms navigable even before custom loot tables exist.
            addDungeonPlacement(unique, originX + roomMinX + width / 2, originY + 4, originZ, "minecraft:lantern");
            if (room.kind() == RoomKind.TREASURE) {
                addDungeonPlacement(unique, originX + roomMinX + width / 2, originY + 1, originZ, "minecraft:chest");
            } else if (room.kind() == RoomKind.BOSS) {
                addDungeonPlacement(unique, originX + roomMinX + width / 2, originY + 1, originZ, "minecraft:polished_blackstone_bricks");
            }
            if (room.index() < layout.rooms().size() - 1) {
                int corridorStart = roomMinX + width;
                for (int x = 0; x < 4; x++) for (int z = -1; z <= 1; z++) {
                    addDungeonPlacement(unique, originX + corridorStart + x, originY, originZ + z, "minecraft:stone_bricks");
                    for (int y = 1; y <= 3; y++) addDungeonPlacement(unique, originX + corridorStart + x, originY + y, originZ + z, "minecraft:air");
                    addDungeonPlacement(unique, originX + corridorStart + x, originY + 4, originZ + z, "minecraft:stone_bricks");
                }
            }
            cursorX += width + 4;
        }
        return List.copyOf(unique.values());
    }

    private static void addDungeonPlacement(Map<String, BlockPlacement> out, int x, int y, int z, String blockId) {
        String key = x + ":" + y + ":" + z;
        out.put(key, new BlockPlacement(x, y, z, blockId, blockId));
    }

    /** Validates that every expedition participant is a unique, active resident of the sending settlement. */
    public static boolean validExpeditionRoster(UUID factionId, UUID settlementId, List<UUID> memberIds,
                                                List<MFCitizenState> availableCitizens) {
        if (factionId == null || settlementId == null || memberIds == null || availableCitizens == null
                || memberIds.isEmpty() || memberIds.size() > 8
                || memberIds.stream().anyMatch(Objects::isNull)
                || new HashSet<>(memberIds).size() != memberIds.size()) return false;
        Map<UUID, MFCitizenState> byId = new LinkedHashMap<>();
        for (MFCitizenState citizen : availableCitizens) {
            if (citizen != null) byId.put(citizen.id(), citizen);
        }
        for (UUID id : memberIds) {
            MFCitizenState citizen = byId.get(id);
            if (citizen == null || citizen.status() != CitizenStatus.ACTIVE
                    || !citizen.factionId().equals(factionId) || !citizen.settlementId().equals(settlementId)
                    || citizen.ageStage() == AgeStage.BABY || citizen.ageStage() == AgeStage.CHILD
                    || citizen.health() <= 0) return false;
        }
        return true;
    }

    public static Expedition startExpedition(UUID factionId, UUID settlementId, List<UUID> members,
                                             long seed, int tier, int days, long provisions, int partyPower) {
        if (factionId == null || settlementId == null || members == null || members.isEmpty() || members.size() > 8
                || new HashSet<>(members).size() != members.size() || members.stream().anyMatch(Objects::isNull)
                || tier < 1 || tier > 10 || days < 1 || days > 365 || provisions <= 0 || partyPower < 0)
            throw new IllegalArgumentException("Invalid expedition departure");
        return new Expedition(UUID.randomUUID(), factionId, settlementId, members, seed, tier, days, provisions,
                partyPower, ExpeditionStatus.TRAVELLING, null, 0);
    }

    /** Resolves a returning party against the generated dungeon power; weak or undersupplied parties can fail. */
    public static ExpeditionResolution resolveExpedition(Expedition expedition, int tier) {
        if (expedition == null || expedition.status() != ExpeditionStatus.RETURNING) throw new IllegalArgumentException("Expedition is not returning");
        DungeonLayout dungeon = generateDungeon(expedition.dungeonSeed(), expedition.dungeonTier());
        long expectedSupplies = Math.max(1, expedition.memberIds().size()) * 2L;
        boolean success = expedition.partyPower() >= dungeon.totalEncounterPower()
                && expedition.provisions() >= expectedSupplies;
        if (!success) {
            Expedition failed = new Expedition(expedition.id(), expedition.factionId(), expedition.settlementId(), expedition.memberIds(),
                    expedition.dungeonSeed(), expedition.dungeonTier(), 0, expedition.provisions(), expedition.partyPower(), ExpeditionStatus.FAILED, null, 0);
            return new ExpeditionResolution(failed, "La expedición fracasó ante " + dungeon.bossId() + ". Sin botín generado.");
        }
        String reward = expedition.dungeonTier() >= 7 ? "medieval_factions:ancient_relic" : expedition.dungeonTier() >= 4 ? "medieval_factions:arcane_shard" : "minecraft:gold_ingot";
        long amount = expedition.dungeonTier() >= 7 ? 1 : expedition.dungeonTier() >= 4 ? 2 : 1L + expedition.dungeonTier();
        Expedition complete = new Expedition(expedition.id(), expedition.factionId(), expedition.settlementId(), expedition.memberIds(),
                expedition.dungeonSeed(), expedition.dungeonTier(), 0, expedition.provisions(), expedition.partyPower(), ExpeditionStatus.COMPLETED, reward, amount);
        return new ExpeditionResolution(complete, "Victoria contra " + dungeon.bossId() + ". Recompensa: " + amount + " x " + reward);
    }

    // ---------------------------------------------------------------------
    // Validated physical construction blueprints
    // ---------------------------------------------------------------------
    public record BlockPlacement(int x, int y, int z, String blockId, String costItemId) {
        public BlockPlacement {
            if (Math.abs((long) x) > 30_000_000L || Math.abs((long) y) > 2_048L || Math.abs((long) z) > 30_000_000L || !validItem(blockId) || !validItem(costItemId))
                throw new IllegalArgumentException("Invalid block placement");
        }
    }
    public record Blueprint(String id, int minSettlementLevel, Map<String, Long> materialCosts,
                            long copperCost, List<BlockPlacement> placements) {
        public Blueprint {
            if (id == null || !id.matches("[a-z0-9_.-]+") || minSettlementLevel < 0 || copperCost < 0 || placements == null || placements.isEmpty())
                throw new IllegalArgumentException("Invalid blueprint");
            materialCosts = Collections.unmodifiableMap(new LinkedHashMap<>(materialCosts));
            placements = List.copyOf(placements);
            materialCosts.forEach((item, amount) -> { if (!validItem(item) || amount == null || amount <= 0) throw new IllegalArgumentException("Invalid material cost"); });
        }
    }
    public enum ConstructionStatus { FUNDED, BUILDING, COMPLETED, CANCELLED }
    public record ConstructionProject(UUID id, UUID settlementId, UUID ownerId, String dimension,
                                      int originX, int originY, int originZ, int rotation, String blueprintId,
                                      int nextPlacement, ConstructionStatus status, long startedDay) {
        public ConstructionProject {
            Objects.requireNonNull(id); Objects.requireNonNull(settlementId); Objects.requireNonNull(ownerId);
            Objects.requireNonNull(status);
            if (dimension == null || dimension.isBlank() || dimension.length() > 128 || blueprintId == null
                    || rotation < 0 || rotation > 3 || nextPlacement < 0 || startedDay < 0) throw new IllegalArgumentException("Invalid construction project");
        }
        public ConstructionProject advance(int amount, int total) {
            if (status == ConstructionStatus.COMPLETED || status == ConstructionStatus.CANCELLED || amount <= 0) return this;
            int next = Math.min(total, nextPlacement + amount);
            return new ConstructionProject(id, settlementId, ownerId, dimension, originX, originY, originZ, rotation,
                    blueprintId, next, next == total ? ConstructionStatus.COMPLETED : ConstructionStatus.BUILDING, startedDay);
        }
    }

    public static Map<String, Blueprint> starterBlueprints() {
        LinkedHashMap<String, Blueprint> result = new LinkedHashMap<>();
        result.put("watchtower", watchtowerBlueprint());
        result.put("small_house", houseBlueprint());
        return Collections.unmodifiableMap(result);
    }

    private static Blueprint watchtowerBlueprint() {
        List<BlockPlacement> blocks = new ArrayList<>();
        String stone = "minecraft:stone_bricks";
        for (int y = 0; y <= 4; y++) {
            for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) {
                boolean edge = Math.abs(x) == 2 || Math.abs(z) == 2;
                if ((y == 0 || y == 4) ? true : edge) blocks.add(new BlockPlacement(x, y, z, stone, stone));
            }
        }
        for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) blocks.add(new BlockPlacement(x, 5, z, "minecraft:oak_planks", "minecraft:oak_planks"));
        blocks.add(new BlockPlacement(0, 6, 0, "minecraft:torch", "minecraft:torch"));
        Map<String, Long> cost = Map.of(stone, (long) blocks.stream().filter(b -> b.costItemId().equals(stone)).count(),
                "minecraft:oak_planks", 9L, "minecraft:torch", 1L);
        return new Blueprint("watchtower", 0, cost, 40L, blocks);
    }

    private static Blueprint houseBlueprint() {
        List<BlockPlacement> blocks = new ArrayList<>();
        for (int y = 0; y <= 3; y++) for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) {
            boolean edge = Math.abs(x) == 2 || Math.abs(z) == 2;
            if (y == 0 || y == 3 || edge) {
                if (!(y == 1 || y == 2) || x != 0 || z != -2)
                    blocks.add(new BlockPlacement(x, y, z, "minecraft:oak_planks", "minecraft:oak_planks"));
            }
        }
        blocks.add(new BlockPlacement(0, 1, -2, "minecraft:oak_fence_gate", "minecraft:oak_fence_gate"));
        blocks.add(new BlockPlacement(0, 2, -2, "minecraft:oak_planks", "minecraft:oak_planks"));
        return new Blueprint("small_house", 0, Map.of("minecraft:oak_planks", 81L, "minecraft:oak_fence_gate", 1L), 25L, blocks);
    }

    public static List<BlockPlacement> worldPlacements(Blueprint blueprint, int originX, int originY, int originZ, int rotation) {
        if (blueprint == null || rotation < 0 || rotation > 3) throw new IllegalArgumentException("Invalid blueprint rotation");
        List<BlockPlacement> out = new ArrayList<>(blueprint.placements().size());
        for (BlockPlacement b : blueprint.placements()) {
            int x, z;
            switch (rotation) {
                case 0 -> { x = b.x(); z = b.z(); }
                case 1 -> { x = -b.z(); z = b.x(); }
                case 2 -> { x = -b.x(); z = -b.z(); }
                default -> { x = b.z(); z = -b.x(); }
            }
            out.add(new BlockPlacement(originX + x, originY + b.y(), originZ + z, b.blockId(), b.costItemId()));
        }
        return List.copyOf(out);
    }

    // ---------------------------------------------------------------------
    // Shared helpers
    // ---------------------------------------------------------------------
    private static boolean validAccountBranch(BankAccount account, MFSettlementState branch) {
        return account != null && branch != null && account.branchSettlement().equals(branch.id());
    }
    private static boolean validItem(String item) { return item != null && item.matches("[a-z0-9_.-]+:[a-z0-9_./-]+"); }
    private static int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }
    private static long safeAdd(long a, long b) { try { return Math.addExact(a, b); } catch (ArithmeticException ex) { return Long.MAX_VALUE; } }
}
