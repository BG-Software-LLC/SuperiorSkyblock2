package com.bgsoftware.superiorskyblock.nms.v1_16_R3;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.nms.NMSDragonFight;
import com.bgsoftware.superiorskyblock.nms.v1_16_R3.dragon.EndWorldEnderDragonBattleHandler;
import com.bgsoftware.superiorskyblock.nms.v1_16_R3.dragon.IslandEnderDragonBattle;
import com.bgsoftware.superiorskyblock.nms.v1_16_R3.dragon.SpikesCache;
import com.google.common.cache.LoadingCache;
import net.minecraft.server.v1_16_R3.EnderDragonBattle;
import net.minecraft.server.v1_16_R3.WorldGenEnder;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.advancement.Advancement;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.entity.Player;

import java.lang.reflect.Modifier;
import java.util.List;

@SuppressWarnings({"unused"})
public final class NMSDragonFightImpl implements NMSDragonFight {

    private static final ReflectField<EnderDragonBattle> WORLD_DRAGON_BATTLE = new ReflectField<EnderDragonBattle>(
            WorldServer.class, EnderDragonBattle.class, Modifier.PRIVATE | Modifier.FINAL, 1)
            .removeFinal();

    private static final ReflectField<LoadingCache<Long, List<WorldGenEnder.Spike>>> SPIKE_CACHE = new ReflectField<LoadingCache<Long, List<WorldGenEnder.Spike>>>(
            WorldGenEnder.class, LoadingCache.class, Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL, 1)
            .removeFinal();

    private static boolean firstWorldPreparation = true;

    @Override
    public void prepareEndWorld(World bukkitWorld) {
        WorldServer worldServer = ((CraftWorld) bukkitWorld).getHandle();
        WORLD_DRAGON_BATTLE.set(worldServer, new EndWorldEnderDragonBattleHandler(worldServer));

        if (firstWorldPreparation) {
            firstWorldPreparation = false;
            SPIKE_CACHE.set(null, SpikesCache.getInstance());
        }
    }

    @Override
    public void startDragonBattle(Island island, Location location) {
        World bukkitWorld = location.getWorld();

        if (bukkitWorld == null)
            return;

        WorldServer worldServer = ((CraftWorld) bukkitWorld).getHandle();

        if (!(worldServer.getDragonBattle() instanceof EndWorldEnderDragonBattleHandler))
            return;

        EndWorldEnderDragonBattleHandler dragonBattleHandler = (EndWorldEnderDragonBattleHandler) worldServer.getDragonBattle();
        dragonBattleHandler.addDragonBattle(island.getUniqueId(), new IslandEnderDragonBattle(island, worldServer, location));
    }

    @Override
    public void removeDragonBattle(Island island) {
        World bukkitWorld = island.getCenter(World.Environment.THE_END).getWorld();

        if (bukkitWorld == null)
            return;

        WorldServer worldServer = ((CraftWorld) bukkitWorld).getHandle();

        if (!(worldServer.getDragonBattle() instanceof EndWorldEnderDragonBattleHandler))
            return;

        EndWorldEnderDragonBattleHandler dragonBattleHandler = (EndWorldEnderDragonBattleHandler) worldServer.getDragonBattle();
        EnderDragonBattle enderDragonBattle = dragonBattleHandler.removeDragonBattle(island.getUniqueId());

        if (enderDragonBattle instanceof IslandEnderDragonBattle) {
            IslandEnderDragonBattle islandEnderDragonBattle = (IslandEnderDragonBattle) enderDragonBattle;
            islandEnderDragonBattle.removeBattlePlayers();
            islandEnderDragonBattle.killEnderDragon();
        }
    }

    @Override
    public void awardTheEndAchievement(Player player) {
        Advancement advancement = Bukkit.getAdvancement(NamespacedKey.minecraft("end/root"));
        if (advancement != null)
            player.getAdvancementProgress(advancement).awardCriteria("");
    }

}
