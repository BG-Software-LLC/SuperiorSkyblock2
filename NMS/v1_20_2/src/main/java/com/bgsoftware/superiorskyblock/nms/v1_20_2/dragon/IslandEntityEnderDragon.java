package com.bgsoftware.superiorskyblock.nms.v1_20_2.dragon;

import com.bgsoftware.common.annotations.NotNull;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftEnderDragon;

public class IslandEntityEnderDragon extends EnderDragon {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    @NotNull
    public static EnderDragon fromEntityTypes(EntityType<? extends EnderDragon> entityTypes, Level level) {
        return plugin.getGrid().isIslandsWorld(level.getWorld()) ? new IslandEntityEnderDragon(level) :
                new EnderDragon(entityTypes, level);
    }

    private final ServerLevel serverLevel;
    private BlockPos islandBlockPos;

    public IslandEntityEnderDragon(Level level, BlockPos islandBlockPos) {
        this(level);
        this.islandBlockPos = islandBlockPos;
    }

    private IslandEntityEnderDragon(Level level) {
        super(EntityType.ENDER_DRAGON, level);
        this.serverLevel = (ServerLevel) level;
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compoundTag) {
        // loadData

        super.readAdditionalSaveData(compoundTag);

        if (!(this.serverLevel.getDragonFight() instanceof EndWorldEndDragonFightHandler dragonBattleHandler))
            return;

        Location entityLocation = getBukkitEntity().getLocation();
        Island island = plugin.getGrid().getIslandAt(entityLocation);

        if (island == null)
            return;

        Location middleBlock = plugin.getSettings().getWorlds().getEnd().getPortalOffset()
                .applyToLocation(island.getCenter(org.bukkit.World.Environment.THE_END));
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
