package com.medievalfactions.domain;

import java.util.UUID;

/** Persisted production order; ingredient reservation occurs at start, output only at completion. */
public record MFWorkOrderState(UUID id, UUID settlementId, UUID workerId, String recipeId,
                               int elapsedDays, Status status) {
    public enum Status { RUNNING, READY_TO_OUTPUT, COMPLETED, CANCELLED, ABORTED }
    public MFWorkOrderState {
        if (id == null || settlementId == null || workerId == null || recipeId == null
                || !recipeId.matches("[a-z0-9_.-]{2,48}") || elapsedDays < 0 || status == null)
            throw new IllegalArgumentException("Invalid work order");
        MFRecipeCatalog.recipe(recipeId).orElseThrow(() -> new IllegalArgumentException("Unknown recipe"));
        int duration = MFRecipeCatalog.recipe(recipeId).orElseThrow().durationDays();
        if (elapsedDays > duration) throw new IllegalArgumentException("Work order exceeds recipe duration");
        if ((status == Status.READY_TO_OUTPUT || status == Status.COMPLETED) && elapsedDays != duration)
            throw new IllegalArgumentException("Finished work order must have completed its duration");
        if (status == Status.CANCELLED && elapsedDays != 0)
            throw new IllegalArgumentException("Cancelled work order must be unstarted");
        if (status == Status.RUNNING && elapsedDays >= duration)
            throw new IllegalArgumentException("Running work order cannot have completed its duration");
    }
    public static MFWorkOrderState start(UUID settlementId, UUID workerId, String recipeId) {
        return new MFWorkOrderState(UUID.randomUUID(), settlementId, workerId, recipeId, 0, Status.RUNNING);
    }
    public boolean active() { return status == Status.RUNNING || status == Status.READY_TO_OUTPUT; }
    public MFWorkOrderState advance() {
        if (status != Status.RUNNING) return this;
        int next = Math.min(MFRecipeCatalog.recipe(recipeId).orElseThrow().durationDays(), elapsedDays + 1);
        return new MFWorkOrderState(id, settlementId, workerId, recipeId, next,
                next >= MFRecipeCatalog.recipe(recipeId).orElseThrow().durationDays() ? Status.READY_TO_OUTPUT : Status.RUNNING);
    }
    public MFWorkOrderState complete() {
        if (status != Status.READY_TO_OUTPUT) throw new IllegalStateException("Work order is not ready");
        return new MFWorkOrderState(id, settlementId, workerId, recipeId, elapsedDays, Status.COMPLETED);
    }
    public MFWorkOrderState cancel() {
        if (status != Status.RUNNING || elapsedDays != 0) throw new IllegalStateException("Only unstarted work can be cancelled");
        return new MFWorkOrderState(id, settlementId, workerId, recipeId, elapsedDays, Status.CANCELLED);
    }

    /** Aborts an in-progress job when its assigned citizen is no longer able to work. Reserved inputs are refunded by the world store. */
    public MFWorkOrderState abort() {
        if (status != Status.RUNNING) throw new IllegalStateException("Only a running work order can be aborted");
        return new MFWorkOrderState(id, settlementId, workerId, recipeId, elapsedDays, Status.ABORTED);
    }
}
