package com.bgsoftware.superiorskyblock.nms.v1_18_R2;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.nms.NMSDragonFight;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.dragon.EndWorldEnderDragonBattleHandler;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.dragon.IslandEnderDragonBattle;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.level.WorldServer;
import net.minecraft.world.entity.boss.enderdragon.EntityEnderDragon;
import net.minecraft.world.level.dimension.end.EnderDragonBattle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.advancement.Advancement;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.entity.Player;

import java.lang.reflect.Modifier;

@SuppressWarnings({"unused"})
public final class NMSDragonFightImpl implements NMSDragonFight {

    private static final ReflectField<EnderDragonBattle> WORLD_DRAGON_BATTLE = new ReflectField<EnderDragonBattle>(
            net.minecraft.server.level.WorldServer.class, EnderDragonBattle.class, Modifier.PRIVATE | Modifier.FINAL, 1)
            .removeFinal();

    @Override
    public void prepareEndWorld(org.bukkit.World bukkitWorld) {
        WorldServer worldServer = new WorldServer(((CraftWorld) bukkitWorld).getHandle());
        WORLD_DRAGON_BATTLE.set(worldServer.getHandle(), new EndWorldEnderDragonBattleHandler(worldServer));
    }

    @Override
    public void startDragonBattle(Island island, Location location) {
        org.bukkit.World bukkitWorld = location.getWorld();

        if (bukkitWorld == null)
            return;

        WorldServer worldServer = new WorldServer(((CraftWorld) bukkitWorld).getHandle());

        if (!(worldServer.getEnderDragonBattle() instanceof EndWorldEnderDragonBattleHandler dragonBattleHandler))
            return;

        dragonBattleHandler.addDragonBattle(island.getUniqueId(), new IslandEnderDragonBattle(island, worldServer, location));
    }

    @Override
    public void removeDragonBattle(Island island) {
        org.bukkit.World bukkitWorld = island.getCenter(World.Environment.THE_END).getWorld();

        if (bukkitWorld == null)
            return;

        WorldServer worldServer = new WorldServer(((CraftWorld) bukkitWorld).getHandle());

        if (!(worldServer.getEnderDragonBattle() instanceof EndWorldEnderDragonBattleHandler dragonBattleHandler))
            return;

        EnderDragonBattle enderDragonBattle = dragonBattleHandler.removeDragonBattle(island.getUniqueId());

        if (enderDragonBattle instanceof IslandEnderDragonBattle islandEnderDragonBattle) {
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
