package com.medievalfactions.production;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Deterministic, server-agnostic production domain model.
 *
 * <p>Inputs are reserved when a job starts. Outputs are created only when the job
 * completes and the destination inventory has room. A full destination leaves the
 * job in READY_TO_OUTPUT; it never deletes the output or duplicates the inputs.</p>
 *
 * <p>This class is intentionally independent of Minecraft classes so its economic
 * invariants can be tested without launching a game. The Minecraft adapter that
 * connects it to NPCs, blocks and SavedData is a later integration layer.</p>
 */
public final class MFProductionEngine {
    public enum JobStatus { RUNNING, READY_TO_OUTPUT, COMPLETED, CANCELLED }

    private final Map<String, Inventory> inventories = new LinkedHashMap<>();
    private final Map<String, Recipe> recipes = new LinkedHashMap<>();
    private final Map<Long, Job> jobs = new LinkedHashMap<>();
    private final Map<String, Long> activeJobByWorker = new LinkedHashMap<>();
    private final AtomicLong nextJobId = new AtomicLong(1L);

    public synchronized void createInventory(String inventoryId, long capacityItems) {
        requireId(inventoryId, "inventoryId");
        if (capacityItems < 1L) throw new IllegalArgumentException("capacityItems must be positive");
        if (inventories.containsKey(inventoryId)) throw new IllegalArgumentException("Inventory already exists: " + inventoryId);
        inventories.put(inventoryId, new Inventory(capacityItems));
    }

    public synchronized void registerRecipe(Recipe recipe) {
        Objects.requireNonNull(recipe, "recipe");
        if (recipes.putIfAbsent(recipe.id(), recipe) != null)
            throw new IllegalArgumentException("Recipe already exists: " + recipe.id());
    }

    public synchronized boolean deposit(String inventoryId, String itemId, long amount) {
        Inventory inventory = inventories.get(inventoryId);
        if (inventory == null || !validItem(itemId) || amount <= 0L || !inventory.canAccept(itemId, amount)) return false;
        inventory.items.merge(itemId, amount, Math::addExact);
        return true;
    }

    public synchronized boolean withdraw(String inventoryId, String itemId, long amount) {
        Inventory inventory = inventories.get(inventoryId);
        if (inventory == null || !validItem(itemId) || amount <= 0L) return false;
        long current = inventory.items.getOrDefault(itemId, 0L);
        if (current < amount) return false;
        long remaining = current - amount;
        if (remaining == 0L) inventory.items.remove(itemId);
        else inventory.items.put(itemId, remaining);
        retryReadyOutputs();
        return true;
    }

    public synchronized Optional<JobView> startJob(String workerId, String sourceInventoryId,
                                                     String destinationInventoryId, String recipeId) {
        if (!validId(workerId)) return Optional.empty();
        Inventory source = inventories.get(sourceInventoryId);
        Inventory destination = inventories.get(destinationInventoryId);
        Recipe recipe = recipes.get(recipeId);
        if (source == null || destination == null || recipe == null || activeJobByWorker.containsKey(workerId))
            return Optional.empty();
        if (!source.contains(recipe.inputs())) return Optional.empty();

        // Reserve all inputs before creating the job; no partial reservation is possible.
        source.removeAll(recipe.inputs());
        long id = nextJobId.getAndIncrement();
        Job job = new Job(id, workerId, sourceInventoryId, destinationInventoryId, recipe);
        jobs.put(id, job);
        activeJobByWorker.put(workerId, id);
        return Optional.of(job.view());
    }

    /** Advances every active job by one simulation tick. Call from the authoritative server only. */
    public synchronized void tick() {
        for (Job job : jobs.values()) {
            if (job.status == JobStatus.RUNNING) {
                job.elapsedTicks = Math.min(job.recipe.durationTicks(), job.elapsedTicks + 1L);
                if (job.elapsedTicks >= job.recipe.durationTicks()) job.status = JobStatus.READY_TO_OUTPUT;
            }
        }
        retryReadyOutputs();
    }

    /** Only a job that has not consumed any work time can be cancelled with a full input refund. */
    public synchronized boolean cancelUnstartedJob(long jobId) {
        Job job = jobs.get(jobId);
        if (job == null || job.status != JobStatus.RUNNING || job.elapsedTicks != 0L) return false;
        Inventory source = inventories.get(job.sourceInventoryId);
        if (source == null || !source.canAcceptAll(job.recipe.inputs())) return false;
        source.addAll(job.recipe.inputs());
        job.status = JobStatus.CANCELLED;
        activeJobByWorker.remove(job.workerId);
        return true;
    }

    public synchronized Optional<JobView> job(long jobId) {
        Job job = jobs.get(jobId);
        return job == null ? Optional.empty() : Optional.of(job.view());
    }

    public synchronized List<JobView> jobs() {
        List<JobView> result = new ArrayList<>();
        for (Job job : jobs.values()) result.add(job.view());
        return Collections.unmodifiableList(result);
    }

    public synchronized InventoryView inventory(String inventoryId) {
        Inventory inventory = inventories.get(inventoryId);
        if (inventory == null) throw new IllegalArgumentException("Unknown inventory: " + inventoryId);
        return new InventoryView(inventory.capacityItems, inventory.totalItems(), Map.copyOf(inventory.items));
    }

    private void retryReadyOutputs() {
        for (Job job : jobs.values()) {
            if (job.status != JobStatus.READY_TO_OUTPUT) continue;
            Inventory destination = inventories.get(job.destinationInventoryId);
            if (destination != null && destination.canAcceptAll(job.recipe.outputs())) {
                destination.addAll(job.recipe.outputs());
                job.status = JobStatus.COMPLETED;
                activeJobByWorker.remove(job.workerId);
            }
        }
    }

    private static boolean validId(String id) {
        return id != null && !id.isBlank() && id.length() <= 128;
    }

    private static void requireId(String id, String name) {
        if (!validId(id)) throw new IllegalArgumentException(name + " must be non-empty and <= 128 chars");
    }

    private static boolean validItem(String id) {
        return validId(id) && id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+");
    }

    public record Recipe(String id, Map<String, Long> inputs, Map<String, Long> outputs, long durationTicks) {
        public Recipe {
            requireId(id, "recipe id");
            inputs = immutablePositiveItems(inputs, "inputs");
            outputs = immutablePositiveItems(outputs, "outputs");
            if (inputs.isEmpty() || outputs.isEmpty()) throw new IllegalArgumentException("Recipe needs inputs and outputs");
            if (durationTicks < 1L) throw new IllegalArgumentException("durationTicks must be positive");
        }

        private static Map<String, Long> immutablePositiveItems(Map<String, Long> values, String name) {
            Objects.requireNonNull(values, name);
            Map<String, Long> copy = new LinkedHashMap<>();
            values.forEach((item, amount) -> {
                if (!validItem(item) || amount == null || amount <= 0L)
                    throw new IllegalArgumentException("Invalid " + name + " entry: " + item);
                copy.put(item, amount);
            });
            return Collections.unmodifiableMap(copy);
        }
    }

    public record InventoryView(long capacityItems, long usedItems, Map<String, Long> items) {}
    public record JobView(long id, String workerId, String recipeId, long elapsedTicks,
                          long durationTicks, JobStatus status) {}

    private static final class Inventory {
        private final long capacityItems;
        private final Map<String, Long> items = new LinkedHashMap<>();

        private Inventory(long capacityItems) { this.capacityItems = capacityItems; }
        private long totalItems() { return items.values().stream().mapToLong(Long::longValue).sum(); }

        private boolean canAccept(String item, long amount) {
            try { return Math.addExact(totalItems(), amount) <= capacityItems
                    && Math.addExact(items.getOrDefault(item, 0L), amount) >= 0L; }
            catch (ArithmeticException ex) { return false; }
        }

        private boolean canAcceptAll(Map<String, Long> incoming) {
            long total = totalItems();
            try {
                for (Map.Entry<String, Long> entry : incoming.entrySet()) {
                    total = Math.addExact(total, entry.getValue());
                    Math.addExact(items.getOrDefault(entry.getKey(), 0L), entry.getValue());
                }
                return total <= capacityItems;
            } catch (ArithmeticException ex) { return false; }
        }

        private boolean contains(Map<String, Long> required) {
            for (Map.Entry<String, Long> entry : required.entrySet())
                if (items.getOrDefault(entry.getKey(), 0L) < entry.getValue()) return false;
            return true;
        }

        private void removeAll(Map<String, Long> amounts) {
            amounts.forEach((item, amount) -> {
                long remaining = items.get(item) - amount;
                if (remaining == 0L) items.remove(item); else items.put(item, remaining);
            });
        }

        private void addAll(Map<String, Long> amounts) {
            // Caller must call canAcceptAll first, under the engine monitor.
            amounts.forEach((item, amount) -> items.merge(item, amount, Math::addExact));
        }
    }

    private static final class Job {
        private final long id;
        private final String workerId;
        private final String sourceInventoryId;
        private final String destinationInventoryId;
        private final Recipe recipe;
        private long elapsedTicks;
        private JobStatus status = JobStatus.RUNNING;

        private Job(long id, String workerId, String sourceInventoryId, String destinationInventoryId, Recipe recipe) {
            this.id = id;
            this.workerId = workerId;
            this.sourceInventoryId = sourceInventoryId;
            this.destinationInventoryId = destinationInventoryId;
            this.recipe = recipe;
        }

        private JobView view() {
            return new JobView(id, workerId, recipe.id(), elapsedTicks, recipe.durationTicks(), status);
        }
    }
}
