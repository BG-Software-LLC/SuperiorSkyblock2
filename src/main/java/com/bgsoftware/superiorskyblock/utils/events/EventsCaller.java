package com.bgsoftware.superiorskyblock.utils.events;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.events.*;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCost;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.List;

public final class EventsCaller {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private EventsCaller(){

    }

    public static boolean callIslandEnterEvent(SuperiorPlayer superiorPlayer, Island island, IslandEnterEvent.EnterCause enterCause){
        if(plugin.getSettings().getDisabledEvents().contains("islandenterevent"))
            return true;

        IslandEnterEvent islandEnterEvent = new IslandEnterEvent(superiorPlayer, island, enterCause);
        Bukkit.getPluginManager().callEvent(islandEnterEvent);
        if(islandEnterEvent.isCancelled() && islandEnterEvent.getCancelTeleport() != null)
            superiorPlayer.teleport(islandEnterEvent.getCancelTeleport());
        return !islandEnterEvent.isCancelled();
    }

    public static boolean callIslandEnterProtectedEvent(SuperiorPlayer superiorPlayer, Island island, IslandEnterEvent.EnterCause enterCause){
        if(plugin.getSettings().getDisabledEvents().contains("islandenterprotectedevent"))
            return true;

        IslandEnterProtectedEvent islandEnterProtectedEvent = new IslandEnterProtectedEvent(superiorPlayer, island, enterCause);
        Bukkit.getPluginManager().callEvent(islandEnterProtectedEvent);
        if(islandEnterProtectedEvent.isCancelled() && islandEnterProtectedEvent.getCancelTeleport() != null)
            superiorPlayer.teleport(islandEnterProtectedEvent.getCancelTeleport());
        return !islandEnterProtectedEvent.isCancelled();
    }

    public static boolean callIslandLeaveEvent(SuperiorPlayer superiorPlayer, Island island, IslandLeaveEvent.LeaveCause leaveCause, Location location){
        if(plugin.getSettings().getDisabledEvents().contains("islandleaveevent"))
            return true;

        IslandLeaveEvent islandLeaveEvent = new IslandLeaveEvent(superiorPlayer, island, leaveCause, location);
        Bukkit.getPluginManager().callEvent(islandLeaveEvent);
        return !islandLeaveEvent.isCancelled();
    }

    public static boolean callIslandLeaveProtectedEvent(SuperiorPlayer superiorPlayer, Island island, IslandLeaveEvent.LeaveCause leaveCause, Location location){
        if(plugin.getSettings().getDisabledEvents().contains("islandleaveprotectedevent"))
            return true;

        IslandLeaveProtectedEvent islandLeaveProtectedEvent = new IslandLeaveProtectedEvent(superiorPlayer, island, leaveCause, location);
        Bukkit.getPluginManager().callEvent(islandLeaveProtectedEvent);
        return !islandLeaveProtectedEvent.isCancelled();
    }

    public static EventResult<Biome> callIslandBiomeChangeEvent(SuperiorPlayer superiorPlayer, Island island, Biome biome){
        if(plugin.getSettings().getDisabledEvents().contains("islandbiomechangeevent"))
            return EventResult.of(false, biome);

        IslandBiomeChangeEvent islandBiomeChangeEvent = new IslandBiomeChangeEvent(superiorPlayer, island, biome);
        Bukkit.getPluginManager().callEvent(islandBiomeChangeEvent);
        return EventResult.of(islandBiomeChangeEvent.isCancelled(), islandBiomeChangeEvent.getBiome());
    }

    public static EventResult<Boolean> callIslandCreateEvent(SuperiorPlayer superiorPlayer, Island island, String schemName){
        if(plugin.getSettings().getDisabledEvents().contains("islandcreateevent"))
            return EventResult.of(false, true);

        IslandCreateEvent islandCreateEvent = new IslandCreateEvent(superiorPlayer, island, schemName);
        Bukkit.getPluginManager().callEvent(islandCreateEvent);
        return EventResult.of(islandCreateEvent.isCancelled(), islandCreateEvent.canTeleport());
    }

    public static boolean callIslandDisbandEvent(SuperiorPlayer superiorPlayer, Island island){
        if(plugin.getSettings().getDisabledEvents().contains("islanddisbandevent"))
            return true;

        IslandDisbandEvent islandDisbandEvent = new IslandDisbandEvent(superiorPlayer, island);
        Bukkit.getPluginManager().callEvent(islandDisbandEvent);
        return !islandDisbandEvent.isCancelled();
    }

    public static boolean callIslandInviteEvent(SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer, Island island){
        if(plugin.getSettings().getDisabledEvents().contains("islandinviteevent"))
            return true;

        IslandInviteEvent islandInviteEvent = new IslandInviteEvent(superiorPlayer, targetPlayer, island);
        Bukkit.getPluginManager().callEvent(islandInviteEvent);
        return !islandInviteEvent.isCancelled();
    }

    @SuppressWarnings("all")
    public static boolean callIslandJoinEvent(SuperiorPlayer superiorPlayer, Island island){
        if(plugin.getSettings().getDisabledEvents().contains("islandjoinevent"))
            return true;

        IslandJoinEvent islandJoinEvent = new IslandJoinEvent(superiorPlayer, island);
        Bukkit.getPluginManager().callEvent(islandJoinEvent);
        return !islandJoinEvent.isCancelled();
    }

    public static void callIslandKickEvent(SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer, Island island){
        if(!plugin.getSettings().getDisabledEvents().contains("islandkickevent")) {
            IslandKickEvent islandKickEvent = new IslandKickEvent(superiorPlayer, targetPlayer, island);
            Bukkit.getPluginManager().callEvent(islandKickEvent);
        }
    }

    public static void callIslandBanEvent(SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer, Island island) {
        if(!plugin.getSettings().getDisabledEvents().contains("islandbanevent")) {
            IslandBanEvent islandBanEvent = new IslandBanEvent(superiorPlayer, targetPlayer, island);
            Bukkit.getPluginManager().callEvent(islandBanEvent);
        }
    }

    public static boolean callIslandQuitEvent(SuperiorPlayer superiorPlayer, Island island){
        if(plugin.getSettings().getDisabledEvents().contains("islandquitevent"))
            return true;

        IslandQuitEvent islandQuitEvent = new IslandQuitEvent(superiorPlayer, island);
        Bukkit.getPluginManager().callEvent(islandQuitEvent);
        return !islandQuitEvent.isCancelled();
    }

    public static void callIslandSchematicPasteEvent(Island island, String name, Location location){
        if(!plugin.getSettings().getDisabledEvents().contains("islandschematicpasteevent")) {
            IslandSchematicPasteEvent islandSchematicPasteEvent = new IslandSchematicPasteEvent(island, name, location);
            Bukkit.getPluginManager().callEvent(islandSchematicPasteEvent);
        }
    }

    public static boolean callIslandTransferEvent(Island island, SuperiorPlayer previousOwner, SuperiorPlayer superiorPlayer){
        if(plugin.getSettings().getDisabledEvents().contains("islandtransferevent"))
            return true;

        IslandTransferEvent islandTransferEvent = new IslandTransferEvent(island, previousOwner, superiorPlayer);
        Bukkit.getPluginManager().callEvent(islandTransferEvent);
        return !islandTransferEvent.isCancelled();
    }

    public static EventResult<Pair<List<String>, UpgradeCost>> callIslandUpgradeEvent(SuperiorPlayer superiorPlayer, Island island, String upgradeName, List<String> commands, UpgradeCost cost){
        if(plugin.getSettings().getDisabledEvents().contains("islandupgradeevent"))
            return EventResult.of(false, new Pair<>(commands, cost));

        IslandUpgradeEvent islandUpgradeEvent = new IslandUpgradeEvent(superiorPlayer, island, upgradeName, commands, cost);
        Bukkit.getPluginManager().callEvent(islandUpgradeEvent);
        return EventResult.of(islandUpgradeEvent.isCancelled(), new Pair<>(islandUpgradeEvent.getCommands(), islandUpgradeEvent.getUpgradeCost()));
    }

    public static void callIslandWorthCalculatedEvent(Island island, SuperiorPlayer asker, BigDecimal islandLevel, BigDecimal islandWorth){
        if(!plugin.getSettings().getDisabledEvents().contains("islandworthcalculatedevent")) {
            IslandWorthCalculatedEvent islandWorthCalculatedEvent = new IslandWorthCalculatedEvent(island, asker, islandLevel, islandWorth);
            Bukkit.getPluginManager().callEvent(islandWorthCalculatedEvent);
        }
    }

    public static void callIslandWorthUpdateEvent(Island island, BigDecimal oldWorth, BigDecimal oldLevel, BigDecimal newWorth, BigDecimal newLevel){
        if(!plugin.getSettings().getDisabledEvents().contains("islandworthupdateevent")) {
            IslandWorthUpdateEvent islandWorthUpdateEvent = new IslandWorthUpdateEvent(island, oldWorth, oldLevel, newWorth, newLevel);
            Bukkit.getPluginManager().callEvent(islandWorthUpdateEvent);
        }
    }

    public static EventResult<Pair<List<ItemStack>, List<String>>> callMissionCompleteEvent(SuperiorPlayer superiorPlayer, Mission<?> mission, boolean islandMission, List<ItemStack> itemRewards, List<String> commandRewards){
        if(plugin.getSettings().getDisabledEvents().contains("missioncompleteevent"))
            return EventResult.of(false, new Pair<>(itemRewards, commandRewards));

        MissionCompleteEvent missionCompleteEvent = new MissionCompleteEvent(superiorPlayer, mission, islandMission, itemRewards, commandRewards);
        Bukkit.getPluginManager().callEvent(missionCompleteEvent);
        return EventResult.of(missionCompleteEvent.isCancelled(), new Pair<>(missionCompleteEvent.getItemRewards(), missionCompleteEvent.getCommandRewards()));
    }

    public static boolean callPreIslandCreateEvent(SuperiorPlayer superiorPlayer, String islandName){
        if(plugin.getSettings().getDisabledEvents().contains("preislandcreateevent"))
            return true;

        PreIslandCreateEvent preIslandCreateEvent = new PreIslandCreateEvent(superiorPlayer, islandName);
        Bukkit.getPluginManager().callEvent(preIslandCreateEvent);
        return !preIslandCreateEvent.isCancelled();
    }

    public static boolean callBlockStackEvent(Block block, Player player, int originalAmount, int newAmount){
        if(plugin.getSettings().getDisabledEvents().contains("blockstackevent"))
            return true;

        BlockStackEvent blockStackEvent = new BlockStackEvent(block, player, originalAmount, newAmount);
        Bukkit.getPluginManager().callEvent(blockStackEvent);
        return !blockStackEvent.isCancelled();
    }

    public static boolean callBlockUnstackEvent(Block block, Player player, int originalAmount, int newAmount){
        if(plugin.getSettings().getDisabledEvents().contains("blockunstackevent"))
            return true;

        BlockUnstackEvent blockUnstackEvent = new BlockUnstackEvent(block, player, originalAmount, newAmount);
        Bukkit.getPluginManager().callEvent(blockUnstackEvent);
        return !blockUnstackEvent.isCancelled();
    }

    public static void callIslandBankDepositEvent(SuperiorPlayer superiorPlayer, Island island, BigDecimal amount){
        if(!plugin.getSettings().getDisabledEvents().contains("islandbankdepositevent")) {
            IslandBankDepositEvent islandBankDepositEvent = new IslandBankDepositEvent(superiorPlayer, island, amount);
            Bukkit.getPluginManager().callEvent(islandBankDepositEvent);
        }
    }

    public static void callIslandBankWithdrawEvent(SuperiorPlayer superiorPlayer, Island island, BigDecimal amount){
        if(!plugin.getSettings().getDisabledEvents().contains("islandbankwithdrawevent")) {
            IslandBankWithdrawEvent islandBankWithdrawEvent = new IslandBankWithdrawEvent(superiorPlayer, island, amount);
            Bukkit.getPluginManager().callEvent(islandBankWithdrawEvent);
        }
    }

    public static void callIslandRestrictMoveEvent(SuperiorPlayer superiorPlayer, IslandRestrictMoveEvent.RestrictReason restrictReason){
        if(!plugin.getSettings().getDisabledEvents().contains("islandrestrictmoveevent")) {
            IslandRestrictMoveEvent islandRestrictMoveEvent = new IslandRestrictMoveEvent(superiorPlayer, restrictReason);
            Bukkit.getPluginManager().callEvent(islandRestrictMoveEvent);
        }
    }

    public static void callPluginInitializeEvent(SuperiorSkyblock plugin){
        Bukkit.getPluginManager().callEvent(new PluginInitializeEvent(plugin));
    }

    public static boolean callIslandCoopPlayerEvent(Island island, SuperiorPlayer player, SuperiorPlayer target){
        if(plugin.getSettings().getDisabledEvents().contains("islandcoopplayerevent"))
            return true;

        IslandCoopPlayerEvent islandCoopPlayerEvent = new IslandCoopPlayerEvent(island, player, target);
        Bukkit.getPluginManager().callEvent(islandCoopPlayerEvent);
        return !islandCoopPlayerEvent.isCancelled();
    }

    public static boolean callIslandUncoopPlayerEvent(Island island, SuperiorPlayer player, SuperiorPlayer target, IslandUncoopPlayerEvent.UncoopReason uncoopReason){
        if(plugin.getSettings().getDisabledEvents().contains("islanduncoopplayerevent"))
            return true;

        IslandUncoopPlayerEvent islandUncoopPlayerEvent = new IslandUncoopPlayerEvent(island, player, target, uncoopReason);
        Bukkit.getPluginManager().callEvent(islandUncoopPlayerEvent);
        return !islandUncoopPlayerEvent.isCancelled();
    }

    public static void callIslandChunkResetEvent(Island island, ChunkPosition chunkPosition){
        if(plugin.getSettings().getDisabledEvents().contains("islandchunkresetevent"))
            return;

        IslandChunkResetEvent islandChunkResetEvent = new IslandChunkResetEvent(island, chunkPosition.getWorld(),
                chunkPosition.getX(), chunkPosition.getZ());
        Bukkit.getPluginManager().callEvent(islandChunkResetEvent);
    }

}
