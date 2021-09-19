package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.events.IslandUncoopPlayerEvent;
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

public final class CmdUncoop implements IPermissibleCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("uncoop", "untrust");
    }

    @Override
    public String getPermission() {
        return "superior.island.uncoop";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "uncoop <" + Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_UNCOOP.getMessage(locale);
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
        return IslandPrivileges.UNCOOP_MEMBER;
    }

    @Override
    public Locale getPermissionLackMessage() {
        return Locale.NO_UNCOOP_PERMISSION;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        SuperiorPlayer targetPlayer = CommandArguments.getPlayer(plugin, superiorPlayer, args[1]);

        if(targetPlayer == null)
            return;

        if(!island.isCoop(targetPlayer)){
            Locale.PLAYER_NOT_COOP.send(superiorPlayer);
            return;
        }

        if(!EventsCaller.callIslandUncoopPlayerEvent(island, superiorPlayer, targetPlayer, IslandUncoopPlayerEvent.UncoopReason.PLAYER))
            return;

        island.removeCoop(targetPlayer);

        IslandUtils.sendMessage(island, Locale.UNCOOP_ANNOUNCEMENT, new ArrayList<>(), superiorPlayer.getName(), targetPlayer.getName());

        if(island.getName().isEmpty())
            Locale.LEFT_ISLAND_COOP.send(targetPlayer, superiorPlayer.getName());
        else
            Locale.LEFT_ISLAND_COOP_NAME.send(targetPlayer, island.getName());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        return args.length == 2 ? CommandTabCompletes.getOnlinePlayers(plugin, args[1],
                plugin.getSettings().isTabCompleteHideVanished(), island::isCoop) : new ArrayList<>();
    }

}
