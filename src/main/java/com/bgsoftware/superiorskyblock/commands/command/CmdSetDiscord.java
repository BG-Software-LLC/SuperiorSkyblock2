package com.bgsoftware.superiorskyblock.commands.command;

import com.bgsoftware.superiorskyblock.wrappers.WrappedPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import com.bgsoftware.superiorskyblock.island.Island;
import com.bgsoftware.superiorskyblock.island.IslandPermission;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CmdSetDiscord implements ICommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setdiscord");
    }

    @Override
    public String getPermission() {
        return "superior.island.setdiscord";
    }

    @Override
    public String getUsage() {
        return "island setdiscord <discord...>";
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
    public void execute(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        WrappedPlayer wrappedPlayer = WrappedPlayer.of(sender);
        Island island = wrappedPlayer.getIsland();

        if(island == null){
            Locale.INVALID_ISLAND.send(wrappedPlayer);
            return;
        }

        if(!wrappedPlayer.hasPermission(IslandPermission.SET_DISCORD)){
            Locale.NO_SET_DISCORD_PERMISSION.send(wrappedPlayer, island.getRequiredRole(IslandPermission.SET_DISCORD));
            return;
        }

        StringBuilder stringBuilder = new StringBuilder();

        for(int i = 1; i < args.length; i++)
            stringBuilder.append(" ").append(args[i]);

        String discord = stringBuilder.toString().substring(1);

        island.setDiscord(discord);
        Locale.CHANGED_DISCORD.send(wrappedPlayer, discord);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
