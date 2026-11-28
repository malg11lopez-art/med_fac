package com.medievalfactions.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/** Server-authoritative drinkable consumables with normal Minecraft use timing and bottle returns. */
public final class MFPotionItem extends Item {
    public enum EffectKind { HEALING, FIRE_RESISTANCE }

    private final EffectKind effectKind;

    public MFPotionItem(EffectKind effectKind, Properties properties) {
        super(properties);
        this.effectKind = effectKind;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity living) {
        if (level.isClientSide) return stack;
        if (living instanceof Player player && player.getAbilities().instabuild) {
            applyEffect(living);
            return stack;
        }
        applyEffect(living);
        stack.shrink(1);
        ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
        if (stack.isEmpty()) return bottle;
        if (living instanceof Player player && !player.getInventory().add(bottle)) player.drop(bottle, false);
        return stack;
    }

    private void applyEffect(LivingEntity living) {
        switch (effectKind) {
            case HEALING -> {
                living.heal(8.0F);
                living.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 8, 1));
            }
            case FIRE_RESISTANCE -> living.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 45, 0));
        }
    }
}
