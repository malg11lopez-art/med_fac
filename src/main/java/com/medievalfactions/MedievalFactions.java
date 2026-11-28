package com.medievalfactions;

import com.medievalfactions.registry.MFBlocks;
import com.medievalfactions.registry.MFBlockEntities;
import com.medievalfactions.registry.MFCreativeTabs;
import com.medievalfactions.registry.MFEntities;
import com.medievalfactions.registry.MFItems;
import com.medievalfactions.registry.MFSounds;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(MedievalFactions.MOD_ID)
public final class MedievalFactions {
    public static final String MOD_ID = "medieval_factions";

    public MedievalFactions(IEventBus modBus, ModContainer modContainer) {
        MFSounds.SOUND_EVENTS.register(modBus);
        MFBlocks.BLOCKS.register(modBus);
        MFItems.ITEMS.register(modBus);
        MFEntities.ENTITY_TYPES.register(modBus);
        MFBlockEntities.BLOCK_ENTITY_TYPES.register(modBus);
        MFCreativeTabs.CREATIVE_TABS.register(modBus);
    }
}
