package com.bgsoftware.superiorskyblock.nms.v1_18_R1.dragon;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.nms.mapping.Remap;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.net.minecraft.core.BlockPosition;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.net.minecraft.server.level.BossBattleServer;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.net.minecraft.server.level.WorldServer;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.net.minecraft.world.entity.Entity;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.net.minecraft.world.level.block.entity.TileEntity;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.entity.boss.enderdragon.EntityEnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonControllerHold;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonControllerLanding;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonControllerPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.IDragonController;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.TileEntityEnderPortal;
import net.minecraft.world.level.block.state.pattern.ShapeDetector;
import net.minecraft.world.level.block.state.pattern.ShapeDetectorBlock;
import net.minecraft.world.level.block.state.pattern.ShapeDetectorBuilder;
import net.minecraft.world.level.block.state.predicate.BlockPredicate;
import net.minecraft.world.level.dimension.end.EnderDragonBattle;
import net.minecraft.world.level.levelgen.HeightMap;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;

import javax.annotation.Nullable;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class IslandEnderDragonBattle extends EnderDragonBattle {

    private static final ReflectField<EnderDragonBattle> DRAGON_BATTLE = new ReflectField<EnderDragonBattle>(
            EntityEnderDragon.class, EnderDragonBattle.class, Modifier.PRIVATE | Modifier.FINAL, 1)
            .removeFinal();
    private static final ReflectField<Boolean> SCAN_FOR_LEGACY_PORTALS = new ReflectField<>(
            EnderDragonBattle.class, boolean.class, Modifier.PRIVATE, 3);
    private static final ReflectField<Boolean> WAS_DRAGON_KILLED = new ReflectField<>(
            EnderDragonBattle.class, boolean.class, Modifier.PRIVATE, 1);
    private static final ReflectField<Vec3D> LANDING_TARGET_POSITION = new ReflectField<>(
            DragonControllerLanding.class, Vec3D.class, Modifier.PRIVATE, 1);
    @Remap(classPath = "net.minecraft.world.level.block.Blocks", name = "BEDROCK", type = Remap.Type.FIELD, remappedName = "z")
    private static final Block BEDROCK_BLOCK = Blocks.z;
    @Remap(classPath = "net.minecraft.world.level.levelgen.Heightmap$Types", name = "MOTION_BLOCKING", type = Remap.Type.FIELD, remappedName = "e")
    private static final HeightMap.Type MOTION_BLOCKING_HEIGHT_MAP = HeightMap.Type.e;
    @Remap(classPath = "net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase", name = "HOLDING_PATTERN", type = Remap.Type.FIELD, remappedName = "a")
    private static final DragonControllerPhase<DragonControllerHold> HOLDING_PATTERN_PHASE = DragonControllerPhase.a;

    @Remap(classPath = "net.minecraft.world.level.block.state.pattern.BlockPatternBuilder", name = "start", type = Remap.Type.METHOD, remappedName = "a")
    @Remap(classPath = "net.minecraft.world.level.block.state.pattern.BlockPatternBuilder", name = "aisle", type = Remap.Type.METHOD, remappedName = "a")
    @Remap(classPath = "net.minecraft.world.level.block.state.pattern.BlockPatternBuilder", name = "where", type = Remap.Type.METHOD, remappedName = "a")
    @Remap(classPath = "net.minecraft.world.level.block.state.pattern.BlockPatternBuilder", name = "build", type = Remap.Type.METHOD, remappedName = "b")
    @Remap(classPath = "net.minecraft.world.level.block.state.pattern.BlockInWorld", name = "hasState", type = Remap.Type.METHOD, remappedName = "a")
    @Remap(classPath = "net.minecraft.world.level.block.state.predicate.BlockPredicate", name = "forBlock", type = Remap.Type.METHOD, remappedName = "a")
    private static final ShapeDetector EXIT_PORTAL_PATTERN = ShapeDetectorBuilder.a()
            .a(new String[]{"       ", "       ", "       ", "   #   ", "       ", "       ", "       "})
            .a(new String[]{"       ", "       ", "       ", "   #   ", "       ", "       ", "       "})
            .a(new String[]{"       ", "       ", "       ", "   #   ", "       ", "       ", "       "})
            .a(new String[]{"  ###  ", " #   # ", "#     #", "#  #  #", "#     #", " #   # ", "  ###  "})
            .a(new String[]{"       ", "  ###  ", " ##### ", " ##### ", " ##### ", "  ###  ", "       "})
            .a('#', ShapeDetectorBlock.a(BlockPredicate.a(BEDROCK_BLOCK)))
            .b();

    private final Island island;
    private final BlockPosition islandBlockPosition;
    private final Vec3D islandBlockVectored;

    private final BossBattleServer bossBattleServer;
    private final WorldServer worldServer;
    private final IslandEntityEnderDragon entityEnderDragon;

    private byte currentTick = 0;

    public IslandEnderDragonBattle(Island island, WorldServer worldServer, Location location) {
        this(island, worldServer, new BlockPosition(location.getX(), location.getY(), location.getZ()), null);
    }

    @Remap(classPath = "net.minecraft.world.phys.Vec3", name = "atBottomCenterOf", type = Remap.Type.METHOD, remappedName = "c")
    @Remap(classPath = "net.minecraft.world.level.dimension.end.EndDragonFight", name = "dragonEvent", type = Remap.Type.FIELD, remappedName = "k")
    public IslandEnderDragonBattle(Island island, WorldServer worldServer, BlockPosition islandBlockPosition,
                                   @Nullable IslandEntityEnderDragon islandEntityEnderDragon) {
        super(worldServer.getHandle(), worldServer.getSeed(), new net.minecraft.nbt.NBTTagCompound());
        SCAN_FOR_LEGACY_PORTALS.set(this, false);
        WAS_DRAGON_KILLED.set(this, false);
        this.island = island;
        this.islandBlockPosition = islandBlockPosition;
        this.islandBlockVectored = Vec3D.c(islandBlockPosition.getHandle());
        this.bossBattleServer = new BossBattleServer(this.k);
        this.worldServer = worldServer;
        this.entityEnderDragon = islandEntityEnderDragon == null ? spawnEnderDragon() : islandEntityEnderDragon;
        DRAGON_BATTLE.set(this.entityEnderDragon, this);
    }

    @Remap(classPath = "net.minecraft.world.level.dimension.end.EndDragonFight",
            name = "tick",
            type = Remap.Type.METHOD,
            remappedName = "b")
    @Remap(classPath = "net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance",
            name = "getFlyTargetLocation",
            type = Remap.Type.METHOD,
            remappedName = "g")
    @Override
    public void b() {
        // doServerTick

        DragonUtils.runWithPodiumPosition(this.islandBlockPosition, super::b);

        IDragonController currentController = this.entityEnderDragon.getEntity().getDragonControllerManager().getDragonController();
        if (currentController instanceof DragonControllerLanding && !this.islandBlockVectored.equals(currentController.g())) {
            LANDING_TARGET_POSITION.set(currentController, this.islandBlockVectored);
        }

        if (++currentTick >= 20) {
            updateBattlePlayers();
            currentTick = 0;
        }
    }

    @Remap(classPath = "net.minecraft.world.level.dimension.end.EndDragonFight",
            name = "findExitPortal",
            type = Remap.Type.METHOD,
            remappedName = "j")
    @Remap(classPath = "net.minecraft.world.level.block.state.pattern.BlockPattern",
            name = "find",
            type = Remap.Type.METHOD,
            remappedName = "a")
    @Remap(classPath = "net.minecraft.world.level.block.state.pattern.BlockPattern$BlockPatternMatch",
            name = "getBlock",
            type = Remap.Type.METHOD,
            remappedName = "a")
    @Remap(classPath = "net.minecraft.world.level.block.state.pattern.BlockInWorld",
            name = "getPos",
            type = Remap.Type.METHOD,
            remappedName = "d")
    @Remap(classPath = "net.minecraft.world.level.dimension.end.EndDragonFight",
            name = "portalLocation",
            type = Remap.Type.FIELD,
            remappedName = "w")
    @Nullable
    @Override
    public ShapeDetector.ShapeDetectorCollection j() {
        // findExitPortal

        int chunkX = this.islandBlockPosition.getX() >> 4;
        int chunkZ = this.islandBlockPosition.getZ() >> 4;

        for (int x = -8; x <= 8; ++x) {
            for (int z = -8; z <= 8; ++z) {
                ChunkAccess chunkAccess = this.worldServer.getChunkAt(chunkX + x, chunkZ + z);

                for (TileEntity tileEntity : chunkAccess.getTileEntities().values()) {
                    if (tileEntity.getHandle() instanceof TileEntityEnderPortal) {
                        ShapeDetector.ShapeDetectorCollection shapeDetectorCollection = EXIT_PORTAL_PATTERN.a(
                                this.worldServer.getHandle(), tileEntity.getPosition().getHandle());

                        if (shapeDetectorCollection != null) {
                            if (this.w == null)
                                this.w = shapeDetectorCollection.a(3, 3, 3).d();

                            return shapeDetectorCollection;
                        }
                    }
                }
            }
        }

        int highestBlock = worldServer.getHighestBlockYAt(MOTION_BLOCKING_HEIGHT_MAP, this.islandBlockPosition).getY();
        int minHeightWorld = worldServer.getWorld().getMinHeight();

        for (int y = highestBlock; y >= minHeightWorld; --y) {
            net.minecraft.core.BlockPosition currentPosition = new net.minecraft.core.BlockPosition(
                    this.islandBlockPosition.getX(), y, this.islandBlockPosition.getZ());

            ShapeDetector.ShapeDetectorCollection shapeDetectorCollection = EXIT_PORTAL_PATTERN.a(
                    this.worldServer.getHandle(), currentPosition);

            if (shapeDetectorCollection != null) {
                if (this.w == null)
                    this.w = shapeDetectorCollection.a(3, 3, 3).d();

                return shapeDetectorCollection;
            }
        }

        return null;
    }

    @Remap(classPath = "net.minecraft.world.level.dimension.end.EndDragonFight",
            name = "resetSpikeCrystals",
            type = Remap.Type.METHOD,
            remappedName = "f")
    @Override
    public void f() {
        // resetSpikeCrystals
        resetSpikeCrystals();
    }

    public void resetSpikeCrystals() {
        DragonUtils.runWithPodiumPosition(this.islandBlockPosition, super::f);
    }

    public void removeBattlePlayers() {
        for (Entity entity : bossBattleServer.getPlayers())
            bossBattleServer.removePlayer(entity);
    }

    public IslandEntityEnderDragon getEnderDragon() {
        return this.entityEnderDragon;
    }

    private void updateBattlePlayers() {
        Set<UUID> nearbyPlayers = new HashSet<>();

        for (SuperiorPlayer superiorPlayer : island.getAllPlayersInside()) {
            Player bukkitPlayer = superiorPlayer.asPlayer();
            assert bukkitPlayer != null;

            Entity player = new Entity(((CraftPlayer) bukkitPlayer).getHandle());

            if (player.isInWorld(this.worldServer)) {
                bossBattleServer.addPlayer(player);
                nearbyPlayers.add(player.getUniqueID());
            }
        }

        bossBattleServer.getPlayers().stream()
                .filter(entityPlayer -> !nearbyPlayers.contains(entityPlayer.getUniqueID()))
                .forEach(bossBattleServer::removePlayer);
    }

    @Remap(classPath = "net.minecraft.world.level.dimension.end.EndDragonFight",
            name = "dragonUUID",
            type = Remap.Type.FIELD,
            remappedName = "u")
    private IslandEntityEnderDragon spawnEnderDragon() {
        IslandEntityEnderDragon entityEnderDragon = new IslandEntityEnderDragon(worldServer, islandBlockPosition);
        entityEnderDragon.getEntity().getDragonControllerManager().setControllerPhase(HOLDING_PATTERN_PHASE);
        entityEnderDragon.getEntity().setPositionRotation(islandBlockPosition.getX(), 128,
                islandBlockPosition.getZ(), worldServer.getRandom().nextFloat() * 360.0F, 0.0F);

        worldServer.addEntity(entityEnderDragon.getEntity(), CreatureSpawnEvent.SpawnReason.NATURAL);

        this.u = entityEnderDragon.getEntity().getUniqueID();
        this.resetSpikeCrystals();

        return entityEnderDragon;
    }

}
