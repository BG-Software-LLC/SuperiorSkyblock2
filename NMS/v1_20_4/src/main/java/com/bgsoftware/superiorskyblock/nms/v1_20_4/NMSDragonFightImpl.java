package com.bgsoftware.superiorskyblock.nms.v1_20_4;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.nms.NMSDragonFight;
import com.bgsoftware.superiorskyblock.nms.v1_20_4.dragon.EndWorldEndDragonFightHandler;
import com.bgsoftware.superiorskyblock.nms.v1_20_4.dragon.IslandEndDragonFight;
import com.bgsoftware.superiorskyblock.nms.v1_20_4.dragon.IslandEntityEnderDragon;
import com.bgsoftware.superiorskyblock.nms.v1_20_4.dragon.SpikesCache;
import com.google.common.cache.LoadingCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.advancement.Advancement;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;

import java.lang.reflect.Modifier;
import java.util.List;

public class NMSDragonFightImpl implements NMSDragonFight {

    private static final ReflectField<EntityType.EntityFactory<?>> ENTITY_TYPES_BUILDER = new ReflectField<EntityType.EntityFactory<?>>(
            EntityType.class, EntityType.EntityFactory.class, Modifier.PRIVATE | Modifier.FINAL, 1)
            .removeFinal();

    private static final ReflectField<EndDragonFight> WORLD_DRAGON_BATTLE = new ReflectField<>(
            ServerLevel.class, EndDragonFight.class, Modifier.PRIVATE, 1);

    private static final ReflectField<LoadingCache<Long, List<SpikeFeature.EndSpike>>> SPIKE_CACHE = new ReflectField<LoadingCache<Long, List<SpikeFeature.EndSpike>>>(
            SpikeFeature.class, LoadingCache.class, Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL, 1)
            .removeFinal();

    private static boolean firstWorldPreparation = true;

    static {
        ENTITY_TYPES_BUILDER.set(EntityType.ENDER_DRAGON, (EntityType.EntityFactory<EnderDragon>) IslandEntityEnderDragon::fromEntityTypes);
    }

    @Override
    public void prepareEndWorld(World bukkitWorld) {
        ServerLevel serverLevel = ((CraftWorld) bukkitWorld).getHandle();
        WORLD_DRAGON_BATTLE.set(serverLevel, new EndWorldEndDragonFightHandler(serverLevel));

        if (firstWorldPreparation) {
            firstWorldPreparation = false;
            SPIKE_CACHE.set(null, SpikesCache.getInstance());
        }
    }

    @Nullable
    @Override
    public org.bukkit.entity.EnderDragon getEnderDragon(Island island) {
        World bukkitWorld = island.getCenter(World.Environment.THE_END).getWorld();

        if (bukkitWorld == null)
            return null;

        ServerLevel serverLevel = ((CraftWorld) bukkitWorld).getHandle();

        if (!(serverLevel.getDragonFight() instanceof EndWorldEndDragonFightHandler dragonFightHandler))
            return null;

        IslandEndDragonFight enderDragonBattle = dragonFightHandler.getDragonFight(island.getUniqueId());
        return enderDragonBattle == null ? null : enderDragonBattle.getEnderDragon().getBukkitEntity();
    }

    @Override
    public void startDragonBattle(Island island, Location location) {
        World bukkitWorld = location.getWorld();

        if (bukkitWorld == null)
            return;

        ServerLevel serverLevel = ((CraftWorld) bukkitWorld).getHandle();

        if (!(serverLevel.getDragonFight() instanceof EndWorldEndDragonFightHandler dragonFightHandler))
            return;

        dragonFightHandler.addDragonFight(island.getUniqueId(), new IslandEndDragonFight(island, serverLevel, location));
    }

    @Override
    public void removeDragonBattle(Island island) {
        World bukkitWorld = island.getCenter(World.Environment.THE_END).getWorld();

        if (bukkitWorld == null)
            return;

        ServerLevel serverLevel = ((CraftWorld) bukkitWorld).getHandle();

        if (!(serverLevel.getDragonFight() instanceof EndWorldEndDragonFightHandler dragonFightHandler))
            return;

        EndDragonFight endDragonFight = dragonFightHandler.removeDragonFight(island.getUniqueId());

        if (endDragonFight instanceof IslandEndDragonFight islandEndDragonFight) {
            islandEndDragonFight.removeBattlePlayers();
            islandEndDragonFight.getEnderDragon().getBukkitEntity().remove();
        }
    }

    @Override
    public void awardTheEndAchievement(Player player) {
        Advancement advancement = Bukkit.getAdvancement(NamespacedKey.minecraft("end/root"));
        if (advancement != null)
            player.getAdvancementProgress(advancement).awardCriteria("");
    }

}
