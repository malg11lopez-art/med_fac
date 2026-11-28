package com.medievalfactions;

import com.medievalfactions.production.MFProductionEngine;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class ProductionEngineInvariantTest {
    @Test void reservesInputsAndProducesOnlyAfterDuration() {
        var engine = new MFProductionEngine();
        engine.createInventory("ore", 10);
        engine.createInventory("output", 10);
        engine.registerRecipe(new MFProductionEngine.Recipe("smelt",
                Map.of("medieval_factions:sunsteel_fragment", 1L),
                Map.of("medieval_factions:sunsteel_ingot", 1L), 2));
        assertTrue(engine.deposit("ore", "medieval_factions:sunsteel_fragment", 1));
        var job = engine.startJob("worker", "ore", "output", "smelt").orElseThrow();
        assertFalse(engine.startJob("worker", "ore", "output", "smelt").isPresent());
        engine.tick();
        assertEquals(MFProductionEngine.JobStatus.RUNNING, engine.job(job.id()).orElseThrow().status());
        engine.tick();
        assertEquals(MFProductionEngine.JobStatus.COMPLETED, engine.job(job.id()).orElseThrow().status());
        assertEquals(1L, engine.inventory("output").items().get("medieval_factions:sunsteel_ingot"));
    }

    @Test void fullOutputStorePausesDeliveryWithoutLosingProducts() {
        var engine = new MFProductionEngine();
        engine.createInventory("ore", 10);
        engine.createInventory("output", 1);
        engine.registerRecipe(new MFProductionEngine.Recipe("smelt",
                Map.of("minecraft:iron_ore", 1L), Map.of("minecraft:iron_ingot", 1L), 1));
        assertTrue(engine.deposit("ore", "minecraft:iron_ore", 1));
        assertTrue(engine.deposit("output", "minecraft:stone", 1));
        var job = engine.startJob("worker", "ore", "output", "smelt").orElseThrow();
        engine.tick();
        assertEquals(MFProductionEngine.JobStatus.READY_TO_OUTPUT, engine.job(job.id()).orElseThrow().status());
        assertTrue(engine.withdraw("output", "minecraft:stone", 1));
        assertEquals(MFProductionEngine.JobStatus.COMPLETED, engine.job(job.id()).orElseThrow().status());
        assertEquals(1L, engine.inventory("output").items().get("minecraft:iron_ingot"));
    }

    @Test void cancellingAnUnstartedJobRefundsInputs() {
        var engine = new MFProductionEngine();
        engine.createInventory("ore", 4);
        engine.createInventory("output", 4);
        engine.registerRecipe(new MFProductionEngine.Recipe("smelt",
                Map.of("minecraft:iron_ore", 2L), Map.of("minecraft:iron_ingot", 1L), 20));
        assertTrue(engine.deposit("ore", "minecraft:iron_ore", 2));
        long id = engine.startJob("worker", "ore", "output", "smelt").orElseThrow().id();
        assertTrue(engine.cancelUnstartedJob(id));
        assertEquals(2L, engine.inventory("ore").items().get("minecraft:iron_ore"));
    }
}
