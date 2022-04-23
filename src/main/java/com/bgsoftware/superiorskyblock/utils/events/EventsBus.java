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

        IslandEnterEvent islandEnterEvent = callEvent(new IslandEnterEvent(superiorPlayer, island, enterCause));

        if (setCancelledEvent(islandEnterEvent) && islandEnterEvent.getCancelTeleport() != null)
            superiorPlayer.teleport(islandEnterEvent.getCancelTeleport());

        return !lastEventCancelled;
    }

    public boolean callIslandEnterProtectedEvent(SuperiorPlayer superiorPlayer, Island island, IslandEnterEvent.EnterCause enterCause) {
        if (plugin.getSettings().getDisabledEvents().contains("islandenterprotectedevent"))
            return true;

        IslandEnterProtectedEvent islandEnterProtectedEvent = callEvent(new IslandEnterProtectedEvent(superiorPlayer, island, enterCause));

        if (setCancelledEvent(islandEnterProtectedEvent) && islandEnterProtectedEvent.getCancelTeleport() != null)
            superiorPlayer.teleport(islandEnterProtectedEvent.getCancelTeleport());

        return !lastEventCancelled;
    }

    public boolean callIslandLeaveEvent(SuperiorPlayer superiorPlayer, Island island, IslandLeaveEvent.LeaveCause leaveCause, Location location) {
        if (plugin.getSettings().getDisabledEvents().contains("islandleaveevent"))
            return true;

        IslandLeaveEvent islandLeaveEvent = callEvent(new IslandLeaveEvent(superiorPlayer, island, leaveCause, location));
        return !setCancelledEvent(islandLeaveEvent);
    }

    public boolean callIslandLeaveProtectedEvent(SuperiorPlayer superiorPlayer, Island island, IslandLeaveEvent.LeaveCause leaveCause, Location location) {
        if (plugin.getSettings().getDisabledEvents().contains("islandleaveprotectedevent"))
            return true;

        IslandLeaveProtectedEvent islandLeaveProtectedEvent = callEvent(new IslandLeaveProtectedEvent(superiorPlayer,
                island, leaveCause, location));
        return !setCancelledEvent(islandLeaveProtectedEvent);
    }

    public EventResult<Biome> callIslandBiomeChangeEvent(SuperiorPlayer superiorPlayer, Island island, Biome biome) {
        if (plugin.getSettings().getDisabledEvents().contains("islandbiomechangeevent"))
            return EventResult.of(false, biome);

        IslandBiomeChangeEvent islandBiomeChangeEvent = callEvent(new IslandBiomeChangeEvent(superiorPlayer, island, biome));
        return EventResult.of(setCancelledEvent(islandBiomeChangeEvent), islandBiomeChangeEvent.getBiome());
    }

    public EventResult<Boolean> callIslandCreateEvent(SuperiorPlayer superiorPlayer, Island island, String schemName) {
        if (plugin.getSettings().getDisabledEvents().contains("islandcreateevent"))
            return EventResult.of(false, true);

        IslandCreateEvent islandCreateEvent = callEvent(new IslandCreateEvent(superiorPlayer, island, schemName));
        return EventResult.of(setCancelledEvent(islandCreateEvent), islandCreateEvent.canTeleport());
    }

    public boolean callIslandDisbandEvent(SuperiorPlayer superiorPlayer, Island island) {
        if (plugin.getSettings().getDisabledEvents().contains("islanddisbandevent"))
            return true;

        IslandDisbandEvent islandDisbandEvent = callEvent(new IslandDisbandEvent(superiorPlayer, island));
        return !setCancelledEvent(islandDisbandEvent);
    }

    public boolean callIslandInviteEvent(SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer, Island island) {
        if (plugin.getSettings().getDisabledEvents().contains("islandinviteevent"))
            return true;

        IslandInviteEvent islandInviteEvent = callEvent(new IslandInviteEvent(superiorPlayer, targetPlayer, island));
        return !setCancelledEvent(islandInviteEvent);
    }

    @SuppressWarnings("all")
    public boolean callIslandJoinEvent(SuperiorPlayer superiorPlayer, Island island) {
        if (plugin.getSettings().getDisabledEvents().contains("islandjoinevent"))
            return true;

        IslandJoinEvent islandJoinEvent = callEvent(new IslandJoinEvent(superiorPlayer, island));
        return !setCancelledEvent(islandJoinEvent);
    }

    public void callIslandKickEvent(SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer, Island island) {
        if (!plugin.getSettings().getDisabledEvents().contains("islandkickevent")) {
            callEvent(new IslandKickEvent(superiorPlayer, targetPlayer, island));
        }
    }

    public void callIslandBanEvent(SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer, Island island) {
        if (!plugin.getSettings().getDisabledEvents().contains("islandbanevent")) {
            callEvent(new IslandBanEvent(superiorPlayer, targetPlayer, island));
        }
    }

    public boolean callIslandQuitEvent(SuperiorPlayer superiorPlayer, Island island) {
        if (plugin.getSettings().getDisabledEvents().contains("islandquitevent"))
            return true;

        IslandQuitEvent islandQuitEvent = callEvent(new IslandQuitEvent(superiorPlayer, island));
        return !setCancelledEvent(islandQuitEvent);
    }

    public void callIslandSchematicPasteEvent(Island island, String name, Location location) {
        if (!plugin.getSettings().getDisabledEvents().contains("islandschematicpasteevent")) {
            callEvent(new IslandSchematicPasteEvent(island, name, location));
        }
    }

    public boolean callIslandTransferEvent(Island island, SuperiorPlayer previousOwner, SuperiorPlayer superiorPlayer) {
        if (plugin.getSettings().getDisabledEvents().contains("islandtransferevent"))
            return true;

        IslandTransferEvent islandTransferEvent = callEvent(new IslandTransferEvent(island, previousOwner, superiorPlayer));
        return !setCancelledEvent(islandTransferEvent);
    }

    public EventResult<UpgradeResult> callIslandUpgradeEvent(SuperiorPlayer superiorPlayer, Island island, String upgradeName, List<String> commands, UpgradeCost cost) {
        if (plugin.getSettings().getDisabledEvents().contains("islandupgradeevent"))
            return EventResult.of(false, new UpgradeResult(commands, cost));

        IslandUpgradeEvent islandUpgradeEvent = callEvent(new IslandUpgradeEvent(superiorPlayer, island, upgradeName, commands, cost));
        return EventResult.of(setCancelledEvent(islandUpgradeEvent), new UpgradeResult(islandUpgradeEvent.getCommands(), islandUpgradeEvent.getUpgradeCost()));
    }

    public void callIslandWorthCalculatedEvent(Island island, SuperiorPlayer asker, BigDecimal islandLevel, BigDecimal islandWorth) {
        if (!plugin.getSettings().getDisabledEvents().contains("islandworthcalculatedevent")) {
            callEvent(new IslandWorthCalculatedEvent(island, asker, islandLevel, islandWorth));
        }
    }

    public void callIslandWorthUpdateEvent(Island island, BigDecimal oldWorth, BigDecimal oldLevel, BigDecimal newWorth, BigDecimal newLevel) {
        if (!plugin.getSettings().getDisabledEvents().contains("islandworthupdateevent")) {
            callEvent(new IslandWorthUpdateEvent(island, oldWorth, oldLevel, newWorth, newLevel));
        }
    }

    public EventResult<MissionRewards> callMissionCompleteEvent(SuperiorPlayer superiorPlayer, Mission<?> mission, boolean islandMission, List<ItemStack> itemRewards, List<String> commandRewards) {
        if (plugin.getSettings().getDisabledEvents().contains("missioncompleteevent"))
            return EventResult.of(false, MissionRewards.of(itemRewards, commandRewards));

        MissionCompleteEvent missionCompleteEvent = callEvent(new MissionCompleteEvent(superiorPlayer, mission,
                islandMission, itemRewards, commandRewards));
        return EventResult.of(setCancelledEvent(missionCompleteEvent),
                MissionRewards.of(missionCompleteEvent.getItemRewards(), missionCompleteEvent.getCommandRewards()));
    }

    public boolean callPreIslandCreateEvent(SuperiorPlayer superiorPlayer, String islandName) {
        if (plugin.getSettings().getDisabledEvents().contains("preislandcreateevent"))
            return true;

        PreIslandCreateEvent preIslandCreateEvent = callEvent(new PreIslandCreateEvent(superiorPlayer, islandName));
        return !setCancelledEvent(preIslandCreateEvent);
    }

    public boolean callBlockStackEvent(Block block, Player player, int originalAmount, int newAmount) {
        if (plugin.getSettings().getDisabledEvents().contains("blockstackevent"))
            return true;

        BlockStackEvent blockStackEvent = callEvent(new BlockStackEvent(block, player, originalAmount, newAmount));
        return !setCancelledEvent(blockStackEvent);
    }

    public boolean callBlockUnstackEvent(Block block, Player player, int originalAmount, int newAmount) {
        if (plugin.getSettings().getDisabledEvents().contains("blockunstackevent"))
            return true;

        BlockUnstackEvent blockUnstackEvent = callEvent(new BlockUnstackEvent(block, player, originalAmount, newAmount));
        return !setCancelledEvent(blockUnstackEvent);
    }

    public EventResult<String> callIslandBankDepositEvent(SuperiorPlayer superiorPlayer, Island island, BigDecimal amount) {
        if (plugin.getSettings().getDisabledEvents().contains("islandbankdepositevent"))
            return EventResult.of(false, null);

        IslandBankDepositEvent islandBankDepositEvent = callEvent(new IslandBankDepositEvent(superiorPlayer, island, amount));
        return EventResult.of(setCancelledEvent(islandBankDepositEvent), islandBankDepositEvent.getFailureReason());
    }

    public EventResult<String> callIslandBankWithdrawEvent(SuperiorPlayer superiorPlayer, Island island, BigDecimal amount) {
        if (plugin.getSettings().getDisabledEvents().contains("islandbankwithdrawevent"))
            return EventResult.of(false, null);

        IslandBankWithdrawEvent islandBankWithdrawEvent = callEvent(new IslandBankWithdrawEvent(superiorPlayer, island, amount));
        return EventResult.of(setCancelledEvent(islandBankWithdrawEvent), islandBankWithdrawEvent.getFailureReason());
    }

    public void callIslandRestrictMoveEvent(SuperiorPlayer superiorPlayer, IslandRestrictMoveEvent.RestrictReason restrictReason) {
        if (!plugin.getSettings().getDisabledEvents().contains("islandrestrictmoveevent")) {
            callEvent(new IslandRestrictMoveEvent(superiorPlayer, restrictReason));
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

        IslandCoopPlayerEvent islandCoopPlayerEvent = callEvent(new IslandCoopPlayerEvent(island, player, target));
        return !setCancelledEvent(islandCoopPlayerEvent);
    }

    public boolean callIslandUncoopPlayerEvent(Island island, SuperiorPlayer player, SuperiorPlayer target,
                                               IslandUncoopPlayerEvent.UncoopReason uncoopReason) {
        if (plugin.getSettings().getDisabledEvents().contains("islanduncoopplayerevent"))
            return true;

        IslandUncoopPlayerEvent islandUncoopPlayerEvent = callEvent(new IslandUncoopPlayerEvent(island, player, target, uncoopReason));
        return !setCancelledEvent(islandUncoopPlayerEvent);
    }

    public void callIslandChunkResetEvent(Island island, ChunkPosition chunkPosition) {
        if (plugin.getSettings().getDisabledEvents().contains("islandchunkresetevent"))
            return;

        callEvent(new IslandChunkResetEvent(island, chunkPosition.getWorld(), chunkPosition.getX(), chunkPosition.getZ()));
    }

    public EventResult<String> callIslandChatEvent(Island island, SuperiorPlayer superiorPlayer, String message) {
        if (plugin.getSettings().getDisabledEvents().contains("islandchatevent"))
            return EventResult.of(false, message);

        IslandChatEvent islandChatEvent = callEvent(new IslandChatEvent(island, superiorPlayer, message));
        return EventResult.of(setCancelledEvent(islandChatEvent), message);
    }

    private boolean setCancelledEvent(Cancellable cancellable) {
        lastEventCancelled = cancellable.isCancelled();
        return lastEventCancelled;
    }

    private static <T extends Event> T callEvent(T event) {
        Bukkit.getPluginManager().callEvent(event);
        return event;
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
