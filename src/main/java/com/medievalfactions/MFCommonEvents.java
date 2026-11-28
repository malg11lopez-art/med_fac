package com.medievalfactions;

import com.medievalfactions.registry.MFEntities;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = MedievalFactions.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class MFCommonEvents {
    private MFCommonEvents() {}
    @SubscribeEvent
    public static void registerEntityAttributes(EntityAttributeCreationEvent event) {
        AttributeSupplier.Builder guard = Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 28.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.24D).add(Attributes.ATTACK_DAMAGE, 6.0D).add(Attributes.ARMOR, 4.0D);
        AttributeSupplier.Builder horse = Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 30.0D).add(Attributes.MOVEMENT_SPEED, 0.30D);
        AttributeSupplier.Builder dragon = Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 180.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D).add(Attributes.ATTACK_DAMAGE, 18.0D).add(Attributes.ARMOR, 12.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D);
        event.put(MFEntities.GUARD.get(), guard.build());
        event.put(MFEntities.RESIDENT.get(), Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D).add(Attributes.FOLLOW_RANGE, 24.0D).build());
        event.put(MFEntities.HORSE.get(), horse.build());
        event.put(MFEntities.DRAGON.get(), dragon.build());
        event.put(MFEntities.CENTAUR.get(), Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 42.0D).add(Attributes.MOVEMENT_SPEED, 0.27D).build());
        event.put(MFEntities.HARPY.get(), Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 26.0D).add(Attributes.MOVEMENT_SPEED, 0.31D).build());
        event.put(MFEntities.MERFOLK.get(), Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 30.0D).add(Attributes.MOVEMENT_SPEED, 0.24D).build());
        event.put(MFEntities.DRACONID.get(), Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 40.0D).add(Attributes.MOVEMENT_SPEED, 0.26D).build());
        event.put(MFEntities.WOLF.get(), Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 40.0D).add(Attributes.MOVEMENT_SPEED, 0.31D).build());
        event.put(MFEntities.GRIFFIN.get(), Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 72.0D).add(Attributes.MOVEMENT_SPEED, 0.32D).build());
        event.put(MFEntities.PHOENIX.get(), Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 64.0D).add(Attributes.MOVEMENT_SPEED, 0.30D).build());
    }
}
