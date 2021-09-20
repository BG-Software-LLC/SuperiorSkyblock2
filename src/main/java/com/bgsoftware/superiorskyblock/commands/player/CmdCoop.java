package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IPermissibleCommand;
import com.bgsoftware.superiorskyblock.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.utils.events.EventsCaller;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CmdCoop implements IPermissibleCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("coop", "trust");
    }

    @Override
    public String getPermission() {
        return "superior.island.coop";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "coop <" + Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_COOP.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 2;
    }

    @Override
    public int getMaxArgs() {
        return 2;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public IslandPrivilege getPrivilege() {
        return IslandPrivileges.COOP_MEMBER;
    }

    @Override
    public Locale getPermissionLackMessage() {
        return Locale.NO_COOP_PERMISSION;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        SuperiorPlayer targetPlayer = CommandArguments.getPlayer(plugin, superiorPlayer, args[1]);

        if(targetPlayer == null)
            return;

        if(!targetPlayer.isOnline()){
            Locale.INVALID_PLAYER.send(superiorPlayer, args[1]);
            return;
        }

        if(island.isMember(targetPlayer)){
            Locale.ALREADY_IN_ISLAND_OTHER.send(superiorPlayer);
            return;
        }

        if(island.isCoop(targetPlayer)){
            Locale.PLAYER_ALREADY_COOP.send(superiorPlayer);
            return;
        }

        if(island.isBanned(targetPlayer)){
            Locale.COOP_BANNED_PLAYER.send(superiorPlayer);
            return;
        }

        if(island.getCoopPlayers().size() >= island.getCoopLimit()){
            Locale.COOP_LIMIT_EXCEED.send(superiorPlayer);
            return;
        }

        if(!EventsCaller.callIslandCoopPlayerEvent(island, superiorPlayer, targetPlayer))
            return;

        island.addCoop(targetPlayer);

        IslandUtils.sendMessage(island, Locale.COOP_ANNOUNCEMENT, new ArrayList<>(), superiorPlayer.getName(), targetPlayer.getName());

        if(island.getName().isEmpty())
            Locale.JOINED_ISLAND_AS_COOP.send(targetPlayer, superiorPlayer.getName());
        else
            Locale.JOINED_ISLAND_AS_COOP_NAME.send(targetPlayer, island.getName());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        return args.length == 2 ? CommandTabCompletes.getOnlinePlayers(plugin, args[1],
                plugin.getSettings().isTabCompleteHideVanished(), onlinePlayer ->
                        !island.isMember(onlinePlayer) && !island.isBanned(onlinePlayer) && !island.isCoop(onlinePlayer))
                : new ArrayList<>();
    }

}
