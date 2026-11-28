package com.medievalfactions;

import com.medievalfactions.entity.MFResidentEntity;
import com.medievalfactions.registry.MFBlocks;
import com.medievalfactions.registry.MFSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Server-authoritative audio hooks for world interactions, combat and player movement. */
@EventBusSubscriber(modid = MedievalFactions.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class MFGameplayAudioEvents {
    private static final String WAS_SWORD_KEY = "mf_audio_was_sword";
    private static final String LAST_STEP_KEY = "mf_audio_last_step_tick";
    private static final String NEXT_AMBIENT_KEY = "mf_audio_next_ambient_tick";

    private MFGameplayAudioEvents() {}

    /** Called from the server tick for each connected player. */
    public static void tickPlayerAudio(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        var data = player.getPersistentData();
        long tick = level.getGameTime();

        boolean holdingSword = player.getMainHandItem().getItem() instanceof SwordItem;
        boolean wasHoldingSword = data.getBoolean(WAS_SWORD_KEY);
        if (holdingSword && !wasHoldingSword && !player.isSpectator()) {
            level.playSound(null, player.blockPosition(), MFSounds.COMBAT_SWORD_DRAW.get(),
                    SoundSource.PLAYERS, 0.62F, 0.94F + player.getRandom().nextFloat() * 0.12F);
        }
        data.putBoolean(WAS_SWORD_KEY, holdingSword);

        if (player.onGround() && !player.isPassenger() && !player.isSpectator()
                && player.getDeltaMovement().horizontalDistanceSqr() > 0.0025D
                && tick - data.getLong(LAST_STEP_KEY) >= 8L) {
            SoundEvent footstep = footstepFor(level.getBlockState(player.blockPosition().below()));
            level.playSound(null, player.blockPosition(), footstep, SoundSource.PLAYERS,
                    0.38F, 0.92F + player.getRandom().nextFloat() * 0.16F);
            data.putLong(LAST_STEP_KEY, tick);
        }

        if (tick >= data.getLong(NEXT_AMBIENT_KEY)) {
            SoundEvent ambience = ambienceFor(player);
            data.putLong(NEXT_AMBIENT_KEY, tick + 100L + player.getRandom().nextInt(81));
            if (ambience != null && !player.isSpectator()) {
                player.playNotifySound(ambience, SoundSource.AMBIENT,
                        0.48F, 0.94F + player.getRandom().nextFloat() * 0.12F);
            }
        }
    }

    private static SoundEvent footstepFor(BlockState floor) {
        if (floor.is(BlockTags.SAND)) return MFSounds.COMBAT_FOOTSTEP_SAND.get();
        if (floor.is(Blocks.SNOW) || floor.is(Blocks.SNOW_BLOCK) || floor.is(Blocks.POWDER_SNOW))
            return MFSounds.COMBAT_FOOTSTEP_SNOW.get();
        if (floor.is(BlockTags.PLANKS) || floor.is(BlockTags.LOGS)) return MFSounds.COMBAT_FOOTSTEP_WOOD.get();
        if (floor.is(BlockTags.BASE_STONE_OVERWORLD) || floor.is(BlockTags.STONE_BRICKS)
                || floor.is(Blocks.COBBLESTONE) || floor.is(Blocks.DEEPSLATE))
            return MFSounds.COMBAT_FOOTSTEP_STONE.get();
        return MFSounds.COMBAT_FOOTSTEP_GRASS.get();
    }

    /** Uses nearby world context to avoid playing ambient clips unrelated to the player's location. */
    private static SoundEvent ambienceFor(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos center = player.blockPosition();
        boolean litCampfire = false;
        boolean forge = false;
        boolean barrel = false;
        boolean brewingStand = false;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -6; x <= 6; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -6; z <= 6; z++) {
                    cursor.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                    if (!level.hasChunkAt(cursor)) continue;
                    BlockState state = level.getBlockState(cursor);
                    if (!litCampfire && (state.is(Blocks.CAMPFIRE) || state.is(Blocks.SOUL_CAMPFIRE))
                            && state.hasProperty(CampfireBlock.LIT) && state.getValue(CampfireBlock.LIT)) litCampfire = true;
                    if (!forge && state.is(MFBlocks.FORGE_CONTROLLER.get())) forge = true;
                    if (!barrel && state.is(Blocks.BARREL)) barrel = true;
                    if (!brewingStand && state.is(Blocks.BREWING_STAND)) brewingStand = true;
                    if (litCampfire && forge && barrel && brewingStand) break;
                }
                if (litCampfire && forge && barrel && brewingStand) break;
            }
            if (litCampfire && forge && barrel && brewingStand) break;
        }

        if (litCampfire) return MFSounds.AMBIENT_CAMPFIRE_LOOP.get();
        if (forge) return MFSounds.AMBIENT_FORGE_LOOP.get();
        if (barrel && brewingStand) return MFSounds.AMBIENT_TAVERN_LOOP.get();
        if (center.getY() < 48 && !level.canSeeSky(center)) return MFSounds.AMBIENT_DUNGEON_LOOP.get();
        if (level.getBiome(center).is(BiomeTags.IS_FOREST)) return MFSounds.AMBIENT_FOREST_LOOP.get();

        long time = Math.floorMod(level.getDayTime(), 24_000L);
        if (time < 12_000L && !level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(16.0D),
                entity -> entity instanceof Villager || entity instanceof MFResidentEntity).isEmpty()) {
            return MFSounds.AMBIENT_TOWN_DAY_LOOP.get();
        }
        return null;
    }

    public static void playArmorHit(ServerPlayer player) {
        if (player.getInventory().armor.stream().anyMatch(stack -> !stack.isEmpty())) {
            player.serverLevel().playSound(null, player.blockPosition(), MFSounds.COMBAT_ARMOR_HIT.get(),
                    SoundSource.PLAYERS, 0.68F, 0.92F + player.getRandom().nextFloat() * 0.16F);
        }
    }

    @SubscribeEvent
    public static void onShieldBlock(LivingShieldBlockEvent event) {
        if (event.getBlockedDamage() <= 0.0F || !(event.getEntity().level() instanceof ServerLevel level)) return;
        level.playSound(null, event.getEntity().blockPosition(), MFSounds.COMBAT_PARRY.get(),
                SoundSource.PLAYERS, 0.78F, 0.94F + event.getEntity().getRandom().nextFloat() * 0.12F);
    }

    @SubscribeEvent
    public static void onDoorUse(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!event.getLevel().getBlockState(event.getPos()).is(BlockTags.DOORS)) return;
        player.serverLevel().playSound(null, event.getPos(), MFSounds.UI_DOOR_OPEN.get(),
                SoundSource.BLOCKS, 0.72F, 0.96F + player.getRandom().nextFloat() * 0.08F);
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getCrafting().isEmpty()) return;
        player.serverLevel().playSound(null, player.blockPosition(), MFSounds.PROFESSION_CRAFT_FINISH.get(),
                SoundSource.PLAYERS, 0.68F, 0.95F + player.getRandom().nextFloat() * 0.1F);
    }

    @SubscribeEvent
    public static void onItemSmelted(PlayerEvent.ItemSmeltedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getSmelting().isEmpty()) return;
        player.serverLevel().playSound(null, player.blockPosition(), MFSounds.PROFESSION_FORGE_FIRE.get(),
                SoundSource.BLOCKS, 0.55F, 0.94F + player.getRandom().nextFloat() * 0.12F);
    }

    @SubscribeEvent
    public static void onAdvancementEarned(AdvancementEvent.AdvancementEarnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        player.playNotifySound(MFSounds.UI_QUEST_COMPLETE.get(), SoundSource.PLAYERS,
                0.76F, 0.96F + player.getRandom().nextFloat() * 0.08F);
    }
}
