package com.bgsoftware.superiorskyblock.nms.v1_16_R3.dragon;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.EntityEnderDragon;
import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.World;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEnderDragon;

public class IslandEntityEnderDragon extends EntityEnderDragon {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    public static EntityEnderDragon fromEntityTypes(EntityTypes<? extends EntityEnderDragon> entityTypes, World world) {
        return plugin.getGrid().isIslandsWorld(world.getWorld()) ? new IslandEntityEnderDragon(world) :
                new EntityEnderDragon(entityTypes, world);
    }

    private final Dimension dimension;
    private BlockPosition islandBlockPosition;

    public IslandEntityEnderDragon(WorldServer worldServer, BlockPosition islandBlockPosition) {
        this(worldServer);
        this.islandBlockPosition = islandBlockPosition;
    }

    private IslandEntityEnderDragon(World world) {
        super(EntityTypes.ENDER_DRAGON, world);
        this.dimension = plugin.getProviders().getWorldsProvider().getIslandsWorldDimension(world.getWorld());
    }

    @Override
    public void loadData(NBTTagCompound nbtTagCompound) {
        super.loadData(nbtTagCompound);

        if (!(world instanceof WorldServer) || !(((WorldServer) world).getDragonBattle() instanceof EndWorldEnderDragonBattleHandler))
            return;

        EndWorldEnderDragonBattleHandler dragonBattleHandler = (EndWorldEnderDragonBattleHandler) ((WorldServer) world).getDragonBattle();

        Location entityLocation = getBukkitEntity().getLocation();
        Island island = plugin.getGrid().getIslandAt(entityLocation);

        if (island == null)
            return;

        Location middleBlock = island.getCenter(dimension);

        SettingsManager.Worlds.DimensionConfig dimensionConfig = plugin.getSettings().getWorlds().getDimensionConfig(dimension);
        if (dimensionConfig instanceof SettingsManager.Worlds.End) {
            middleBlock = ((SettingsManager.Worlds.End) dimensionConfig).getPortalOffset().applyToLocation(middleBlock);
        }

        this.islandBlockPosition = new BlockPosition(middleBlock.getX(), middleBlock.getY(), middleBlock.getZ());

        dragonBattleHandler.addDragonBattle(island.getUniqueId(), new IslandEnderDragonBattle(island,
                (WorldServer) world, this.islandBlockPosition, this));
    }

    @Override
    public void movementTick() {
        DragonUtils.runWithPodiumPosition(this.islandBlockPosition, super::movementTick);
    }

    @Override
    public CraftEnderDragon getBukkitEntity() {
        return (CraftEnderDragon) super.getBukkitEntity();
    }

}
