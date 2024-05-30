package com.bgsoftware.superiorskyblock.nms.v1_20_4.dragon;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonLandingPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockPredicate;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class IslandEndDragonFight extends EndDragonFight {

    private static final ReflectField<EndDragonFight> DRAGON_BATTLE = new ReflectField<EndDragonFight>(
            EnderDragon.class, EndDragonFight.class, Modifier.PRIVATE | Modifier.FINAL, 1)
            .removeFinal();
    private static final ReflectField<Boolean> SCAN_FOR_LEGACY_PORTALS = new ReflectField<>(
            EndDragonFight.class, boolean.class, Modifier.PRIVATE, 4);
    private static final ReflectField<Boolean> WAS_DRAGON_KILLED = new ReflectField<>(
            EndDragonFight.class, boolean.class, Modifier.PRIVATE, 1);

    private static final ReflectField<Integer> TICKS_SINCE_LAST_PLAYER_SCAN = new ReflectField<>(
            EndDragonFight.class, int.class, Modifier.PRIVATE, 4);
    private static final ReflectField<Vec3> LANDING_TARGET_POSITION = new ReflectField<>(
            DragonLandingPhase.class, Vec3.class, Modifier.PRIVATE, 1);

    private static final BlockPattern EXIT_PORTAL_PATTERN = BlockPatternBuilder.start()
            .aisle("       ", "       ", "       ", "   #   ", "       ", "       ", "       ")
            .aisle("       ", "       ", "       ", "   #   ", "       ", "       ", "       ")
            .aisle("       ", "       ", "       ", "   #   ", "       ", "       ", "       ")
            .aisle("  ###  ", " #   # ", "#     #", "#  #  #", "#     #", " #   # ", "  ###  ")
            .aisle("       ", "  ###  ", " ##### ", " ##### ", " ##### ", "  ###  ", "       ")
            .where('#', BlockInWorld.hasState(BlockPredicate.forBlock(Blocks.BEDROCK)))
            .build();

    private final Island island;
    private final BlockPos islandBlockPos;
    private final Vec3 islandBlockVectored;
    private final ServerLevel serverLevel;
    private final IslandEntityEnderDragon entityEnderDragon;

    private byte currentTick = 0;

    public IslandEndDragonFight(Island island, ServerLevel serverLevel, Location location) {
        this(island, serverLevel, new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()), null);
    }

    public IslandEndDragonFight(Island island, ServerLevel serverLevel, BlockPos islandBlockPos,
                                @Nullable IslandEntityEnderDragon islandEntityEnderDragon) {
        super(serverLevel, serverLevel.getSeed(), serverLevel.serverLevelData.endDragonFightData());

        this.island = island;
        this.islandBlockPos = islandBlockPos;
        this.islandBlockVectored = Vec3.atBottomCenterOf(islandBlockPos);
        this.serverLevel = serverLevel;
        this.entityEnderDragon = islandEntityEnderDragon == null ? spawnEnderDragon() : islandEntityEnderDragon;

        SCAN_FOR_LEGACY_PORTALS.set(this, false);
        WAS_DRAGON_KILLED.set(this, false);
        DRAGON_BATTLE.set(this.entityEnderDragon, this);

        TICKS_SINCE_LAST_PLAYER_SCAN.set(this, Integer.MIN_VALUE);
    }

    @Override
    public void tick() {
        // doServerTick

        DragonUtils.runWithPodiumPosition(this.islandBlockPos, super::tick);

        DragonPhaseInstance currentPhase = this.entityEnderDragon.getPhaseManager().getCurrentPhase();
        if (currentPhase instanceof DragonLandingPhase && !this.islandBlockVectored.equals(currentPhase.getFlyTargetLocation())) {
            LANDING_TARGET_POSITION.set(currentPhase, this.islandBlockVectored);
        }

        if (++currentTick >= 20) {
            updateBattlePlayers();
            currentTick = 0;
            TICKS_SINCE_LAST_PLAYER_SCAN.set(this, Integer.MIN_VALUE);
        }
    }

    @Nullable
    @Override
    public BlockPattern.BlockPatternMatch findExitPortal() {
        int chunkX = this.islandBlockPos.getX() >> 4;
        int chunkZ = this.islandBlockPos.getZ() >> 4;

        for (int x = -8; x <= 8; ++x) {
            for (int z = -8; z <= 8; ++z) {
                LevelChunk levelChunk = this.serverLevel.getChunk(chunkX + x, chunkZ + z);

                for (BlockEntity blockEntity : levelChunk.getBlockEntities().values()) {
                    if (blockEntity instanceof TheEndPortalBlockEntity) {
                        BlockPattern.BlockPatternMatch blockPatternMatch = EXIT_PORTAL_PATTERN.find(
                                this.serverLevel, blockEntity.getBlockPos());

                        if (blockPatternMatch != null) {
                            if (this.portalLocation == null)
                                this.portalLocation = blockPatternMatch.getBlock(3, 3, 3).getPos();

                            return blockPatternMatch;
                        }
                    }
                }
            }
        }

        int highestBlock = this.serverLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, this.islandBlockPos).getY();

        for (int y = highestBlock; y >= this.serverLevel.getMinBuildHeight(); --y) {
            BlockPos currentPosition = new BlockPos(this.islandBlockPos.getX(), y, this.islandBlockPos.getZ());

            BlockPattern.BlockPatternMatch blockPatternMatch = EXIT_PORTAL_PATTERN.find(this.serverLevel, currentPosition);

            if (blockPatternMatch != null) {
                if (this.portalLocation == null)
                    this.portalLocation = blockPatternMatch.getBlock(3, 3, 3).getPos();

                return blockPatternMatch;
            }
        }

        return null;
    }

    @Override
    public void resetSpikeCrystals() {
        DragonUtils.runWithPodiumPosition(this.islandBlockPos, super::resetSpikeCrystals);
    }

    public void removeBattlePlayers() {
        List<ServerPlayer> dragonEventPlayers = new SequentialListBuilder<ServerPlayer>()
                .build(this.dragonEvent.getPlayers());
        for (ServerPlayer serverPlayer : dragonEventPlayers)
            this.dragonEvent.removePlayer(serverPlayer);
    }

    public IslandEntityEnderDragon getEnderDragon() {
        return this.entityEnderDragon;
    }

    private void updateBattlePlayers() {
        Set<UUID> nearbyPlayers = new HashSet<>();

        for (SuperiorPlayer superiorPlayer : island.getAllPlayersInside()) {
            Player bukkitPlayer = superiorPlayer.asPlayer();
            if (bukkitPlayer != null) {
                ServerPlayer serverPlayer = ((CraftPlayer) bukkitPlayer).getHandle();
                if (serverPlayer.serverLevel().equals(this.serverLevel)) {
                    this.dragonEvent.addPlayer(serverPlayer);
                    nearbyPlayers.add(serverPlayer.getUUID());
                }
            }
        }

        List<ServerPlayer> dragonEventPlayers = new SequentialListBuilder<ServerPlayer>()
                .build(this.dragonEvent.getPlayers());

        dragonEventPlayers.stream()
                .filter(entityPlayer -> !nearbyPlayers.contains(entityPlayer.getUUID()))
                .forEach(this.dragonEvent::removePlayer);
    }

    private IslandEntityEnderDragon spawnEnderDragon() {
        IslandEntityEnderDragon entityEnderDragon = new IslandEntityEnderDragon(this.serverLevel, this.islandBlockPos);

        entityEnderDragon.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
        entityEnderDragon.absMoveTo(this.islandBlockPos.getX(), 128, this.islandBlockPos.getZ(),
                this.serverLevel.getRandom().nextFloat() * 360.0F, 0.0F);

        this.serverLevel.addFreshEntity(entityEnderDragon, CreatureSpawnEvent.SpawnReason.NATURAL);

        this.dragonUUID = entityEnderDragon.getUUID();
        this.resetSpikeCrystals();

        return entityEnderDragon;
    }

}
