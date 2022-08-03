package com.bgsoftware.superiorskyblock.nms.v1_18_R1.dragon;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.nms.mapping.Remap;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.net.minecraft.core.BlockPosition;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.net.minecraft.server.level.WorldServer;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.net.minecraft.world.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.boss.enderdragon.EntityEnderDragon;
import net.minecraft.world.level.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftEnderDragon;
import org.jetbrains.annotations.NotNull;

public final class IslandEntityEnderDragon extends EntityEnderDragon {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    @NotNull
    public static EntityEnderDragon fromEntityTypes(EntityTypes<? extends EntityEnderDragon> entityTypes, World world) {
        return plugin.getGrid().isIslandsWorld(world.getWorld()) ? new IslandEntityEnderDragon(world) :
                new EntityEnderDragon(entityTypes, world);
    }

    private final Entity entity = new Entity(this);

    private BlockPosition islandBlockPosition;

    public IslandEntityEnderDragon(WorldServer worldServer, BlockPosition islandBlockPosition) {
        this(worldServer.getHandle());
        this.islandBlockPosition = islandBlockPosition;
    }

    @Remap(classPath = "net.minecraft.world.entity.EntityType", name = "ENDER_DRAGON", type = Remap.Type.FIELD, remappedName = "v")
    private IslandEntityEnderDragon(World world) {
        super(EntityTypes.v, world);
    }

    @Remap(classPath = "net.minecraft.world.entity.Entity",
            name = "readAdditionalSaveData",
            type = Remap.Type.METHOD,
            remappedName = "a")
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

    @Remap(classPath = "net.minecraft.world.entity.LivingEntity",
            name = "aiStep",
            type = Remap.Type.METHOD,
            remappedName = "w_")
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
