package com.bgsoftware.superiorskyblock.nms.v1_21_7.dragon;

import com.bgsoftware.common.annotations.NotNull;
import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftEnderDragon;

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

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level,
                                        DifficultyInstance difficulty,
                                        EntitySpawnReason spawnReason,
                                        @Nullable SpawnGroupData spawnGroupData) {
        if (this.islandBlockPos == null)
            finalizeIslandEnderDragon();

        return super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        // loadData
        super.readAdditionalSaveData(input);
        finalizeIslandEnderDragon();
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

    private void finalizeIslandEnderDragon() {
        if (!(this.serverLevel.getDragonFight() instanceof EndWorldEndDragonFightHandler dragonBattleHandler))
            return;

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

        this.islandBlockPos = new BlockPos(middleBlock.getBlockX(), middleBlock.getBlockY(), middleBlock.getBlockZ());

        IslandEndDragonFight dragonBattle = new IslandEndDragonFight(island, this.serverLevel, this.islandBlockPos, this);
        dragonBattleHandler.addDragonFight(island.getCache(), dragonBattle);
    }

}
