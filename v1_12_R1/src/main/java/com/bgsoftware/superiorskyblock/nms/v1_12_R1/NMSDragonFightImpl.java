package com.bgsoftware.superiorskyblock.nms.v1_12_R1;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.nms.NMSDragonFight;
import com.bgsoftware.superiorskyblock.nms.v1_12_R1.dragon.EndWorldEnderDragonBattleHandler;
import com.bgsoftware.superiorskyblock.nms.v1_12_R1.dragon.IslandEnderDragonBattle;
import com.bgsoftware.superiorskyblock.nms.v1_12_R1.dragon.IslandEntityEnderDragon;
import net.minecraft.server.v1_12_R1.EnderDragonBattle;
import net.minecraft.server.v1_12_R1.EntityTypes;
import net.minecraft.server.v1_12_R1.MinecraftKey;
import net.minecraft.server.v1_12_R1.WorldProviderTheEnd;
import net.minecraft.server.v1_12_R1.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.advancement.Advancement;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.entity.Player;

import java.lang.reflect.Modifier;

@SuppressWarnings({"unused"})
public final class NMSDragonFightImpl implements NMSDragonFight {

    private static final ReflectField<EnderDragonBattle> WORLD_DRAGON_BATTLE = new ReflectField<>(
            WorldProviderTheEnd.class, EnderDragonBattle.class, Modifier.PRIVATE, 1);


    static {
        EntityTypes.b.a(63, new MinecraftKey("ender_dragon"), IslandEntityEnderDragon.class);
    }

    @Override
    public void prepareEndWorld(World bukkitWorld) {
        WorldServer worldServer = ((CraftWorld) bukkitWorld).getHandle();
        WORLD_DRAGON_BATTLE.set(worldServer.worldProvider, new EndWorldEnderDragonBattleHandler(worldServer));
    }

    @Override
    public void startDragonBattle(Island island, Location location) {
        World bukkitWorld = location.getWorld();

        if (bukkitWorld == null)
            return;

        WorldServer worldServer = ((CraftWorld) bukkitWorld).getHandle();

        if (!(worldServer.worldProvider instanceof WorldProviderTheEnd))
            return;

        EnderDragonBattle enderDragonBattle = ((WorldProviderTheEnd) worldServer.worldProvider).t();

        if (!(enderDragonBattle instanceof EndWorldEnderDragonBattleHandler))
            return;

        EndWorldEnderDragonBattleHandler dragonBattleHandler = (EndWorldEnderDragonBattleHandler) enderDragonBattle;
        dragonBattleHandler.addDragonBattle(island.getUniqueId(), new IslandEnderDragonBattle(island, worldServer, location));
    }

    @Override
    public void removeDragonBattle(Island island) {
        World bukkitWorld = island.getCenter(World.Environment.THE_END).getWorld();

        if (bukkitWorld == null)
            return;

        WorldServer worldServer = ((CraftWorld) bukkitWorld).getHandle();

        if (!(worldServer.worldProvider instanceof WorldProviderTheEnd))
            return;

        EnderDragonBattle worldEnderDragonBattle = ((WorldProviderTheEnd) worldServer.worldProvider).t();

        if (!(worldEnderDragonBattle instanceof EndWorldEnderDragonBattleHandler))
            return;

        EndWorldEnderDragonBattleHandler dragonBattleHandler = (EndWorldEnderDragonBattleHandler) worldEnderDragonBattle;
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
