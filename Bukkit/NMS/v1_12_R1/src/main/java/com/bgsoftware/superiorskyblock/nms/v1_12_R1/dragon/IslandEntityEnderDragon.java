package com.bgsoftware.superiorskyblock.nms.v1_12_R1.dragon;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.DifficultyDamageScaler;
import net.minecraft.server.v1_12_R1.EnderDragonBattle;
import net.minecraft.server.v1_12_R1.EntityEnderDragon;
import net.minecraft.server.v1_12_R1.GroupDataEntity;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.World;
import net.minecraft.server.v1_12_R1.WorldProviderTheEnd;
import net.minecraft.server.v1_12_R1.WorldServer;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEnderDragon;

public class IslandEntityEnderDragon extends EntityEnderDragon {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final Dimension dimension;

    private BlockPosition islandBlockPosition;

    public IslandEntityEnderDragon(World world) {
        // Used when loading entities to the world.
        super(world);
        this.dimension = plugin.getProviders().getWorldsProvider().getIslandsWorldDimension(world.getWorld());
    }

    public IslandEntityEnderDragon(WorldServer worldServer, BlockPosition islandBlockPosition) {
        this(worldServer);
        this.islandBlockPosition = islandBlockPosition;
    }

    @Nullable
    @Override
    public GroupDataEntity prepare(DifficultyDamageScaler difficultydamagescaler,
                                   @Nullable GroupDataEntity groupdataentity) {
        if (this.islandBlockPosition == null)
            finalizeIslandEnderDragon();

        return super.prepare(difficultydamagescaler, groupdataentity);
    }

    @Override
    public void a(NBTTagCompound nbtTagCompound) {
        super.a(nbtTagCompound);
        finalizeIslandEnderDragon();
    }

    @Override
    public void n() {
        DragonUtils.runWithPodiumPosition(this.islandBlockPosition, super::n);
    }

    @Override
    public CraftEnderDragon getBukkitEntity() {
        return (CraftEnderDragon) super.getBukkitEntity();
    }

    private void finalizeIslandEnderDragon() {
        if (!(world.worldProvider instanceof WorldProviderTheEnd) || !plugin.getGrid().isIslandsWorld(world.getWorld()))
            return;

        EnderDragonBattle enderDragonBattle = ((WorldProviderTheEnd) world.worldProvider).t();

        if (!(enderDragonBattle instanceof EndWorldEnderDragonBattleHandler))
            return;

        EndWorldEnderDragonBattleHandler dragonBattleHandler = (EndWorldEnderDragonBattleHandler) enderDragonBattle;

        Island island;
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            island = plugin.getGrid().getIslandAt(getBukkitEntity().getLocation(wrapper.getHandle()));
        }

        if (island == null)
            return;

        Location middleBlock = island.getCenter(dimension);

        SettingsManager.Worlds.DimensionConfig dimensionConfig = plugin.getSettings().getWorlds().getDimensionConfig(dimension);
        if (dimensionConfig instanceof SettingsManager.Worlds.End) {
            middleBlock = ((SettingsManager.Worlds.End) dimensionConfig).getPortalOffset().applyToLocation(middleBlock);
        }

        this.islandBlockPosition = new BlockPosition(middleBlock.getX(), middleBlock.getY(), middleBlock.getZ());

        IslandEnderDragonBattle dragonBattle = new IslandEnderDragonBattle(island, (WorldServer) world, this.islandBlockPosition, this);
        dragonBattleHandler.addDragonBattle(island.getCache(), dragonBattle);
    }

}
