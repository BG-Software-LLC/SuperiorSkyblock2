package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.MenuConfirmLeave;
import com.bgsoftware.superiorskyblock.utils.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.utils.events.EventsCaller;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.Locale;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdLeave implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("leave");
    }

    @Override
    public String getPermission() {
        return "superior.island.leave";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "leave";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_LEAVE.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public int getMaxArgs() {
        return 1;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        Pair<Island, SuperiorPlayer> arguments = CommandArguments.getSenderIsland(plugin, sender);

        Island island = arguments.getKey();

        if(island == null)
            return;

        SuperiorPlayer superiorPlayer = arguments.getValue();

        if(superiorPlayer.getPlayerRole().getNextRole() == null){
            Locale.LEAVE_ISLAND_AS_LEADER.send(superiorPlayer);
            return;
        }

        if (plugin.getSettings().leaveConfirm) {
            MenuConfirmLeave.openInventory(superiorPlayer, null);
            return;
        }

        if(!EventsCaller.callIslandQuitEvent(superiorPlayer, island))
            return;

        island.kickMember(superiorPlayer);

        IslandUtils.sendMessage(island, Locale.LEAVE_ANNOUNCEMENT, new ArrayList<>(), superiorPlayer.getName());

        Locale.LEFT_ISLAND.send(superiorPlayer);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

}
