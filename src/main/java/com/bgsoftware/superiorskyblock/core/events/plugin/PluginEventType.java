package com.bgsoftware.superiorskyblock.core.events.plugin;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.events.AttemptPlayerSendMessageEvent;
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
import com.bgsoftware.superiorskyblock.api.events.IslandClearFlagsEvent;
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
import com.bgsoftware.superiorskyblock.api.events.IslandEnterPortalEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandEnterProtectedEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandGenerateBlockEvent;
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
import com.bgsoftware.superiorskyblock.api.events.PlayerChangeBorderColorEvent;
import com.bgsoftware.superiorskyblock.api.events.PlayerChangeLanguageEvent;
import com.bgsoftware.superiorskyblock.api.events.PlayerChangeNameEvent;
import com.bgsoftware.superiorskyblock.api.events.PlayerChangeRoleEvent;
import com.bgsoftware.superiorskyblock.api.events.PlayerCloseMenuEvent;
import com.bgsoftware.superiorskyblock.api.events.PlayerOpenMenuEvent;
import com.bgsoftware.superiorskyblock.api.events.PlayerReplaceEvent;
import com.bgsoftware.superiorskyblock.api.events.PlayerToggleBlocksStackerEvent;
import com.bgsoftware.superiorskyblock.api.events.PlayerToggleBorderEvent;
import com.bgsoftware.superiorskyblock.api.events.PlayerToggleBypassEvent;
import com.bgsoftware.superiorskyblock.api.events.PlayerToggleFlyEvent;
import com.bgsoftware.superiorskyblock.api.events.PlayerTogglePanelEvent;
import com.bgsoftware.superiorskyblock.api.events.PlayerToggleSpyEvent;
import com.bgsoftware.superiorskyblock.api.events.PlayerToggleTeamChatEvent;
import com.bgsoftware.superiorskyblock.api.events.PluginInitializeEvent;
import com.bgsoftware.superiorskyblock.api.events.PluginInitializedEvent;
import com.bgsoftware.superiorskyblock.api.events.PluginLoadDataEvent;
import com.bgsoftware.superiorskyblock.api.events.PostIslandCreateEvent;
import com.bgsoftware.superiorskyblock.api.events.PreIslandCreateEvent;
import com.bgsoftware.superiorskyblock.api.events.SendMessageEvent;
import com.bgsoftware.superiorskyblock.core.events.EventType;
import com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.AttemptPlayerSendMessage;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.BlockStack;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.BlockUnstack;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.Empty;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandBan;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandBankDeposit;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandBankWithdraw;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandBiomeChange;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandChangeBankLimit;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandChangeBlockLimit;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandChangeBorderSize;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandChangeCoopLimit;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandChangeCropGrowth;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandChangeDescription;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandChangeDiscord;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandChangeEffectLevel;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandChangeEntityLimit;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandChangeGeneratorRate;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandChangeLevelBonus;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandChangeMembersLimit;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandChangeMobDrops;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandChangePaypal;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandChangePlayerPrivilege;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandChangeRoleLimit;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandChangeRolePrivilege;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandChangeSpawnerRates;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandChangeWarpCategoryIcon;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandChangeWarpCategorySlot;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandChangeWarpIcon;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandChangeWarpLocation;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandChangeWarpsLimit;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandChangeWorthBonus;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandChat;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandChunkReset;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandClearFlags;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandClearGeneratorRates;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandClearPlayerPrivileges;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandClearRatings;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandClearRolesPrivileges;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandClose;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandCloseWarp;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandCoopPlayer;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandCreate;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandCreateWarp;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandCreateWarpCategory;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandDeleteWarp;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandDisableFlag;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandDisband;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandEnableFlag;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandEnter;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandEnterPortal;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandEnterProtected;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandGenerateBlock;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandInvite;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandJoin;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandKick;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandLeave;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandLeaveProtected;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandLockWorld;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandOpen;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandOpenWarp;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandQuit;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandRate;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandRemoveBlockLimit;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandRemoveEffect;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandRemoveGeneratorRate;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandRemoveRating;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandRemoveRoleLimit;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandRemoveVisitorHome;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandRename;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandRenameWarp;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandRenameWarpCategory;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandRestrictMove;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandSchematicPaste;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandSetHome;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandSetVisitorHome;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandTransfer;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandUnban;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandUncoopPlayer;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandUnlockWorld;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandUpgrade;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandWorldReset;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandWorthCalculated;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.IslandWorthUpdate;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.MissionComplete;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.MissionReset;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.PlayerChangeBorderColor;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.PlayerChangeLanguage;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.PlayerChangeName;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.PlayerChangeRole;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.PlayerCloseMenu;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.PlayerOpenMenu;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.PlayerReplace;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.PlayerToggleBlocksStacker;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.PlayerToggleBorder;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.PlayerToggleBypass;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.PlayerToggleFly;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.PlayerTogglePanel;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.PlayerToggleSpy;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.PlayerToggleTeamChat;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.PluginInitialize;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.PluginInitialized;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.PluginLoadData;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.PostIslandCreate;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.PreIslandCreate;
import static com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs.SendMessage;

;

public abstract class PluginEventType<Args extends PluginEventArgs> extends EventType<Args, PluginEvent<Args>> {

    private static final List<PluginEventType<?>> ALL_TYPES = new LinkedList<>();

    public static final PluginEventType<Empty> SETTINGS_UPDATE_EVENT = new PluginEventType<Empty>(null) {
        @Override
        public Event createBukkitEvent(Empty args) {
            return null;
        }
    };
    public static final PluginEventType<Empty> COMMANDS_UPDATE_EVENT = new PluginEventType<Empty>(null) {
        @Override
        public Event createBukkitEvent(Empty args) {
            return null;
        }
    };

    public static final PluginEventType<AttemptPlayerSendMessage> ATTEMPT_PLAYER_SEND_MESSAGE_EVENT = new PluginEventType<AttemptPlayerSendMessage>(AttemptPlayerSendMessageEvent.class) {
        @Override
        public Event createBukkitEvent(AttemptPlayerSendMessage args) {
            return new AttemptPlayerSendMessageEvent(args.receiver, args.messageType, args.args);
        }
    };
    public static final PluginEventType<BlockStack> BLOCK_STACK_EVENT = new PluginEventType<BlockStack>(BlockStackEvent.class) {
        @Override
        public Event createBukkitEvent(BlockStack args) {
            return new BlockStackEvent(args.block, args.player, args.originalCount, args.newCount);
        }
    };
    public static final PluginEventType<BlockUnstack> BLOCK_UNSTACK_EVENT = new PluginEventType<BlockUnstack>(BlockUnstackEvent.class) {
        @Override
        public Event createBukkitEvent(BlockUnstack args) {
            return new BlockUnstackEvent(args.block, args.player, args.originalCount, args.newCount);
        }
    };
    public static final PluginEventType<IslandBan> ISLAND_BAN_EVENT = new PluginEventType<IslandBan>(IslandBanEvent.class) {
        @Override
        public Event createBukkitEvent(IslandBan args) {
            return new IslandBanEvent(args.superiorPlayer, args.targetPlayer, args.island);
        }
    };
    public static final PluginEventType<IslandBankDeposit> ISLAND_BANK_DEPOSIT_EVENT = new PluginEventType<IslandBankDeposit>(IslandBankDepositEvent.class) {
        @Override
        public Event createBukkitEvent(IslandBankDeposit args) {
            return new IslandBankDepositEvent(args.superiorPlayer, args.island, args.amount);
        }

        @Override
        public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<IslandBankDeposit> pluginEvent) {
            super.applyBukkitToPluginEvent(bukkitEvent, pluginEvent);
            pluginEvent.getArgs().failureReason = ((IslandBankDepositEvent) bukkitEvent).getFailureReason();
        }
    };
    public static final PluginEventType<IslandBankWithdraw> ISLAND_BANK_WITHDRAW_EVENT = new PluginEventType<IslandBankWithdraw>(IslandBankWithdrawEvent.class) {
        @Override
        public Event createBukkitEvent(IslandBankWithdraw args) {
            return new IslandBankWithdrawEvent(args.superiorPlayer, args.island, args.amount);
        }

        @Override
        public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<IslandBankWithdraw> pluginEvent) {
            super.applyBukkitToPluginEvent(bukkitEvent, pluginEvent);
            pluginEvent.getArgs().failureReason = ((IslandBankWithdrawEvent) bukkitEvent).getFailureReason();
        }
    };
    public static final PluginEventType<IslandBiomeChange> ISLAND_BIOME_CHANGE_EVENT = new PluginEventType<IslandBiomeChange>(IslandBiomeChangeEvent.class) {
        @Override
        public Event createBukkitEvent(IslandBiomeChange args) {
            return new IslandBiomeChangeEvent(args.superiorPlayer, args.island, args.biome);
        }

        @Override
        public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<IslandBiomeChange> pluginEvent) {
            super.applyBukkitToPluginEvent(bukkitEvent, pluginEvent);
            pluginEvent.getArgs().biome = ((IslandBiomeChangeEvent) bukkitEvent).getBiome();
        }
    };
    public static final PluginEventType<IslandChangeBankLimit> ISLAND_CHANGE_BANK_LIMIT_EVENT = new PluginEventType<IslandChangeBankLimit>(IslandChangeBankLimitEvent.class) {
        @Override
        public Event createBukkitEvent(IslandChangeBankLimit args) {
            return new IslandChangeBankLimitEvent(args.superiorPlayer, args.island, args.bankLimit);
        }

        @Override
        public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<IslandChangeBankLimit> pluginEvent) {
            super.applyBukkitToPluginEvent(bukkitEvent, pluginEvent);
            pluginEvent.getArgs().bankLimit = ((IslandChangeBankLimitEvent) bukkitEvent).getBankLimit();
        }
    };
    public static final PluginEventType<IslandChangeBlockLimit> ISLAND_CHANGE_BLOCK_LIMIT_EVENT = new PluginEventType<IslandChangeBlockLimit>(IslandChangeBlockLimitEvent.class) {
        @Override
        public Event createBukkitEvent(IslandChangeBlockLimit args) {
            return new IslandChangeBlockLimitEvent(args.superiorPlayer, args.island, args.block, args.blockLimit);
        }

        @Override
        public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<IslandChangeBlockLimit> pluginEvent) {
            super.applyBukkitToPluginEvent(bukkitEvent, pluginEvent);
            pluginEvent.getArgs().blockLimit = ((IslandChangeBlockLimitEvent) bukkitEvent).getBlockLimit();
        }
    };
    public static final PluginEventType<IslandChangeBorderSize> ISLAND_CHANGE_BORDER_SIZE_EVENT = new PluginEventType<IslandChangeBorderSize>(IslandChangeBorderSizeEvent.class) {
        @Override
        public Event createBukkitEvent(IslandChangeBorderSize args) {
            return new IslandChangeBorderSizeEvent(args.superiorPlayer, args.island, args.borderSize);
        }

        @Override
        public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<IslandChangeBorderSize> pluginEvent) {
            super.applyBukkitToPluginEvent(bukkitEvent, pluginEvent);
            pluginEvent.getArgs().borderSize = ((IslandChangeBorderSizeEvent) bukkitEvent).getBorderSize();
        }
    };
    public static final PluginEventType<IslandChangeCoopLimit> ISLAND_CHANGE_COOP_LIMIT_EVENT = new PluginEventType<IslandChangeCoopLimit>(IslandChangeCoopLimitEvent.class) {
        @Override
        public Event createBukkitEvent(IslandChangeCoopLimit args) {
            return new IslandChangeCoopLimitEvent(args.superiorPlayer, args.island, args.coopLimit);
        }

        @Override
        public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<IslandChangeCoopLimit> pluginEvent) {
            super.applyBukkitToPluginEvent(bukkitEvent, pluginEvent);
            pluginEvent.getArgs().coopLimit = ((IslandChangeCoopLimitEvent) bukkitEvent).getCoopLimit();
        }
    };
    public static final PluginEventType<IslandChangeCropGrowth> ISLAND_CHANGE_CROP_GROWTH_EVENT = new PluginEventType<IslandChangeCropGrowth>(IslandChangeCropGrowthEvent.class) {
        @Override
        public Event createBukkitEvent(IslandChangeCropGrowth args) {
            return new IslandChangeCropGrowthEvent(args.superiorPlayer, args.island, args.cropGrowth);
        }

        @Override
        public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<IslandChangeCropGrowth> pluginEvent) {
            super.applyBukkitToPluginEvent(bukkitEvent, pluginEvent);
            pluginEvent.getArgs().cropGrowth = ((IslandChangeCropGrowthEvent) bukkitEvent).getCropGrowth();
        }
    };
    public static final PluginEventType<IslandChangeDescription> ISLAND_CHANGE_DESCRIPTION_EVENT = new PluginEventType<IslandChangeDescription>(IslandChangeDescriptionEvent.class) {
        @Override
        public Event createBukkitEvent(IslandChangeDescription args) {
            return new IslandChangeDescriptionEvent(args.island, args.superiorPlayer, args.description);
        }

        @Override
        public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<IslandChangeDescription> pluginEvent) {
            super.applyBukkitToPluginEvent(bukkitEvent, pluginEvent);
            pluginEvent.getArgs().description = ((IslandChangeDescriptionEvent) bukkitEvent).getDescription();
        }
    };
    public static final PluginEventType<IslandChangeDiscord> ISLAND_CHANGE_DISCORD_EVENT = new PluginEventType<IslandChangeDiscord>(IslandChangeDiscordEvent.class) {
        @Override
        public Event createBukkitEvent(IslandChangeDiscord args) {
            return new IslandChangeDiscordEvent(args.superiorPlayer, args.island, args.discord);
        }

        @Override
        public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<IslandChangeDiscord> pluginEvent) {
            super.applyBukkitToPluginEvent(bukkitEvent, pluginEvent);
            pluginEvent.getArgs().discord = ((IslandChangeDiscordEvent) bukkitEvent).getDiscord();
        }
    };
    public static final PluginEventType<IslandChangeEffectLevel> ISLAND_CHANGE_EFFECT_LEVEL_EVENT = new PluginEventType<IslandChangeEffectLevel>(IslandChangeEffectLevelEvent.class) {
        @Override
        public Event createBukkitEvent(IslandChangeEffectLevel args) {
            return new IslandChangeEffectLevelEvent(args.superiorPlayer, args.island, args.effectType, args.effectLevel);
        }

        @Override
        public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<IslandChangeEffectLevel> pluginEvent) {
            super.applyBukkitToPluginEvent(bukkitEvent, pluginEvent);
            pluginEvent.getArgs().effectLevel = ((IslandChangeEffectLevelEvent) bukkitEvent).getEffectLevel();
        }
    };
    public static final PluginEventType<IslandChangeEntityLimit> ISLAND_CHANGE_ENTITY_LIMIT_EVENT = new PluginEventType<IslandChangeEntityLimit>(IslandChangeEntityLimitEvent.class) {
        @Override
        public Event createBukkitEvent(IslandChangeEntityLimit args) {
            return new IslandChangeEntityLimitEvent(args.superiorPlayer, args.island, args.entity, args.entityLimit);
        }

        @Override
        public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<IslandChangeEntityLimit> pluginEvent) {
            super.applyBukkitToPluginEvent(bukkitEvent, pluginEvent);
            pluginEvent.getArgs().entityLimit = ((IslandChangeEntityLimitEvent) bukkitEvent).getEntityLimit();
        }
    };
    public static final PluginEventType<IslandChangeGeneratorRate> ISLAND_CHANGE_GENERATOR_RATE_EVENT = new PluginEventType<IslandChangeGeneratorRate>(IslandChangeGeneratorRateEvent.class) {
        @Override
        public Event createBukkitEvent(IslandChangeGeneratorRate args) {
            return new IslandChangeGeneratorRateEvent(args.superiorPlayer, args.island, args.block, args.dimension, args.generatorRate);
        }

        @Override
        public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<IslandChangeGeneratorRate> pluginEvent) {
            super.applyBukkitToPluginEvent(bukkitEvent, pluginEvent);
            pluginEvent.getArgs().generatorRate = ((IslandChangeGeneratorRateEvent) bukkitEvent).getGeneratorRate();
        }
    };
    public static final PluginEventType<IslandChangeLevelBonus> ISLAND_CHANGE_LEVEL_BONUS_EVENT = new PluginEventType<IslandChangeLevelBonus>(IslandChangeLevelBonusEvent.class) {
        @Override
        public Event createBukkitEvent(IslandChangeLevelBonus args) {
            return new IslandChangeLevelBonusEvent(args.superiorPlayer, args.island, args.reason, args.levelBonus);
        }

        @Override
        public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<IslandChangeLevelBonus> pluginEvent) {
            super.applyBukkitToPluginEvent(bukkitEvent, pluginEvent);
            pluginEvent.getArgs().levelBonus = ((IslandChangeLevelBonusEvent) bukkitEvent).getLevelBonus();
        }
    };
    public static final PluginEventType<IslandChangeMembersLimit> ISLAND_CHANGE_MEMBERS_LIMIT_EVENT = new PluginEventType<IslandChangeMembersLimit>(IslandChangeMembersLimitEvent.class) {
        @Override
        public Event createBukkitEvent(IslandChangeMembersLimit args) {
            return new IslandChangeMembersLimitEvent(args.superiorPlayer, args.island, args.membersLimit);
        }

        @Override
        public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<IslandChangeMembersLimit> pluginEvent) {
            super.applyBukkitToPluginEvent(bukkitEvent, pluginEvent);
            pluginEvent.getArgs().membersLimit = ((IslandChangeMembersLimitEvent) bukkitEvent).getMembersLimit();
        }
    };
    public static final PluginEventType<IslandChangeMobDrops> ISLAND_CHANGE_MOB_DROPS_EVENT = new PluginEventType<IslandChangeMobDrops>(IslandChangeMobDropsEvent.class) {
        @Override
        public Event createBukkitEvent(IslandChangeMobDrops args) {
            return new IslandChangeMobDropsEvent(args.superiorPlayer, args.island, args.mobDrops);
        }

        @Override
        public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<IslandChangeMobDrops> pluginEvent) {
            super.applyBukkitToPluginEvent(bukkitEvent, pluginEvent);
            pluginEvent.getArgs().mobDrops = ((IslandChangeMobDropsEvent) bukkitEvent).getMobDrops();
        }
    };
    public static final PluginEventType<IslandChangePaypal> ISLAND_CHANGE_PAYPAL_EVENT = new PluginEventType<IslandChangePaypal>(IslandChangePaypalEvent.class) {
        @Override
        public Event createBukkitEvent(IslandChangePaypal args) {
            return new IslandChangePaypalEvent(args.superiorPlayer, args.island, args.paypal);
        }

        @Override
        public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<IslandChangePaypal> pluginEvent) {
            super.applyBukkitToPluginEvent(bukkitEvent, pluginEvent);
            pluginEvent.getArgs().paypal = ((IslandChangePaypalEvent) bukkitEvent).getPaypal();
        }
    };
    public static final PluginEventType<IslandChangePlayerPrivilege> ISLAND_CHANGE_PLAYER_PRIVILEGE_EVENT = new PluginEventType<IslandChangePlayerPrivilege>(IslandChangePlayerPrivilegeEvent.class) {
        @Override
        public Event createBukkitEvent(IslandChangePlayerPrivilege args) {
            return new IslandChangePlayerPrivilegeEvent(args.island, args.superiorPlayer, args.privilegedPlayer, args.privilegeEnabled);
        }
    };
    public static final PluginEventType<IslandChangeRoleLimit> ISLAND_CHANGE_ROLE_LIMIT_EVENT = new PluginEventType<IslandChangeRoleLimit>(IslandChangeRoleLimitEvent.class) {
        @Override
        public Event createBukkitEvent(IslandChangeRoleLimit args) {
            return new IslandChangeRoleLimitEvent(args.superiorPlayer, args.island, args.playerRole, args.roleLimit);
        }

        @Override
        public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<IslandChangeRoleLimit> pluginEvent) {
            super.applyBukkitToPluginEvent(bukkitEvent, pluginEvent);
            pluginEvent.getArgs().roleLimit = ((IslandChangeRoleLimitEvent) bukkitEvent).getRoleLimit();
        }
    };
    public static final PluginEventType<IslandChangeSpawnerRates> ISLAND_CHANGE_SPAWNER_RATES_EVENT = new PluginEventType<IslandChangeSpawnerRates>(IslandChangeSpawnerRatesEvent.class) {
        @Override
        public Event createBukkitEvent(IslandChangeSpawnerRates args) {
            return new IslandChangeSpawnerRatesEvent(args.superiorPlayer, args.island, args.spawnerRates);
        }

        @Override
        public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<IslandChangeSpawnerRates> pluginEvent) {
            super.applyBukkitToPluginEvent(bukkitEvent, pluginEvent);
            pluginEvent.getArgs().spawnerRates = ((IslandChangeSpawnerRatesEvent) bukkitEvent).getSpawnerRates();
        }
    };
    public static final PluginEventType<IslandChangeWarpCategoryIcon> ISLAND_CHANGE_WARP_CATEGORY_ICON_EVENT = new PluginEventType<IslandChangeWarpCategoryIcon>(IslandChangeWarpIconEvent.class) {
        @Override
        public Event createBukkitEvent(IslandChangeWarpCategoryIcon args) {
            return new IslandChangeWarpCategoryIconEvent(args.superiorPlayer, args.island, args.warpCategory, args.icon);
        }

        @Override
        public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<IslandChangeWarpCategoryIcon> pluginEvent) {
            super.applyBukkitToPluginEvent(bukkitEvent, pluginEvent);
            pluginEvent.getArgs().icon = ((IslandChangeWarpCategoryIconEvent) bukkitEvent).getIcon();
        }
    };
    public static final PluginEventType<IslandChangeWarpCategorySlot> ISLAND_CHANGE_WARP_CATEGORY_SLOT_EVENT = new PluginEventType<IslandChangeWarpCategorySlot>(IslandChangeWarpCategorySlotEvent.class) {
        @Override
        public Event createBukkitEvent(IslandChangeWarpCategorySlot args) {
            return new IslandChangeWarpCategorySlotEvent(args.superiorPlayer, args.island, args.warpCategory, args.slot, args.maxSlot);
        }

        @Override
        public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<IslandChangeWarpCategorySlot> pluginEvent) {
            super.applyBukkitToPluginEvent(bukkitEvent, pluginEvent);
            pluginEvent.getArgs().slot = ((IslandChangeWarpCategorySlotEvent) bukkitEvent).getSlot();
        }
    };
    public static final PluginEventType<IslandChangeWarpIcon> ISLAND_CHANGE_WARP_ICON_EVENT = new PluginEventType<IslandChangeWarpIcon>(IslandChangeWarpIconEvent.class) {
        @Override
        public Event createBukkitEvent(IslandChangeWarpIcon args) {
            return new IslandChangeWarpIconEvent(args.superiorPlayer, args.island, args.islandWarp, args.icon);
        }

        @Override
        public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<IslandChangeWarpIcon> pluginEvent) {
            super.applyBukkitToPluginEvent(bukkitEvent, pluginEvent);
            pluginEvent.getArgs().icon = ((IslandChangeWarpIconEvent) bukkitEvent).getIcon();
        }
    };
    public static final PluginEventType<IslandChangeWarpLocation> ISLAND_CHANGE_WARP_LOCATION_EVENT = new PluginEventType<IslandChangeWarpLocation>(IslandChangeWarpLocationEvent.class) {
        @Override
        public Event createBukkitEvent(IslandChangeWarpLocation args) {
            return new IslandChangeWarpLocationEvent(args.superiorPlayer, args.island, args.islandWarp, args.location);
        }

        @Override
        public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<IslandChangeWarpLocation> pluginEvent) {
            super.applyBukkitToPluginEvent(bukkitEvent, pluginEvent);
            pluginEvent.getArgs().location = ((IslandChangeWarpLocationEvent) bukkitEvent).getLocation();
        }
    };
    public static final PluginEventType<IslandChangeWarpsLimit> ISLAND_CHANGE_WARPS_LIMIT_EVENT = new PluginEventType<IslandChangeWarpsLimit>(IslandChangeWarpsLimitEvent.class) {
        @Override
        public Event createBukkitEvent(IslandChangeWarpsLimit args) {
            return new IslandChangeWarpsLimitEvent(args.superiorPlayer, args.island, args.warpsLimit);
        }

        @Override
        public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<IslandChangeWarpsLimit> pluginEvent) {
            super.applyBukkitToPluginEvent(bukkitEvent, pluginEvent);
            pluginEvent.getArgs().warpsLimit = ((IslandChangeWarpsLimitEvent) bukkitEvent).getWarpsLimit();
        }
    };
    public static final PluginEventType<IslandChangeWorthBonus> ISLAND_CHANGE_WORTH_BONUS_EVENT = new PluginEventType<IslandChangeWorthBonus>(IslandChangeWorthBonusEvent.class) {
        @Override
        public Event createBukkitEvent(IslandChangeWorthBonus args) {
            return new IslandChangeWorthBonusEvent(args.superiorPlayer, args.island, args.reason, args.worthBonus);
        }

        @Override
        public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<IslandChangeWorthBonus> pluginEvent) {
            super.applyBukkitToPluginEvent(bukkitEvent, pluginEvent);
            pluginEvent.getArgs().worthBonus = ((IslandChangeWorthBonusEvent) bukkitEvent).getWorthBonus();
        }
    };
    public static final PluginEventType<IslandChangeRolePrivilege> ISLAND_CHANGE_ROLE_PRIVILEGE_EVENT = new PluginEventType<IslandChangeRolePrivilege>(IslandChangeRolePrivilegeEvent.class) {
        @Override
        public Event createBukkitEvent(IslandChangeRolePrivilege args) {
            return new IslandChangeRolePrivilegeEvent(args.island, args.superiorPlayer, args.playerRole);
        }
    };
    public static final PluginEventType<IslandChat> ISLAND_CHAT_EVENT = new PluginEventType<IslandChat>(IslandChatEvent.class) {
        @Override
        public Event createBukkitEvent(IslandChat args) {
            return new IslandChatEvent(args.island, args.superiorPlayer, args.message);
        }

        @Override
        public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<IslandChat> pluginEvent) {
            super.applyBukkitToPluginEvent(bukkitEvent, pluginEvent);
            pluginEvent.getArgs().message = ((IslandChatEvent) bukkitEvent).getMessage();
        }
    };
    public static final PluginEventType<IslandChunkReset> ISLAND_CHUNK_RESET_EVENT = new PluginEventType<IslandChunkReset>(IslandChunkResetEvent.class) {
        @Override
        public Event createBukkitEvent(IslandChunkReset args) {
            return new IslandChunkResetEvent(args.island, args.chunkPosition.getWorld(), args.chunkPosition.getX(),
                    args.chunkPosition.getZ());
        }

        @Override
        public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<IslandChunkReset> pluginEvent) {
            // Do nothing
        }
    };
    public static final PluginEventType<IslandClearGeneratorRates> ISLAND_CLEAR_GENERATOR_RATES_EVENT = new PluginEventType<IslandClearGeneratorRates>(IslandClearGeneratorRatesEvent.class) {
        @Override
        public Event createBukkitEvent(IslandClearGeneratorRates args) {
            return new IslandClearGeneratorRatesEvent(args.superiorPlayer, args.island, args.dimension);
        }
    };
    public static final PluginEventType<IslandClearPlayerPrivileges> ISLAND_CLEAR_PLAYER_PRIVILEGES_EVENT = new PluginEventType<IslandClearPlayerPrivileges>(IslandClearPlayerPrivilegesEvent.class) {
        @Override
        public Event createBukkitEvent(IslandClearPlayerPrivileges args) {
            return new IslandClearPlayerPrivilegesEvent(args.island, args.superiorPlayer, args.privilegedPlayer);
        }
    };
    public static final PluginEventType<IslandClearRatings> ISLAND_CLEAR_RATINGS_EVENT = new PluginEventType<IslandClearRatings>(IslandClearRatingsEvent.class) {
        @Override
        public Event createBukkitEvent(IslandClearRatings args) {
            return new IslandClearRatingsEvent(args.superiorPlayer, args.island);
        }
    };
    public static final PluginEventType<IslandClearRolesPrivileges> ISLAND_CLEAR_ROLES_PRIVILEGES_EVENT = new PluginEventType<IslandClearRolesPrivileges>(IslandClearRolesPrivilegesEvent.class) {
        @Override
        public Event createBukkitEvent(IslandClearRolesPrivileges args) {
            return new IslandClearRolesPrivilegesEvent(args.island, args.superiorPlayer);
        }
    };
    public static final PluginEventType<IslandClose> ISLAND_CLOSE_EVENT = new PluginEventType<IslandClose>(IslandCloseEvent.class) {
        @Override
        public Event createBukkitEvent(IslandClose args) {
            return new IslandCloseEvent(args.superiorPlayer, args.island);
        }
    };
    public static final PluginEventType<IslandCloseWarp> ISLAND_CLOSE_WARP_EVENT = new PluginEventType<IslandCloseWarp>(IslandCloseWarpEvent.class) {
        @Override
        public Event createBukkitEvent(IslandCloseWarp args) {
            return new IslandCloseWarpEvent(args.superiorPlayer, args.island, args.islandWarp);
        }
    };
    public static final PluginEventType<IslandCoopPlayer> ISLAND_COOP_PLAYER_EVENT = new PluginEventType<IslandCoopPlayer>(IslandCoopPlayerEvent.class) {
        @Override
        public Event createBukkitEvent(IslandCoopPlayer args) {
            return new IslandCoopPlayerEvent(args.island, args.superiorPlayer, args.targetPlayer);
        }
    };
    public static final PluginEventType<IslandCreate> ISLAND_CREATE_EVENT = new PluginEventType<IslandCreate>(IslandCreateEvent.class) {
        @Override
        public Event createBukkitEvent(IslandCreate args) {
            return new IslandCreateEvent(args.superiorPlayer, args.island, args.schematicName, args.canTeleport);
        }

        @Override
        public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<IslandCreate> pluginEvent) {
            super.applyBukkitToPluginEvent(bukkitEvent, pluginEvent);
            pluginEvent.getArgs().canTeleport = ((IslandCreateEvent) bukkitEvent).canTeleport();
        }
    };
    public static final PluginEventType<IslandCreateWarpCategory> ISLAND_CREATE_WARP_CATEGORY_EVENT = new PluginEventType<IslandCreateWarpCategory>(IslandCreateWarpCategoryEvent.class) {
        @Override
        public Event createBukkitEvent(IslandCreateWarpCategory args) {
            return new IslandCreateWarpCategoryEvent(args.superiorPlayer, args.island, args.categoryName);
        }
    };
    public static final PluginEventType<IslandCreateWarp> ISLAND_CREATE_WARP_EVENT = new PluginEventType<IslandCreateWarp>(IslandCreateWarpEvent.class) {
        @Override
        public Event createBukkitEvent(IslandCreateWarp args) {
            return new IslandCreateWarpEvent(args.superiorPlayer, args.island, args.warpName, args.location, args.openToPublic, args.warpCategory);
        }
    };
    public static final PluginEventType<IslandDeleteWarp> ISLAND_DELETE_WARP_EVENT = new PluginEventType<IslandDeleteWarp>(IslandDeleteWarpEvent.class) {
        @Override
        public Event createBukkitEvent(IslandDeleteWarp args) {
            return new IslandDeleteWarpEvent(args.superiorPlayer, args.island, args.islandWarp);
        }
    };
    public static final PluginEventType<IslandDisableFlag> ISLAND_DISABLE_FLAG_EVENT = new PluginEventType<IslandDisableFlag>(IslandDisableFlagEvent.class) {
        @Override
        public Event createBukkitEvent(IslandDisableFlag args) {
            return new IslandDisableFlagEvent(args.superiorPlayer, args.island, args.islandFlag);
        }
    };
    public static final PluginEventType<IslandDisband> ISLAND_DISBAND_EVENT = new PluginEventType<IslandDisband>(IslandDisbandEvent.class) {
        @Override
        public Event createBukkitEvent(IslandDisband args) {
            return new IslandDisbandEvent(args.superiorPlayer, args.island);
        }
    };
    public static final PluginEventType<IslandEnableFlag> ISLAND_ENABLE_FLAG_EVENT = new PluginEventType<IslandEnableFlag>(IslandEnableFlagEvent.class) {
        @Override
        public Event createBukkitEvent(IslandEnableFlag args) {
            return new IslandEnableFlagEvent(args.superiorPlayer, args.island, args.islandFlag);
        }
    };
    public static final PluginEventType<IslandClearFlags> ISLAND_CLEAR_FLAGS_EVENT = new PluginEventType<IslandClearFlags>(IslandClearFlagsEvent.class) {
        @Override
        public Event createBukkitEvent(IslandClearFlags args) {
            return new IslandClearFlagsEvent(args.island, args.superiorPlayer);
        }
    };
    public static final PluginEventType<IslandEnter> ISLAND_ENTER_EVENT = new PluginEventType<IslandEnter>(IslandEnterEvent.class) {
        @Override
        public Event createBukkitEvent(IslandEnter args) {
            return new IslandEnterEvent(args.superiorPlayer, args.island, args.enterCause);
        }

        @Override
        public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<IslandEnter> pluginEvent) {
            super.applyBukkitToPluginEvent(bukkitEvent, pluginEvent);

            IslandEnterEvent islandEnterEvent = (IslandEnterEvent) bukkitEvent;

            if (islandEnterEvent.isCancelled() && islandEnterEvent.getCancelTeleport() != null)
                pluginEvent.getArgs().superiorPlayer.teleport(islandEnterEvent.getCancelTeleport());
        }
    };
    public static final PluginEventType<IslandEnterPortal> ISLAND_ENTER_PORTAL_EVENT = new PluginEventType<IslandEnterPortal>(IslandEnterPortalEvent.class) {
        @Override
        public Event createBukkitEvent(IslandEnterPortal args) {
            return new IslandEnterPortalEvent(args.island, args.superiorPlayer, args.portalType, args.destination,
                    args.schematic, args.ignoreInvalidSchematic);
        }

        @Override
        public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<IslandEnterPortal> pluginEvent) {
            super.applyBukkitToPluginEvent(bukkitEvent, pluginEvent);
            pluginEvent.getArgs().destination = ((IslandEnterPortalEvent) bukkitEvent).getDestinationDimension();
            pluginEvent.getArgs().schematic = ((IslandEnterPortalEvent) bukkitEvent).getSchematic();
            pluginEvent.getArgs().ignoreInvalidSchematic = ((IslandEnterPortalEvent) bukkitEvent).isIgnoreInvalidSchematic();
        }
    };
    public static final PluginEventType<IslandEnterProtected> ISLAND_ENTER_PROTECTED_EVENT = new PluginEventType<IslandEnterProtected>(IslandEnterProtectedEvent.class) {
        @Override
        public Event createBukkitEvent(IslandEnterProtected args) {
            return new IslandEnterProtectedEvent(args.superiorPlayer, args.island, args.enterCause);
        }

        @Override
        public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<IslandEnterProtected> pluginEvent) {
            super.applyBukkitToPluginEvent(bukkitEvent, pluginEvent);

            IslandEnterProtectedEvent islandEnterProtectedEvent = (IslandEnterProtectedEvent) bukkitEvent;

            if (islandEnterProtectedEvent.isCancelled() && islandEnterProtectedEvent.getCancelTeleport() != null)
                pluginEvent.getArgs().superiorPlayer.teleport(islandEnterProtectedEvent.getCancelTeleport());
        }
    };
    public static final PluginEventType<IslandGenerateBlock> ISLAND_GENERATE_BLOCK_EVENT = new PluginEventType<IslandGenerateBlock>(IslandGenerateBlockEvent.class) {
        @Override
        public Event createBukkitEvent(IslandGenerateBlock args) {
            return new IslandGenerateBlockEvent(args.island, args.location, args.block);
        }

        @Override
        public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<IslandGenerateBlock> pluginEvent) {
            super.applyBukkitToPluginEvent(bukkitEvent, pluginEvent);
            pluginEvent.getArgs().block = ((IslandGenerateBlockEvent) bukkitEvent).getBlock();
            pluginEvent.getArgs().placeBlock = ((IslandGenerateBlockEvent) bukkitEvent).isPlaceBlock();
        }
    };
    public static final PluginEventType<IslandInvite> ISLAND_INVITE_EVENT = new PluginEventType<IslandInvite>(IslandInviteEvent.class) {
        @Override
        public Event createBukkitEvent(IslandInvite args) {
            return new IslandInviteEvent(args.superiorPlayer, args.targetPlayer, args.island);
        }
    };
    public static final PluginEventType<IslandJoin> ISLAND_JOIN_EVENT = new PluginEventType<IslandJoin>(IslandJoinEvent.class) {
        @Override
        public Event createBukkitEvent(IslandJoin args) {
            return new IslandJoinEvent(args.superiorPlayer, args.island, args.joinCause);
        }
    };
    public static final PluginEventType<IslandKick> ISLAND_KICK_EVENT = new PluginEventType<IslandKick>(IslandKickEvent.class) {
        @Override
        public Event createBukkitEvent(IslandKick args) {
            return new IslandKickEvent(args.superiorPlayer, args.targetPlayer, args.island);
        }
    };
    public static final PluginEventType<IslandLeave> ISLAND_LEAVE_EVENT = new PluginEventType<IslandLeave>(IslandLeaveEvent.class) {
        @Override
        public Event createBukkitEvent(IslandLeave args) {
            return new IslandLeaveEvent(args.superiorPlayer, args.island, args.leaveCause, args.location);
        }
    };
    public static final PluginEventType<IslandLeaveProtected> ISLAND_LEAVE_PROTECTED_EVENT = new PluginEventType<IslandLeaveProtected>(IslandLeaveProtectedEvent.class) {
        @Override
        public Event createBukkitEvent(IslandLeaveProtected args) {
            return new IslandLeaveEvent(args.superiorPlayer, args.island, args.leaveCause, args.location);
        }
    };
    public static final PluginEventType<IslandLockWorld> ISLAND_LOCK_WORLD_EVENT = new PluginEventType<IslandLockWorld>(IslandLockWorldEvent.class) {
        @Override
        public Event createBukkitEvent(IslandLockWorld args) {
            return new IslandLockWorldEvent(args.island, args.dimension);
        }
    };
    public static final PluginEventType<IslandOpen> ISLAND_OPEN_EVENT = new PluginEventType<IslandOpen>(IslandOpenEvent.class) {
        @Override
        public Event createBukkitEvent(IslandOpen args) {
            return new IslandOpenEvent(args.superiorPlayer, args.island);
        }
    };
    public static final PluginEventType<IslandOpenWarp> ISLAND_OPEN_WARP_EVENT = new PluginEventType<IslandOpenWarp>(IslandOpenWarpEvent.class) {
        @Override
        public Event createBukkitEvent(IslandOpenWarp args) {
            return new IslandOpenWarpEvent(args.superiorPlayer, args.island, args.islandWarp);
        }
    };
    public static final PluginEventType<IslandQuit> ISLAND_QUIT_EVENT = new PluginEventType<IslandQuit>(IslandQuitEvent.class) {
        @Override
        public Event createBukkitEvent(IslandQuit args) {
            return new IslandQuitEvent(args.superiorPlayer, args.island);
        }
    };
    public static final PluginEventType<IslandRate> ISLAND_RATE_EVENT = new PluginEventType<IslandRate>(IslandRateEvent.class) {
        @Override
        public Event createBukkitEvent(IslandRate args) {
            return new IslandRateEvent(args.superiorPlayer, args.ratingPlayer, args.island, args.rating);
        }
    };
    public static final PluginEventType<IslandRemoveBlockLimit> ISLAND_REMOVE_BLOCK_LIMIT_EVENT = new PluginEventType<IslandRemoveBlockLimit>(IslandRemoveBlockLimitEvent.class) {
        @Override
        public Event createBukkitEvent(IslandRemoveBlockLimit args) {
            return new IslandRemoveBlockLimitEvent(args.superiorPlayer, args.island, args.block);
        }
    };
    public static final PluginEventType<IslandRemoveEffect> ISLAND_REMOVE_EFFECT_EVENT = new PluginEventType<IslandRemoveEffect>(IslandRemoveEffectEvent.class) {
        @Override
        public Event createBukkitEvent(IslandRemoveEffect args) {
            return new IslandRemoveEffectEvent(args.superiorPlayer, args.island, args.effectType);
        }
    };
    public static final PluginEventType<IslandRemoveGeneratorRate> ISLAND_REMOVE_GENERATOR_RATE_EVENT = new PluginEventType<IslandRemoveGeneratorRate>(IslandRemoveGeneratorRateEvent.class) {
        @Override
        public Event createBukkitEvent(IslandRemoveGeneratorRate args) {
            return new IslandRemoveGeneratorRateEvent(args.superiorPlayer, args.island, args.block, args.dimension);
        }
    };
    public static final PluginEventType<IslandRemoveRating> ISLAND_REMOVE_RATING_EVENT = new PluginEventType<IslandRemoveRating>(IslandRemoveRatingEvent.class) {
        @Override
        public Event createBukkitEvent(IslandRemoveRating args) {
            return new IslandRemoveRatingEvent(args.superiorPlayer, args.ratingPlayer, args.island);
        }
    };
    public static final PluginEventType<IslandRemoveRoleLimit> ISLAND_REMOVE_ROLE_LIMIT_EVENT = new PluginEventType<IslandRemoveRoleLimit>(IslandRemoveRoleLimitEvent.class) {
        @Override
        public Event createBukkitEvent(IslandRemoveRoleLimit args) {
            return new IslandRemoveRoleLimitEvent(args.superiorPlayer, args.island, args.playerRole);
        }
    };
    public static final PluginEventType<IslandRemoveVisitorHome> ISLAND_REMOVE_VISITOR_HOME_EVENT = new PluginEventType<IslandRemoveVisitorHome>(IslandRemoveVisitorHomeEvent.class) {
        @Override
        public Event createBukkitEvent(IslandRemoveVisitorHome args) {
            return new IslandRemoveVisitorHomeEvent(args.superiorPlayer, args.island);
        }
    };
    public static final PluginEventType<IslandRename> ISLAND_RENAME_EVENT = new PluginEventType<IslandRename>(IslandRenameEvent.class) {
        @Override
        public Event createBukkitEvent(IslandRename args) {
            return new IslandRenameEvent(args.island, args.superiorPlayer, args.islandName);
        }

        @Override
        public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<IslandRename> pluginEvent) {
            super.applyBukkitToPluginEvent(bukkitEvent, pluginEvent);
            pluginEvent.getArgs().islandName = ((IslandRenameEvent) bukkitEvent).getIslandName();
        }
    };
    public static final PluginEventType<IslandRenameWarpCategory> ISLAND_RENAME_WARP_CATEGORY_EVENT = new PluginEventType<IslandRenameWarpCategory>(IslandRenameWarpCategoryEvent.class) {
        @Override
        public Event createBukkitEvent(IslandRenameWarpCategory args) {
            return new IslandRenameWarpCategoryEvent(args.superiorPlayer, args.island, args.warpCategory, args.categoryName);
        }

        @Override
        public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<IslandRenameWarpCategory> pluginEvent) {
            super.applyBukkitToPluginEvent(bukkitEvent, pluginEvent);
            pluginEvent.getArgs().categoryName = ((IslandRenameWarpCategoryEvent) bukkitEvent).getCategoryName();
        }
    };
    public static final PluginEventType<IslandRenameWarp> ISLAND_RENAME_WARP_EVENT = new PluginEventType<IslandRenameWarp>(IslandRenameWarpEvent.class) {
        @Override
        public Event createBukkitEvent(IslandRenameWarp args) {
            return new IslandRenameWarpEvent(args.superiorPlayer, args.island, args.islandWarp, args.warpName);
        }

        @Override
        public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<IslandRenameWarp> pluginEvent) {
            super.applyBukkitToPluginEvent(bukkitEvent, pluginEvent);
            pluginEvent.getArgs().warpName = ((IslandRenameWarpEvent) bukkitEvent).getWarpName();
        }
    };
    public static final PluginEventType<IslandRestrictMove> ISLAND_RESTRICT_MOVE_EVENT = new PluginEventType<IslandRestrictMove>(IslandRestrictMoveEvent.class) {
        @Override
        public Event createBukkitEvent(IslandRestrictMove args) {
            return new IslandRestrictMoveEvent(args.superiorPlayer, args.restrictReason);
        }

        @Override
        public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<IslandRestrictMove> pluginEvent) {
            // Do nothing
        }
    };
    public static final PluginEventType<IslandSchematicPaste> ISLAND_SCHEMATIC_PASTE_EVENT = new PluginEventType<IslandSchematicPaste>(IslandSchematicPasteEvent.class) {
        @Override
        public Event createBukkitEvent(IslandSchematicPaste args) {
            return new IslandSchematicPasteEvent(args.island, args.schematicName, args.location);
        }

        @Override
        public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<IslandSchematicPaste> pluginEvent) {
            // Do nothing
        }
    };
    public static final PluginEventType<IslandSetHome> ISLAND_SET_HOME_EVENT = new PluginEventType<IslandSetHome>(IslandSetHomeEvent.class) {
        @Override
        public Event createBukkitEvent(IslandSetHome args) {
            return new IslandSetHomeEvent(args.island, args.islandHome, args.reason, args.superiorPlayer);
        }

        @Override
        public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<IslandSetHome> pluginEvent) {
            super.applyBukkitToPluginEvent(bukkitEvent, pluginEvent);
            pluginEvent.getArgs().islandHome = ((IslandSetHomeEvent) bukkitEvent).getIslandHome();
        }
    };
    public static final PluginEventType<IslandSetVisitorHome> ISLAND_SET_VISITOR_HOME_EVENT = new PluginEventType<IslandSetVisitorHome>(IslandSetVisitorHomeEvent.class) {
        @Override
        public Event createBukkitEvent(IslandSetVisitorHome args) {
            return new IslandSetVisitorHomeEvent(args.superiorPlayer, args.island, args.islandVisitorHome);
        }

        @Override
        public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<IslandSetVisitorHome> pluginEvent) {
            super.applyBukkitToPluginEvent(bukkitEvent, pluginEvent);
            pluginEvent.getArgs().islandVisitorHome = ((IslandSetVisitorHomeEvent) bukkitEvent).getIslandVisitorHome();
        }
    };
    public static final PluginEventType<IslandTransfer> ISLAND_TRANSFER_EVENT = new PluginEventType<IslandTransfer>(IslandTransferEvent.class) {
        @Override
        public Event createBukkitEvent(IslandTransfer args) {
            return new IslandTransferEvent(args.island, args.previousOwner, args.superiorPlayer);
        }
    };
    public static final PluginEventType<IslandUnban> ISLAND_UNBAN_EVENT = new PluginEventType<IslandUnban>(IslandUnbanEvent.class) {
        @Override
        public Event createBukkitEvent(IslandUnban args) {
            return new IslandUnbanEvent(args.superiorPlayer, args.unbannedPlayer, args.island);
        }
    };
    public static final PluginEventType<IslandUncoopPlayer> ISLAND_UNCOOP_PLAYER_EVENT = new PluginEventType<IslandUncoopPlayer>(IslandUncoopPlayerEvent.class) {
        @Override
        public Event createBukkitEvent(IslandUncoopPlayer args) {
            return new IslandUncoopPlayerEvent(args.island, args.superiorPlayer, args.targetPlayer, args.uncoopReason);
        }
    };
    public static final PluginEventType<IslandUnlockWorld> ISLAND_UNLOCK_WORLD_EVENT = new PluginEventType<IslandUnlockWorld>(IslandUnlockWorldEvent.class) {
        @Override
        public Event createBukkitEvent(IslandUnlockWorld args) {
            return new IslandUnlockWorldEvent(args.island, args.dimension);
        }
    };
    public static final PluginEventType<IslandUpgrade> ISLAND_UPGRADE_EVENT = new PluginEventType<IslandUpgrade>(IslandUpgradeEvent.class) {
        @Override
        public Event createBukkitEvent(IslandUpgrade args) {
            return new IslandUpgradeEvent(args.superiorPlayer, args.island, args.upgrade, args.nextLevel, args.commands,
                    args.upgradeCause, args.upgradeCost);
        }

        @Override
        public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<IslandUpgrade> pluginEvent) {
            super.applyBukkitToPluginEvent(bukkitEvent, pluginEvent);
            pluginEvent.getArgs().commands = ((IslandUpgradeEvent) bukkitEvent).getCommands();
            pluginEvent.getArgs().upgradeCost = ((IslandUpgradeEvent) bukkitEvent).getUpgradeCost();
        }
    };
    public static final PluginEventType<IslandWorldReset> ISLAND_WORLD_RESET_EVENT = new PluginEventType<IslandWorldReset>(IslandWorldResetEvent.class) {
        @Override
        public Event createBukkitEvent(IslandWorldReset args) {
            return new IslandWorldResetEvent(args.superiorPlayer, args.island, args.dimension);
        }
    };
    public static final PluginEventType<IslandWorthCalculated> ISLAND_WORTH_CALCULATED_EVENT = new PluginEventType<IslandWorthCalculated>(IslandWorthCalculatedEvent.class) {
        @Override
        public Event createBukkitEvent(IslandWorthCalculated args) {
            return new IslandWorthCalculatedEvent(args.island, args.superiorPlayer, args.islandLevel, args.islandWorth);
        }
    };
    public static final PluginEventType<IslandWorthUpdate> ISLAND_WORTH_UPDATE_EVENT = new PluginEventType<IslandWorthUpdate>(IslandWorthUpdateEvent.class) {
        @Override
        public Event createBukkitEvent(IslandWorthUpdate args) {
            return new IslandWorthUpdateEvent(args.island, args.oldWorth, args.oldLevel, args.newWorth, args.newLevel);
        }

        @Override
        public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<IslandWorthUpdate> pluginEvent) {
            // Do nothing
        }
    };
    public static final PluginEventType<MissionComplete> MISSION_COMPLETE_EVENT = new PluginEventType<MissionComplete>(MissionCompleteEvent.class) {
        @Override
        public Event createBukkitEvent(MissionComplete args) {
            return new MissionCompleteEvent(args.superiorPlayer, args.missionsHolder, args.mission, args.itemRewards, args.commandRewards);
        }

        @Override
        public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<MissionComplete> pluginEvent) {
            super.applyBukkitToPluginEvent(bukkitEvent, pluginEvent);
            pluginEvent.getArgs().itemRewards = ((MissionCompleteEvent) bukkitEvent).getItemRewards();
            pluginEvent.getArgs().commandRewards = ((MissionCompleteEvent) bukkitEvent).getCommandRewards();
        }
    };
    public static final PluginEventType<MissionReset> MISSION_RESET_EVENT = new PluginEventType<MissionReset>(MissionResetEvent.class) {
        @Override
        public Event createBukkitEvent(MissionReset args) {
            return new MissionResetEvent(args.superiorPlayer, args.missionsHolder, args.mission);
        }
    };
    public static final PluginEventType<PlayerChangeBorderColor> PLAYER_CHANGE_BORDER_COLOR_EVENT = new PluginEventType<PlayerChangeBorderColor>(PlayerChangeBorderColorEvent.class) {
        @Override
        public Event createBukkitEvent(PlayerChangeBorderColor args) {
            return new PlayerChangeBorderColorEvent(args.superiorPlayer, args.borderColor);
        }
    };
    public static final PluginEventType<PlayerChangeLanguage> PLAYER_CHANGE_LANGUAGE_EVENT = new PluginEventType<PlayerChangeLanguage>(PlayerChangeLanguageEvent.class) {
        @Override
        public Event createBukkitEvent(PlayerChangeLanguage args) {
            return new PlayerChangeLanguageEvent(args.superiorPlayer, args.language);
        }
    };
    public static final PluginEventType<PlayerChangeName> PLAYER_CHANGE_NAME_EVENT = new PluginEventType<PlayerChangeName>(PlayerChangeNameEvent.class) {
        @Override
        public Event createBukkitEvent(PlayerChangeName args) {
            return new PlayerChangeNameEvent(args.superiorPlayer, args.newName);
        }

        @Override
        public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<PlayerChangeName> pluginEvent) {
            // Do nothing
        }
    };
    public static final PluginEventType<PlayerChangeRole> PLAYER_CHANGE_ROLE_EVENT = new PluginEventType<PlayerChangeRole>(PlayerChangeRoleEvent.class) {
        @Override
        public Event createBukkitEvent(PlayerChangeRole args) {
            return new PlayerChangeRoleEvent(args.superiorPlayer, args.newRole);
        }
    };
    public static final PluginEventType<PlayerCloseMenu> PLAYER_CLOSE_MENU_EVENT = new PluginEventType<PlayerCloseMenu>(PlayerCloseMenuEvent.class) {
        @Override
        public Event createBukkitEvent(PlayerCloseMenu args) {
            return new PlayerCloseMenuEvent(args.superiorPlayer, args.menuView, args.newMenuView);
        }
    };
    public static final PluginEventType<PlayerOpenMenu> PLAYER_OPEN_MENU_EVENT = new PluginEventType<PlayerOpenMenu>(PlayerOpenMenuEvent.class) {
        @Override
        public Event createBukkitEvent(PlayerOpenMenu args) {
            return new PlayerOpenMenuEvent(args.superiorPlayer, args.menuView);
        }
    };
    public static final PluginEventType<PlayerReplace> PLAYER_REPLACE_EVENT = new PluginEventType<PlayerReplace>(PlayerReplaceEvent.class) {
        @Override
        public Event createBukkitEvent(PlayerReplace args) {
            return new PlayerReplaceEvent(args.superiorPlayer, args.newPlayer);
        }

        @Override
        public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<PlayerReplace> pluginEvent) {
            // Do nothing
        }
    };
    public static final PluginEventType<PlayerToggleBlocksStacker> PLAYER_TOGGLE_BLOCKS_STACKER_EVENT = new PluginEventType<PlayerToggleBlocksStacker>(PlayerToggleBlocksStackerEvent.class) {
        @Override
        public Event createBukkitEvent(PlayerToggleBlocksStacker args) {
            return new PlayerToggleBlocksStackerEvent(args.superiorPlayer);
        }
    };
    public static final PluginEventType<PlayerToggleBorder> PLAYER_TOGGLE_BORDER_EVENT = new PluginEventType<PlayerToggleBorder>(PlayerToggleBorderEvent.class) {
        @Override
        public Event createBukkitEvent(PlayerToggleBorder args) {
            return new PlayerToggleBorderEvent(args.superiorPlayer);
        }
    };
    public static final PluginEventType<PlayerToggleBypass> PLAYER_TOGGLE_BYPASS_EVENT = new PluginEventType<PlayerToggleBypass>(PlayerToggleBypassEvent.class) {
        @Override
        public Event createBukkitEvent(PlayerToggleBypass args) {
            return new PlayerToggleBypassEvent(args.superiorPlayer);
        }
    };
    public static final PluginEventType<PlayerToggleFly> PLAYER_TOGGLE_FLY_EVENT = new PluginEventType<PlayerToggleFly>(PlayerToggleFlyEvent.class) {
        @Override
        public Event createBukkitEvent(PlayerToggleFly args) {
            return new PlayerToggleFlyEvent(args.superiorPlayer);
        }
    };
    public static final PluginEventType<PlayerTogglePanel> PLAYER_TOGGLE_PANEL_EVENT = new PluginEventType<PlayerTogglePanel>(PlayerTogglePanelEvent.class) {
        @Override
        public Event createBukkitEvent(PlayerTogglePanel args) {
            return new PlayerTogglePanelEvent(args.superiorPlayer);
        }
    };
    public static final PluginEventType<PlayerToggleSpy> PLAYER_TOGGLE_SPY_EVENT = new PluginEventType<PlayerToggleSpy>(PlayerToggleSpyEvent.class) {
        @Override
        public Event createBukkitEvent(PlayerToggleSpy args) {
            return new PlayerToggleSpyEvent(args.superiorPlayer);
        }
    };
    public static final PluginEventType<PlayerToggleTeamChat> PLAYER_TOGGLE_TEAM_CHAT_EVENT = new PluginEventType<PlayerToggleTeamChat>(PlayerToggleTeamChatEvent.class) {
        @Override
        public Event createBukkitEvent(PlayerToggleTeamChat args) {
            return new PlayerToggleTeamChatEvent(args.superiorPlayer);
        }
    };
    public static final PluginEventType<PluginInitialized> PLUGIN_INITIALIZED_EVENT = new PluginEventType<PluginInitialized>(PluginInitializedEvent.class) {
        @Override
        public Event createBukkitEvent(PluginInitialized args) {
            return new PluginInitializedEvent(args.plugin);
        }

        @Override
        public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<PluginInitialized> pluginEvent) {
            // Do nothing
        }
    };
    public static final PluginEventType<PluginInitialize> PLUGIN_INITIALIZE_EVENT = new PluginEventType<PluginInitialize>(PluginInitializeEvent.class) {
        @Override
        public Event createBukkitEvent(PluginInitialize args) {
            return new PluginInitializeEvent(args.plugin);
        }

        @Override
        public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<PluginInitialize> pluginEvent) {
            pluginEvent.getArgs().islandsContainer = ((PluginInitializeEvent) bukkitEvent).getIslandsContainer();
            pluginEvent.getArgs().playersContainer = ((PluginInitializeEvent) bukkitEvent).getPlayersContainer();
        }
    };
    public static final PluginEventType<PluginLoadData> PLUGIN_LOAD_DATA_EVENT = new PluginEventType<PluginLoadData>(null) {
        @Override
        public Event createBukkitEvent(PluginLoadData args) {
            return new PluginLoadDataEvent(args.plugin);
        }
    };
    public static final PluginEventType<PostIslandCreate> POST_ISLAND_CREATE_EVENT = new PluginEventType<PostIslandCreate>(PostIslandCreateEvent.class) {
        @Override
        public Event createBukkitEvent(PostIslandCreate args) {
            return new PostIslandCreateEvent(args.superiorPlayer, args.island);
        }
    };
    public static final PluginEventType<PreIslandCreate> PRE_ISLAND_CREATE_EVENT = new PluginEventType<PreIslandCreate>(PreIslandCreateEvent.class) {
        @Override
        public Event createBukkitEvent(PreIslandCreate args) {
            return new PreIslandCreateEvent(args.superiorPlayer, args.islandName);
        }
    };
    public static final PluginEventType<SendMessage> SEND_MESSAGE_EVENT = new PluginEventType<SendMessage>(SendMessageEvent.class) {
        @Override
        public Event createBukkitEvent(SendMessage args) {
            return new SendMessageEvent(args.receiver, args.messageType, args.messageComponent, args.args);
        }

        @Override
        public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<SendMessage> pluginEvent) {
            super.applyBukkitToPluginEvent(bukkitEvent, pluginEvent);
            pluginEvent.getArgs().messageComponent = ((SendMessageEvent) bukkitEvent).getMessageComponent();
        }
    };

    private final String bukkitEventName;

    private PluginEventType(@Nullable Class<? extends Event> bukkitEventClass) {
        this.bukkitEventName = bukkitEventClass == null ? null : bukkitEventClass.getSimpleName().toLowerCase(Locale.ENGLISH);
        ALL_TYPES.add(this);
    }

    @Override
    public PluginEvent<Args> createEvent(Args args) {
        return new PluginEvent<>(this, args);
    }

    @Nullable
    public String getBukkitEventName() {
        return bukkitEventName;
    }

    @Nullable
    public abstract Event createBukkitEvent(Args args);

    public void applyBukkitToPluginEvent(Event bukkitEvent, PluginEvent<Args> pluginEvent) {
        if (bukkitEvent instanceof Cancellable && ((Cancellable) bukkitEvent).isCancelled())
            pluginEvent.setCancelled();
    }

    public static Collection<PluginEventType<?>> values() {
        return Collections.unmodifiableList(ALL_TYPES);
    }

}
