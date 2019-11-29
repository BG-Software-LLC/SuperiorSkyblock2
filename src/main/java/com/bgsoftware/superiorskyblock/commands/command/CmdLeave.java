package com.bgsoftware.superiorskyblock.commands.command;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.events.IslandQuitEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdLeave implements ICommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("leave");
    }

    @Override
    public String getPermission() {
        return "superior.island.leave";
    }

    @Override
    public String getUsage() {
        return "leave";
    }

    @Override
    public String getDescription() {
        return Locale.COMMAND_DESCRIPTION_LEAVE.getMessage();
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
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(sender);
        Island island = superiorPlayer.getIsland();

        if(island == null){
            Locale.INVALID_ISLAND.send(superiorPlayer);
            return;
        }

        if(superiorPlayer.getPlayerRole().getNextRole() == null){
            Locale.LEAVE_ISLAND_AS_LEADER.send(superiorPlayer);
            return;
        }

        IslandQuitEvent islandQuitEvent = new IslandQuitEvent(superiorPlayer, island);
        Bukkit.getPluginManager().callEvent(islandQuitEvent);

        if(islandQuitEvent.isCancelled())
            return;

        island.kickMember(superiorPlayer);

        if(!Locale.LEAVE_ANNOUNCEMENT.isEmpty())
            island.sendMessage(Locale.LEAVE_ANNOUNCEMENT.getMessage(superiorPlayer.getName()));

        Locale.LEFT_ISLAND.send(superiorPlayer);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
