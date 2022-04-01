package com.bgsoftware.superiorskyblock.nms.v1_18_R2.dragon;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.BlockPosition;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.level.BossBattleServer;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.level.WorldServer;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.level.block.entity.TileEntity;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.level.chunk.ChunkAccess;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EntityEnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonControllerPhase;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.TileEntityEnderPortal;
import net.minecraft.world.level.block.state.pattern.ShapeDetector;
import net.minecraft.world.level.block.state.pattern.ShapeDetectorBlock;
import net.minecraft.world.level.block.state.pattern.ShapeDetectorBuilder;
import net.minecraft.world.level.block.state.predicate.BlockPredicate;
import net.minecraft.world.level.dimension.end.EnderDragonBattle;
import net.minecraft.world.level.levelgen.HeightMap;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
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

    private static final ShapeDetector EXIT_PORTAL_PATTERN = ShapeDetectorBuilder.a()
            .a(new String[]{"       ", "       ", "       ", "   #   ", "       ", "       ", "       "})
            .a(new String[]{"       ", "       ", "       ", "   #   ", "       ", "       ", "       "})
            .a(new String[]{"       ", "       ", "       ", "   #   ", "       ", "       ", "       "})
            .a(new String[]{"  ###  ", " #   # ", "#     #", "#  #  #", "#     #", " #   # ", "  ###  "})
            .a(new String[]{"       ", "  ###  ", " ##### ", " ##### ", " ##### ", "  ###  ", "       "})
            .a('#', ShapeDetectorBlock.a(BlockPredicate.a(Blocks.z)))
            .b();

    private final Island island;
    private final BlockPosition islandBlockPosition;

    private final BossBattleServer bossBattleServer;
    private final WorldServer worldServer;
    private final IslandEntityEnderDragon entityEnderDragon;

    private byte currentTick = 0;

    public IslandEnderDragonBattle(Island island, WorldServer worldServer, Location location) {
        super(worldServer.getHandle(), worldServer.getSeed(), new net.minecraft.nbt.NBTTagCompound());
        this.island = island;
        this.islandBlockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());
        this.bossBattleServer = new BossBattleServer(this.k);
        this.worldServer = worldServer;
        this.entityEnderDragon = spawnEnderDragon();
    }

    @Override
    public void b() {
        // doServerTick

        DragonUtils.runWithPodiumPosition(this.islandBlockPosition, super::b);
        if (++currentTick >= 20) {
            updateBattlePlayers();
            currentTick = 0;
        }
    }

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

        int highestBlock = worldServer.getHighestBlockYAt(HeightMap.Type.e, this.islandBlockPosition).getY();
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

    @Override
    public void f() {
        // resetSpikeCrystals

        DragonUtils.runWithPodiumPosition(this.islandBlockPosition, super::f);
    }

    public void removeBattlePlayers() {
        for (Entity entity : bossBattleServer.getPlayers())
            bossBattleServer.removePlayer(entity);
    }

    public void killEnderDragon() {
        this.entityEnderDragon.getBukkitEntity().remove();
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

    private IslandEntityEnderDragon spawnEnderDragon() {
        IslandEntityEnderDragon entityEnderDragon = new IslandEntityEnderDragon(worldServer, islandBlockPosition);
        entityEnderDragon.getEntity().getDragonControllerManager().setControllerPhase(DragonControllerPhase.a);
        entityEnderDragon.getEntity().setPositionRotation(islandBlockPosition.getX(), 128,
                islandBlockPosition.getZ(), worldServer.getRandom().nextFloat() * 360.0F, 0.0F);
        DRAGON_BATTLE.set(entityEnderDragon, this);

        worldServer.addEntity(entityEnderDragon.getEntity(), CreatureSpawnEvent.SpawnReason.NATURAL);

        this.u = entityEnderDragon.getEntity().getUniqueID();
        this.f(); // scan for crystals

        return entityEnderDragon;
    }

}
