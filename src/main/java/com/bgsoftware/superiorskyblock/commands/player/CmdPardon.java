package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IPermissibleCommand;
import com.bgsoftware.superiorskyblock.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CmdPardon implements IPermissibleCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("pardon", "unban");
    }

    @Override
    public String getPermission() {
        return "superior.island.pardon";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "pardon <" + Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_PARDON.getMessage(locale);
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
        return IslandPrivileges.BAN_MEMBER;
    }

    @Override
    public Locale getPermissionLackMessage() {
        return Locale.NO_BAN_PERMISSION;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        SuperiorPlayer targetPlayer = CommandArguments.getPlayer(plugin, superiorPlayer, args[1]);

        if(targetPlayer == null)
            return;

        if(!island.isBanned(targetPlayer)){
            Locale.PLAYER_NOT_BANNED.send(superiorPlayer);
            return;
        }

        island.unbanMember(targetPlayer);

        IslandUtils.sendMessage(island, Locale.UNBAN_ANNOUNCEMENT, new ArrayList<>(), targetPlayer.getName(), superiorPlayer.getName());

        Locale.GOT_UNBANNED.send(targetPlayer, island.getOwner().getName());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        return args.length == 2 ? CommandTabCompletes.getIslandBannedPlayers(island, args[1]) : new ArrayList<>();
    }

}
