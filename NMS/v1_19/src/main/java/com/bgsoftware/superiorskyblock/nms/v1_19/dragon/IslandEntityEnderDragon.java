package com.bgsoftware.superiorskyblock.nms.v1_19.dragon;

import com.bgsoftware.common.annotations.NotNull;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftEnderDragon;

public class IslandEntityEnderDragon extends EnderDragon {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    @NotNull
    public static EnderDragon fromEntityTypes(EntityType<? extends EnderDragon> entityTypes, Level level) {
        return plugin.getGrid().isIslandsWorld(level.getWorld()) ? new IslandEntityEnderDragon(level) :
                new EnderDragon(entityTypes, level);
    }

    private final ServerLevel serverLevel;
    private final Dimension dimension;
    private BlockPos islandBlockPos;

    public IslandEntityEnderDragon(Level level, BlockPos islandBlockPos) {
        this(level);
        this.islandBlockPos = islandBlockPos;
    }

    private IslandEntityEnderDragon(Level level) {
        super(EntityType.ENDER_DRAGON, level);
        this.serverLevel = (ServerLevel) level;
        this.dimension = plugin.getProviders().getWorldsProvider().getIslandsWorldDimension(level.getWorld());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compoundTag) {
        // loadData

        super.readAdditionalSaveData(compoundTag);

        if (!(this.serverLevel.dragonFight() instanceof EndWorldEndDragonFightHandler dragonBattleHandler))
            return;

        Location entityLocation = getBukkitEntity().getLocation();
        Island island = plugin.getGrid().getIslandAt(entityLocation);

        if (island == null)
            return;

        Location middleBlock = island.getCenter(dimension);

        SettingsManager.Worlds.DimensionConfig dimensionConfig = plugin.getSettings().getWorlds().getDimensionConfig(dimension);
        if (dimensionConfig instanceof SettingsManager.Worlds.End) {
            middleBlock = ((SettingsManager.Worlds.End) dimensionConfig).getPortalOffset().applyToLocation(middleBlock);
        }

        this.islandBlockPos = new BlockPos(middleBlock.getBlockX(), middleBlock.getBlockY(), middleBlock.getBlockZ());

        IslandEndDragonFight dragonBattle = new IslandEndDragonFight(island, this.serverLevel, this.islandBlockPos, this);
        dragonBattleHandler.addDragonFight(island.getUniqueId(), dragonBattle);
    }

    @Override
    public void aiStep() {
        DragonUtils.runWithPodiumPosition(this.islandBlockPos, super::aiStep);
    }

    @Override
    @NotNull
    public CraftEnderDragon getBukkitEntity() {
        return (CraftEnderDragon) super.getBukkitEntity();
    }

}
