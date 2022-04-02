package com.bgsoftware.superiorskyblock.nms.v1_16_R3.dragon;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.BlockPredicate;
import net.minecraft.server.v1_16_R3.Blocks;
import net.minecraft.server.v1_16_R3.Chunk;
import net.minecraft.server.v1_16_R3.DragonControllerPhase;
import net.minecraft.server.v1_16_R3.EnderDragonBattle;
import net.minecraft.server.v1_16_R3.EntityEnderDragon;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.HeightMap;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.ShapeDetector;
import net.minecraft.server.v1_16_R3.ShapeDetectorBlock;
import net.minecraft.server.v1_16_R3.ShapeDetectorBuilder;
import net.minecraft.server.v1_16_R3.TileEntity;
import net.minecraft.server.v1_16_R3.TileEntityEnderPortal;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
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
            .a('#', ShapeDetectorBlock.a(BlockPredicate.a(Blocks.BEDROCK)))
            .b();

    private final Island island;
    private final BlockPosition islandBlockPosition;

    private final IslandEntityEnderDragon entityEnderDragon;

    private byte currentTick = 0;

    public IslandEnderDragonBattle(Island island, WorldServer worldServer, Location location) {
        super(worldServer, worldServer.getSeed(), new NBTTagCompound());
        this.island = island;
        this.islandBlockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());
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
    public ShapeDetector.ShapeDetectorCollection getExitPortalShape() {
        // findExitPortal

        int chunkX = this.islandBlockPosition.getX() >> 4;
        int chunkZ = this.islandBlockPosition.getZ() >> 4;

        for (int x = -8; x <= 8; ++x) {
            for (int z = -8; z <= 8; ++z) {
                Chunk chunk = this.world.getChunkAt(chunkX + x, chunkZ + z);

                for (TileEntity tileEntity : chunk.getTileEntities().values()) {
                    if (tileEntity instanceof TileEntityEnderPortal) {
                        ShapeDetector.ShapeDetectorCollection shapeDetectorCollection = EXIT_PORTAL_PATTERN.a(this.world, tileEntity.getPosition());
                        if (shapeDetectorCollection != null) {
                            if (this.exitPortalLocation == null)
                                this.exitPortalLocation = shapeDetectorCollection.a(3, 3, 3).getPosition();

                            return shapeDetectorCollection;
                        }
                    }
                }
            }
        }

        int highestBlock = this.world.getHighestBlockYAt(HeightMap.Type.MOTION_BLOCKING, this.islandBlockPosition).getY();

        for (int y = highestBlock; y >= 0; --y) {
            BlockPosition currentPosition = new BlockPosition(this.islandBlockPosition.getX(), y, this.islandBlockPosition.getZ());

            ShapeDetector.ShapeDetectorCollection shapeDetectorCollection = EXIT_PORTAL_PATTERN.a(this.world, currentPosition);

            if (shapeDetectorCollection != null) {
                if (this.exitPortalLocation == null)
                    this.exitPortalLocation = shapeDetectorCollection.a(3, 3, 3).getPosition();

                return shapeDetectorCollection;
            }
        }

        return null;
    }

    @Override
    public void resetCrystals() {
        // resetSpikeCrystals

        DragonUtils.runWithPodiumPosition(this.islandBlockPosition, super::resetCrystals);
    }

    public void removeBattlePlayers() {
        this.bossBattle.getPlayers().forEach(this.bossBattle::removePlayer);
    }

    public void killEnderDragon() {
        this.entityEnderDragon.die();
    }

    private void updateBattlePlayers() {
        Set<UUID> nearbyPlayers = new HashSet<>();

        for (SuperiorPlayer superiorPlayer : island.getAllPlayersInside()) {
            Player bukkitPlayer = superiorPlayer.asPlayer();
            assert bukkitPlayer != null;

            EntityPlayer entityPlayer = ((CraftPlayer) bukkitPlayer).getHandle();

            if (entityPlayer.getWorld().equals(this.world)) {
                this.bossBattle.addPlayer(entityPlayer);
                nearbyPlayers.add(entityPlayer.getUniqueID());
            }
        }

        this.bossBattle.getPlayers().stream()
                .filter(entityPlayer -> !nearbyPlayers.contains(entityPlayer.getUniqueID()))
                .forEach(this.bossBattle::removePlayer);
    }

    private IslandEntityEnderDragon spawnEnderDragon() {
        IslandEntityEnderDragon entityEnderDragon = new IslandEntityEnderDragon(this.world, islandBlockPosition);
        entityEnderDragon.getDragonControllerManager().setControllerPhase(DragonControllerPhase.HOLDING_PATTERN);
        entityEnderDragon.setPositionRotation(islandBlockPosition.getX(), 128,
                islandBlockPosition.getZ(), this.world.getRandom().nextFloat() * 360.0F, 0.0F);
        DRAGON_BATTLE.set(entityEnderDragon, this);

        this.world.addEntity(entityEnderDragon, CreatureSpawnEvent.SpawnReason.NATURAL);

        this.dragonUUID = entityEnderDragon.getUniqueID();
        this.resetCrystals(); // scan for crystals

        return entityEnderDragon;
    }

}
