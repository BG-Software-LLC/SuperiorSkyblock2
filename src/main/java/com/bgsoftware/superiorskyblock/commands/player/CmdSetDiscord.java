package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IPermissibleCommand;
import com.bgsoftware.superiorskyblock.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.Locale;

import java.util.Collections;
import java.util.List;

public final class CmdSetDiscord implements IPermissibleCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setdiscord");
    }

    @Override
    public String getPermission() {
        return "superior.island.setdiscord";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "setdiscord <" + Locale.COMMAND_ARGUMENT_DISCORD.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_SET_DISCORD.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 2;
    }

    @Override
    public int getMaxArgs() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public IslandPrivilege getPrivilege() {
        return IslandPrivileges.SET_DISCORD;
    }

    @Override
    public Locale getPermissionLackMessage() {
        return Locale.NO_SET_DISCORD_PERMISSION;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        String discord = CommandArguments.buildLongString(args, 1, false);
        island.setDiscord(discord);
        Locale.CHANGED_DISCORD.send(superiorPlayer, discord);
    }

}
