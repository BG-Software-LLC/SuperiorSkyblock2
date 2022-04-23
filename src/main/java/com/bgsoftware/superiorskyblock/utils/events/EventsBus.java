package com.bgsoftware.superiorskyblock.utils.events;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.events.BlockStackEvent;
import com.bgsoftware.superiorskyblock.api.events.BlockUnstackEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandBanEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandBankDepositEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandBankWithdrawEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandBiomeChangeEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandChatEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandChunkResetEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandCoopPlayerEvent;
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
import com.bgsoftware.superiorskyblock.api.events.IslandRestrictMoveEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandSchematicPasteEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandTransferEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandUncoopPlayerEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandUpgradeEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandWorthCalculatedEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandWorthUpdateEvent;
import com.bgsoftware.superiorskyblock.api.events.MissionCompleteEvent;
import com.bgsoftware.superiorskyblock.api.events.PluginInitializeEvent;
import com.bgsoftware.superiorskyblock.api.events.PluginInitializedEvent;
import com.bgsoftware.superiorskyblock.api.events.PreIslandCreateEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCost;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.world.chunks.ChunkPosition;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public final class EventsBus {

    private final SuperiorSkyblockPlugin plugin;

    private boolean lastEventCancelled = false;

    public EventsBus(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isLastEventCancelled() {
        return lastEventCancelled;
    }

    public boolean callIslandEnterEvent(SuperiorPlayer superiorPlayer, Island island, IslandEnterEvent.EnterCause enterCause) {
        if (plugin.getSettings().getDisabledEvents().contains("islandenterevent"))
            return true;

        IslandEnterEvent islandEnterEvent = new IslandEnterEvent(superiorPlayer, island, enterCause);
        callEvent(islandEnterEvent);

        if (setCancelledEvent(islandEnterEvent) && islandEnterEvent.getCancelTeleport() != null)
            superiorPlayer.teleport(islandEnterEvent.getCancelTeleport());

        return !lastEventCancelled;
    }

    public boolean callIslandEnterProtectedEvent(SuperiorPlayer superiorPlayer, Island island, IslandEnterEvent.EnterCause enterCause) {
        if (plugin.getSettings().getDisabledEvents().contains("islandenterprotectedevent"))
            return true;

        IslandEnterProtectedEvent islandEnterProtectedEvent = new IslandEnterProtectedEvent(superiorPlayer, island, enterCause);
        callEvent(islandEnterProtectedEvent);

        if (setCancelledEvent(islandEnterProtectedEvent) && islandEnterProtectedEvent.getCancelTeleport() != null)
            superiorPlayer.teleport(islandEnterProtectedEvent.getCancelTeleport());

        return !lastEventCancelled;
    }

    public boolean callIslandLeaveEvent(SuperiorPlayer superiorPlayer, Island island, IslandLeaveEvent.LeaveCause leaveCause, Location location) {
        if (plugin.getSettings().getDisabledEvents().contains("islandleaveevent"))
            return true;

        IslandLeaveEvent islandLeaveEvent = new IslandLeaveEvent(superiorPlayer, island, leaveCause, location);
        callEvent(islandLeaveEvent);

        return !setCancelledEvent(islandLeaveEvent);
    }

    public boolean callIslandLeaveProtectedEvent(SuperiorPlayer superiorPlayer, Island island, IslandLeaveEvent.LeaveCause leaveCause, Location location) {
        if (plugin.getSettings().getDisabledEvents().contains("islandleaveprotectedevent"))
            return true;

        IslandLeaveProtectedEvent islandLeaveProtectedEvent = new IslandLeaveProtectedEvent(superiorPlayer, island, leaveCause, location);
        callEvent(islandLeaveProtectedEvent);
        return !setCancelledEvent(islandLeaveProtectedEvent);
    }

    public EventResult<Biome> callIslandBiomeChangeEvent(SuperiorPlayer superiorPlayer, Island island, Biome biome) {
        if (plugin.getSettings().getDisabledEvents().contains("islandbiomechangeevent"))
            return EventResult.of(false, biome);

        IslandBiomeChangeEvent islandBiomeChangeEvent = new IslandBiomeChangeEvent(superiorPlayer, island, biome);
        callEvent(islandBiomeChangeEvent);
        return EventResult.of(setCancelledEvent(islandBiomeChangeEvent), islandBiomeChangeEvent.getBiome());
    }

    public EventResult<Boolean> callIslandCreateEvent(SuperiorPlayer superiorPlayer, Island island, String schemName) {
        if (plugin.getSettings().getDisabledEvents().contains("islandcreateevent"))
            return EventResult.of(false, true);

        IslandCreateEvent islandCreateEvent = new IslandCreateEvent(superiorPlayer, island, schemName);
        callEvent(islandCreateEvent);
        return EventResult.of(setCancelledEvent(islandCreateEvent), islandCreateEvent.canTeleport());
    }

    public boolean callIslandDisbandEvent(SuperiorPlayer superiorPlayer, Island island) {
        if (plugin.getSettings().getDisabledEvents().contains("islanddisbandevent"))
            return true;

        IslandDisbandEvent islandDisbandEvent = new IslandDisbandEvent(superiorPlayer, island);
        callEvent(islandDisbandEvent);
        return !setCancelledEvent(islandDisbandEvent);
    }

    public boolean callIslandInviteEvent(SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer, Island island) {
        if (plugin.getSettings().getDisabledEvents().contains("islandinviteevent"))
            return true;

        IslandInviteEvent islandInviteEvent = new IslandInviteEvent(superiorPlayer, targetPlayer, island);
        callEvent(islandInviteEvent);
        return !setCancelledEvent(islandInviteEvent);
    }

    @SuppressWarnings("all")
    public boolean callIslandJoinEvent(SuperiorPlayer superiorPlayer, Island island) {
        if (plugin.getSettings().getDisabledEvents().contains("islandjoinevent"))
            return true;

        IslandJoinEvent islandJoinEvent = new IslandJoinEvent(superiorPlayer, island);
        callEvent(islandJoinEvent);
        return !setCancelledEvent(islandJoinEvent);
    }

    public void callIslandKickEvent(SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer, Island island) {
        if (!plugin.getSettings().getDisabledEvents().contains("islandkickevent")) {
            IslandKickEvent islandKickEvent = new IslandKickEvent(superiorPlayer, targetPlayer, island);
            callEvent(islandKickEvent);
        }
    }

    public void callIslandBanEvent(SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer, Island island) {
        if (!plugin.getSettings().getDisabledEvents().contains("islandbanevent")) {
            IslandBanEvent islandBanEvent = new IslandBanEvent(superiorPlayer, targetPlayer, island);
            callEvent(islandBanEvent);
        }
    }

    public boolean callIslandQuitEvent(SuperiorPlayer superiorPlayer, Island island) {
        if (plugin.getSettings().getDisabledEvents().contains("islandquitevent"))
            return true;

        IslandQuitEvent islandQuitEvent = new IslandQuitEvent(superiorPlayer, island);
        callEvent(islandQuitEvent);
        return !setCancelledEvent(islandQuitEvent);
    }

    public void callIslandSchematicPasteEvent(Island island, String name, Location location) {
        if (!plugin.getSettings().getDisabledEvents().contains("islandschematicpasteevent")) {
            IslandSchematicPasteEvent islandSchematicPasteEvent = new IslandSchematicPasteEvent(island, name, location);
            callEvent(islandSchematicPasteEvent);
        }
    }

    public boolean callIslandTransferEvent(Island island, SuperiorPlayer previousOwner, SuperiorPlayer superiorPlayer) {
        if (plugin.getSettings().getDisabledEvents().contains("islandtransferevent"))
            return true;

        IslandTransferEvent islandTransferEvent = new IslandTransferEvent(island, previousOwner, superiorPlayer);
        callEvent(islandTransferEvent);
        return !setCancelledEvent(islandTransferEvent);
    }

    public EventResult<UpgradeResult> callIslandUpgradeEvent(SuperiorPlayer superiorPlayer, Island island, String upgradeName, List<String> commands, UpgradeCost cost) {
        if (plugin.getSettings().getDisabledEvents().contains("islandupgradeevent"))
            return EventResult.of(false, new UpgradeResult(commands, cost));

        IslandUpgradeEvent islandUpgradeEvent = new IslandUpgradeEvent(superiorPlayer, island, upgradeName, commands, cost);
        callEvent(islandUpgradeEvent);
        return EventResult.of(setCancelledEvent(islandUpgradeEvent), new UpgradeResult(islandUpgradeEvent.getCommands(), islandUpgradeEvent.getUpgradeCost()));
    }

    public void callIslandWorthCalculatedEvent(Island island, SuperiorPlayer asker, BigDecimal islandLevel, BigDecimal islandWorth) {
        if (!plugin.getSettings().getDisabledEvents().contains("islandworthcalculatedevent")) {
            IslandWorthCalculatedEvent islandWorthCalculatedEvent = new IslandWorthCalculatedEvent(island, asker, islandLevel, islandWorth);
            callEvent(islandWorthCalculatedEvent);
        }
    }

    public void callIslandWorthUpdateEvent(Island island, BigDecimal oldWorth, BigDecimal oldLevel, BigDecimal newWorth, BigDecimal newLevel) {
        if (!plugin.getSettings().getDisabledEvents().contains("islandworthupdateevent")) {
            IslandWorthUpdateEvent islandWorthUpdateEvent = new IslandWorthUpdateEvent(island, oldWorth, oldLevel, newWorth, newLevel);
            callEvent(islandWorthUpdateEvent);
        }
    }

    public EventResult<MissionRewards> callMissionCompleteEvent(SuperiorPlayer superiorPlayer, Mission<?> mission, boolean islandMission, List<ItemStack> itemRewards, List<String> commandRewards) {
        if (plugin.getSettings().getDisabledEvents().contains("missioncompleteevent"))
            return EventResult.of(false, MissionRewards.of(itemRewards, commandRewards));

        MissionCompleteEvent missionCompleteEvent = new MissionCompleteEvent(superiorPlayer, mission, islandMission, itemRewards, commandRewards);
        callEvent(missionCompleteEvent);
        return EventResult.of(setCancelledEvent(missionCompleteEvent),
                MissionRewards.of(missionCompleteEvent.getItemRewards(), missionCompleteEvent.getCommandRewards()));
    }

    public boolean callPreIslandCreateEvent(SuperiorPlayer superiorPlayer, String islandName) {
        if (plugin.getSettings().getDisabledEvents().contains("preislandcreateevent"))
            return true;

        PreIslandCreateEvent preIslandCreateEvent = new PreIslandCreateEvent(superiorPlayer, islandName);
        callEvent(preIslandCreateEvent);
        return !setCancelledEvent(preIslandCreateEvent);
    }

    public boolean callBlockStackEvent(Block block, Player player, int originalAmount, int newAmount) {
        if (plugin.getSettings().getDisabledEvents().contains("blockstackevent"))
            return true;

        BlockStackEvent blockStackEvent = new BlockStackEvent(block, player, originalAmount, newAmount);
        callEvent(blockStackEvent);
        return !setCancelledEvent(blockStackEvent);
    }

    public boolean callBlockUnstackEvent(Block block, Player player, int originalAmount, int newAmount) {
        if (plugin.getSettings().getDisabledEvents().contains("blockunstackevent"))
            return true;

        BlockUnstackEvent blockUnstackEvent = new BlockUnstackEvent(block, player, originalAmount, newAmount);
        callEvent(blockUnstackEvent);
        return !setCancelledEvent(blockUnstackEvent);
    }

    public EventResult<String> callIslandBankDepositEvent(SuperiorPlayer superiorPlayer, Island island, BigDecimal amount) {
        if (plugin.getSettings().getDisabledEvents().contains("islandbankdepositevent"))
            return EventResult.of(false, null);

        IslandBankDepositEvent islandBankDepositEvent = new IslandBankDepositEvent(superiorPlayer, island, amount);
        callEvent(islandBankDepositEvent);
        return EventResult.of(setCancelledEvent(islandBankDepositEvent), islandBankDepositEvent.getFailureReason());
    }

    public EventResult<String> callIslandBankWithdrawEvent(SuperiorPlayer superiorPlayer, Island island, BigDecimal amount) {
        if (plugin.getSettings().getDisabledEvents().contains("islandbankwithdrawevent"))
            return EventResult.of(false, null);

        IslandBankWithdrawEvent islandBankWithdrawEvent = new IslandBankWithdrawEvent(superiorPlayer, island, amount);
        callEvent(islandBankWithdrawEvent);
        return EventResult.of(setCancelledEvent(islandBankWithdrawEvent), islandBankWithdrawEvent.getFailureReason());
    }

    public void callIslandRestrictMoveEvent(SuperiorPlayer superiorPlayer, IslandRestrictMoveEvent.RestrictReason restrictReason) {
        if (!plugin.getSettings().getDisabledEvents().contains("islandrestrictmoveevent")) {
            IslandRestrictMoveEvent islandRestrictMoveEvent = new IslandRestrictMoveEvent(superiorPlayer, restrictReason);
            callEvent(islandRestrictMoveEvent);
        }
    }

    public void callPluginInitializeEvent(SuperiorSkyblock plugin) {
        callEvent(new PluginInitializeEvent(plugin));
    }

    public void callPluginInitializedEvent(SuperiorSkyblock plugin) {
        callEvent(new PluginInitializedEvent(plugin));
    }

    public boolean callIslandCoopPlayerEvent(Island island, SuperiorPlayer player, SuperiorPlayer target) {
        if (plugin.getSettings().getDisabledEvents().contains("islandcoopplayerevent"))
            return true;

        IslandCoopPlayerEvent islandCoopPlayerEvent = new IslandCoopPlayerEvent(island, player, target);
        callEvent(islandCoopPlayerEvent);
        return !setCancelledEvent(islandCoopPlayerEvent);
    }

    public boolean callIslandUncoopPlayerEvent(Island island, SuperiorPlayer player, SuperiorPlayer target,
                                               IslandUncoopPlayerEvent.UncoopReason uncoopReason) {
        if (plugin.getSettings().getDisabledEvents().contains("islanduncoopplayerevent"))
            return true;

        IslandUncoopPlayerEvent islandUncoopPlayerEvent = new IslandUncoopPlayerEvent(island, player, target, uncoopReason);
        callEvent(islandUncoopPlayerEvent);
        return !setCancelledEvent(islandUncoopPlayerEvent);
    }

    public void callIslandChunkResetEvent(Island island, ChunkPosition chunkPosition) {
        if (plugin.getSettings().getDisabledEvents().contains("islandchunkresetevent"))
            return;

        IslandChunkResetEvent islandChunkResetEvent = new IslandChunkResetEvent(island, chunkPosition.getWorld(),
                chunkPosition.getX(), chunkPosition.getZ());
        callEvent(islandChunkResetEvent);
    }

    public EventResult<String> callIslandChatEvent(Island island, SuperiorPlayer superiorPlayer, String message) {
        if (plugin.getSettings().getDisabledEvents().contains("islandchatevent"))
            return EventResult.of(false, message);

        IslandChatEvent islandChatEvent = new IslandChatEvent(island, superiorPlayer, message);
        callEvent(islandChatEvent);
        return EventResult.of(setCancelledEvent(islandChatEvent), message);
    }

    private boolean setCancelledEvent(Cancellable cancellable) {
        lastEventCancelled = cancellable.isCancelled();
        return lastEventCancelled;
    }

    private static void callEvent(Event event) {
        Bukkit.getPluginManager().callEvent(event);
    }

    public static final class MissionRewards {

        private static final MissionRewards EMPTY = new MissionRewards(Collections.emptyList(), Collections.emptyList());

        private final List<ItemStack> itemRewards;
        private final List<String> commandRewards;

        private static MissionRewards of(List<ItemStack> itemRewards, List<String> commandRewards) {
            return itemRewards.isEmpty() && commandRewards.isEmpty() ? EMPTY : new MissionRewards(itemRewards, commandRewards);
        }

        private MissionRewards(List<ItemStack> itemRewards, List<String> commandRewards) {
            this.itemRewards = itemRewards;
            this.commandRewards = commandRewards;
        }

        public List<ItemStack> getItemRewards() {
            return itemRewards;
        }

        public List<String> getCommandRewards() {
            return commandRewards;
        }

    }

    public static final class UpgradeResult {

        private final List<String> commands;
        private final UpgradeCost upgradeCost;

        public UpgradeResult(List<String> commands, UpgradeCost upgradeCost) {
            this.commands = commands;
            this.upgradeCost = upgradeCost;
        }

        public List<String> getCommands() {
            return commands;
        }

        public UpgradeCost getUpgradeCost() {
            return upgradeCost;
        }

    }

}
