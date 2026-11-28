package com.medievalfactions.registry;

import com.medievalfactions.MedievalFactions;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class MFCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MedievalFactions.MOD_ID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = CREATIVE_TABS.register("main", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.medieval_factions.main"))
        .icon(() -> new ItemStack(MFItems.ITEMS.getEntries().stream().findFirst().orElseThrow().get()))
        .displayItems((parameters, output) -> MFItems.all().values().forEach(holder -> output.accept(holder.get())))
        .build());
    private MFCreativeTabs() {}
}
