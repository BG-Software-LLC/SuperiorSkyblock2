package com.bgsoftware.superiorskyblock;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.config.LangComments;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public enum Locale {

    BUILD_OUTSIDE_ISLAND,
    DESTROY_OUTSIDE_ISLAND,
    NO_ISLAND_INVITE,
    JOIN_WHILE_IN_ISLAND,
    JOIN_FULL_ISLAND,
    JOIN_ANNOUNCEMENT,
    JOINED_ISLAND,
    INVALID_PLAYER,
    INVALID_ISLAND,
    INVALID_ISLAND_OTHER,
    LAST_ROLE_DEMOTE,
    LAST_ROLE_PROMOTE,
    DEMOTED_MEMBER,
    GOT_DEMOTED,
    PROMOTED_MEMBER,
    GOT_PROMOTED,
    ROLE_GUEST,
    ROLE_MEMBER,
    ROLE_MOD,
    ROLE_ADMIN,
    ROLE_LEADER,
    ALREADY_IN_ISLAND,
    ALREADY_IN_ISLAND_OTHER,
    PLAYER_NOT_ONLINE,
    MESSAGE_SENT,
    GLOBAL_MESSAGE_SENT,
    NO_BAN_PERMISSION,
    BAN_PLAYERS_WITH_LOWER_ROLE,
    PLAYER_ALREADY_BANNED,
    BAN_ANNOUNCEMENT,
    GOT_BANNED,
    NO_DEMOTE_PERMISSION,
    DEMOTE_PLAYERS_WITH_LOWER_ROLE,
    NO_DEPOSIT_PERMISSION,
    INVALID_AMOUNT,
    NOT_ENOUGH_MONEY_TO_DEPOSIT,
    DEPOSIT_ANNOUNCEMENT,
    NO_DISBAND_PERMISSION,
    DISBAND_ANNOUNCEMENT,
    DISBANDED_ISLAND,
    PLAYER_NOT_INSIDE_ISLAND,
    PLAYER_EXPEL_BYPASS,
    EXPELLED_PLAYER,
    GOT_EXPELLED,
    NO_INVITE_PERMISSION,
    INVITE_BANNED_PLAYER,
    REVOKE_INVITE_ANNOUNCEMENT,
    GOT_REVOKED,
    INVITE_TO_FULL_ISLAND,
    INVITE_ANNOUNCEMENT,
    GOT_INVITE,
    NO_KICK_PERMISSION,
    KICK_PLAYERS_WITH_LOWER_ROLE,
    KICK_ANNOUNCEMENT,
    GOT_KICKED,
    LEAVE_ISLAND_AS_LEADER,
    LEAVE_ANNOUNCEMENT,
    LEFT_ISLAND,
    NO_PERMISSION_CHECK_PERMISSION,
    INVALID_ROLE,
    PERMISSION_CHECK,
    NO_PROMOTE_PERMISSION,
    PROMOTE_PLAYERS_WITH_LOWER_ROLE,
    RECALC_PROCCESS_REQUEST,
    ISLAND_WORTH_RESULT,
    NO_SET_BIOME_PERMISSION,
    CHANGED_BIOME,
    NO_SET_DISCORD_PERMISSION,
    CHANGED_DISCORD,
    NO_SET_PAYPAL_PERMISSION,
    CHANGED_PAYPAL,
    NO_PERMISSION_SET_PERMISSION,
    CHANGE_PERMISSION_FOR_HIGHER_ROLE,
    INVALID_ISLAND_PERMISSION,
    LACK_CHANGE_PERMISSION,
    INVALID_BOOLEAN,
    UPDATED_PERMISSION,
    NO_SET_ROLE_PERMISSION,
    CANNOT_SET_ROLE,
    PLAYER_ALREADY_IN_ROLE,
    TELEPORTED_SUCCESS,
    TOGGLED_WORLD_BORDER_ON,
    TOGGLED_WORLD_BORDER_OFF,
    TOGGLED_STACKED_BLCOKS_ON,
    TOGGLED_STACKED_BLCOKS_OFF,
    INVALID_TOGGLE_MODE,
    NO_WITHDRAW_PERMISSION,
    ISLAND_BANK_EMPTY,
    WITHDRAW_ALL_MONEY,
    WITHDRAW_ANNOUNCEMENT,
    CREATE_ISLAND,
    ISLAND_GOT_DELETED_WHILE_INSIDE,
    ISLAND_CREATE_PROCCESS_REQUEST,
    HIT_TEAM_MEMBER,
    NOT_ENOUGH_MONEY_TO_UPGRADE,
    ISLAND_PROTECTED,
    ISLAND_CALC_ANNOUNCEMENT,
    NO_COMMAND_PERMISSION,
    COMMAND_USAGE,
    ADMIN_HELP_HEADER,
    ADMIN_HELP_LINE,
    ADMIN_HELP_FOOTER,
    ADMIN_DEPOSIT_MONEY,
    INVALID_MULTIPLIER,
    CHANGED_CROP_GROWTH,
    INVALID_LIMIT,
    CHANGED_HOPPERS_LIMIT,
    CHANGED_MOB_DROPS,
    INVALID_SIZE,
    CHANGED_ISLAND_SIZE,
    CHANGED_SPAWNER_RATES,
    CHANGED_TEAM_LIMIT,
    INVALID_UPGRADE,
    INVALID_LEVEL,
    MAXIMUM_LEVEL,
    SET_UPGRADE_LEVEL,
    WITHDRAWN_MONEY,
    SELF_ROLE_CHANGE,
    ISLAND_INFO_HEADER,
    ISLAND_INFO_OWNER,
    ISLAND_INFO_LOCATION,
    ISLAND_INFO_BANK,
    ISLAND_INFO_WORTH,
    ISLAND_INFO_DISCORD,
    ISLAND_INFO_PAYPAL,
    ISLAND_INFO_ADMINS,
    ISLAND_INFO_MODS,
    ISLAND_INFO_MEMBERS,
    ISLAND_INFO_PLAYER_LINE,
    ISLAND_INFO_FOOTER,
    ISLAND_TEAM_STATUS_HEADER,
    ISLAND_TEAM_STATUS_LEADER,
    ISLAND_TEAM_STATUS_ADMINS,
    ISLAND_TEAM_STATUS_MODS,
    ISLAND_TEAM_STATUS_MEMBERS,
    ISLAND_TEAM_STATUS_ONLINE,
    ISLAND_TEAM_STATUS_OFFLINE,
    ISLAND_HELP_HEADER,
    ISLAND_HELP_LINE,
    ISLAND_HELP_FOOTER,
    ISLAND_SAVE_ANNOUNCEMENT,
    REACHED_HOPPERS_LIMIT,
    TOGGLED_SCHEMATIC_ON,
    TOGGLED_SCHEMATIC_OFF,
    SCHEMATIC_RIGHT_SELECT,
    SCHEMATIC_LEFT_SELECT,
    SCHEMATIC_READY_TO_CREATE,
    SCHEMATIC_NOT_READY,
    SCHEMATIC_PROCCESS_REQUEST,
    SCHEMATIC_SAVED,
    PLAYER_JOIN_ANNOUNCEMENT,
    PLAYER_QUIT_ANNOUNCEMENT,
    TOGGLED_TEAM_CHAT_ON,
    TOGGLED_TEAM_CHAT_OFF,
    TEAM_CHAT_FORMAT,
    SAVE_PROCCESS_REQUEST,
    SAVED_DATABASE,
    RELOAD_PROCCESS_REQUEST,
    RELOAD_COMPLETED,
    WARP_ALREADY_EXIST,
    SET_WARP_OUTSIDE,
    SET_WARP,
    TELEPORTED_TO_WARP,
    UNSAFE_WARP,
    DELETE_WARP,
    INVALID_WARP,
    INVALID_ITEM_IN_HAND,
    BLOCK_VALUE_WORTHLESS,
    BLOCK_VALUE,
    NO_DELETE_WARP_PERMISSION,
    NO_EXPEL_PERMISSION,
    NO_SET_WARP_PERMISSION,
    PERMISSION_SPACER,
    TOGGLED_BYPASS_ON,
    TOGGLED_BYPASS_OFF;

    private String message;

    public boolean isEmpty(){
        return message == null || message.isEmpty();
    }

    public String getMessage(Object... objects){
        if(!isEmpty()) {
            String msg = message;

            for (int i = 0; i < objects.length; i++)
                msg = msg.replace("{" + i + "}", objects[i].toString());

            return msg;
        }

        return null;
    }

    public void send(SuperiorPlayer superiorPlayer, Object... objects){
        send(superiorPlayer.asPlayer(), objects);
    }

    public void send(CommandSender sender, Object... objects){
        String message = getMessage(objects);
        if(message != null && sender != null)
            sender.sendMessage(message);
    }

    private void setMessage(String message){
        this.message = message;
    }

    private static SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    public static void reload(){
        SuperiorSkyblockPlugin.log("Loading messages started...");
        long startTime = System.currentTimeMillis();
        int messagesAmount = 0;
        File file = new File(plugin.getDataFolder(), "lang.yml");

        if(!file.exists())
            plugin.saveResource("lang.yml", false);

        CommentedConfiguration cfg = new CommentedConfiguration(LangComments.class);
        cfg.load(file);

        cfg.resetYamlFile(plugin, "lang.yml");

        for(Locale locale : values()){
            locale.setMessage(ChatColor.translateAlternateColorCodes('&', cfg.getString(locale.name(), "")));
            messagesAmount++;
        }

        SuperiorSkyblockPlugin.log(" - Found " + messagesAmount + " messages in lang.yml.");
        SuperiorSkyblockPlugin.log("Loading messages done (Took " + (System.currentTimeMillis() - startTime) + "ms)");
    }

    public static void sendMessage(SuperiorPlayer superiorPlayer, String message){
        sendMessage(superiorPlayer.asPlayer(), message);
    }

    public static void sendMessage(CommandSender sender, String message){
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    private static Set<UUID> noInteractMessages = new HashSet<>();

    public static void sendProtectionMessage(SuperiorPlayer superiorPlayer){
        sendProtectionMessage(superiorPlayer.asPlayer());
    }

    public static void sendProtectionMessage(Player player){
        if(!noInteractMessages.contains(player.getUniqueId())){
            noInteractMessages.add(player.getUniqueId());
            ISLAND_PROTECTED.send(player);
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> noInteractMessages.remove(player.getUniqueId()), 60L);
        }
    }

}
