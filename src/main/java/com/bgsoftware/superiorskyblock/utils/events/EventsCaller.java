package com.bgsoftware.superiorskyblock.utils.events;

import com.bgsoftware.superiorskyblock.api.events.IslandBiomeChangeEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandCreateEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandDisbandEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandEnterEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandEnterProtectedEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandInviteEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandJoinEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandKickEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandLeaveEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandLeaveProtectedEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandQuitEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandSchematicPasteEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandTransferEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandUpgradeEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandWorthCalculatedEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandWorthUpdateEvent;
import com.bgsoftware.superiorskyblock.api.events.MissionCompleteEvent;
import com.bgsoftware.superiorskyblock.api.events.PreIslandCreateEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.List;

public final class EventsCaller {

    public static boolean callIslandEnterEvent(SuperiorPlayer superiorPlayer, Island island, IslandEnterEvent.EnterCause enterCause){
        IslandEnterEvent islandEnterEvent = new IslandEnterEvent(superiorPlayer, island, enterCause);
        Bukkit.getPluginManager().callEvent(islandEnterEvent);
        if(islandEnterEvent.isCancelled() && islandEnterEvent.getCancelTeleport() != null)
            superiorPlayer.asPlayer().teleport(islandEnterEvent.getCancelTeleport());
        return !islandEnterEvent.isCancelled();
    }

    public static boolean callIslandEnterProtectedEvent(SuperiorPlayer superiorPlayer, Island island, IslandEnterEvent.EnterCause enterCause){
        IslandEnterProtectedEvent islandEnterProtectedEvent = new IslandEnterProtectedEvent(superiorPlayer, island, enterCause);
        Bukkit.getPluginManager().callEvent(islandEnterProtectedEvent);
        if(islandEnterProtectedEvent.isCancelled() && islandEnterProtectedEvent.getCancelTeleport() != null)
            superiorPlayer.asPlayer().teleport(islandEnterProtectedEvent.getCancelTeleport());
        return !islandEnterProtectedEvent.isCancelled();
    }

    public static boolean callIslandLeaveEvent(SuperiorPlayer superiorPlayer, Island island, IslandLeaveEvent.LeaveCause leaveCause, Location location){
        IslandLeaveEvent islandLeaveEvent = new IslandLeaveEvent(superiorPlayer, island, leaveCause, location);
        Bukkit.getPluginManager().callEvent(islandLeaveEvent);
        return !islandLeaveEvent.isCancelled();
    }

    public static boolean callIslandLeaveProtectedEvent(SuperiorPlayer superiorPlayer, Island island, IslandLeaveEvent.LeaveCause leaveCause, Location location){
        IslandLeaveProtectedEvent islandLeaveProtectedEvent = new IslandLeaveProtectedEvent(superiorPlayer, island, leaveCause, location);
        Bukkit.getPluginManager().callEvent(islandLeaveProtectedEvent);
        return !islandLeaveProtectedEvent.isCancelled();
    }

    public static EventResult<Biome> callIslandBiomeChangeEvent(SuperiorPlayer superiorPlayer, Island island, Biome biome){
        IslandBiomeChangeEvent islandBiomeChangeEvent = new IslandBiomeChangeEvent(superiorPlayer, island, biome);
        Bukkit.getPluginManager().callEvent(islandBiomeChangeEvent);
        return EventResult.of(islandBiomeChangeEvent.isCancelled(), islandBiomeChangeEvent.getBiome());
    }

    public static EventResult<Boolean> callIslandCreateEvent(SuperiorPlayer superiorPlayer, Island island, String schemName){
        IslandCreateEvent islandCreateEvent = new IslandCreateEvent(superiorPlayer, island, schemName);
        Bukkit.getPluginManager().callEvent(islandCreateEvent);
        return EventResult.of(islandCreateEvent.isCancelled(), islandCreateEvent.canTeleport());
    }

    public static boolean callIslandDisbandEvent(SuperiorPlayer superiorPlayer, Island island){
        IslandDisbandEvent islandDisbandEvent = new IslandDisbandEvent(superiorPlayer, island);
        Bukkit.getPluginManager().callEvent(islandDisbandEvent);
        return !islandDisbandEvent.isCancelled();
    }

    public static boolean callIslandInviteEvent(SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer, Island island){
        IslandInviteEvent islandInviteEvent = new IslandInviteEvent(superiorPlayer, targetPlayer, island);
        Bukkit.getPluginManager().callEvent(islandInviteEvent);
        return !islandInviteEvent.isCancelled();
    }

    public static boolean callIslandJoinEvent(SuperiorPlayer superiorPlayer, Island island){
        IslandJoinEvent islandJoinEvent = new IslandJoinEvent(superiorPlayer, island);
        Bukkit.getPluginManager().callEvent(islandJoinEvent);
        return !islandJoinEvent.isCancelled();
    }

    public static void callIslandKickEvent(SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer, Island island){
        IslandKickEvent islandKickEvent = new IslandKickEvent(superiorPlayer, targetPlayer, island);
        Bukkit.getPluginManager().callEvent(islandKickEvent);
    }

    public static boolean callIslandQuitEvent(SuperiorPlayer superiorPlayer, Island island){
        IslandQuitEvent islandQuitEvent = new IslandQuitEvent(superiorPlayer, island);
        Bukkit.getPluginManager().callEvent(islandQuitEvent);
        return !islandQuitEvent.isCancelled();
    }

    public static void callIslandSchematicPasteEvent(Island island, String name, Location location){
        IslandSchematicPasteEvent islandSchematicPasteEvent = new IslandSchematicPasteEvent(island, name, location);
        Bukkit.getPluginManager().callEvent(islandSchematicPasteEvent);
    }

    public static boolean callIslandTransferEvent(Island island, SuperiorPlayer previousOwner, SuperiorPlayer superiorPlayer){
        IslandTransferEvent islandTransferEvent = new IslandTransferEvent(island, previousOwner, superiorPlayer);
        Bukkit.getPluginManager().callEvent(islandTransferEvent);
        return !islandTransferEvent.isCancelled();
    }

    public static EventResult<Pair<List<String>, Double>> callIslandUpgradeEvent(SuperiorPlayer superiorPlayer, Island island, String upgradeName, List<String> commands, double price){
        IslandUpgradeEvent islandUpgradeEvent = new IslandUpgradeEvent(superiorPlayer, island, upgradeName, commands, price);
        Bukkit.getPluginManager().callEvent(islandUpgradeEvent);
        return EventResult.of(islandUpgradeEvent.isCancelled(), new Pair<>(islandUpgradeEvent.getCommands(), islandUpgradeEvent.getAmountToWithdraw()));
    }

    public static void callIslandWorthCalculatedEvent(Island island, SuperiorPlayer asker, BigDecimal islandLevel, BigDecimal islandWorth){
        IslandWorthCalculatedEvent islandWorthCalculatedEvent = new IslandWorthCalculatedEvent(island, asker, islandLevel, islandWorth);
        Bukkit.getPluginManager().callEvent(islandWorthCalculatedEvent);
    }

    public static void callIslandWorthUpdateEvent(Island island, BigDecimal oldWorth, BigDecimal oldLevel, BigDecimal newWorth, BigDecimal newLevel){
        IslandWorthUpdateEvent islandWorthUpdateEvent = new IslandWorthUpdateEvent(island, oldWorth, oldLevel, newWorth, newLevel);
        Bukkit.getPluginManager().callEvent(islandWorthUpdateEvent);
    }

    public static EventResult<Pair<List<ItemStack>, List<String>>> callMissionCompleteEvent(SuperiorPlayer superiorPlayer, Mission mission, boolean islandMission, List<ItemStack> itemRewards, List<String> commandRewards){
        MissionCompleteEvent missionCompleteEvent = new MissionCompleteEvent(superiorPlayer, mission, islandMission, itemRewards, commandRewards);
        Bukkit.getPluginManager().callEvent(missionCompleteEvent);
        return EventResult.of(missionCompleteEvent.isCancelled(), new Pair<>(missionCompleteEvent.getItemRewards(), missionCompleteEvent.getCommandRewards()));
    }

    public static boolean callPreIslandCreateEvent(SuperiorPlayer superiorPlayer, String islandName){
        PreIslandCreateEvent preIslandCreateEvent = new PreIslandCreateEvent(superiorPlayer, islandName);
        Bukkit.getPluginManager().callEvent(preIslandCreateEvent);
        return !preIslandCreateEvent.isCancelled();
    }
}
