package com.bgsoftware.superiorskyblock.nms.v1_18_R1.dragon;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.BlockPosition;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.level.WorldServer;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.world.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.boss.enderdragon.EntityEnderDragon;
import net.minecraft.world.level.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftEnderDragon;
import org.jetbrains.annotations.NotNull;

public final class IslandEntityEnderDragon extends EntityEnderDragon {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final Entity entity = new Entity(this);

    private BlockPosition islandBlockPosition;

    @NotNull
    public static EntityEnderDragon fromEntityTypes(EntityTypes<? extends EntityEnderDragon> entityTypes, World world) {
        return plugin.getGrid().isIslandsWorld(world.getWorld()) ? new IslandEntityEnderDragon(world) :
                new EntityEnderDragon(entityTypes, world);
    }

    public IslandEntityEnderDragon(WorldServer worldServer, BlockPosition islandBlockPosition) {
        this(worldServer.getHandle());
        this.islandBlockPosition = islandBlockPosition;
    }

    private IslandEntityEnderDragon(World world) {
        super(EntityTypes.v, world);
    }

    @Override
    public void a(@NotNull NBTTagCompound nbtTagCompound) {
        // loadData

        super.a(nbtTagCompound);

        WorldServer worldServer = entity.getWorld();

        if (!(worldServer.getEnderDragonBattle() instanceof EndWorldEnderDragonBattleHandler dragonBattleHandler))
            return;

        Location entityLocation = getBukkitEntity().getLocation();
        Island island = plugin.getGrid().getIslandAt(entityLocation);

        if (island == null)
            return;

        Location middleBlock = plugin.getSettings().getWorlds().getEnd().getPortalOffset()
                .applyToLocation(island.getCenter(org.bukkit.World.Environment.THE_END));
        this.islandBlockPosition = new BlockPosition(middleBlock.getX(), middleBlock.getY(), middleBlock.getZ());

        dragonBattleHandler.addDragonBattle(island.getUniqueId(), new IslandEnderDragonBattle(island,
                worldServer, this.islandBlockPosition, this));
    }

    @Override
    public void w_() {
        DragonUtils.runWithPodiumPosition(this.islandBlockPosition, super::w_);
    }

    @Override
    @NotNull
    public CraftEnderDragon getBukkitEntity() {
        return (CraftEnderDragon) super.getBukkitEntity();
    }

    public Entity getEntity() {
        return entity;
    }

}
