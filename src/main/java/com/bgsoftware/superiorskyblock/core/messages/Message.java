package com.bgsoftware.superiorskyblock.core.messages;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.bgsoftware.superiorskyblock.api.service.message.MessagesService;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.collections.AutoRemovalCollection;
import com.bgsoftware.superiorskyblock.core.events.EventResult;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.messages.component.impl.ComplexMessageComponent;
import com.bgsoftware.superiorskyblock.player.PlayerLocales;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public enum Message {

    ADMIN_ADD_PLAYER,
    ADMIN_ADD_PLAYER_NAME,
    ADMIN_DEPOSIT_MONEY,
    ADMIN_DEPOSIT_MONEY_NAME,
    ADMIN_HELP_FOOTER,
    ADMIN_HELP_HEADER,
    ADMIN_HELP_LINE,
    ADMIN_HELP_NEXT_PAGE,
    ALREADY_IN_ISLAND,
    ALREADY_IN_ISLAND_OTHER,
    BANK_DEPOSIT_COMPLETED,
    BANK_DEPOSIT_CUSTOM,
    BANK_LIMIT_EXCEED,
    BANK_WITHDRAW_COMPLETED,
    BANK_WITHDRAW_CUSTOM,
    BANNED_FROM_ISLAND,
    BAN_ANNOUNCEMENT,
    BAN_PLAYERS_WITH_LOWER_ROLE,
    BLOCK_COUNTS_CHECK,
    BLOCK_COUNTS_CHECK_EMPTY,
    BLOCK_COUNTS_CHECK_MATERIAL,
    BLOCK_COUNT_CHECK,
    BLOCK_LEVEL,
    BLOCK_LEVEL_WORTHLESS,
    BLOCK_VALUE,
    BLOCK_VALUE_WORTHLESS,
    BONUS_SET_SUCCESS,
    BONUS_SYNC_ALL,
    BONUS_SYNC_NAME,
    BONUS_SYNC,
    BORDER_PLAYER_COLOR_NAME_BLUE,
    BORDER_PLAYER_COLOR_NAME_GREEN,
    BORDER_PLAYER_COLOR_NAME_RED,
    BORDER_PLAYER_COLOR_UPDATED,
    BUILD_OUTSIDE_ISLAND,
    CANNOT_SET_ROLE,
    CHANGED_BANK_LIMIT,
    CHANGED_BANK_LIMIT_ALL,
    CHANGED_BANK_LIMIT_NAME,
    CHANGED_BIOME,
    CHANGED_BIOME_ALL,
    CHANGED_BIOME_NAME,
    CHANGED_BIOME_OTHER,
    CHANGED_BLOCK_AMOUNT,
    CHANGED_BLOCK_LIMIT,
    CHANGED_BLOCK_LIMIT_ALL,
    CHANGED_BLOCK_LIMIT_NAME,
    CHANGED_CHEST_SIZE,
    CHANGED_CHEST_SIZE_ALL,
    CHANGED_CHEST_SIZE_NAME,
    CHANGED_COOP_LIMIT,
    CHANGED_COOP_LIMIT_ALL,
    CHANGED_COOP_LIMIT_NAME,
    CHANGED_CROP_GROWTH,
    CHANGED_CROP_GROWTH_ALL,
    CHANGED_CROP_GROWTH_NAME,
    CHANGED_DISCORD,
    CHANGED_ENTITY_LIMIT,
    CHANGED_ENTITY_LIMIT_ALL,
    CHANGED_ENTITY_LIMIT_NAME,
    CHANGED_ISLAND_EFFECT_LEVEL,
    CHANGED_ISLAND_EFFECT_LEVEL_ALL,
    CHANGED_ISLAND_EFFECT_LEVEL_NAME,
    CHANGED_ISLAND_SIZE,
    CHANGED_ISLAND_SIZE_ALL,
    CHANGED_ISLAND_SIZE_BUILD_OUTSIDE,
    CHANGED_ISLAND_SIZE_NAME,
    CHANGED_LANGUAGE,
    CHANGED_MOB_DROPS,
    CHANGED_MOB_DROPS_ALL,
    CHANGED_MOB_DROPS_NAME,
    CHANGED_NAME,
    CHANGED_NAME_OTHER,
    CHANGED_NAME_OTHER_NAME,
    CHANGED_PAYPAL,
    CHANGED_ROLE_LIMIT,
    CHANGED_ROLE_LIMIT_ALL,
    CHANGED_ROLE_LIMIT_NAME,
    CHANGED_SPAWNER_RATES,
    CHANGED_SPAWNER_RATES_ALL,
    CHANGED_SPAWNER_RATES_NAME,
    CHANGED_TEAM_LIMIT,
    CHANGED_TEAM_LIMIT_ALL,
    CHANGED_TEAM_LIMIT_NAME,
    CHANGED_TELEPORT_LOCATION,
    CHANGED_WARPS_LIMIT,
    CHANGED_WARPS_LIMIT_ALL,
    CHANGED_WARPS_LIMIT_NAME,
    CHANGE_PERMISSION_FOR_HIGHER_ROLE,
    COMMAND_ARGUMENT_ALL_ISLANDS("*"),
    COMMAND_ARGUMENT_ALL_PLAYERS("*"),
    COMMAND_ARGUMENT_AMOUNT("amount"),
    COMMAND_ARGUMENT_BIOME("biome"),
    COMMAND_ARGUMENT_BORDER_COLOR("border-color"),
    COMMAND_ARGUMENT_COMMAND("command..."),
    COMMAND_ARGUMENT_DISCORD("discord..."),
    COMMAND_ARGUMENT_EFFECT("effect"),
    COMMAND_ARGUMENT_EMAIL("email"),
    COMMAND_ARGUMENT_ENTITY("entity"),
    COMMAND_ARGUMENT_ISLAND_NAME("island-name"),
    COMMAND_ARGUMENT_ISLAND_ROLE("island-role"),
    COMMAND_ARGUMENT_LEADER("leader"),
    COMMAND_ARGUMENT_LEVEL("level"),
    COMMAND_ARGUMENT_LIMIT("limit"),
    COMMAND_ARGUMENT_MATERIAL("material"),
    COMMAND_ARGUMENT_MENU("menu-name"),
    COMMAND_ARGUMENT_MESSAGE("message..."),
    COMMAND_ARGUMENT_MISSION_NAME("mission-name"),
    COMMAND_ARGUMENT_MODULE_NAME("module-name"),
    COMMAND_ARGUMENT_MULTIPLIER("multiplier"),
    COMMAND_ARGUMENT_NEW_LEADER("new-leader"),
    COMMAND_ARGUMENT_PAGE("page"),
    COMMAND_ARGUMENT_PATH("path"),
    COMMAND_ARGUMENT_PERMISSION("permission"),
    COMMAND_ARGUMENT_PLAYER_NAME("player-name"),
    COMMAND_ARGUMENT_PRIVATE("private"),
    COMMAND_ARGUMENT_RATING("rating"),
    COMMAND_ARGUMENT_ROWS("rows"),
    COMMAND_ARGUMENT_SCHEMATIC_NAME("schematic-name"),
    COMMAND_ARGUMENT_SETTINGS("settings"),
    COMMAND_ARGUMENT_SIZE("size"),
    COMMAND_ARGUMENT_TIME("time"),
    COMMAND_ARGUMENT_TITLE_DURATION("duration"),
    COMMAND_ARGUMENT_TITLE_FADE_IN("fade-in"),
    COMMAND_ARGUMENT_TITLE_FADE_OUT("fade-out"),
    COMMAND_ARGUMENT_UPGRADE_NAME("upgrade-name"),
    COMMAND_ARGUMENT_VALUE("value"),
    COMMAND_ARGUMENT_WARP_CATEGORY("warp-category"),
    COMMAND_ARGUMENT_WARP_NAME("warp-name"),
    COMMAND_ARGUMENT_WORLD("world"),
    COMMAND_COOLDOWN_FORMAT,
    COMMAND_DESCRIPTION_ACCEPT,
    COMMAND_DESCRIPTION_ADMIN,
    COMMAND_DESCRIPTION_ADMIN_ADD,
    COMMAND_DESCRIPTION_ADMIN_ADD_BLOCK_LIMIT,
    COMMAND_DESCRIPTION_ADMIN_ADD_BONUS,
    COMMAND_DESCRIPTION_ADMIN_ADD_COOP_LIMIT,
    COMMAND_DESCRIPTION_ADMIN_ADD_CROP_GROWTH,
    COMMAND_DESCRIPTION_ADMIN_ADD_EFFECT,
    COMMAND_DESCRIPTION_ADMIN_ADD_ENTITY_LIMIT,
    COMMAND_DESCRIPTION_ADMIN_ADD_GENERATOR,
    COMMAND_DESCRIPTION_ADMIN_ADD_MOB_DROPS,
    COMMAND_DESCRIPTION_ADMIN_ADD_SIZE,
    COMMAND_DESCRIPTION_ADMIN_ADD_SPAWNER_RATES,
    COMMAND_DESCRIPTION_ADMIN_ADD_TEAM_LIMIT,
    COMMAND_DESCRIPTION_ADMIN_ADD_WARPS_LIMIT,
    COMMAND_DESCRIPTION_ADMIN_BONUS,
    COMMAND_DESCRIPTION_ADMIN_BYPASS,
    COMMAND_DESCRIPTION_ADMIN_CHEST,
    COMMAND_DESCRIPTION_ADMIN_CLEAR_GENERATOR,
    COMMAND_DESCRIPTION_ADMIN_CLOSE,
    COMMAND_DESCRIPTION_ADMIN_CMD_ALL,
    COMMAND_DESCRIPTION_ADMIN_COUNT,
    COMMAND_DESCRIPTION_ADMIN_DATA,
    COMMAND_DESCRIPTION_ADMIN_DEBUG,
    COMMAND_DESCRIPTION_ADMIN_DEL_WARP,
    COMMAND_DESCRIPTION_ADMIN_DEMOTE,
    COMMAND_DESCRIPTION_ADMIN_DEPOSIT,
    COMMAND_DESCRIPTION_ADMIN_DISBAND,
    COMMAND_DESCRIPTION_ADMIN_GIVE_DISBANDS,
    COMMAND_DESCRIPTION_ADMIN_IGNORE,
    COMMAND_DESCRIPTION_ADMIN_JOIN,
    COMMAND_DESCRIPTION_ADMIN_KICK,
    COMMAND_DESCRIPTION_ADMIN_MISSION,
    COMMAND_DESCRIPTION_ADMIN_MODULES,
    COMMAND_DESCRIPTION_ADMIN_MSG,
    COMMAND_DESCRIPTION_ADMIN_MSG_ALL,
    COMMAND_DESCRIPTION_ADMIN_NAME,
    COMMAND_DESCRIPTION_ADMIN_OPEN,
    COMMAND_DESCRIPTION_ADMIN_OPEN_MENU,
    COMMAND_DESCRIPTION_ADMIN_PROMOTE,
    COMMAND_DESCRIPTION_ADMIN_PURGE,
    COMMAND_DESCRIPTION_ADMIN_RANKUP,
    COMMAND_DESCRIPTION_ADMIN_RECALC,
    COMMAND_DESCRIPTION_ADMIN_RELOAD,
    COMMAND_DESCRIPTION_ADMIN_REMOVE_BLOCK_LIMIT,
    COMMAND_DESCRIPTION_ADMIN_REMOVE_RATINGS,
    COMMAND_DESCRIPTION_ADMIN_RESET_WORLD,
    COMMAND_DESCRIPTION_ADMIN_SCHEMATIC,
    COMMAND_DESCRIPTION_ADMIN_SETTINGS,
    COMMAND_DESCRIPTION_ADMIN_SET_BANK_LIMIT,
    COMMAND_DESCRIPTION_ADMIN_SET_BIOME,
    COMMAND_DESCRIPTION_ADMIN_SET_BLOCK_AMOUNT,
    COMMAND_DESCRIPTION_ADMIN_SET_BLOCK_LIMIT,
    COMMAND_DESCRIPTION_ADMIN_SET_CHEST_ROW,
    COMMAND_DESCRIPTION_ADMIN_SET_COOP_LIMIT,
    COMMAND_DESCRIPTION_ADMIN_SET_CROP_GROWTH,
    COMMAND_DESCRIPTION_ADMIN_SET_DISBANDS,
    COMMAND_DESCRIPTION_ADMIN_SET_EFFECT,
    COMMAND_DESCRIPTION_ADMIN_SET_ENTITY_LIMIT,
    COMMAND_DESCRIPTION_ADMIN_SET_GENERATOR,
    COMMAND_DESCRIPTION_ADMIN_SET_LEADER,
    COMMAND_DESCRIPTION_ADMIN_SET_MOB_DROPS,
    COMMAND_DESCRIPTION_ADMIN_SET_PERMISSION,
    COMMAND_DESCRIPTION_ADMIN_SET_RATE,
    COMMAND_DESCRIPTION_ADMIN_SET_ROLE_LIMIT,
    COMMAND_DESCRIPTION_ADMIN_SET_SETTINGS,
    COMMAND_DESCRIPTION_ADMIN_SET_SIZE,
    COMMAND_DESCRIPTION_ADMIN_SET_SPAWN,
    COMMAND_DESCRIPTION_ADMIN_SET_SPAWNER_RATES,
    COMMAND_DESCRIPTION_ADMIN_SET_TEAM_LIMIT,
    COMMAND_DESCRIPTION_ADMIN_SET_UPGRADE,
    COMMAND_DESCRIPTION_ADMIN_SET_WARPS_LIMIT,
    COMMAND_DESCRIPTION_ADMIN_SHOW,
    COMMAND_DESCRIPTION_ADMIN_SPAWN,
    COMMAND_DESCRIPTION_ADMIN_SPY,
    COMMAND_DESCRIPTION_ADMIN_STATS,
    COMMAND_DESCRIPTION_ADMIN_SYNC_BONUS,
    COMMAND_DESCRIPTION_ADMIN_SYNC_UPGRADES,
    COMMAND_DESCRIPTION_ADMIN_TELEPORT,
    COMMAND_DESCRIPTION_ADMIN_TITLE,
    COMMAND_DESCRIPTION_ADMIN_TITLE_ALL,
    COMMAND_DESCRIPTION_ADMIN_UNIGNORE,
    COMMAND_DESCRIPTION_ADMIN_UNLOCK_WORLD,
    COMMAND_DESCRIPTION_ADMIN_WITHDRAW,
    COMMAND_DESCRIPTION_BALANCE,
    COMMAND_DESCRIPTION_BAN,
    COMMAND_DESCRIPTION_BANK,
    COMMAND_DESCRIPTION_BIOME,
    COMMAND_DESCRIPTION_BORDER,
    COMMAND_DESCRIPTION_CHEST,
    COMMAND_DESCRIPTION_CLOSE,
    COMMAND_DESCRIPTION_COOP,
    COMMAND_DESCRIPTION_COOPS,
    COMMAND_DESCRIPTION_COUNTS,
    COMMAND_DESCRIPTION_CREATE,
    COMMAND_DESCRIPTION_DEL_WARP,
    COMMAND_DESCRIPTION_DEMOTE,
    COMMAND_DESCRIPTION_DEPOSIT,
    COMMAND_DESCRIPTION_DISBAND,
    COMMAND_DESCRIPTION_EXPEL,
    COMMAND_DESCRIPTION_FLY,
    COMMAND_DESCRIPTION_HELP,
    COMMAND_DESCRIPTION_INVITE,
    COMMAND_DESCRIPTION_KICK,
    COMMAND_DESCRIPTION_LANG,
    COMMAND_DESCRIPTION_LEAVE,
    COMMAND_DESCRIPTION_MEMBERS,
    COMMAND_DESCRIPTION_MISSION,
    COMMAND_DESCRIPTION_MISSIONS,
    COMMAND_DESCRIPTION_NAME,
    COMMAND_DESCRIPTION_OPEN,
    COMMAND_DESCRIPTION_PANEL,
    COMMAND_DESCRIPTION_PARDON,
    COMMAND_DESCRIPTION_PERMISSIONS,
    COMMAND_DESCRIPTION_PROMOTE,
    COMMAND_DESCRIPTION_RANKUP,
    COMMAND_DESCRIPTION_RATE,
    COMMAND_DESCRIPTION_RATINGS,
    COMMAND_DESCRIPTION_RECALC,
    COMMAND_DESCRIPTION_SETTINGS,
    COMMAND_DESCRIPTION_SET_DISCORD,
    COMMAND_DESCRIPTION_SET_PAYPAL,
    COMMAND_DESCRIPTION_SET_ROLE,
    COMMAND_DESCRIPTION_SET_TELEPORT,
    COMMAND_DESCRIPTION_SET_WARP,
    COMMAND_DESCRIPTION_SHOW,
    COMMAND_DESCRIPTION_TEAM,
    COMMAND_DESCRIPTION_TEAM_CHAT,
    COMMAND_DESCRIPTION_TELEPORT,
    COMMAND_DESCRIPTION_TOGGLE,
    COMMAND_DESCRIPTION_TOP,
    COMMAND_DESCRIPTION_TRANSFER,
    COMMAND_DESCRIPTION_UNCOOP,
    COMMAND_DESCRIPTION_UPGRADE,
    COMMAND_DESCRIPTION_VALUE,
    COMMAND_DESCRIPTION_VALUES,
    COMMAND_DESCRIPTION_VISIT,
    COMMAND_DESCRIPTION_VISITORS,
    COMMAND_DESCRIPTION_WARP,
    COMMAND_DESCRIPTION_WARPS,
    COMMAND_DESCRIPTION_WITHDRAW,
    COMMAND_USAGE,
    COOP_ANNOUNCEMENT,
    COOP_BANNED_PLAYER,
    COOP_LIMIT_EXCEED,
    CREATE_ISLAND,
    CREATE_ISLAND_FAILURE,
    CREATE_WORLD_FAILURE,
    DEBUG_MODE_DISABLED,
    DEBUG_MODE_ENABLED,
    DEBUG_MODE_FILTER_ADD,
    DEBUG_MODE_FILTER_CLEAR,
    DEBUG_MODE_FILTER_REMOVE,
    DELETE_WARP,
    DELETE_WARP_SIGN_BROKE,
    DEMOTED_MEMBER,
    DEMOTE_LEADER,
    DEMOTE_PLAYERS_WITH_LOWER_ROLE,
    DEPOSIT_ANNOUNCEMENT,
    DEPOSIT_ERROR,
    DESTROY_OUTSIDE_ISLAND,
    DISBANDED_ISLAND,
    DISBANDED_ISLAND_OTHER,
    DISBANDED_ISLAND_OTHER_NAME,
    DISBAND_ANNOUNCEMENT,
    DISBAND_GIVE,
    DISBAND_GIVE_ALL,
    DISBAND_GIVE_OTHER,
    DISBAND_ISLAND_BALANCE_REFUND,
    DISBAND_SET,
    DISBAND_SET_ALL,
    DISBAND_SET_OTHER,
    ENTER_PVP_ISLAND,
    EXPELLED_PLAYER,
    FORMAT_BILLION,
    FORMAT_DAYS_NAME,
    FORMAT_DAY_NAME,
    FORMAT_HOURS_NAME,
    FORMAT_HOUR_NAME,
    FORMAT_MILLION,
    FORMAT_MINUTES_NAME,
    FORMAT_MINUTE_NAME,
    FORMAT_QUAD,
    FORMAT_SECONDS_NAME,
    FORMAT_SECOND_NAME,
    FORMAT_THOUSANDS,
    FORMAT_TRILLION,
    GENERATOR_CLEARED,
    GENERATOR_CLEARED_ALL,
    GENERATOR_CLEARED_NAME,
    GENERATOR_UPDATED,
    GENERATOR_UPDATED_ALL,
    GENERATOR_UPDATED_NAME,
    GLOBAL_COMMAND_EXECUTED,
    GLOBAL_COMMAND_EXECUTED_NAME,
    GLOBAL_MESSAGE_SENT,
    GLOBAL_MESSAGE_SENT_NAME,
    GLOBAL_TITLE_SENT,
    GLOBAL_TITLE_SENT_NAME,
    GOT_BANNED,
    GOT_DEMOTED,
    GOT_EXPELLED,
    GOT_INVITE {
        @Override
        public void send(CommandSender sender, Locale locale, Object... args) {
            if (!(sender instanceof Player)) {
                super.send(sender, locale, args);
            } else {
                String message = getMessage(locale);

                if (message == null)
                    return;

                BaseComponent[] baseComponents = TextComponent.fromLegacyText(message);
                if (!GOT_INVITE_TOOLTIP.isEmpty(locale)) {
                    for (BaseComponent baseComponent : baseComponents)
                        baseComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent(GOT_INVITE_TOOLTIP.getMessage(locale))}));
                }

                for (BaseComponent baseComponent : baseComponents)
                    baseComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + plugin.getCommands().getLabel() + " accept " + args[0]));

                IMessageComponent messageComponent = ComplexMessageComponent.of(baseComponents);
                if (messageComponent != null) {
                    EventResult<IMessageComponent> eventResult = plugin.getEventsBus().callSendMessageEvent(sender, name(), messageComponent, args);
                    if (!eventResult.isCancelled())
                        eventResult.getResult().sendMessage(sender, args);
                }
            }
        }
    },
    GOT_INVITE_TOOLTIP,
    GOT_KICKED,
    GOT_PROMOTED,
    GOT_REVOKED,
    GOT_UNBANNED,
    HIT_ISLAND_MEMBER,
    HIT_PLAYER_IN_ISLAND,
    IGNORED_ISLAND,
    IGNORED_ISLAND_NAME,
    INTERACT_OUTSIDE_ISLAND,
    INVALID_AMOUNT,
    INVALID_BIOME,
    INVALID_BLOCK,
    INVALID_BORDER_COLOR,
    INVALID_EFFECT,
    INVALID_ENTITY,
    INVALID_ENVIRONMENT,
    INVALID_INTERVAL,
    INVALID_ISLAND,
    INVALID_ISLAND_LOCATION,
    INVALID_ISLAND_OTHER,
    INVALID_ISLAND_OTHER_NAME,
    INVALID_ISLAND_PERMISSION,
    INVALID_LEVEL,
    INVALID_LIMIT,
    INVALID_MATERIAL,
    INVALID_MATERIAL_DATA,
    INVALID_MISSION,
    INVALID_MODULE,
    INVALID_MULTIPLIER,
    INVALID_PAGE,
    INVALID_PERCENTAGE,
    INVALID_PLAYER,
    INVALID_RATE,
    INVALID_ROLE,
    INVALID_ROWS,
    INVALID_SCHEMATIC,
    INVALID_SETTINGS,
    INVALID_SIZE,
    INVALID_SLOT,
    INVALID_TITLE,
    INVALID_TOGGLE_MODE,
    INVALID_UPGRADE,
    INVALID_VISIT_LOCATION,
    INVALID_VISIT_LOCATION_BYPASS,
    INVALID_WARP,
    INVALID_WORLD,
    INVITE_ANNOUNCEMENT,
    INVITE_BANNED_PLAYER,
    INVITE_TO_FULL_ISLAND,
    ISLAND_ALREADY_EXIST,
    ISLAND_BANK_EMPTY,
    ISLAND_BANK_SHOW,
    ISLAND_BANK_SHOW_OTHER,
    ISLAND_BANK_SHOW_OTHER_NAME,
    ISLAND_BEING_CALCULATED,
    ISLAND_CLOSED,
    ISLAND_CREATE_PROCCESS_REQUEST,
    ISLAND_CREATE_PROCESS_FAIL,
    ISLAND_FLY_DISABLED,
    ISLAND_FLY_ENABLED,
    ISLAND_GOT_DELETED_WHILE_INSIDE,
    ISLAND_GOT_PVP_ENABLED_WHILE_INSIDE,
    ISLAND_HELP_FOOTER,
    ISLAND_HELP_HEADER,
    ISLAND_HELP_LINE,
    ISLAND_HELP_NEXT_PAGE,
    ISLAND_INFO_ADMIN_BANK_LIMIT,
    ISLAND_INFO_ADMIN_BLOCKS_LIMITS,
    ISLAND_INFO_ADMIN_BLOCKS_LIMITS_LINE,
    ISLAND_INFO_ADMIN_COOP_LIMIT,
    ISLAND_INFO_ADMIN_CROPS_MULTIPLIER,
    ISLAND_INFO_ADMIN_DROPS_MULTIPLIER,
    ISLAND_INFO_ADMIN_ENTITIES_LIMITS,
    ISLAND_INFO_ADMIN_ENTITIES_LIMITS_LINE,
    ISLAND_INFO_ADMIN_GENERATOR_RATES,
    ISLAND_INFO_ADMIN_GENERATOR_RATES_LINE,
    ISLAND_INFO_ADMIN_ISLAND_EFFECTS,
    ISLAND_INFO_ADMIN_ISLAND_EFFECTS_LINE,
    ISLAND_INFO_ADMIN_ROLE_LIMITS,
    ISLAND_INFO_ADMIN_ROLE_LIMITS_LINE,
    ISLAND_INFO_ADMIN_SIZE,
    ISLAND_INFO_ADMIN_SPAWNERS_MULTIPLIER,
    ISLAND_INFO_ADMIN_TEAM_LIMIT,
    ISLAND_INFO_ADMIN_UPGRADES,
    ISLAND_INFO_ADMIN_UPGRADE_LINE,
    ISLAND_INFO_ADMIN_VALUE_SYNCED,
    ISLAND_INFO_ADMIN_WARPS_LIMIT,
    ISLAND_INFO_BANK,
    ISLAND_INFO_BONUS,
    ISLAND_INFO_BONUS_LEVEL,
    ISLAND_INFO_CREATION_TIME,
    ISLAND_INFO_DISCORD,
    ISLAND_INFO_FOOTER,
    ISLAND_INFO_HEADER,
    ISLAND_INFO_LAST_TIME_UPDATED,
    ISLAND_INFO_LAST_TIME_UPDATED_CURRENTLY_ACTIVE,
    ISLAND_INFO_LEVEL,
    ISLAND_INFO_LOCATION,
    ISLAND_INFO_NAME,
    ISLAND_INFO_OWNER,
    ISLAND_INFO_PAYPAL,
    ISLAND_INFO_PLAYER_LINE,
    ISLAND_INFO_RATE,
    ISLAND_INFO_RATE_EMPTY_SYMBOL,
    ISLAND_INFO_RATE_FIVE_COLOR,
    ISLAND_INFO_RATE_FOUR_COLOR,
    ISLAND_INFO_RATE_ONE_COLOR,
    ISLAND_INFO_RATE_SYMBOL,
    ISLAND_INFO_RATE_THREE_COLOR,
    ISLAND_INFO_RATE_TWO_COLOR,
    ISLAND_INFO_ROLES,
    ISLAND_INFO_VISITORS_COUNT,
    ISLAND_INFO_WORTH,
    ISLAND_OPENED,
    ISLAND_PREVIEW_CANCEL,
    ISLAND_PREVIEW_CANCEL_DISTANCE,
    ISLAND_PREVIEW_CANCEL_TEXT,
    ISLAND_PREVIEW_CONFIRM_TEXT,
    ISLAND_PREVIEW_START,
    ISLAND_PROTECTED,
    ISLAND_PROTECTED_OPPED,
    ISLAND_TEAM_STATUS_FOOTER,
    ISLAND_TEAM_STATUS_HEADER,
    ISLAND_TEAM_STATUS_OFFLINE,
    ISLAND_TEAM_STATUS_ONLINE,
    ISLAND_TEAM_STATUS_ROLES,
    ISLAND_TOP_STATUS_OFFLINE,
    ISLAND_TOP_STATUS_ONLINE,
    ISLAND_WARP_PRIVATE,
    ISLAND_WARP_PUBLIC,
    ISLAND_WAS_CLOSED,
    ISLAND_WORTH_ERROR,
    ISLAND_WORTH_RESULT,
    ISLAND_WORTH_TIME_OUT,
    JOINED_ISLAND,
    JOINED_ISLAND_AS_COOP,
    JOINED_ISLAND_AS_COOP_NAME,
    JOINED_ISLAND_NAME,
    JOIN_ADMIN_ANNOUNCEMENT,
    JOIN_ANNOUNCEMENT,
    JOIN_FULL_ISLAND,
    JOIN_WHILE_IN_ISLAND,
    KICK_ANNOUNCEMENT,
    KICK_ISLAND_LEADER,
    KICK_PLAYERS_WITH_LOWER_ROLE,
    LACK_CHANGE_PERMISSION,
    LAST_ROLE_DEMOTE,
    LAST_ROLE_PROMOTE,
    LEAVE_ANNOUNCEMENT,
    LEAVE_ISLAND_AS_LEADER,
    LEFT_ISLAND,
    LEFT_ISLAND_COOP,
    LEFT_ISLAND_COOP_NAME,
    MATERIAL_NOT_SOLID,
    MAXIMUM_LEVEL,
    MESSAGE_SENT,
    MISSION_CANNOT_COMPLETE,
    MISSION_NOT_COMPLETE_REQUIRED_MISSIONS,
    MISSION_NO_AUTO_REWARD,
    MISSION_STATUS_COMPLETE,
    MISSION_STATUS_COMPLETE_ALL,
    MISSION_STATUS_RESET,
    MISSION_STATUS_RESET_ALL,
    MODULES_LIST,
    MODULES_LIST_MODULE_NAME,
    MODULES_LIST_SEPARATOR,
    MODULE_ALREADY_INITIALIZED,
    MODULE_INFO,
    MODULE_LOADED_FAILURE,
    MODULE_LOADED_SUCCESS,
    MODULE_UNLOADED_SUCCESS,
    NAME_ANNOUNCEMENT,
    NAME_BLACKLISTED,
    NAME_CHAT_FORMAT,
    NAME_SAME_AS_PLAYER,
    NAME_TOO_LONG,
    NAME_TOO_SHORT,
    NOT_ENOUGH_MONEY_TO_DEPOSIT,
    NOT_ENOUGH_MONEY_TO_UPGRADE,
    NOT_ENOUGH_MONEY_TO_WARP,
    NO_BAN_PERMISSION,
    NO_CLOSE_BYPASS,
    NO_CLOSE_PERMISSION,
    NO_COMMAND_PERMISSION,
    NO_COOP_PERMISSION,
    NO_DELETE_WARP_PERMISSION,
    NO_DEMOTE_PERMISSION,
    NO_DEPOSIT_PERMISSION,
    NO_DISBAND_PERMISSION,
    NO_EXPEL_PERMISSION,
    NO_INVITE_PERMISSION,
    NO_ISLANDS_TO_PURGE,
    NO_ISLAND_CHEST_PERMISSION,
    NO_ISLAND_INVITE,
    NO_KICK_PERMISSION,
    NO_MORE_DISBANDS,
    NO_MORE_WARPS,
    NO_NAME_PERMISSION,
    NO_OPEN_PERMISSION,
    NO_PERMISSION_CHECK_PERMISSION,
    NO_PROMOTE_PERMISSION,
    NO_RANKUP_PERMISSION,
    NO_RATINGS_PERMISSION,
    NO_SET_BIOME_PERMISSION,
    NO_SET_DISCORD_PERMISSION,
    NO_SET_HOME_PERMISSION,
    NO_SET_PAYPAL_PERMISSION,
    NO_SET_ROLE_PERMISSION,
    NO_SET_SETTINGS_PERMISSION,
    NO_SET_WARP_PERMISSION,
    NO_TRANSFER_PERMISSION,
    NO_UNCOOP_PERMISSION,
    NO_UPGRADE_PERMISSION,
    NO_WITHDRAW_PERMISSION,
    OPEN_MENU_WHILE_SLEEPING,
    PANEL_TOGGLE_OFF,
    PANEL_TOGGLE_ON,
    PERMISSIONS_RESET_PLAYER,
    PERMISSIONS_RESET_ROLES,
    PERMISSION_CHANGED,
    PERMISSION_CHANGED_ALL,
    PERMISSION_CHANGED_NAME,
    PERSISTENT_DATA_EMPTY,
    PERSISTENT_DATA_MODIFY,
    PERSISTENT_DATA_SHOW,
    PERSISTENT_DATA_SHOW_PATH,
    PERSISTENT_DATA_SHOW_VALUE,
    PERSISTENT_DATA_SHOW_SPACER,
    PERSISTENT_DATA_REMOVED,
    PLACEHOLDER_NO,
    PLACEHOLDER_YES,
    PLAYER_ALREADY_BANNED,
    PLAYER_ALREADY_COOP,
    PLAYER_ALREADY_IN_ISLAND,
    PLAYER_ALREADY_IN_ROLE,
    PLAYER_EXPEL_BYPASS,
    PLAYER_JOIN_ANNOUNCEMENT,
    PLAYER_NOT_BANNED,
    PLAYER_NOT_COOP,
    PLAYER_NOT_INSIDE_ISLAND,
    PLAYER_NOT_ONLINE,
    PLAYER_QUIT_ANNOUNCEMENT,
    PROMOTED_MEMBER,
    PROMOTE_PLAYERS_WITH_LOWER_ROLE,
    PURGED_ISLANDS,
    PURGE_CLEAR,
    RANKUP_SUCCESS,
    RANKUP_SUCCESS_ALL,
    RANKUP_SUCCESS_NAME,
    RATE_ANNOUNCEMENT,
    RATE_CHANGE_OTHER,
    RATE_OWN_ISLAND,
    RATE_REMOVE_ALL,
    RATE_REMOVE_ALL_ISLANDS,
    RATE_SUCCESS,
    REACHED_BLOCK_LIMIT,
    REACHED_ENTITY_LIMIT,
    RECALC_ALL_ISLANDS,
    RECALC_ALL_ISLANDS_DONE,
    RECALC_ALREADY_RUNNING,
    RECALC_ALREADY_RUNNING_OTHER,
    RECALC_PROCCESS_REQUEST,
    RELOAD_COMPLETED,
    RELOAD_PROCCESS_REQUEST,
    RESET_WORLD_SUCCEED,
    RESET_WORLD_SUCCEED_ALL,
    RESET_WORLD_SUCCEED_NAME,
    REVOKE_INVITE_ANNOUNCEMENT,
    SAME_NAME_CHANGE,
    SCHEMATIC_LEFT_SELECT,
    SCHEMATIC_NOT_READY,
    SCHEMATIC_PROCCESS_REQUEST,
    SCHEMATIC_READY_TO_CREATE,
    SCHEMATIC_RIGHT_SELECT,
    SCHEMATIC_SAVED,
    SELF_ROLE_CHANGE,
    SETTINGS_UPDATED,
    SETTINGS_UPDATED_ALL,
    SETTINGS_UPDATED_NAME,
    SET_UPGRADE_LEVEL,
    SET_UPGRADE_LEVEL_NAME,
    SET_WARP,
    SET_WARP_OUTSIDE,
    SIZE_BIGGER_MAX,
    SPAWN_SET_SUCCESS,
    SPAWN_TELEPORT_SUCCESS,
    SPY_TEAM_CHAT_FORMAT,
    SYNC_UPGRADES,
    SYNC_UPGRADES_ALL,
    SYNC_UPGRADES_NAME,
    TEAM_CHAT_FORMAT,
    TELEPORTED_FAILED,
    TELEPORTED_SUCCESS,
    TELEPORTED_TO_WARP,
    TELEPORTED_TO_WARP_ANNOUNCEMENT,
    TELEPORT_LOCATION_OUTSIDE_ISLAND,
    TELEPORT_OUTSIDE_ISLAND,
    TELEPORT_WARMUP,
    TELEPORT_WARMUP_CANCEL,
    TITLE_SENT,
    TOGGLED_BYPASS_OFF,
    TOGGLED_BYPASS_ON,
    TOGGLED_FLY_OFF,
    TOGGLED_FLY_ON,
    TOGGLED_SCHEMATIC_OFF,
    TOGGLED_SCHEMATIC_ON,
    TOGGLED_SPY_OFF,
    TOGGLED_SPY_ON,
    TOGGLED_STACKED_BLOCKS_OFF,
    TOGGLED_STACKED_BLOCKS_ON,
    TOGGLED_TEAM_CHAT_OFF,
    TOGGLED_TEAM_CHAT_ON,
    TOGGLED_WORLD_BORDER_OFF,
    TOGGLED_WORLD_BORDER_ON,
    TRANSFER_ADMIN,
    TRANSFER_ADMIN_ALREADY_LEADER,
    TRANSFER_ADMIN_DIFFERENT_ISLAND,
    TRANSFER_ADMIN_NOT_LEADER,
    TRANSFER_ALREADY_LEADER,
    TRANSFER_BROADCAST,
    UNBAN_ANNOUNCEMENT,
    UNCOOP_ANNOUNCEMENT,
    UNCOOP_AUTO_ANNOUNCEMENT,
    UNCOOP_LEFT_ANNOUNCEMENT,
    UNIGNORED_ISLAND,
    UNIGNORED_ISLAND_NAME,
    UNLOCK_WORLD_ANNOUNCEMENT,
    UNSAFE_WARP,
    UPDATED_PERMISSION,
    UPDATED_SETTINGS,
    UPGRADE_COOLDOWN_FORMAT,
    VISITOR_BLOCK_COMMAND,
    WARP_ALREADY_EXIST,
    WARP_CATEGORY_ICON_NEW_LORE,
    WARP_CATEGORY_ICON_NEW_NAME,
    WARP_CATEGORY_ICON_NEW_TYPE,
    WARP_CATEGORY_ICON_UPDATED,
    WARP_CATEGORY_ILLEGAL_NAME,
    WARP_CATEGORY_NAME_TOO_LONG,
    WARP_CATEGORY_RENAME,
    WARP_CATEGORY_RENAME_ALREADY_EXIST,
    WARP_CATEGORY_RENAME_SUCCESS,
    WARP_CATEGORY_SLOT,
    WARP_CATEGORY_SLOT_ALREADY_TAKEN,
    WARP_CATEGORY_SLOT_SUCCESS,
    WARP_ICON_NEW_LORE,
    WARP_ICON_NEW_NAME,
    WARP_ICON_NEW_TYPE,
    WARP_ICON_UPDATED,
    WARP_ILLEGAL_NAME,
    WARP_LOCATION_UPDATE,
    WARP_NAME_TOO_LONG,
    WARP_PRIVATE_UPDATE,
    WARP_PUBLIC_UPDATE,
    WARP_RENAME,
    WARP_RENAME_ALREADY_EXIST,
    WARP_RENAME_SUCCESS,
    WITHDRAWN_MONEY,
    WITHDRAWN_MONEY_NAME,
    WITHDRAW_ALL_MONEY,
    WITHDRAW_ANNOUNCEMENT,
    WITHDRAW_ERROR,
    WORLD_NOT_ENABLED,
    WORLD_NOT_UNLOCKED,

    SCHEMATICS(true) {

        private final Collection<UUID> noSchematicMessages = AutoRemovalCollection.newHashSet(3, TimeUnit.SECONDS);

        @Override
        public void send(CommandSender sender, Locale locale, Object... objects) {
            if (!(sender instanceof Player) || objects.length != 1 || !(objects[0] instanceof String))
                return;

            UUID playerUUID = ((Player) sender).getUniqueId();
            String message = (String) objects[0];

            if (noSchematicMessages.add(playerUUID)) {
                Message.CUSTOM.send(sender, locale, message, false);
            }
        }
    },

    PROTECTION(true) {

        @Nullable
        private Collection<UUID> noInteractMessages;

        @Override
        public void send(CommandSender sender, Locale locale, Object... args) {
            if (!(sender instanceof Player))
                return;

            if (noInteractMessages == null)
                noInteractMessages = AutoRemovalCollection.newHashSet(plugin.getSettings().getProtectedMessageDelay() * 50, TimeUnit.MILLISECONDS);

            UUID playerUUID = ((Player) sender).getUniqueId();

            if (noInteractMessages.add(playerUUID)) {
                Message.ISLAND_PROTECTED.send(sender, locale, args);

                SuperiorCommand bypassCommand = plugin.getCommands().getAdminCommand("bypass");

                if (bypassCommand != null && sender.hasPermission(bypassCommand.getPermission()))
                    Message.ISLAND_PROTECTED_OPPED.send(sender, locale, args);
            }
        }
    },

    CUSTOM(true) {
        @Override
        public void send(CommandSender sender, Locale locale, Object... args) {
            String message = args.length == 0 ? null : args[0] == null ? null : args[0].toString();
            boolean translateColors = args.length >= 2 && args[1] instanceof Boolean && (boolean) args[1];
            if (!Text.isBlank(message))
                sender.sendMessage(translateColors ? Formatters.COLOR_FORMATTER.format(message) : message);
        }

    };

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final LazyReference<MessagesService> messagesService = new LazyReference<MessagesService>() {
        @Override
        protected MessagesService create() {
            return plugin.getServices().getService(MessagesService.class);
        }
    };

    private final String defaultMessage;
    private final boolean isCustom;
    private final Map<Locale, IMessageComponent> messages = new HashMap<>();

    Message() {
        this(null);
    }

    Message(boolean isCustom) {
        this(null, isCustom);
    }

    Message(String defaultMessage) {
        this(defaultMessage, false);
    }

    Message(String defaultMessage, boolean isCustom) {
        this.defaultMessage = defaultMessage;
        this.isCustom = isCustom;
    }

    public static void reload() {
        Log.info("Loading messages started...");
        long startTime = System.currentTimeMillis();

        convertOldFile();
        PlayerLocales.clearLocales();

        File langFolder = new File(plugin.getDataFolder(), "lang");

        if (!langFolder.exists()) {
            plugin.saveResource("lang/de-DE.yml", false);
            plugin.saveResource("lang/en-US.yml", false);
            plugin.saveResource("lang/es-ES.yml", false);
            plugin.saveResource("lang/fr-FR.yml", false);
            plugin.saveResource("lang/it-IT.yml", false);
            plugin.saveResource("lang/iw-IL.yml", false);
            plugin.saveResource("lang/pl-PL.yml", false);
            plugin.saveResource("lang/vi-VN.yml", false);
            plugin.saveResource("lang/zh-CN.yml", false);
        }

        int messagesAmount = 0;
        boolean countMessages = true;

        for (File langFile : Objects.requireNonNull(langFolder.listFiles())) {
            String fileName = langFile.getName().split("\\.")[0];
            Locale fileLocale;

            try {
                fileLocale = PlayerLocales.getLocale(fileName);
            } catch (IllegalArgumentException ex) {
                Log.warn("The language ", fileName, " is invalid, skipping...");
                continue;
            }

            PlayerLocales.registerLocale(fileLocale);

            if (plugin.getSettings().getDefaultLanguage().equalsIgnoreCase(fileName))
                PlayerLocales.setDefaultLocale(fileLocale);

            CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(langFile);
            InputStream inputStream = plugin.getResource("lang/" + langFile.getName());

            try {
                cfg.syncWithConfig(langFile, inputStream == null ? plugin.getResource("lang/en-US.yml") : inputStream, "lang/en-US.yml");
            } catch (Exception error) {
                Log.error(error, "An unexpected error occurred while saving lang file ", langFile.getName(), ":");
            }

            for (Message locale : values()) {
                if (!locale.isCustom()) {
                    locale.setMessage(fileLocale, messagesService.get().parseComponent(cfg, locale.name()));
                    if (countMessages)
                        messagesAmount++;
                }
            }

            countMessages = false;
        }

        Log.info(" - Found " + messagesAmount + " messages in the language files.");
        Log.info("Loading messages done (Took " + (System.currentTimeMillis() - startTime) + "ms)");
    }

    public boolean isCustom() {
        return isCustom;
    }

    public boolean isEmpty(Locale locale) {
        IMessageComponent messageContainer = getComponent(locale);
        return messageContainer == null || messageContainer.getType() == IMessageComponent.Type.EMPTY ||
                messageContainer.getMessage().isEmpty();
    }

    @Nullable
    public IMessageComponent getComponent(Locale locale) {
        return messages.get(locale);
    }

    @Nullable
    public String getMessage(Locale locale, Object... objects) {
        return isEmpty(locale) ? defaultMessage : replaceArgs(messages.get(locale).getMessage(), objects).orElse(null);
    }

    public final void send(SuperiorPlayer superiorPlayer, Object... args) {
        if (plugin.getEventsBus().callAttemptPlayerSendMessageEvent(superiorPlayer, name(), args))
            superiorPlayer.runIfOnline(player -> send(player, superiorPlayer.getUserLocale(), args));
    }

    public final void send(CommandSender sender, Object... args) {
        if (sender instanceof Player) {
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);
            if (!plugin.getEventsBus().callAttemptPlayerSendMessageEvent(superiorPlayer, name(), args))
                return;
        }

        send(sender, PlayerLocales.getLocale(sender), args);
    }

    public void send(CommandSender sender, Locale locale, Object... objects) {
        IMessageComponent messageComponent = getComponent(locale);
        if (messageComponent != null) {
            EventResult<IMessageComponent> eventResult = plugin.getEventsBus().callSendMessageEvent(sender, name(), messageComponent, objects);
            if (!eventResult.isCancelled())
                eventResult.getResult().sendMessage(sender, objects);
        }
    }

    private void setMessage(Locale locale, IMessageComponent messageComponent) {
        messages.put(locale, messageComponent);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void convertOldFile() {
        File file = new File(plugin.getDataFolder(), "lang.yml");
        if (file.exists()) {
            File dest = new File(plugin.getDataFolder(), "lang/en-US.yml");
            dest.getParentFile().mkdirs();
            file.renameTo(dest);
        }
    }

    public static Optional<String> replaceArgs(String msg, Object... objects) {
        if (Text.isBlank(msg))
            return Optional.empty();

        for (int i = 0; i < objects.length; i++) {
            String objectString = objects[i] instanceof BigDecimal ?
                    Formatters.NUMBER_FORMATTER.format((BigDecimal) objects[i]) : objects[i].toString();
            msg = msg.replace("{" + i + "}", objectString);
        }

        return msg.isEmpty() ? Optional.empty() : Optional.of(msg);
    }

}
