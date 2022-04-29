package com.bgsoftware.superiorskyblock.utils.events;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.events.BlockStackEvent;
import com.bgsoftware.superiorskyblock.api.events.BlockUnstackEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandBanEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandBankDepositEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandBankWithdrawEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandBiomeChangeEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandChangeBankLimitEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandChangeBlockLimitEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandChangeBorderSizeEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandChangeCoopLimitEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandChangeCropGrowthEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandChangeDescriptionEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandChangeDiscordEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandChangeEffectLevelEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandChangeEntityLimitEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandChangeGeneratorRateEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandChangeLevelBonusEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandChangeMembersLimitEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandChangeMobDropsEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandChangePaypalEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandChangePlayerPrivilegeEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandChangeRoleLimitEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandChangeRolePrivilegeEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandChangeSpawnerRatesEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandChangeWarpCategoryIconEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandChangeWarpCategorySlotEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandChangeWarpIconEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandChangeWarpLocationEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandChangeWarpsLimitEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandChangeWorthBonusEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandChatEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandChunkResetEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandClearGeneratorRatesEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandClearPlayerPrivilegesEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandClearRatingsEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandClearRolesPrivilegesEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandCloseEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandCloseWarpEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandCoopPlayerEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandCreateEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandCreateWarpCategoryEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandCreateWarpEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandDeleteWarpEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandDisableFlagEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandDisbandEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandEnableFlagEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandEnterEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandEnterProtectedEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandInviteEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandJoinEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandKickEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandLeaveEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandLeaveProtectedEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandLockWorldEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandOpenEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandOpenWarpEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandQuitEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandRateEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandRemoveBlockLimitEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandRemoveEffectEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandRemoveGeneratorRateEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandRemoveRatingEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandRemoveRoleLimitEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandRemoveVisitorHomeEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandRenameEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandRenameWarpCategoryEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandRenameWarpEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandRestrictMoveEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandSchematicPasteEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandSetHomeEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandSetVisitorHomeEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandTransferEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandUnbanEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandUncoopPlayerEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandUnlockWorldEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandUpgradeEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandWorldResetEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandWorthCalculatedEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandWorthUpdateEvent;
import com.bgsoftware.superiorskyblock.api.events.MissionCompleteEvent;
import com.bgsoftware.superiorskyblock.api.events.MissionResetEvent;
import com.bgsoftware.superiorskyblock.api.events.PlayerChangeRoleEvent;
import com.bgsoftware.superiorskyblock.api.events.PluginInitializeEvent;
import com.bgsoftware.superiorskyblock.api.events.PluginInitializedEvent;
import com.bgsoftware.superiorskyblock.api.events.PreIslandCreateEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.missions.IMissionsHolder;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.upgrades.UpgradeLevel;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCost;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.world.chunks.ChunkPosition;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public final class EventsBus {

    private final SuperiorSkyblockPlugin plugin;

    public EventsBus(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean callBlockStackEvent(Block block, Player player, int originalAmount, int newAmount) {
        return callEvent(() -> new BlockStackEvent(block, player, originalAmount, newAmount),
                "blockstackevent");
    }

    public boolean callBlockUnstackEvent(Block block, Player player, int originalAmount, int newAmount) {
        return callEvent(() -> new BlockUnstackEvent(block, player, originalAmount, newAmount),
                "blockunstackevent");
    }

    public void callIslandBanEvent(SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer, Island island) {
        if (!plugin.getSettings().getDisabledEvents().contains("islandbanevent")) {
            callEvent(new IslandBanEvent(superiorPlayer, targetPlayer, island));
        }
    }

    public EventResult<String> callIslandBankDepositEvent(SuperiorPlayer superiorPlayer, Island island, BigDecimal amount) {
        return callEvent(() -> new IslandBankDepositEvent(superiorPlayer, island, amount),
                "islandbankdepositevent", IslandBankDepositEvent::getFailureReason);
    }

    public EventResult<String> callIslandBankWithdrawEvent(SuperiorPlayer superiorPlayer, Island island, BigDecimal amount) {
        return callEvent(() -> new IslandBankWithdrawEvent(superiorPlayer, island, amount),
                "islandbankwithdrawevent", IslandBankWithdrawEvent::getFailureReason);
    }

    public EventResult<Biome> callIslandBiomeChangeEvent(SuperiorPlayer superiorPlayer, Island island, Biome biome) {
        return callEvent(() -> new IslandBiomeChangeEvent(superiorPlayer, island, biome),
                "islandbiomechangeevent", IslandBiomeChangeEvent::getBiome);
    }

    public EventResult<BigDecimal> callIslandChangeBankLimitEvent(CommandSender commandSender, Island island, BigDecimal bankLimit) {
        return callEvent(() -> new IslandChangeBankLimitEvent(getSuperiorPlayer(commandSender), island, bankLimit),
                "islandchangebanklimitevent", IslandChangeBankLimitEvent::getBankLimit);
    }

    public EventResult<Integer> callIslandChangeBlockLimitEvent(CommandSender commandSender, Island island, Key block, int blockLimit) {
        return callEvent(() -> new IslandChangeBlockLimitEvent(getSuperiorPlayer(commandSender), island, block, blockLimit),
                "islandchangeblocklimitevent", IslandChangeBlockLimitEvent::getBlockLimit);
    }

    public EventResult<Integer> callIslandChangeBorderSizeEvent(CommandSender commandSender, Island island, int borderSize) {
        return callEvent(() -> new IslandChangeBorderSizeEvent(getSuperiorPlayer(commandSender), island, borderSize),
                "islandchangebordersizeevent", IslandChangeBorderSizeEvent::getBorderSize);
    }

    public EventResult<Integer> callIslandChangeCoopLimitEvent(CommandSender commandSender, Island island, int coopLimit) {
        return callEvent(() -> new IslandChangeCoopLimitEvent(getSuperiorPlayer(commandSender), island, coopLimit),
                "islandchangecooplimitevent", IslandChangeCoopLimitEvent::getCoopLimit);
    }

    public EventResult<Double> callIslandChangeCropGrowthEvent(CommandSender commandSender, Island island, double cropGrowth) {
        return callEvent(() -> new IslandChangeCropGrowthEvent(getSuperiorPlayer(commandSender), island, cropGrowth),
                "islandchangecropgrowthevent", IslandChangeCropGrowthEvent::getCropGrowth);
    }

    public EventResult<String> callIslandChangeDescriptionEvent(SuperiorPlayer superiorPlayer, Island island, String description) {
        return callEvent(() -> new IslandChangeDescriptionEvent(island, superiorPlayer, description),
                "islandchangedescriptionevent", IslandChangeDescriptionEvent::getDescription);
    }

    public EventResult<String> callIslandChangeDiscordEvent(SuperiorPlayer superiorPlayer, Island island, String discord) {
        return callEvent(() -> new IslandChangeDiscordEvent(superiorPlayer, island, discord),
                "islandchangediscordevent", IslandChangeDiscordEvent::getDiscord);
    }

    public EventResult<Integer> callIslandChangeEffectLevelEvent(CommandSender commandSender, Island island,
                                                                 PotionEffectType effectType, int effectLevel) {
        return callEvent(() -> new IslandChangeEffectLevelEvent(getSuperiorPlayer(commandSender), island, effectType, effectLevel),
                "islandchangeeffectlevelevent", IslandChangeEffectLevelEvent::getEffectLevel);
    }

    public EventResult<Integer> callIslandChangeEntityLimitEvent(CommandSender commandSender, Island island, Key entity, int entityLimit) {
        return callEvent(() -> new IslandChangeEntityLimitEvent(getSuperiorPlayer(commandSender), island, entity, entityLimit),
                "islandchangeentitylimitevent", IslandChangeEntityLimitEvent::getEntityLimit);
    }

    public EventResult<Integer> callIslandChangeGeneratorRateEvent(CommandSender commandSender, Island island, Key block,
                                                                   World.Environment environment, int generatorRate) {
        return callIslandChangeGeneratorRateEvent(getSuperiorPlayer(commandSender), island, block, environment, generatorRate);
    }

    public EventResult<Integer> callIslandChangeGeneratorRateEvent(@Nullable SuperiorPlayer superiorPlayer, Island island, Key block,
                                                                   World.Environment environment, int generatorRate) {
        return callEvent(() -> new IslandChangeGeneratorRateEvent(superiorPlayer, island, block, environment, generatorRate),
                "islandchangegeneratorrateevent", IslandChangeGeneratorRateEvent::getGeneratorRate);
    }

    public EventResult<BigDecimal> callIslandChangeLevelBonusEvent(CommandSender commandSender, Island island,
                                                                   IslandChangeLevelBonusEvent.Reason reason,
                                                                   BigDecimal levelBonus) {
        return callEvent(() -> new IslandChangeLevelBonusEvent(getSuperiorPlayer(commandSender), island, reason, levelBonus),
                "islandchangelevelbonusevent", IslandChangeLevelBonusEvent::getLevelBonus);
    }

    public EventResult<Integer> callIslandChangeMembersLimitEvent(CommandSender commandSender, Island island, int membersLimit) {
        return callEvent(() -> new IslandChangeMembersLimitEvent(getSuperiorPlayer(commandSender), island, membersLimit),
                "islandchangememberslimitevent", IslandChangeMembersLimitEvent::getMembersLimit);
    }

    public EventResult<Double> callIslandChangeMobDropsEvent(CommandSender commandSender, Island island, double mobDrops) {
        return callEvent(() -> new IslandChangeMobDropsEvent(getSuperiorPlayer(commandSender), island, mobDrops),
                "islandchangemobdropsevent", IslandChangeMobDropsEvent::getMobDrops);
    }

    public EventResult<String> callIslandChangePaypalEvent(SuperiorPlayer superiorPlayer, Island island, String paypal) {
        return callEvent(() -> new IslandChangePaypalEvent(superiorPlayer, island, paypal),
                "islandchangepaypalevent", IslandChangePaypalEvent::getPaypal);
    }

    public boolean callIslandChangePlayerPrivilegeEvent(Island island, SuperiorPlayer superiorPlayer,
                                                        SuperiorPlayer privilegedPlayer, boolean privilegeEnabled) {
        return callEvent(() -> new IslandChangePlayerPrivilegeEvent(island, superiorPlayer, privilegedPlayer, privilegeEnabled),
                "islandchangeplayerprivilegeevent");
    }

    public EventResult<Integer> callIslandChangeRoleLimitEvent(CommandSender commandSender, Island island, PlayerRole playerRole, int roleLimit) {
        return callEvent(() -> new IslandChangeRoleLimitEvent(getSuperiorPlayer(commandSender), island, playerRole, roleLimit),
                "islandchangerolelimitevent", IslandChangeRoleLimitEvent::getRoleLimit);
    }

    public boolean callIslandChangeRolePrivilegeEvent(Island island, PlayerRole playerRole) {
        return callIslandChangeRolePrivilegeEvent(island, null, playerRole);
    }

    public EventResult<Double> callIslandChangeSpawnerRatesEvent(CommandSender commandSender, Island island, double spawnerRates) {
        return callEvent(() -> new IslandChangeSpawnerRatesEvent(getSuperiorPlayer(commandSender), island, spawnerRates),
                "islandchangespawnerratesevent", IslandChangeSpawnerRatesEvent::getSpawnerRates);
    }

    public EventResult<ItemStack> callIslandChangeWarpCategoryIconEvent(SuperiorPlayer superiorPlayer, Island island,
                                                                        WarpCategory warpCategory, @Nullable ItemStack icon) {
        return callEvent(() -> new IslandChangeWarpCategoryIconEvent(superiorPlayer, island, warpCategory, icon),
                "islandchangewarpcategoryiconevent", IslandChangeWarpCategoryIconEvent::getIcon);
    }

    public EventResult<Integer> callIslandChangeWarpCategorySlotEvent(SuperiorPlayer superiorPlayer, Island island,
                                                                      WarpCategory warpCategory, int slot, int maxSlot) {
        return callEvent(() -> new IslandChangeWarpCategorySlotEvent(superiorPlayer, island, warpCategory, slot, maxSlot),
                "islandchangewarpcategoryslotevent", IslandChangeWarpCategorySlotEvent::getSlot);
    }

    public EventResult<ItemStack> callIslandChangeWarpIconEvent(SuperiorPlayer superiorPlayer, Island island,
                                                                IslandWarp islandWarp, @Nullable ItemStack icon) {
        return callEvent(() -> new IslandChangeWarpIconEvent(superiorPlayer, island, islandWarp, icon),
                "islandchangewarpiconevent", IslandChangeWarpIconEvent::getIcon);
    }

    public EventResult<Location> callIslandChangeWarpLocationEvent(SuperiorPlayer superiorPlayer, Island island,
                                                                   IslandWarp islandWarp, Location location) {
        return callEvent(() -> new IslandChangeWarpLocationEvent(superiorPlayer, island, islandWarp, location),
                "islandchangewarplocationevent", IslandChangeWarpLocationEvent::getLocation);
    }

    public EventResult<Integer> callIslandChangeWarpsLimitEvent(CommandSender commandSender, Island island, int warpsLimit) {
        return callEvent(() -> new IslandChangeWarpsLimitEvent(getSuperiorPlayer(commandSender), island, warpsLimit),
                "islandchangewarpslimitevent", IslandChangeWarpsLimitEvent::getWarpsLimit);
    }

    public EventResult<BigDecimal> callIslandChangeWorthBonusEvent(CommandSender commandSender, Island island,
                                                                   IslandChangeWorthBonusEvent.Reason reason,
                                                                   BigDecimal worthBonus) {
        return callEvent(() -> new IslandChangeWorthBonusEvent(getSuperiorPlayer(commandSender), island, reason, worthBonus),
                "islandchangeworthbonusevent", IslandChangeWorthBonusEvent::getWorthBonus);
    }

    public boolean callIslandChangeRolePrivilegeEvent(Island island, @Nullable SuperiorPlayer superiorPlayer, PlayerRole playerRole) {
        return callEvent(() -> new IslandChangeRolePrivilegeEvent(island, superiorPlayer, playerRole),
                "islandchangeroleprivilegeevent");
    }

    public EventResult<String> callIslandChatEvent(Island island, SuperiorPlayer superiorPlayer, String message) {
        return callEvent(() -> new IslandChatEvent(island, superiorPlayer, message),
                "islandchatevent", IslandChatEvent::getMessage);
    }

    public void callIslandChunkResetEvent(Island island, ChunkPosition chunkPosition) {
        if (!plugin.getSettings().getDisabledEvents().contains("islandchunkresetevent")) {
            callEvent(new IslandChunkResetEvent(island, chunkPosition.getWorld(), chunkPosition.getX(), chunkPosition.getZ()));
        }
    }

    public boolean callIslandClearGeneratorRatesEvent(CommandSender commandSender, Island island, World.Environment environment) {
        return callEvent(() -> new IslandClearGeneratorRatesEvent(getSuperiorPlayer(commandSender), island, environment),
                "islandcleargeneratorratesevent");
    }

    public boolean callIslandClearPlayerPrivilegesEvent(Island island, SuperiorPlayer superiorPlayer,
                                                        SuperiorPlayer privilegedPlayer) {
        return callEvent(() -> new IslandClearPlayerPrivilegesEvent(island, superiorPlayer, privilegedPlayer),
                "islandclearplayerprivilegesevent");
    }

    public boolean callIslandClearRatingsEvent(CommandSender sender, Island island) {
        return callEvent(() -> new IslandClearRatingsEvent(getSuperiorPlayer(sender), island), "islandclearratingssevent");
    }

    public boolean callIslandClearRolesPrivilegesEvent(Island island, SuperiorPlayer superiorPlayer) {
        return callEvent(() -> new IslandClearRolesPrivilegesEvent(island, superiorPlayer),
                "islandclearrolesprivilegesevent");
    }

    public boolean callIslandCloseEvent(Island island, CommandSender commandSender) {
        return callIslandCloseEvent(island, getSuperiorPlayer(commandSender));
    }

    public boolean callIslandCloseEvent(Island island, @Nullable SuperiorPlayer superiorPlayer) {
        return callEvent(() -> new IslandCloseEvent(superiorPlayer, island), "islandcloseevent");
    }

    public boolean callIslandCloseWarpEvent(Island island, SuperiorPlayer superiorPlayer, IslandWarp islandWarp) {
        return callEvent(() -> new IslandCloseWarpEvent(superiorPlayer, island, islandWarp), "islandclosewarpevent");
    }

    public boolean callIslandCoopPlayerEvent(Island island, SuperiorPlayer player, SuperiorPlayer target) {
        return callEvent(() -> new IslandCoopPlayerEvent(island, player, target), "islandcoopplayerevent");
    }

    public EventResult<Boolean> callIslandCreateEvent(SuperiorPlayer superiorPlayer, Island island, String schemName) {
        return callEvent(() -> new IslandCreateEvent(superiorPlayer, island, schemName),
                "islandcreateevent", IslandCreateEvent::canTeleport);
    }

    public boolean callIslandCreateWarpCategoryEvent(SuperiorPlayer superiorPlayer, Island island, String categoryName) {
        return callEvent(() -> new IslandCreateWarpCategoryEvent(superiorPlayer, island, categoryName),
                "islandcreatewarpcategoryevent");
    }

    public boolean callIslandCreateWarpEvent(SuperiorPlayer superiorPlayer, Island island, String warpName,
                                             Location location, @Nullable WarpCategory warpCategory) {
        return callIslandCreateWarpEvent(superiorPlayer, island, warpName, location, plugin.getSettings().isPublicWarps(), warpCategory);
    }

    public boolean callIslandCreateWarpEvent(SuperiorPlayer superiorPlayer, Island island, String warpName,
                                             Location location, boolean openToPublic, @Nullable WarpCategory warpCategory) {
        return callEvent(() -> new IslandCreateWarpEvent(superiorPlayer, island, warpName, location, openToPublic, warpCategory),
                "islandcreatewarpevent");
    }

    public boolean callIslandDeleteWarpEvent(CommandSender commandSender, Island island, IslandWarp islandWarp) {
        return callIslandDeleteWarpEvent(getSuperiorPlayer(commandSender), island, islandWarp);
    }

    public boolean callIslandDeleteWarpEvent(@Nullable SuperiorPlayer superiorPlayer, Island island, IslandWarp islandWarp) {
        return callEvent(() -> new IslandDeleteWarpEvent(superiorPlayer, island, islandWarp), "islanddeletewarpevent");
    }

    public boolean callIslandDisableFlagEvent(CommandSender commandSender, Island island, IslandFlag islandFlag) {
        return callIslandDisableFlagEvent(getSuperiorPlayer(commandSender), island, islandFlag);
    }

    public boolean callIslandDisableFlagEvent(@Nullable SuperiorPlayer superiorPlayer, Island island, IslandFlag islandFlag) {
        return callEvent(() -> new IslandDisableFlagEvent(superiorPlayer, island, islandFlag), "islanddisableflagevent");
    }

    public boolean callIslandDisbandEvent(SuperiorPlayer superiorPlayer, Island island) {
        return callEvent(() -> new IslandDisbandEvent(superiorPlayer, island), "islanddisbandevent");
    }

    public boolean callIslandEnableFlagEvent(CommandSender commandSender, Island island, IslandFlag islandFlag) {
        return callIslandEnableFlagEvent(getSuperiorPlayer(commandSender), island, islandFlag);
    }

    public boolean callIslandEnableFlagEvent(@Nullable SuperiorPlayer superiorPlayer, Island island, IslandFlag islandFlag) {
        return callEvent(() -> new IslandEnableFlagEvent(superiorPlayer, island, islandFlag), "islandenableflagevent");
    }

    public boolean callIslandEnterEvent(SuperiorPlayer superiorPlayer, Island island, IslandEnterEvent.EnterCause enterCause) {
        if (plugin.getSettings().getDisabledEvents().contains("islandenterevent"))
            return true;

        IslandEnterEvent islandEnterEvent = callEvent(new IslandEnterEvent(superiorPlayer, island, enterCause));

        if (islandEnterEvent.isCancelled() && islandEnterEvent.getCancelTeleport() != null)
            superiorPlayer.teleport(islandEnterEvent.getCancelTeleport());

        return !islandEnterEvent.isCancelled();
    }

    public boolean callIslandEnterProtectedEvent(SuperiorPlayer superiorPlayer, Island island, IslandEnterEvent.EnterCause enterCause) {
        if (plugin.getSettings().getDisabledEvents().contains("islandenterprotectedevent"))
            return true;

        IslandEnterProtectedEvent islandEnterProtectedEvent = callEvent(new IslandEnterProtectedEvent(superiorPlayer, island, enterCause));

        if (islandEnterProtectedEvent.isCancelled() && islandEnterProtectedEvent.getCancelTeleport() != null)
            superiorPlayer.teleport(islandEnterProtectedEvent.getCancelTeleport());

        return !islandEnterProtectedEvent.isCancelled();
    }

    public boolean callIslandInviteEvent(SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer, Island island) {
        return callEvent(() -> new IslandInviteEvent(superiorPlayer, targetPlayer, island), "islandinviteevent");
    }

    @SuppressWarnings("all")
    public boolean callIslandJoinEvent(SuperiorPlayer superiorPlayer, Island island) {
        return callEvent(() -> new IslandJoinEvent(superiorPlayer, island), "islandjoinevent");
    }

    public void callIslandKickEvent(SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer, Island island) {
        if (!plugin.getSettings().getDisabledEvents().contains("islandkickevent")) {
            callEvent(new IslandKickEvent(superiorPlayer, targetPlayer, island));
        }
    }

    public boolean callIslandLeaveEvent(SuperiorPlayer superiorPlayer, Island island,
                                        IslandLeaveEvent.LeaveCause leaveCause, Location location) {
        return callEvent(() -> new IslandLeaveEvent(superiorPlayer, island, leaveCause, location), "islandleaveevent");
    }

    public boolean callIslandLeaveProtectedEvent(SuperiorPlayer superiorPlayer, Island island,
                                                 IslandLeaveEvent.LeaveCause leaveCause, Location location) {
        return callEvent(() -> new IslandLeaveProtectedEvent(superiorPlayer, island, leaveCause, location),
                "islandleaveprotectedevent");
    }

    public boolean callIslandLockWorldEvent(Island island, World.Environment environment) {
        return callEvent(() -> new IslandLockWorldEvent(island, environment), "islandlockworldevent");
    }

    public boolean callIslandOpenEvent(Island island, CommandSender commandSender) {
        return callIslandOpenEvent(island, getSuperiorPlayer(commandSender));
    }

    public boolean callIslandOpenEvent(Island island, @Nullable SuperiorPlayer superiorPlayer) {
        return callEvent(() -> new IslandOpenEvent(superiorPlayer, island), "islandopenevent");
    }

    public boolean callIslandOpenWarpEvent(Island island, SuperiorPlayer superiorPlayer, IslandWarp islandWarp) {
        return callEvent(() -> new IslandOpenWarpEvent(superiorPlayer, island, islandWarp), "islandopenwarpevent");
    }

    public boolean callIslandQuitEvent(SuperiorPlayer superiorPlayer, Island island) {
        return callEvent(() -> new IslandQuitEvent(superiorPlayer, island), "islandquitevent");
    }

    public boolean callIslandRateEvent(CommandSender commandSender, SuperiorPlayer ratingPlayer, Island island, Rating rating) {
        return callIslandRateEvent(getSuperiorPlayer(commandSender), ratingPlayer, island, rating);
    }

    public boolean callIslandRateEvent(@Nullable SuperiorPlayer superiorPlayer, SuperiorPlayer ratingPlayer, Island island, Rating rating) {
        return callEvent(() -> new IslandRateEvent(superiorPlayer, ratingPlayer, island, rating), "islandrateevent");
    }

    public boolean callIslandRemoveBlockLimitEvent(CommandSender commandSender, Island island, Key block) {
        return callEvent(() -> new IslandRemoveBlockLimitEvent(getSuperiorPlayer(commandSender), island, block),
                "islandremoveblocklimitevent");
    }

    public boolean callIslandRemoveEffectEvent(CommandSender commandSender, Island island, PotionEffectType effectType) {
        return callEvent(() -> new IslandRemoveEffectEvent(getSuperiorPlayer(commandSender), island, effectType),
                "islandremoveeffectevent");
    }

    public boolean callIslandRemoveGeneratorRateEvent(CommandSender commandSender, Island island, Key block,
                                                      World.Environment environment) {
        return callIslandRemoveGeneratorRateEvent(getSuperiorPlayer(commandSender), island, block, environment);
    }

    public boolean callIslandRemoveGeneratorRateEvent(@Nullable SuperiorPlayer superiorPlayer, Island island, Key block,
                                                      World.Environment environment) {
        return callEvent(() -> new IslandRemoveGeneratorRateEvent(superiorPlayer, island, block, environment),
                "islandremovegeneratorrateevent");
    }

    public boolean callIslandRemoveRatingEvent(CommandSender commandSender, SuperiorPlayer ratingPlayer, Island island) {
        return callIslandRemoveRatingEvent(getSuperiorPlayer(commandSender), ratingPlayer, island);
    }

    public boolean callIslandRemoveRatingEvent(@Nullable SuperiorPlayer superiorPlayer, SuperiorPlayer ratingPlayer, Island island) {
        return callEvent(() -> new IslandRemoveRatingEvent(superiorPlayer, ratingPlayer, island),
                "islandremoveratingevent");
    }

    public boolean callIslandRemoveRoleLimitEvent(CommandSender commandSender, Island island, PlayerRole playerRole) {
        return callEvent(() -> new IslandRemoveRoleLimitEvent(getSuperiorPlayer(commandSender), island, playerRole),
                "islandremoverolelimitevent");
    }

    public boolean callIslandRemoveVisitorHomeEvent(SuperiorPlayer superiorPlayer, Island island) {
        return callEvent(() -> new IslandRemoveVisitorHomeEvent(superiorPlayer, island), "islandremovevisitorhomeevent");
    }

    public EventResult<String> callIslandRenameEvent(Island island, String islandName) {
        return callIslandRenameEvent(island, null, islandName);
    }

    public EventResult<String> callIslandRenameEvent(Island island, @Nullable SuperiorPlayer superiorPlayer, String islandName) {
        return callEvent(() -> new IslandRenameEvent(island, superiorPlayer, islandName),
                "islandrenameevent", IslandRenameEvent::getIslandName);
    }

    public EventResult<String> callIslandRenameWarpCategoryEvent(Island island, SuperiorPlayer superiorPlayer,
                                                                 WarpCategory warpCategory, String categoryName) {
        return callEvent(() -> new IslandRenameWarpCategoryEvent(superiorPlayer, island, warpCategory, categoryName),
                "islandrenamewarpcategoryevent", IslandRenameWarpCategoryEvent::getCategoryName);
    }

    public EventResult<String> callIslandRenameWarpEvent(Island island, SuperiorPlayer superiorPlayer,
                                                         IslandWarp islandWarp, String warpName) {
        return callEvent(() -> new IslandRenameWarpEvent(superiorPlayer, island, islandWarp, warpName),
                "islandrenamewarpevent", IslandRenameWarpEvent::getWarpName);
    }

    public void callIslandRestrictMoveEvent(SuperiorPlayer superiorPlayer, IslandRestrictMoveEvent.RestrictReason restrictReason) {
        if (!plugin.getSettings().getDisabledEvents().contains("islandrestrictmoveevent")) {
            callEvent(new IslandRestrictMoveEvent(superiorPlayer, restrictReason));
        }
    }

    public void callIslandSchematicPasteEvent(Island island, String name, Location location) {
        if (!plugin.getSettings().getDisabledEvents().contains("islandschematicpasteevent")) {
            callEvent(new IslandSchematicPasteEvent(island, name, location));
        }
    }

    public EventResult<Location> callIslandSetHomeEvent(Island island, Location islandHome, IslandSetHomeEvent.Reason reason,
                                                        @Nullable SuperiorPlayer superiorPlayer) {
        return callEvent(() -> new IslandSetHomeEvent(island, islandHome, reason, superiorPlayer),
                "islandsethomeevent", IslandSetHomeEvent::getIslandHome);
    }

    public EventResult<Location> callIslandSetVisitorHomeEvent(SuperiorPlayer superiorPlayer, Island island,
                                                               Location islandVisitorHome) {
        return callEvent(() -> new IslandSetVisitorHomeEvent(superiorPlayer, island, islandVisitorHome),
                "islandsetvisitorhomeevent", IslandSetVisitorHomeEvent::getIslandVisitorHome);
    }

    public boolean callIslandTransferEvent(Island island, SuperiorPlayer previousOwner, SuperiorPlayer superiorPlayer) {
        return callEvent(() -> new IslandTransferEvent(island, previousOwner, superiorPlayer), "islandtransferevent");
    }

    public boolean callIslandUnbanEvent(SuperiorPlayer superiorPlayer, SuperiorPlayer unbannedPlayer, Island island) {
        return callEvent(() -> new IslandUnbanEvent(superiorPlayer, unbannedPlayer, island), "islandunbanevent");
    }

    public boolean callIslandUncoopPlayerEvent(Island island, SuperiorPlayer player, SuperiorPlayer target,
                                               IslandUncoopPlayerEvent.UncoopReason uncoopReason) {
        return callEvent(() -> new IslandUncoopPlayerEvent(island, player, target, uncoopReason), "islanduncoopplayerevent");
    }

    public boolean callIslandUnlockWorldEvent(Island island, World.Environment environment) {
        return callEvent(() -> new IslandUnlockWorldEvent(island, environment), "islandunlockworldevent");
    }

    public EventResult<UpgradeResult> callIslandUpgradeEvent(CommandSender commandSender, Island island,
                                                             Upgrade upgrade, UpgradeLevel upgradeLevel) {
        return callIslandUpgradeEvent(getSuperiorPlayer(commandSender), island, upgrade, upgradeLevel,
                Collections.emptyList(), null);
    }

    public EventResult<UpgradeResult> callIslandUpgradeEvent(@Nullable SuperiorPlayer superiorPlayer, Island island,
                                                             Upgrade upgrade, UpgradeLevel upgradeLevel) {
        return callIslandUpgradeEvent(superiorPlayer, island, upgrade, upgradeLevel, upgradeLevel.getCommands(), upgradeLevel.getCost());
    }

    public EventResult<UpgradeResult> callIslandUpgradeEvent(@Nullable SuperiorPlayer superiorPlayer, Island island,
                                                             Upgrade upgrade, UpgradeLevel upgradeLevel,
                                                             List<String> commands, @Nullable UpgradeCost upgradeCost) {
        return callEvent(() -> new IslandUpgradeEvent(superiorPlayer, island, upgrade, upgradeLevel,
                        commands, upgradeCost),
                "islandupgradeevent", UpgradeResult::new);
    }

    public boolean callIslandWorldResetEvent(CommandSender sender, Island island, World.Environment environment) {
        return callEvent(() -> new IslandWorldResetEvent(getSuperiorPlayer(sender), island, environment),
                "islandworldresetevent");
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

    public EventResult<MissionRewards> callMissionCompleteEvent(SuperiorPlayer superiorPlayer, IMissionsHolder missionsHolder,
                                                                Mission<?> mission, List<ItemStack> itemRewards,
                                                                List<String> commandRewards) {
        return callEvent(() -> new MissionCompleteEvent(superiorPlayer, missionsHolder, mission, itemRewards, commandRewards),
                "missioncompleteevent", MissionRewards::of);
    }

    public boolean callMissionResetEvent(CommandSender commandSender, IMissionsHolder missionsHolder, Mission<?> mission) {
        return callMissionResetEvent(getSuperiorPlayer(commandSender), missionsHolder, mission);
    }

    public boolean callMissionResetEvent(@Nullable SuperiorPlayer superiorPlayer, IMissionsHolder missionsHolder, Mission<?> mission) {
        return callEvent(() -> new MissionResetEvent(superiorPlayer, missionsHolder, mission), "missionresetevent");
    }

    public boolean callPlayerChangeRoleEvent(SuperiorPlayer superiorPlayer, PlayerRole newPlayer) {
        return callEvent(() -> new PlayerChangeRoleEvent(superiorPlayer, newPlayer), "playerchangeroleevent");
    }

    public void callPluginInitializedEvent(SuperiorSkyblock plugin) {
        callEvent(new PluginInitializedEvent(plugin));
    }

    public void callPluginInitializeEvent(SuperiorSkyblock plugin) {
        callEvent(new PluginInitializeEvent(plugin));
    }

    public boolean callPreIslandCreateEvent(SuperiorPlayer superiorPlayer, String islandName) {
        return callEvent(() -> new PreIslandCreateEvent(superiorPlayer, islandName), "preislandcreateevent");
    }

    private <T, E extends Event & Cancellable> EventResult<T> callEvent(Supplier<E> eventSupplier, String eventName,
                                                                        Function<E, T> getResultFunction) {
        if (plugin.getSettings().getDisabledEvents().contains(eventName))
            return EventResult.of(false, null);

        E event = eventSupplier.get();

        Bukkit.getPluginManager().callEvent(event);
        return EventResult.of(event.isCancelled(), getResultFunction.apply(event));
    }

    private <E extends Event & Cancellable> boolean callEvent(Supplier<E> eventSupplier, String eventName) {
        if (plugin.getSettings().getDisabledEvents().contains(eventName))
            return true;

        E event = eventSupplier.get();

        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    private static <T extends Event> T callEvent(T event) {
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    @Nullable
    private SuperiorPlayer getSuperiorPlayer(CommandSender commandSender) {
        return commandSender instanceof Player ? plugin.getPlayers().getSuperiorPlayer(commandSender) : null;
    }

    public static final class MissionRewards {

        private static final MissionRewards EMPTY = new MissionRewards(Collections.emptyList(), Collections.emptyList());

        private final List<ItemStack> itemRewards;
        private final List<String> commandRewards;

        private static MissionRewards of(MissionCompleteEvent missionCompleteEvent) {
            return missionCompleteEvent.getItemRewards().isEmpty() && missionCompleteEvent.getCommandRewards().isEmpty()
                    ? EMPTY : new MissionRewards(missionCompleteEvent.getItemRewards(), missionCompleteEvent.getCommandRewards());
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

        public UpgradeResult(IslandUpgradeEvent islandUpgradeEvent) {
            this.commands = islandUpgradeEvent.getCommands();
            this.upgradeCost = islandUpgradeEvent.getUpgradeCost();
        }

        public List<String> getCommands() {
            return commands;
        }

        public UpgradeCost getUpgradeCost() {
            return upgradeCost;
        }

    }

}
