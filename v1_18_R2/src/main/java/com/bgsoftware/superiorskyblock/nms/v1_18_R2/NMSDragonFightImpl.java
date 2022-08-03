package com.bgsoftware.superiorskyblock.nms.v1_18_R2;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.nms.NMSDragonFight;
import com.bgsoftware.superiorskyblock.nms.mapping.Remap;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.dragon.EndWorldEnderDragonBattleHandler;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.dragon.IslandEnderDragonBattle;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.dragon.IslandEntityEnderDragon;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.dragon.SpikesCache;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.net.minecraft.server.level.WorldServer;
import com.google.common.cache.LoadingCache;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.boss.enderdragon.EntityEnderDragon;
import net.minecraft.world.level.dimension.end.EnderDragonBattle;
import net.minecraft.world.level.levelgen.feature.WorldGenEnder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.advancement.Advancement;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.lang.reflect.Modifier;
import java.util.List;

@SuppressWarnings({"unused"})
public final class NMSDragonFightImpl implements NMSDragonFight {

    private static final ReflectField<EntityTypes.b<?>> ENTITY_TYPES_BUILDER = new ReflectField<EntityTypes.b<?>>(
            EntityTypes.class, EntityTypes.b.class, Modifier.PRIVATE | Modifier.FINAL, 1)
            .removeFinal();

    private static final ReflectField<EnderDragonBattle> WORLD_DRAGON_BATTLE = new ReflectField<EnderDragonBattle>(
            net.minecraft.server.level.WorldServer.class, EnderDragonBattle.class, Modifier.PRIVATE | Modifier.FINAL, 1)
            .removeFinal();

    private static final ReflectField<LoadingCache<Long, List<WorldGenEnder.Spike>>> SPIKE_CACHE = new ReflectField<LoadingCache<Long, List<WorldGenEnder.Spike>>>(
            WorldGenEnder.class, LoadingCache.class, Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL, 1)
            .removeFinal();

    @Remap(classPath = "net.minecraft.world.entity.EntityType", name = "ENDER_DRAGON", type = Remap.Type.FIELD, remappedName = "v")
    private static boolean firstWorldPreparation = true;

    static {
        ENTITY_TYPES_BUILDER.set(EntityTypes.v, (EntityTypes.b<EntityEnderDragon>) IslandEntityEnderDragon::fromEntityTypes);
    }

    @Override
    public void prepareEndWorld(World bukkitWorld) {
        WorldServer worldServer = new WorldServer(((CraftWorld) bukkitWorld).getHandle());
        WORLD_DRAGON_BATTLE.set(worldServer.getHandle(), new EndWorldEnderDragonBattleHandler(worldServer));

        if (firstWorldPreparation) {
            firstWorldPreparation = false;
            SPIKE_CACHE.set(null, SpikesCache.getInstance());
        }
    }

    @Nullable
    @Override
    public EnderDragon getEnderDragon(Island island) {
        World bukkitWorld = island.getCenter(World.Environment.THE_END).getWorld();

        if (bukkitWorld == null)
            return null;

        WorldServer worldServer = new WorldServer(((CraftWorld) bukkitWorld).getHandle());

        if (!(worldServer.getEnderDragonBattle() instanceof EndWorldEnderDragonBattleHandler dragonBattleHandler))
            return null;

        IslandEnderDragonBattle enderDragonBattle = dragonBattleHandler.getDragonBattle(island.getUniqueId());
        return enderDragonBattle == null ? null : enderDragonBattle.getEnderDragon().getBukkitEntity();
    }

    @Override
    public void startDragonBattle(Island island, Location location) {
        World bukkitWorld = location.getWorld();

        if (bukkitWorld == null)
            return;

        WorldServer worldServer = new WorldServer(((CraftWorld) bukkitWorld).getHandle());

        if (!(worldServer.getEnderDragonBattle() instanceof EndWorldEnderDragonBattleHandler dragonBattleHandler))
            return;

        dragonBattleHandler.addDragonBattle(island.getUniqueId(), new IslandEnderDragonBattle(island, worldServer, location));
    }

    @Override
    public void removeDragonBattle(Island island) {
        World bukkitWorld = island.getCenter(World.Environment.THE_END).getWorld();

        if (bukkitWorld == null)
            return;

        WorldServer worldServer = new WorldServer(((CraftWorld) bukkitWorld).getHandle());

        if (!(worldServer.getEnderDragonBattle() instanceof EndWorldEnderDragonBattleHandler dragonBattleHandler))
            return;

        EnderDragonBattle enderDragonBattle = dragonBattleHandler.removeDragonBattle(island.getUniqueId());

        if (enderDragonBattle instanceof IslandEnderDragonBattle islandEnderDragonBattle) {
            islandEnderDragonBattle.removeBattlePlayers();
            islandEnderDragonBattle.getEnderDragon().getBukkitEntity().remove();
        }
    }

    @Override
    public void awardTheEndAchievement(Player player) {
        Advancement advancement = Bukkit.getAdvancement(NamespacedKey.minecraft("end/root"));
        if (advancement != null)
            player.getAdvancementProgress(advancement).awardCriteria("");
    }

}
