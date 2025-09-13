package com.bgsoftware.superiorskyblock.core.events.plugin;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.events.IslandChangeLevelBonusEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandChangeWorthBonusEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandEnterEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandJoinEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandLeaveEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandRestrictMoveEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandSetHomeEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandUncoopPlayerEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandUpgradeEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.missions.IMissionsHolder;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.upgrades.UpgradeLevel;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCost;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs;
import org.bukkit.Location;
import org.bukkit.PortalType;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.*;
import static com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventType.*;

public class PluginEventsFactory {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    public static void callSettingsUpdateEvent() {
        fireEvent(SETTINGS_UPDATE_EVENT, Empty.INSTANCE);
    }

    public static void callCommandsUpdateEvent() {
        fireEvent(COMMANDS_UPDATE_EVENT, Empty.INSTANCE);
    }

    public static boolean callAttemptPlayerSendMessageEvent(SuperiorPlayer receiver, String messageType, Object... args) {
        AttemptPlayerSendMessage attemptPlayerSendMessage = new AttemptPlayerSendMessage();
        attemptPlayerSendMessage.receiver = receiver;
        attemptPlayerSendMessage.messageType = messageType;
        attemptPlayerSendMessage.args = args;
        return !fireEvent(ATTEMPT_PLAYER_SEND_MESSAGE_EVENT, attemptPlayerSendMessage).isCancelled();
    }

    public static boolean callBlockStackEvent(Block block, Player player, int originalCount, int newCount) {
        BlockStack blockStack = new BlockStack();
        blockStack.block = block;
        blockStack.player = player;
        blockStack.originalCount = originalCount;
        blockStack.newCount = newCount;
        return !fireEvent(BLOCK_STACK_EVENT, blockStack).isCancelled();
    }

    public static boolean callBlockUnstackEvent(Block block, Player player, int originalCount, int newCount) {
        BlockUnstack blockUnstack = new BlockUnstack();
        blockUnstack.block = block;
        blockUnstack.player = player;
        blockUnstack.originalCount = originalCount;
        blockUnstack.newCount = newCount;
        return !fireEvent(BLOCK_UNSTACK_EVENT, blockUnstack).isCancelled();
    }

    public static boolean callIslandBanEvent(Island island, SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer) {
        IslandBan islandBan = new IslandBan();
        islandBan.island = island;
        islandBan.superiorPlayer = superiorPlayer;
        islandBan.targetPlayer = targetPlayer;
        return !fireEvent(ISLAND_BAN_EVENT, islandBan).isCancelled();
    }

    public static PluginEvent<IslandBankDeposit> callIslandBankDepositEvent(Island island, CommandSender commandSender, BigDecimal amount) {
        return callIslandBankDepositEvent(island, commandSenderToSuperiorPlayer(commandSender), amount);
    }

    public static PluginEvent<IslandBankDeposit> callIslandBankDepositEvent(Island island, @Nullable SuperiorPlayer superiorPlayer, BigDecimal amount) {
        IslandBankDeposit islandBankDeposit = new IslandBankDeposit();
        islandBankDeposit.island = island;
        islandBankDeposit.superiorPlayer = superiorPlayer;
        islandBankDeposit.amount = amount;
        return fireEvent(ISLAND_BANK_DEPOSIT_EVENT, islandBankDeposit);
    }

    public static PluginEvent<IslandBankWithdraw> callIslandBankWithdrawEvent(Island island, CommandSender commandSender, BigDecimal amount) {
        return callIslandBankWithdrawEvent(island, commandSenderToSuperiorPlayer(commandSender), amount);
    }

    public static PluginEvent<IslandBankWithdraw> callIslandBankWithdrawEvent(Island island, @Nullable SuperiorPlayer superiorPlayer, BigDecimal amount) {
        IslandBankWithdraw islandBankWithdraw = new IslandBankWithdraw();
        islandBankWithdraw.island = island;
        islandBankWithdraw.superiorPlayer = superiorPlayer;
        islandBankWithdraw.amount = amount;
        return fireEvent(ISLAND_BANK_WITHDRAW_EVENT, islandBankWithdraw);
    }

    public static PluginEvent<IslandBiomeChange> callIslandBiomeChangeEvent(Island island, SuperiorPlayer superiorPlayer, Biome biome) {
        IslandBiomeChange islandBiomeChange = new IslandBiomeChange();
        islandBiomeChange.island = island;
        islandBiomeChange.superiorPlayer = superiorPlayer;
        islandBiomeChange.biome = biome;
        return fireEvent(ISLAND_BIOME_CHANGE_EVENT, islandBiomeChange);
    }

    public static PluginEvent<IslandChangeBankLimit> callIslandChangeBankLimitEvent(Island island, CommandSender commandSender, BigDecimal bankLimit) {
        return callIslandChangeBankLimitEvent(island, commandSenderToSuperiorPlayer(commandSender), bankLimit);
    }

    public static PluginEvent<IslandChangeBankLimit> callIslandChangeBankLimitEvent(Island island, @Nullable SuperiorPlayer superiorPlayer, BigDecimal bankLimit) {
        IslandChangeBankLimit islandChangeBankLimit = new IslandChangeBankLimit();
        islandChangeBankLimit.island = island;
        islandChangeBankLimit.superiorPlayer = superiorPlayer;
        islandChangeBankLimit.bankLimit = bankLimit;
        return fireEvent(ISLAND_CHANGE_BANK_LIMIT_EVENT, islandChangeBankLimit);
    }

    public static PluginEvent<IslandChangeBlockLimit> callIslandChangeBlockLimitEvent(Island island, CommandSender commandSender, Key block, int blockLimit) {
        return callIslandChangeBlockLimitEvent(island, commandSenderToSuperiorPlayer(commandSender), block, blockLimit);
    }

    public static PluginEvent<IslandChangeBlockLimit> callIslandChangeBlockLimitEvent(Island island, @Nullable SuperiorPlayer superiorPlayer, Key block, int blockLimit) {
        IslandChangeBlockLimit islandChangeBlockLimit = new IslandChangeBlockLimit();
        islandChangeBlockLimit.island = island;
        islandChangeBlockLimit.superiorPlayer = superiorPlayer;
        islandChangeBlockLimit.block = block;
        islandChangeBlockLimit.blockLimit = blockLimit;
        return fireEvent(ISLAND_CHANGE_BLOCK_LIMIT_EVENT, islandChangeBlockLimit);
    }

    public static PluginEvent<IslandChangeBorderSize> callIslandChangeBorderSizeEvent(Island island, CommandSender commandSender, int borderSize) {
        return callIslandChangeBorderSizeEvent(island, commandSenderToSuperiorPlayer(commandSender), borderSize);
    }

    public static PluginEvent<IslandChangeBorderSize> callIslandChangeBorderSizeEvent(Island island, @Nullable SuperiorPlayer superiorPlayer, int borderSize) {
        IslandChangeBorderSize islandChangeBorderSize = new IslandChangeBorderSize();
        islandChangeBorderSize.island = island;
        islandChangeBorderSize.superiorPlayer = superiorPlayer;
        islandChangeBorderSize.borderSize = borderSize;
        return fireEvent(ISLAND_CHANGE_BORDER_SIZE_EVENT, islandChangeBorderSize);
    }

    public static PluginEvent<IslandChangeCoopLimit> callIslandChangeCoopLimitEvent(Island island, CommandSender commandSender, int coopLimit) {
        return callIslandChangeCoopLimitEvent(island, commandSenderToSuperiorPlayer(commandSender), coopLimit);
    }

    public static PluginEvent<IslandChangeCoopLimit> callIslandChangeCoopLimitEvent(Island island, @Nullable SuperiorPlayer superiorPlayer, int coopLimit) {
        IslandChangeCoopLimit islandChangeCoopLimit = new IslandChangeCoopLimit();
        islandChangeCoopLimit.island = island;
        islandChangeCoopLimit.superiorPlayer = superiorPlayer;
        islandChangeCoopLimit.coopLimit = coopLimit;
        return fireEvent(ISLAND_CHANGE_COOP_LIMIT_EVENT, islandChangeCoopLimit);
    }

    public static PluginEvent<IslandChangeCropGrowth> callIslandChangeCropGrowthEvent(Island island, CommandSender commandSender, double cropGrowth) {
        return callIslandChangeCropGrowthEvent(island, commandSenderToSuperiorPlayer(commandSender), cropGrowth);
    }

    public static PluginEvent<IslandChangeCropGrowth> callIslandChangeCropGrowthEvent(Island island, @Nullable SuperiorPlayer superiorPlayer, double cropGrowth) {
        IslandChangeCropGrowth islandChangeCropGrowth = new IslandChangeCropGrowth();
        islandChangeCropGrowth.island = island;
        islandChangeCropGrowth.superiorPlayer = superiorPlayer;
        islandChangeCropGrowth.cropGrowth = cropGrowth;
        return fireEvent(ISLAND_CHANGE_CROP_GROWTH_EVENT, islandChangeCropGrowth);
    }

    public static PluginEvent<IslandChangeDescription> callIslandChangeDescriptionEvent(Island island, SuperiorPlayer superiorPlayer, String description) {
        IslandChangeDescription islandChangeDescription = new IslandChangeDescription();
        islandChangeDescription.island = island;
        islandChangeDescription.superiorPlayer = superiorPlayer;
        islandChangeDescription.description = description;
        return fireEvent(ISLAND_CHANGE_DESCRIPTION_EVENT, islandChangeDescription);
    }

    public static PluginEvent<IslandChangeDiscord> callIslandChangeDiscordEvent(Island island, SuperiorPlayer superiorPlayer, String discord) {
        IslandChangeDiscord islandChangeDiscord = new IslandChangeDiscord();
        islandChangeDiscord.island = island;
        islandChangeDiscord.superiorPlayer = superiorPlayer;
        islandChangeDiscord.discord = discord;
        return fireEvent(ISLAND_CHANGE_DISCORD_EVENT, islandChangeDiscord);
    }

    public static PluginEvent<IslandChangeEffectLevel> callIslandChangeEffectLevelEvent(Island island, CommandSender commandSender,
                                                                                        PotionEffectType effectType, int effectLevel) {
        return callIslandChangeEffectLevelEvent(island, commandSenderToSuperiorPlayer(commandSender), effectType, effectLevel);
    }

    public static PluginEvent<IslandChangeEffectLevel> callIslandChangeEffectLevelEvent(Island island, @Nullable SuperiorPlayer superiorPlayer,
                                                                                        PotionEffectType effectType, int effectLevel) {
        IslandChangeEffectLevel islandChangeEffectLevel = new IslandChangeEffectLevel();
        islandChangeEffectLevel.island = island;
        islandChangeEffectLevel.superiorPlayer = superiorPlayer;
        islandChangeEffectLevel.effectType = effectType;
        islandChangeEffectLevel.effectLevel = effectLevel;
        return fireEvent(ISLAND_CHANGE_EFFECT_LEVEL_EVENT, islandChangeEffectLevel);
    }

    public static PluginEvent<IslandChangeEntityLimit> callIslandChangeEntityLimitEvent(Island island, CommandSender commandSender,
                                                                                        Key entity, int entityLimit) {
        return callIslandChangeEntityLimitEvent(island, commandSenderToSuperiorPlayer(commandSender), entity, entityLimit);
    }

    public static PluginEvent<IslandChangeEntityLimit> callIslandChangeEntityLimitEvent(Island island, @Nullable SuperiorPlayer superiorPlayer,
                                                                                        Key entity, int entityLimit) {
        IslandChangeEntityLimit islandChangeEntityLimit = new IslandChangeEntityLimit();
        islandChangeEntityLimit.island = island;
        islandChangeEntityLimit.superiorPlayer = superiorPlayer;
        islandChangeEntityLimit.entity = entity;
        islandChangeEntityLimit.entityLimit = entityLimit;
        return fireEvent(ISLAND_CHANGE_ENTITY_LIMIT_EVENT, islandChangeEntityLimit);
    }

    public static PluginEvent<IslandChangeGeneratorRate> callIslandChangeGeneratorRateEvent(Island island, CommandSender commandSender,
                                                                                            Key block, Dimension dimension, int generatorRate) {
        return callIslandChangeGeneratorRateEvent(island, commandSenderToSuperiorPlayer(commandSender), block, dimension, generatorRate);
    }

    public static PluginEvent<IslandChangeGeneratorRate> callIslandChangeGeneratorRateEvent(Island island, @Nullable SuperiorPlayer superiorPlayer,
                                                                                            Key block, Dimension dimension, int generatorRate) {
        IslandChangeGeneratorRate islandChangeGeneratorRate = new IslandChangeGeneratorRate();
        islandChangeGeneratorRate.island = island;
        islandChangeGeneratorRate.superiorPlayer = superiorPlayer;
        islandChangeGeneratorRate.block = block;
        islandChangeGeneratorRate.dimension = dimension;
        islandChangeGeneratorRate.generatorRate = generatorRate;
        return fireEvent(ISLAND_CHANGE_GENERATOR_RATE_EVENT, islandChangeGeneratorRate);
    }

    public static PluginEvent<IslandChangeLevelBonus> callIslandChangeLevelBonusEvent(Island island, CommandSender commandSender,
                                                                                      IslandChangeLevelBonusEvent.Reason reason, BigDecimal levelBonus) {
        return callIslandChangeLevelBonusEvent(island, commandSenderToSuperiorPlayer(commandSender), reason, levelBonus);
    }

    public static PluginEvent<IslandChangeLevelBonus> callIslandChangeLevelBonusEvent(Island island, @Nullable SuperiorPlayer superiorPlayer,
                                                                                      IslandChangeLevelBonusEvent.Reason reason, BigDecimal levelBonus) {
        IslandChangeLevelBonus islandChangeLevelBonus = new IslandChangeLevelBonus();
        islandChangeLevelBonus.island = island;
        islandChangeLevelBonus.superiorPlayer = superiorPlayer;
        islandChangeLevelBonus.reason = reason;
        islandChangeLevelBonus.levelBonus = levelBonus;
        return fireEvent(ISLAND_CHANGE_LEVEL_BONUS_EVENT, islandChangeLevelBonus);
    }

    public static PluginEvent<IslandChangeMembersLimit> callIslandChangeMembersLimitEvent(Island island, CommandSender commandSender, int membersLimit) {
        return callIslandChangeMembersLimitEvent(island, commandSenderToSuperiorPlayer(commandSender), membersLimit);
    }

    public static PluginEvent<IslandChangeMembersLimit> callIslandChangeMembersLimitEvent(Island island, @Nullable SuperiorPlayer superiorPlayer, int membersLimit) {
        IslandChangeMembersLimit islandChangeMembersLimit = new IslandChangeMembersLimit();
        islandChangeMembersLimit.island = island;
        islandChangeMembersLimit.superiorPlayer = superiorPlayer;
        islandChangeMembersLimit.membersLimit = membersLimit;
        return fireEvent(ISLAND_CHANGE_MEMBERS_LIMIT_EVENT, islandChangeMembersLimit);
    }

    public static PluginEvent<IslandChangeMobDrops> callIslandChangeMobDropsEvent(Island island, CommandSender commandSender, double mobDrops) {
        return callIslandChangeMobDropsEvent(island, commandSenderToSuperiorPlayer(commandSender), mobDrops);
    }

    public static PluginEvent<IslandChangeMobDrops> callIslandChangeMobDropsEvent(Island island, @Nullable SuperiorPlayer superiorPlayer, double mobDrops) {
        IslandChangeMobDrops islandChangeMobDrops = new IslandChangeMobDrops();
        islandChangeMobDrops.island = island;
        islandChangeMobDrops.superiorPlayer = superiorPlayer;
        islandChangeMobDrops.mobDrops = mobDrops;
        return fireEvent(ISLAND_CHANGE_MOB_DROPS_EVENT, islandChangeMobDrops);
    }

    public static PluginEvent<IslandChangePaypal> callIslandChangePaypalEvent(Island island, SuperiorPlayer superiorPlayer, String paypal) {
        IslandChangePaypal islandChangePaypal = new IslandChangePaypal();
        islandChangePaypal.island = island;
        islandChangePaypal.superiorPlayer = superiorPlayer;
        islandChangePaypal.paypal = paypal;
        return fireEvent(ISLAND_CHANGE_PAYPAL_EVENT, islandChangePaypal);
    }

    public static boolean callIslandChangePlayerPrivilegeEvent(Island island, SuperiorPlayer superiorPlayer,
                                                               SuperiorPlayer privilegedPlayer, boolean privilegeEnabled) {
        IslandChangePlayerPrivilege islandChangePlayerPrivilege = new IslandChangePlayerPrivilege();
        islandChangePlayerPrivilege.island = island;
        islandChangePlayerPrivilege.superiorPlayer = superiorPlayer;
        islandChangePlayerPrivilege.privilegedPlayer = privilegedPlayer;
        islandChangePlayerPrivilege.privilegeEnabled = privilegeEnabled;
        return !fireEvent(ISLAND_CHANGE_PLAYER_PRIVILEGE_EVENT, islandChangePlayerPrivilege).isCancelled();
    }

    public static PluginEvent<IslandChangeRoleLimit> callIslandChangeRoleLimitEvent(Island island, CommandSender commandSender,
                                                                                    PlayerRole playerRole, int roleLimit) {
        return callIslandChangeRoleLimitEvent(island, commandSenderToSuperiorPlayer(commandSender), playerRole, roleLimit);
    }

    public static PluginEvent<IslandChangeRoleLimit> callIslandChangeRoleLimitEvent(Island island, @Nullable SuperiorPlayer superiorPlayer,
                                                                                    PlayerRole playerRole, int roleLimit) {
        IslandChangeRoleLimit islandChangeRoleLimit = new IslandChangeRoleLimit();
        islandChangeRoleLimit.island = island;
        islandChangeRoleLimit.superiorPlayer = superiorPlayer;
        islandChangeRoleLimit.playerRole = playerRole;
        islandChangeRoleLimit.roleLimit = roleLimit;
        return fireEvent(ISLAND_CHANGE_ROLE_LIMIT_EVENT, islandChangeRoleLimit);
    }

    public static PluginEvent<IslandChangeSpawnerRates> callIslandChangeSpawnerRatesEvent(Island island, CommandSender commandSender, double spawnerRates) {
        return callIslandChangeSpawnerRatesEvent(island, commandSenderToSuperiorPlayer(commandSender), spawnerRates);
    }

    public static PluginEvent<IslandChangeSpawnerRates> callIslandChangeSpawnerRatesEvent(Island island, @Nullable SuperiorPlayer superiorPlayer, double spawnerRates) {
        IslandChangeSpawnerRates islandChangeSpawnerRates = new IslandChangeSpawnerRates();
        islandChangeSpawnerRates.island = island;
        islandChangeSpawnerRates.superiorPlayer = superiorPlayer;
        islandChangeSpawnerRates.spawnerRates = spawnerRates;
        return fireEvent(ISLAND_CHANGE_SPAWNER_RATES_EVENT, islandChangeSpawnerRates);
    }

    public static PluginEvent<IslandChangeWarpCategoryIcon> callIslandChangeWarpCategoryIconEvent(Island island, SuperiorPlayer superiorPlayer,
                                                                                                  WarpCategory warpCategory, @Nullable ItemStack icon) {
        IslandChangeWarpCategoryIcon islandChangeWarpCategoryIcon = new IslandChangeWarpCategoryIcon();
        islandChangeWarpCategoryIcon.island = island;
        islandChangeWarpCategoryIcon.superiorPlayer = superiorPlayer;
        islandChangeWarpCategoryIcon.warpCategory = warpCategory;
        islandChangeWarpCategoryIcon.icon = icon;
        return fireEvent(ISLAND_CHANGE_WARP_CATEGORY_ICON_EVENT, islandChangeWarpCategoryIcon);
    }

    public static PluginEvent<IslandChangeWarpCategorySlot> callIslandChangeWarpCategorySlotEvent(Island island, SuperiorPlayer superiorPlayer,
                                                                                                  WarpCategory warpCategory, int slot, int maxSlot) {
        IslandChangeWarpCategorySlot islandChangeWarpCategorySlot = new IslandChangeWarpCategorySlot();
        islandChangeWarpCategorySlot.island = island;
        islandChangeWarpCategorySlot.superiorPlayer = superiorPlayer;
        islandChangeWarpCategorySlot.warpCategory = warpCategory;
        islandChangeWarpCategorySlot.slot = slot;
        islandChangeWarpCategorySlot.maxSlot = maxSlot;
        return fireEvent(ISLAND_CHANGE_WARP_CATEGORY_SLOT_EVENT, islandChangeWarpCategorySlot);
    }

    public static PluginEvent<IslandChangeWarpIcon> callIslandChangeWarpIconEvent(Island island, SuperiorPlayer superiorPlayer,
                                                                                  IslandWarp islandWarp, @Nullable ItemStack icon) {
        IslandChangeWarpIcon islandChangeWarpIcon = new IslandChangeWarpIcon();
        islandChangeWarpIcon.island = island;
        islandChangeWarpIcon.superiorPlayer = superiorPlayer;
        islandChangeWarpIcon.islandWarp = islandWarp;
        islandChangeWarpIcon.icon = icon;
        return fireEvent(ISLAND_CHANGE_WARP_ICON_EVENT, islandChangeWarpIcon);
    }

    public static PluginEvent<IslandChangeWarpLocation> callIslandChangeWarpLocationEvent(Island island, Player player,
                                                                                          IslandWarp islandWarp, Location location) {
        return callIslandChangeWarpLocationEvent(island, commandSenderToSuperiorPlayer(player), islandWarp, location);
    }

    public static PluginEvent<IslandChangeWarpLocation> callIslandChangeWarpLocationEvent(Island island, SuperiorPlayer superiorPlayer,
                                                                                          IslandWarp islandWarp, Location location) {
        IslandChangeWarpLocation islandChangeWarpLocation = new IslandChangeWarpLocation();
        islandChangeWarpLocation.island = island;
        islandChangeWarpLocation.superiorPlayer = superiorPlayer;
        islandChangeWarpLocation.islandWarp = islandWarp;
        islandChangeWarpLocation.location = location;
        return fireEvent(ISLAND_CHANGE_WARP_LOCATION_EVENT, islandChangeWarpLocation);
    }

    public static PluginEvent<IslandChangeWarpsLimit> callIslandChangeWarpsLimitEvent(Island island, CommandSender commandSender, int warpsLimit) {
        return callIslandChangeWarpsLimitEvent(island, commandSenderToSuperiorPlayer(commandSender), warpsLimit);
    }

    public static PluginEvent<IslandChangeWarpsLimit> callIslandChangeWarpsLimitEvent(Island island, @Nullable SuperiorPlayer superiorPlayer, int warpsLimit) {
        IslandChangeWarpsLimit islandChangeWarpsLimit = new IslandChangeWarpsLimit();
        islandChangeWarpsLimit.island = island;
        islandChangeWarpsLimit.superiorPlayer = superiorPlayer;
        islandChangeWarpsLimit.warpsLimit = warpsLimit;
        return fireEvent(ISLAND_CHANGE_WARPS_LIMIT_EVENT, islandChangeWarpsLimit);
    }

    public static PluginEvent<IslandChangeWorthBonus> callIslandChangeWorthBonusEvent(Island island, CommandSender commandSender,
                                                                                      IslandChangeWorthBonusEvent.Reason reason, BigDecimal worthBonus) {
        return callIslandChangeWorthBonusEvent(island, commandSenderToSuperiorPlayer(commandSender), reason, worthBonus);
    }

    public static PluginEvent<IslandChangeWorthBonus> callIslandChangeWorthBonusEvent(Island island, @Nullable SuperiorPlayer superiorPlayer,
                                                                                      IslandChangeWorthBonusEvent.Reason reason, BigDecimal worthBonus) {
        IslandChangeWorthBonus islandChangeWorthBonus = new IslandChangeWorthBonus();
        islandChangeWorthBonus.island = island;
        islandChangeWorthBonus.superiorPlayer = superiorPlayer;
        islandChangeWorthBonus.reason = reason;
        islandChangeWorthBonus.worthBonus = worthBonus;
        return fireEvent(ISLAND_CHANGE_WORTH_BONUS_EVENT, islandChangeWorthBonus);
    }

    public static boolean callIslandChangeRolePrivilegeEvent(Island island, CommandSender commandSender, PlayerRole playerRole) {
        return callIslandChangeRolePrivilegeEvent(island, commandSenderToSuperiorPlayer(commandSender), playerRole);
    }

    public static boolean callIslandChangeRolePrivilegeEvent(Island island, @Nullable SuperiorPlayer superiorPlayer, PlayerRole playerRole) {
        IslandChangeRolePrivilege islandChangeRolePrivilege = new IslandChangeRolePrivilege();
        islandChangeRolePrivilege.island = island;
        islandChangeRolePrivilege.superiorPlayer = superiorPlayer;
        islandChangeRolePrivilege.playerRole = playerRole;
        return !fireEvent(ISLAND_CHANGE_ROLE_PRIVILEGE_EVENT, islandChangeRolePrivilege).isCancelled();
    }

    public static PluginEvent<IslandChat> callIslandChatEvent(Island island, SuperiorPlayer superiorPlayer, String message) {
        IslandChat islandChat = new IslandChat();
        islandChat.island = island;
        islandChat.superiorPlayer = superiorPlayer;
        islandChat.message = message;
        return fireEvent(ISLAND_CHAT_EVENT, islandChat);
    }

    public static void callIslandChunkResetEvent(Island island, ChunkPosition chunkPosition) {
        IslandChunkReset islandChunkReset = new IslandChunkReset();
        islandChunkReset.island = island;
        islandChunkReset.chunkPosition = chunkPosition;
        fireEvent(ISLAND_CHUNK_RESET_EVENT, islandChunkReset);
    }

    public static boolean callIslandClearFlagsEvent(Island island, CommandSender commandSender) {
        return callIslandClearFlagsEvent(island, commandSenderToSuperiorPlayer(commandSender));
    }

    public static boolean callIslandClearFlagsEvent(Island island, SuperiorPlayer superiorPlayer) {
        IslandClearFlags islandClearFlags = new IslandClearFlags();
        islandClearFlags.island = island;
        islandClearFlags.superiorPlayer = superiorPlayer;
        return !fireEvent(ISLAND_CLEAR_FLAGS_EVENT, islandClearFlags).isCancelled();
    }

    public static boolean callIslandClearGeneratorRatesEvent(Island island, CommandSender commandSender, Dimension dimension) {
        return callIslandClearGeneratorRatesEvent(island, commandSenderToSuperiorPlayer(commandSender), dimension);
    }

    public static boolean callIslandClearGeneratorRatesEvent(Island island, @Nullable SuperiorPlayer superiorPlayer, Dimension dimension) {
        IslandClearGeneratorRates islandClearGeneratorRates = new IslandClearGeneratorRates();
        islandClearGeneratorRates.island = island;
        islandClearGeneratorRates.superiorPlayer = superiorPlayer;
        islandClearGeneratorRates.dimension = dimension;
        return !fireEvent(ISLAND_CLEAR_GENERATOR_RATES_EVENT, islandClearGeneratorRates).isCancelled();
    }

    public static boolean callIslandClearPlayerPrivilegesEvent(Island island, SuperiorPlayer superiorPlayer,
                                                               SuperiorPlayer privilegedPlayer) {
        IslandClearPlayerPrivileges islandClearPlayerPrivileges = new IslandClearPlayerPrivileges();
        islandClearPlayerPrivileges.island = island;
        islandClearPlayerPrivileges.superiorPlayer = superiorPlayer;
        islandClearPlayerPrivileges.privilegedPlayer = privilegedPlayer;
        return !fireEvent(ISLAND_CLEAR_PLAYER_PRIVILEGES_EVENT, islandClearPlayerPrivileges).isCancelled();
    }

    public static boolean callIslandClearRatingsEvent(Island island, CommandSender commandSender) {
        return callIslandClearRatingsEvent(island, commandSenderToSuperiorPlayer(commandSender));
    }

    public static boolean callIslandClearRatingsEvent(Island island, @Nullable SuperiorPlayer superiorPlayer) {
        IslandClearRatings islandClearRatings = new IslandClearRatings();
        islandClearRatings.island = island;
        islandClearRatings.superiorPlayer = superiorPlayer;
        return !fireEvent(ISLAND_CLEAR_RATINGS_EVENT, islandClearRatings).isCancelled();
    }

    public static boolean callIslandClearRolesPrivilegesEvent(Island island, CommandSender commandSender) {
        return callIslandClearRolesPrivilegesEvent(island, commandSenderToSuperiorPlayer(commandSender));
    }

    public static boolean callIslandClearRolesPrivilegesEvent(Island island, SuperiorPlayer superiorPlayer) {
        IslandClearRolesPrivileges islandClearRolesPrivileges = new IslandClearRolesPrivileges();
        islandClearRolesPrivileges.island = island;
        islandClearRolesPrivileges.superiorPlayer = superiorPlayer;
        return !fireEvent(ISLAND_CLEAR_ROLES_PRIVILEGES_EVENT, islandClearRolesPrivileges).isCancelled();
    }

    public static boolean callIslandCloseEvent(Island island, CommandSender commandSender) {
        return callIslandCloseEvent(island, commandSenderToSuperiorPlayer(commandSender));
    }

    public static boolean callIslandCloseEvent(Island island, @Nullable SuperiorPlayer superiorPlayer) {
        IslandClose islandClose = new IslandClose();
        islandClose.island = island;
        islandClose.superiorPlayer = superiorPlayer;
        return !fireEvent(ISLAND_CLOSE_EVENT, islandClose).isCancelled();
    }

    public static boolean callIslandCloseWarpEvent(Island island, SuperiorPlayer superiorPlayer, IslandWarp islandWarp) {
        IslandCloseWarp islandCloseWarp = new IslandCloseWarp();
        islandCloseWarp.island = island;
        islandCloseWarp.superiorPlayer = superiorPlayer;
        islandCloseWarp.islandWarp = islandWarp;
        return !fireEvent(ISLAND_CLOSE_WARP_EVENT, islandCloseWarp).isCancelled();
    }

    public static boolean callIslandCoopPlayerEvent(Island island, SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer) {
        IslandCoopPlayer islandCoopPlayer = new IslandCoopPlayer();
        islandCoopPlayer.island = island;
        islandCoopPlayer.superiorPlayer = superiorPlayer;
        islandCoopPlayer.targetPlayer = targetPlayer;
        return !fireEvent(ISLAND_COOP_PLAYER_EVENT, islandCoopPlayer).isCancelled();
    }

    public static PluginEvent<IslandCreate> callIslandCreateEvent(Island island, SuperiorPlayer superiorPlayer, String schematicName, boolean canTeleport) {
        IslandCreate islandCreate = new IslandCreate();
        islandCreate.island = island;
        islandCreate.superiorPlayer = superiorPlayer;
        islandCreate.schematicName = schematicName;
        islandCreate.canTeleport = canTeleport;
        return fireEvent(ISLAND_CREATE_EVENT, islandCreate);
    }

    public static boolean callIslandCreateWarpCategoryEvent(Island island, SuperiorPlayer superiorPlayer, String categoryName) {
        IslandCreateWarpCategory islandCreateWarpCategory = new IslandCreateWarpCategory();
        islandCreateWarpCategory.island = island;
        islandCreateWarpCategory.superiorPlayer = superiorPlayer;
        islandCreateWarpCategory.categoryName = categoryName;
        return !fireEvent(ISLAND_CREATE_WARP_CATEGORY_EVENT, islandCreateWarpCategory).isCancelled();
    }

    public static boolean callIslandCreateWarpEvent(Island island, SuperiorPlayer superiorPlayer, String warpName,
                                                    Location location, @Nullable WarpCategory warpCategory) {
        return callIslandCreateWarpEvent(island, superiorPlayer, warpName, location, plugin.getSettings().isPublicWarps(), warpCategory);
    }

    public static boolean callIslandCreateWarpEvent(Island island, SuperiorPlayer superiorPlayer, String warpName,
                                                    Location location, boolean openToPublic, @Nullable WarpCategory warpCategory) {
        IslandCreateWarp islandCreateWarp = new IslandCreateWarp();
        islandCreateWarp.island = island;
        islandCreateWarp.superiorPlayer = superiorPlayer;
        islandCreateWarp.warpName = warpName;
        islandCreateWarp.location = location;
        islandCreateWarp.openToPublic = openToPublic;
        islandCreateWarp.warpCategory = warpCategory;
        return !fireEvent(ISLAND_CREATE_WARP_EVENT, islandCreateWarp).isCancelled();
    }

    public static boolean callIslandDeleteWarpEvent(Island island, CommandSender commandSender, IslandWarp islandWarp) {
        return callIslandDeleteWarpEvent(island, commandSenderToSuperiorPlayer(commandSender), islandWarp);
    }

    public static boolean callIslandDeleteWarpEvent(Island island, @Nullable SuperiorPlayer superiorPlayer, IslandWarp islandWarp) {
        IslandDeleteWarp islandDeleteWarp = new IslandDeleteWarp();
        islandDeleteWarp.island = island;
        islandDeleteWarp.superiorPlayer = superiorPlayer;
        islandDeleteWarp.islandWarp = islandWarp;
        return !fireEvent(ISLAND_DELETE_WARP_EVENT, islandDeleteWarp).isCancelled();
    }

    public static boolean callIslandDisableFlagEvent(Island island, CommandSender commandSender, IslandFlag islandFlag) {
        return callIslandDisableFlagEvent(island, commandSenderToSuperiorPlayer(commandSender), islandFlag);
    }

    public static boolean callIslandDisableFlagEvent(Island island, @Nullable SuperiorPlayer superiorPlayer, IslandFlag islandFlag) {
        IslandDisableFlag islandDisableFlag = new IslandDisableFlag();
        islandDisableFlag.island = island;
        islandDisableFlag.superiorPlayer = superiorPlayer;
        islandDisableFlag.islandFlag = islandFlag;
        return !fireEvent(ISLAND_DISABLE_FLAG_EVENT, islandDisableFlag).isCancelled();
    }

    public static boolean callIslandDisbandEvent(Island island, SuperiorPlayer superiorPlayer) {
        IslandDisband islandDisband = new IslandDisband();
        islandDisband.island = island;
        islandDisband.superiorPlayer = superiorPlayer;
        return !fireEvent(ISLAND_DISBAND_EVENT, islandDisband).isCancelled();
    }

    public static boolean callIslandEnableFlagEvent(Island island, CommandSender commandSender, IslandFlag islandFlag) {
        return callIslandEnableFlagEvent(island, commandSenderToSuperiorPlayer(commandSender), islandFlag);
    }

    public static boolean callIslandEnableFlagEvent(Island island, @Nullable SuperiorPlayer superiorPlayer, IslandFlag islandFlag) {
        IslandEnableFlag islandEnableFlag = new IslandEnableFlag();
        islandEnableFlag.island = island;
        islandEnableFlag.superiorPlayer = superiorPlayer;
        islandEnableFlag.islandFlag = islandFlag;
        return !fireEvent(ISLAND_ENABLE_FLAG_EVENT, islandEnableFlag).isCancelled();
    }

    public static boolean callIslandEnterEvent(Island island, SuperiorPlayer superiorPlayer, IslandEnterEvent.EnterCause enterCause) {
        IslandEnter islandEnter = new IslandEnter();
        islandEnter.island = island;
        islandEnter.superiorPlayer = superiorPlayer;
        islandEnter.enterCause = enterCause;
        return !fireEvent(ISLAND_ENTER_EVENT, islandEnter).isCancelled();
    }

    public static PluginEvent<IslandEnterPortal> callIslandEnterPortalEvent(Island island, SuperiorPlayer superiorPlayer,
                                                                            PortalType portalType, Dimension destination,
                                                                            @Nullable Schematic schematic, boolean ignoreInvalidSchematic) {
        IslandEnterPortal islandEnterPortal = new IslandEnterPortal();
        islandEnterPortal.island = island;
        islandEnterPortal.superiorPlayer = superiorPlayer;
        islandEnterPortal.portalType = portalType;
        islandEnterPortal.destination = destination;
        islandEnterPortal.schematic = schematic;
        islandEnterPortal.ignoreInvalidSchematic = ignoreInvalidSchematic;
        return fireEvent(ISLAND_ENTER_PORTAL_EVENT, islandEnterPortal);
    }

    public static boolean callIslandEnterProtectedEvent(Island island, SuperiorPlayer superiorPlayer, IslandEnterEvent.EnterCause enterCause) {
        IslandEnterProtected islandEnterProtected = new IslandEnterProtected();
        islandEnterProtected.island = island;
        islandEnterProtected.superiorPlayer = superiorPlayer;
        islandEnterProtected.enterCause = enterCause;
        return !fireEvent(ISLAND_ENTER_PROTECTED_EVENT, islandEnterProtected).isCancelled();
    }

    public static PluginEvent<IslandGenerateBlock> callIslandGenerateBlockEvent(Island island, Location location, Key block) {
        IslandGenerateBlock islandGenerateBlock = new IslandGenerateBlock();
        islandGenerateBlock.island = island;
        islandGenerateBlock.location = location;
        islandGenerateBlock.block = block;
        return fireEvent(ISLAND_GENERATE_BLOCK_EVENT, islandGenerateBlock);
    }

    public static boolean callIslandInviteEvent(Island island, SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer) {
        IslandInvite islandInvite = new IslandInvite();
        islandInvite.island = island;
        islandInvite.superiorPlayer = superiorPlayer;
        islandInvite.targetPlayer = targetPlayer;
        return !fireEvent(ISLAND_INVITE_EVENT, islandInvite).isCancelled();
    }

    public static boolean callIslandJoinEvent(Island island, SuperiorPlayer superiorPlayer, IslandJoinEvent.Cause joinCause) {
        IslandJoin islandJoin = new IslandJoin();
        islandJoin.island = island;
        islandJoin.superiorPlayer = superiorPlayer;
        islandJoin.joinCause = joinCause;
        return !fireEvent(ISLAND_JOIN_EVENT, islandJoin).isCancelled();
    }

    public static boolean callIslandKickEvent(Island island, SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer) {
        IslandKick islandKick = new IslandKick();
        islandKick.island = island;
        islandKick.superiorPlayer = superiorPlayer;
        islandKick.targetPlayer = targetPlayer;
        return !fireEvent(ISLAND_KICK_EVENT, islandKick).isCancelled();
    }

    public static boolean callIslandLeaveEvent(Island island, SuperiorPlayer superiorPlayer,
                                               IslandLeaveEvent.LeaveCause leaveCause, Location location) {
        IslandLeave islandLeave = new IslandLeave();
        islandLeave.island = island;
        islandLeave.superiorPlayer = superiorPlayer;
        islandLeave.leaveCause = leaveCause;
        islandLeave.location = location;
        return !fireEvent(ISLAND_LEAVE_EVENT, islandLeave).isCancelled();
    }

    public static boolean callIslandLeaveProtectedEvent(Island island, SuperiorPlayer superiorPlayer,
                                                        IslandLeaveEvent.LeaveCause leaveCause, Location location) {
        IslandLeaveProtected islandLeaveProtected = new IslandLeaveProtected();
        islandLeaveProtected.island = island;
        islandLeaveProtected.superiorPlayer = superiorPlayer;
        islandLeaveProtected.leaveCause = leaveCause;
        islandLeaveProtected.location = location;
        return !fireEvent(ISLAND_LEAVE_PROTECTED_EVENT, islandLeaveProtected).isCancelled();
    }

    public static boolean callIslandLockWorldEvent(Island island, CommandSender commandSender, Dimension dimension) {
        return callIslandLockWorldEvent(island, commandSenderToSuperiorPlayer(commandSender), dimension);
    }

    public static boolean callIslandLockWorldEvent(Island island, @Nullable SuperiorPlayer superiorPlayer, Dimension dimension) {
        IslandLockWorld islandLockWorld = new IslandLockWorld();
        islandLockWorld.island = island;
        islandLockWorld.superiorPlayer = superiorPlayer;
        islandLockWorld.dimension = dimension;
        return !fireEvent(ISLAND_LOCK_WORLD_EVENT, islandLockWorld).isCancelled();
    }

    public static boolean callIslandOpenEvent(Island island, CommandSender commandSender) {
        return callIslandOpenEvent(island, commandSenderToSuperiorPlayer(commandSender));
    }

    public static boolean callIslandOpenEvent(Island island, @Nullable SuperiorPlayer superiorPlayer) {
        IslandOpen islandOpen = new IslandOpen();
        islandOpen.island = island;
        islandOpen.superiorPlayer = superiorPlayer;
        return !fireEvent(ISLAND_OPEN_EVENT, islandOpen).isCancelled();
    }

    public static boolean callIslandOpenWarpEvent(Island island, SuperiorPlayer superiorPlayer, IslandWarp islandWarp) {
        IslandOpenWarp islandOpenWarp = new IslandOpenWarp();
        islandOpenWarp.island = island;
        islandOpenWarp.superiorPlayer = superiorPlayer;
        islandOpenWarp.islandWarp = islandWarp;
        return !fireEvent(ISLAND_OPEN_WARP_EVENT, islandOpenWarp).isCancelled();
    }

    public static boolean callIslandQuitEvent(Island island, SuperiorPlayer superiorPlayer) {
        IslandQuit islandQuit = new IslandQuit();
        islandQuit.island = island;
        islandQuit.superiorPlayer = superiorPlayer;
        return !fireEvent(ISLAND_QUIT_EVENT, islandQuit).isCancelled();
    }

    public static boolean callIslandRateEvent(Island island, CommandSender commandSender, SuperiorPlayer ratingPlayer, Rating rating) {
        return callIslandRateEvent(island, commandSenderToSuperiorPlayer(commandSender), ratingPlayer, rating);
    }

    public static boolean callIslandRateEvent(Island island, @Nullable SuperiorPlayer superiorPlayer, SuperiorPlayer ratingPlayer, Rating rating) {
        IslandRate islandRate = new IslandRate();
        islandRate.island = island;
        islandRate.superiorPlayer = superiorPlayer;
        islandRate.ratingPlayer = ratingPlayer;
        islandRate.rating = rating;
        return !fireEvent(ISLAND_RATE_EVENT, islandRate).isCancelled();
    }

    public static boolean callIslandRemoveBlockLimitEvent(Island island, CommandSender commandSender, Key block) {
        return callIslandRemoveBlockLimitEvent(island, commandSenderToSuperiorPlayer(commandSender), block);
    }

    public static boolean callIslandRemoveBlockLimitEvent(Island island, @Nullable SuperiorPlayer superiorPlayer, Key block) {
        IslandRemoveBlockLimit islandRemoveBlockLimit = new IslandRemoveBlockLimit();
        islandRemoveBlockLimit.island = island;
        islandRemoveBlockLimit.superiorPlayer = superiorPlayer;
        islandRemoveBlockLimit.block = block;
        return !fireEvent(ISLAND_REMOVE_BLOCK_LIMIT_EVENT, islandRemoveBlockLimit).isCancelled();
    }

    public static boolean callIslandRemoveEffectEvent(Island island, CommandSender commandSender, PotionEffectType effectType) {
        return callIslandRemoveEffectEvent(island, commandSenderToSuperiorPlayer(commandSender), effectType);
    }

    public static boolean callIslandRemoveEffectEvent(Island island, @Nullable SuperiorPlayer superiorPlayer, PotionEffectType effectType) {
        IslandRemoveEffect islandRemoveEffect = new IslandRemoveEffect();
        islandRemoveEffect.island = island;
        islandRemoveEffect.superiorPlayer = superiorPlayer;
        islandRemoveEffect.effectType = effectType;
        return !fireEvent(ISLAND_REMOVE_EFFECT_EVENT, islandRemoveEffect).isCancelled();
    }

    public static boolean callIslandRemoveGeneratorRateEvent(Island island, CommandSender commandSender, Key block, Dimension dimension) {
        return callIslandRemoveGeneratorRateEvent(island, commandSenderToSuperiorPlayer(commandSender), block, dimension);
    }

    public static boolean callIslandRemoveGeneratorRateEvent(Island island, @Nullable SuperiorPlayer superiorPlayer, Key block, Dimension dimension) {
        IslandRemoveGeneratorRate islandRemoveGeneratorRate = new IslandRemoveGeneratorRate();
        islandRemoveGeneratorRate.island = island;
        islandRemoveGeneratorRate.superiorPlayer = superiorPlayer;
        islandRemoveGeneratorRate.block = block;
        islandRemoveGeneratorRate.dimension = dimension;
        return !fireEvent(ISLAND_REMOVE_GENERATOR_RATE_EVENT, islandRemoveGeneratorRate).isCancelled();
    }

    public static boolean callIslandRemoveRatingEvent(Island island, CommandSender commandSender, SuperiorPlayer ratingPlayer) {
        return callIslandRemoveRatingEvent(island, commandSenderToSuperiorPlayer(commandSender), ratingPlayer);
    }

    public static boolean callIslandRemoveRatingEvent(Island island, @Nullable SuperiorPlayer superiorPlayer, SuperiorPlayer ratingPlayer) {
        IslandRemoveRating islandRemoveRating = new IslandRemoveRating();
        islandRemoveRating.island = island;
        islandRemoveRating.superiorPlayer = superiorPlayer;
        islandRemoveRating.ratingPlayer = ratingPlayer;
        return !fireEvent(ISLAND_REMOVE_RATING_EVENT, islandRemoveRating).isCancelled();
    }

    public static boolean callIslandRemoveRoleLimitEvent(Island island, CommandSender commandSender, PlayerRole playerRole) {
        return callIslandRemoveRoleLimitEvent(island, commandSenderToSuperiorPlayer(commandSender), playerRole);
    }

    public static boolean callIslandRemoveRoleLimitEvent(Island island, @Nullable SuperiorPlayer superiorPlayer, PlayerRole playerRole) {
        IslandRemoveRoleLimit islandRemoveRoleLimit = new IslandRemoveRoleLimit();
        islandRemoveRoleLimit.island = island;
        islandRemoveRoleLimit.superiorPlayer = superiorPlayer;
        islandRemoveRoleLimit.playerRole = playerRole;
        return !fireEvent(ISLAND_REMOVE_ROLE_LIMIT_EVENT, islandRemoveRoleLimit).isCancelled();
    }

    public static boolean callIslandRemoveVisitorHomeEvent(Island island, SuperiorPlayer superiorPlayer) {
        IslandRemoveVisitorHome islandRemoveVisitorHome = new IslandRemoveVisitorHome();
        islandRemoveVisitorHome.island = island;
        islandRemoveVisitorHome.superiorPlayer = superiorPlayer;
        return !fireEvent(ISLAND_REMOVE_VISITOR_HOME_EVENT, islandRemoveVisitorHome).isCancelled();
    }

    public static PluginEvent<IslandRename> callIslandRenameEvent(Island island, CommandSender commandSender, String islandName) {
        return callIslandRenameEvent(island, commandSenderToSuperiorPlayer(commandSender), islandName);
    }

    public static PluginEvent<IslandRename> callIslandRenameEvent(Island island, @Nullable SuperiorPlayer superiorPlayer, String islandName) {
        IslandRename islandRename = new IslandRename();
        islandRename.island = island;
        islandRename.superiorPlayer = superiorPlayer;
        islandRename.islandName = islandName;
        return fireEvent(ISLAND_RENAME_EVENT, islandRename);
    }

    public static PluginEvent<IslandRenameWarpCategory> callIslandRenameWarpCategoryEvent(Island island, SuperiorPlayer superiorPlayer,
                                                                                          WarpCategory warpCategory, String categoryName) {
        IslandRenameWarpCategory islandRenameWarpCategory = new IslandRenameWarpCategory();
        islandRenameWarpCategory.island = island;
        islandRenameWarpCategory.superiorPlayer = superiorPlayer;
        islandRenameWarpCategory.warpCategory = warpCategory;
        islandRenameWarpCategory.categoryName = categoryName;
        return fireEvent(ISLAND_RENAME_WARP_CATEGORY_EVENT, islandRenameWarpCategory);
    }

    public static PluginEvent<IslandRenameWarp> callIslandRenameWarpEvent(Island island, Player player,
                                                                          IslandWarp islandWarp, String warpName) {
        return callIslandRenameWarpEvent(island, commandSenderToSuperiorPlayer(player), islandWarp, warpName);
    }

    public static PluginEvent<IslandRenameWarp> callIslandRenameWarpEvent(Island island, SuperiorPlayer superiorPlayer,
                                                                          IslandWarp islandWarp, String warpName) {
        IslandRenameWarp islandRenameWarp = new IslandRenameWarp();
        islandRenameWarp.island = island;
        islandRenameWarp.superiorPlayer = superiorPlayer;
        islandRenameWarp.islandWarp = islandWarp;
        islandRenameWarp.warpName = warpName;
        return fireEvent(ISLAND_RENAME_WARP_EVENT, islandRenameWarp);
    }

    public static void callIslandRestrictMoveEvent(Island island, SuperiorPlayer superiorPlayer, IslandRestrictMoveEvent.RestrictReason restrictReason) {
        IslandRestrictMove islandRestrictMove = new IslandRestrictMove();
        islandRestrictMove.island = island;
        islandRestrictMove.superiorPlayer = superiorPlayer;
        islandRestrictMove.restrictReason = restrictReason;
        fireEvent(ISLAND_RESTRICT_MOVE_EVENT, islandRestrictMove);
    }

    public static void callIslandSchematicPasteEvent(Island island, @Nullable SuperiorPlayer superiorPlayer, String schematicName, Location location) {
        IslandSchematicPaste islandSchematicPaste = new IslandSchematicPaste();
        islandSchematicPaste.island = island;
        islandSchematicPaste.superiorPlayer = superiorPlayer;
        islandSchematicPaste.schematicName = schematicName;
        islandSchematicPaste.location = location;
        fireEvent(ISLAND_SCHEMATIC_PASTE_EVENT, islandSchematicPaste);
    }

    public static PluginEvent<IslandSetHome> callIslandSetHomeEvent(Island island, CommandSender commandSender,
                                                                    Location islandHome, IslandSetHomeEvent.Reason reason) {
        return callIslandSetHomeEvent(island, commandSenderToSuperiorPlayer(commandSender), islandHome, reason);
    }

    public static PluginEvent<IslandSetHome> callIslandSetHomeEvent(Island island, @Nullable SuperiorPlayer superiorPlayer,
                                                                    Location islandHome, IslandSetHomeEvent.Reason reason) {
        IslandSetHome islandRenameWarp = new IslandSetHome();
        islandRenameWarp.island = island;
        islandRenameWarp.superiorPlayer = superiorPlayer;
        islandRenameWarp.islandHome = islandHome;
        islandRenameWarp.reason = reason;
        return fireEvent(ISLAND_SET_HOME_EVENT, islandRenameWarp);
    }

    public static PluginEvent<IslandSetVisitorHome> callIslandSetVisitorHomeEvent(Island island, SuperiorPlayer superiorPlayer,
                                                                                  Location islandVisitorHome) {
        IslandSetVisitorHome islandSetVisitorHome = new IslandSetVisitorHome();
        islandSetVisitorHome.island = island;
        islandSetVisitorHome.superiorPlayer = superiorPlayer;
        islandSetVisitorHome.islandVisitorHome = islandVisitorHome;
        return fireEvent(ISLAND_SET_VISITOR_HOME_EVENT, islandSetVisitorHome);
    }

    public static boolean callIslandTransferEvent(Island island, SuperiorPlayer previousOwner, SuperiorPlayer superiorPlayer) {
        IslandTransfer islandTransfer = new IslandTransfer();
        islandTransfer.island = island;
        islandTransfer.superiorPlayer = superiorPlayer;
        islandTransfer.previousOwner = previousOwner;
        return !fireEvent(ISLAND_TRANSFER_EVENT, islandTransfer).isCancelled();
    }

    public static boolean callIslandUnbanEvent(Island island, SuperiorPlayer superiorPlayer, SuperiorPlayer unbannedPlayer) {
        IslandUnban islandUnban = new IslandUnban();
        islandUnban.island = island;
        islandUnban.superiorPlayer = superiorPlayer;
        islandUnban.unbannedPlayer = unbannedPlayer;
        return !fireEvent(ISLAND_UNBAN_EVENT, islandUnban).isCancelled();
    }

    public static boolean callIslandUncoopPlayerEvent(Island island, SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer,
                                                      IslandUncoopPlayerEvent.UncoopReason uncoopReason) {
        IslandUncoopPlayer islandUncoopPlayer = new IslandUncoopPlayer();
        islandUncoopPlayer.island = island;
        islandUncoopPlayer.superiorPlayer = superiorPlayer;
        islandUncoopPlayer.targetPlayer = targetPlayer;
        islandUncoopPlayer.uncoopReason = uncoopReason;
        return !fireEvent(ISLAND_UNCOOP_PLAYER_EVENT, islandUncoopPlayer).isCancelled();
    }

    public static boolean callIslandUnlockWorldEvent(Island island, CommandSender commandSender, Dimension dimension) {
        return callIslandUnlockWorldEvent(island, commandSenderToSuperiorPlayer(commandSender), dimension);
    }

    public static boolean callIslandUnlockWorldEvent(Island island, @Nullable SuperiorPlayer superiorPlayer, Dimension dimension) {
        IslandUnlockWorld islandUncoopPlayer = new IslandUnlockWorld();
        islandUncoopPlayer.island = island;
        islandUncoopPlayer.superiorPlayer = superiorPlayer;
        islandUncoopPlayer.dimension = dimension;
        return !fireEvent(ISLAND_UNLOCK_WORLD_EVENT, islandUncoopPlayer).isCancelled();
    }

    public static PluginEvent<IslandUpgrade> callIslandUpgradeEvent(Island island, @Nullable SuperiorPlayer superiorPlayer,
                                                                    Upgrade upgrade, UpgradeLevel currentLevel,
                                                                    UpgradeLevel nextLevel, IslandUpgradeEvent.Cause upgradeCause) {
        return callIslandUpgradeEvent(island, superiorPlayer, upgrade, nextLevel, currentLevel.getCommands(),
                upgradeCause, currentLevel.getCost());
    }

    public static PluginEvent<IslandUpgrade> callIslandUpgradeEvent(Island island, CommandSender commandSender,
                                                                    Upgrade upgrade, UpgradeLevel nextLevel,
                                                                    IslandUpgradeEvent.Cause upgradeCause) {
        return callIslandUpgradeEvent(island, commandSenderToSuperiorPlayer(commandSender), upgrade, nextLevel,
                Collections.emptyList(), upgradeCause, null);
    }

    public static PluginEvent<IslandUpgrade> callIslandUpgradeEvent(Island island, @Nullable SuperiorPlayer superiorPlayer,
                                                                    Upgrade upgrade, UpgradeLevel nextLevel,
                                                                    List<String> commands, IslandUpgradeEvent.Cause upgradeCause,
                                                                    @Nullable UpgradeCost upgradeCost) {
        IslandUpgrade islandUpgrade = new IslandUpgrade();
        islandUpgrade.island = island;
        islandUpgrade.superiorPlayer = superiorPlayer;
        islandUpgrade.upgrade = upgrade;
        islandUpgrade.nextLevel = nextLevel;
        islandUpgrade.commands = commands;
        islandUpgrade.upgradeCause = upgradeCause;
        islandUpgrade.upgradeCost = upgradeCost;
        return fireEvent(ISLAND_UPGRADE_EVENT, islandUpgrade);
    }

    public static boolean callIslandWarpTeleportEvent(Island island, SuperiorPlayer superiorPlayer, IslandWarp islandWarp) {
        IslandWarpTeleport islandWarpTeleport = new IslandWarpTeleport();
        islandWarpTeleport.island = island;
        islandWarpTeleport.superiorPlayer = superiorPlayer;
        islandWarpTeleport.islandWarp = islandWarp;
        return !fireEvent(ISLAND_WARP_TELEPORT_EVENT, islandWarpTeleport).isCancelled();
    }

    public static boolean callIslandWorldResetEvent(Island island, CommandSender commandSender, Dimension dimension) {
        return callIslandWorldResetEvent(island, commandSenderToSuperiorPlayer(commandSender), dimension);
    }

    public static boolean callIslandWorldResetEvent(Island island, @Nullable SuperiorPlayer superiorPlayer, Dimension dimension) {
        IslandWorldReset islandWorldReset = new IslandWorldReset();
        islandWorldReset.island = island;
        islandWorldReset.superiorPlayer = superiorPlayer;
        islandWorldReset.dimension = dimension;
        return !fireEvent(ISLAND_WORLD_RESET_EVENT, islandWorldReset).isCancelled();
    }

    public static void callIslandWorthCalculatedEvent(Island island, SuperiorPlayer asker, BigDecimal islandLevel, BigDecimal islandWorth) {
        IslandWorthCalculated islandWorthCalculated = new IslandWorthCalculated();
        islandWorthCalculated.island = island;
        islandWorthCalculated.superiorPlayer = asker;
        islandWorthCalculated.islandLevel = islandLevel;
        islandWorthCalculated.islandWorth = islandWorth;
        fireEvent(ISLAND_WORTH_CALCULATED_EVENT, islandWorthCalculated);
    }

    public static void callIslandWorthUpdateEvent(Island island, BigDecimal oldWorth, BigDecimal oldLevel, BigDecimal newWorth, BigDecimal newLevel) {
        IslandWorthUpdate islandWorthUpdate = new IslandWorthUpdate();
        islandWorthUpdate.island = island;
        islandWorthUpdate.oldWorth = oldWorth;
        islandWorthUpdate.oldLevel = oldLevel;
        islandWorthUpdate.newWorth = newWorth;
        islandWorthUpdate.newLevel = newLevel;
        fireEvent(ISLAND_WORTH_UPDATE_EVENT, islandWorthUpdate);
    }

    public static PluginEvent<MissionComplete> callMissionCompleteEvent(SuperiorPlayer superiorPlayer, IMissionsHolder missionsHolder,
                                                                        Mission<?> mission, List<ItemStack> itemRewards,
                                                                        List<String> commandRewards) {
        MissionComplete missionComplete = new MissionComplete();
        missionComplete.superiorPlayer = superiorPlayer;
        missionComplete.missionsHolder = missionsHolder;
        missionComplete.mission = mission;
        missionComplete.itemRewards = itemRewards;
        missionComplete.commandRewards = commandRewards;
        return fireEvent(MISSION_COMPLETE_EVENT, missionComplete);
    }

    public static boolean callMissionResetEvent(CommandSender commandSender, IMissionsHolder missionsHolder, Mission<?> mission) {
        return callMissionResetEvent(commandSenderToSuperiorPlayer(commandSender), missionsHolder, mission);
    }

    public static boolean callMissionResetEvent(@Nullable SuperiorPlayer superiorPlayer, IMissionsHolder missionsHolder, Mission<?> mission) {
        MissionReset missionReset = new MissionReset();
        missionReset.superiorPlayer = superiorPlayer;
        missionReset.missionsHolder = missionsHolder;
        missionReset.mission = mission;
        return !fireEvent(MISSION_RESET_EVENT, missionReset).isCancelled();
    }

    public static boolean callPlayerChangeBorderColorEvent(SuperiorPlayer superiorPlayer, BorderColor borderColor) {
        PlayerChangeBorderColor playerChangeBorderColor = new PlayerChangeBorderColor();
        playerChangeBorderColor.superiorPlayer = superiorPlayer;
        playerChangeBorderColor.borderColor = borderColor;
        return !fireEvent(PLAYER_CHANGE_BORDER_COLOR_EVENT, playerChangeBorderColor).isCancelled();
    }

    public static boolean callPlayerChangeLanguageEvent(SuperiorPlayer superiorPlayer, Locale language) {
        PlayerChangeLanguage playerChangeLanguage = new PlayerChangeLanguage();
        playerChangeLanguage.superiorPlayer = superiorPlayer;
        playerChangeLanguage.language = language;
        return !fireEvent(PLAYER_CHANGE_LANGUAGE_EVENT, playerChangeLanguage).isCancelled();
    }

    public static void callPlayerChangeNameEvent(SuperiorPlayer superiorPlayer, String newName) {
        PlayerChangeName missionComplete = new PlayerChangeName();
        missionComplete.superiorPlayer = superiorPlayer;
        missionComplete.newName = newName;
        fireEvent(PLAYER_CHANGE_NAME_EVENT, missionComplete);
    }

    public static boolean callPlayerChangeRoleEvent(SuperiorPlayer superiorPlayer, PlayerRole newRole) {
        PlayerChangeRole playerChangeRole = new PlayerChangeRole();
        playerChangeRole.superiorPlayer = superiorPlayer;
        playerChangeRole.newRole = newRole;
        return !fireEvent(PLAYER_CHANGE_ROLE_EVENT, playerChangeRole).isCancelled();
    }

    public static PluginEvent<PlayerCloseMenu> callPlayerCloseMenuEvent(SuperiorPlayer superiorPlayer, MenuView<?, ?> menuView,
                                                                        @Nullable MenuView<?, ?> newMenuView) {
        PlayerCloseMenu playerCloseMenu = new PlayerCloseMenu();
        playerCloseMenu.superiorPlayer = superiorPlayer;
        playerCloseMenu.menuView = menuView;
        playerCloseMenu.newMenuView = newMenuView;
        return fireEvent(PLAYER_CLOSE_MENU_EVENT, playerCloseMenu);
    }

    public static boolean callPlayerOpenMenuEvent(SuperiorPlayer superiorPlayer, MenuView<?, ?> menuView) {
        PlayerOpenMenu playerOpenMenu = new PlayerOpenMenu();
        playerOpenMenu.superiorPlayer = superiorPlayer;
        playerOpenMenu.menuView = menuView;
        return !fireEvent(PLAYER_OPEN_MENU_EVENT, playerOpenMenu).isCancelled();
    }

    public static void callPlayerReplaceEvent(SuperiorPlayer oldPlayer, SuperiorPlayer newPlayer) {
        PlayerReplace playerReplace = new PlayerReplace();
        playerReplace.superiorPlayer = oldPlayer;
        playerReplace.newPlayer = newPlayer;
        fireEvent(PLAYER_REPLACE_EVENT, playerReplace);
    }

    public static boolean callPlayerToggleBlocksStackerEvent(SuperiorPlayer superiorPlayer) {
        PlayerToggleBlocksStacker playerToggleBlocksStacker = new PlayerToggleBlocksStacker();
        playerToggleBlocksStacker.superiorPlayer = superiorPlayer;
        return !fireEvent(PLAYER_TOGGLE_BLOCKS_STACKER_EVENT, playerToggleBlocksStacker).isCancelled();
    }

    public static boolean callPlayerToggleBorderEvent(SuperiorPlayer superiorPlayer) {
        PlayerToggleBorder playerToggleBorder = new PlayerToggleBorder();
        playerToggleBorder.superiorPlayer = superiorPlayer;
        return !fireEvent(PLAYER_TOGGLE_BORDER_EVENT, playerToggleBorder).isCancelled();
    }

    public static boolean callPlayerToggleBypassEvent(SuperiorPlayer superiorPlayer) {
        PlayerToggleBypass playerToggleBypass = new PlayerToggleBypass();
        playerToggleBypass.superiorPlayer = superiorPlayer;
        return !fireEvent(PLAYER_TOGGLE_BYPASS_EVENT, playerToggleBypass).isCancelled();
    }

    public static boolean callPlayerToggleFlyEvent(SuperiorPlayer superiorPlayer) {
        PlayerToggleFly playerToggleFly = new PlayerToggleFly();
        playerToggleFly.superiorPlayer = superiorPlayer;
        return !fireEvent(PLAYER_TOGGLE_FLY_EVENT, playerToggleFly).isCancelled();
    }

    public static boolean callPlayerTogglePanelEvent(SuperiorPlayer superiorPlayer) {
        PlayerTogglePanel playerTogglePanel = new PlayerTogglePanel();
        playerTogglePanel.superiorPlayer = superiorPlayer;
        return !fireEvent(PLAYER_TOGGLE_PANEL_EVENT, playerTogglePanel).isCancelled();
    }

    public static boolean callPlayerToggleSpyEvent(SuperiorPlayer superiorPlayer) {
        PlayerToggleSpy playerToggleSpy = new PlayerToggleSpy();
        playerToggleSpy.superiorPlayer = superiorPlayer;
        return !fireEvent(PLAYER_TOGGLE_SPY_EVENT, playerToggleSpy).isCancelled();
    }

    public static boolean callPlayerToggleTeamChatEvent(SuperiorPlayer superiorPlayer) {
        PlayerToggleTeamChat playerToggleTeamChat = new PlayerToggleTeamChat();
        playerToggleTeamChat.superiorPlayer = superiorPlayer;
        return !fireEvent(PLAYER_TOGGLE_TEAM_CHAT_EVENT, playerToggleTeamChat).isCancelled();
    }

    public static void callPluginInitializedEvent() {
        PluginInitialized pluginInitialized = new PluginInitialized();
        pluginInitialized.plugin = plugin;
        fireEvent(PLUGIN_INITIALIZED_EVENT, pluginInitialized);
    }

    public static PluginEvent<PluginInitialize> callPluginInitializeEvent() {
        PluginInitialize pluginInitialize = new PluginInitialize();
        pluginInitialize.plugin = plugin;
        return fireEvent(PLUGIN_INITIALIZE_EVENT, pluginInitialize);
    }

    public static boolean callPluginLoadDataEvent() {
        PluginLoadData pluginLoadData = new PluginLoadData();
        pluginLoadData.plugin = plugin;
        return !fireEvent(PLUGIN_LOAD_DATA_EVENT, pluginLoadData).isCancelled();
    }

    public static void callPostIslandCreateEvent(Island island, SuperiorPlayer superiorPlayer) {
        PostIslandCreate postIslandCreate = new PostIslandCreate();
        postIslandCreate.island = island;
        postIslandCreate.superiorPlayer = superiorPlayer;
        fireEvent(POST_ISLAND_CREATE_EVENT, postIslandCreate);
    }

    public static boolean callPreIslandCreateEvent(SuperiorPlayer superiorPlayer, String islandName) {
        PreIslandCreate preIslandCreate = new PreIslandCreate();
        preIslandCreate.superiorPlayer = superiorPlayer;
        preIslandCreate.islandName = islandName;
        return !fireEvent(PRE_ISLAND_CREATE_EVENT, preIslandCreate).isCancelled();
    }

    public static PluginEvent<SendMessage> callSendMessageEvent(CommandSender receiver, String messageType,
                                                                IMessageComponent messageComponent, Object... args) {
        SendMessage pluginInitialize = new SendMessage();
        pluginInitialize.receiver = receiver;
        pluginInitialize.messageType = messageType;
        pluginInitialize.messageComponent = messageComponent;
        pluginInitialize.args = args;
        return fireEvent(SEND_MESSAGE_EVENT, pluginInitialize);
    }

    private static <Args extends PluginEventArgs> PluginEvent<Args> fireEvent(PluginEventType<Args> type, Args args) {
        return plugin.getPluginEventsDispatcher().fireEvent(type, args);
    }

    @Nullable
    private static SuperiorPlayer commandSenderToSuperiorPlayer(@Nullable CommandSender commandSender) {
        return commandSender instanceof Player ? plugin.getPlayers().getSuperiorPlayer(commandSender) : null;
    }

    private PluginEventsFactory() {

    }

}
