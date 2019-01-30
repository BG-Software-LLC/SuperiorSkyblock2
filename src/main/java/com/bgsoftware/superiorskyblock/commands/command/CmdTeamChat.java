package com.bgsoftware.superiorskyblock.commands.command;

import com.bgsoftware.superiorskyblock.wrappers.WrappedPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import com.bgsoftware.superiorskyblock.island.Island;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CmdTeamChat implements ICommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("teamchat", "chat");
    }

    @Override
    public String getPermission() {
        return "superior.island.teamchat";
    }

    @Override
    public String getUsage() {
        return "island teamchat [message...]";
    }

    @Override
    public int getMinArgs() {
        return 1;
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

        if(args.length == 1){
            if(wrappedPlayer.hasTeamChatEnabled()){
                Locale.TOGGLED_TEAM_CHAT_OFF.send(wrappedPlayer);
            }else{
                Locale.TOGGLED_TEAM_CHAT_ON.send(wrappedPlayer);
            }
            wrappedPlayer.toggleTeamChat();
        }

        else{
            StringBuilder message = new StringBuilder();

            for(int i = 1; i < args.length; i++)
                message.append(" ").append(args[i]);

            island.sendMessage(Locale.TEAM_CHAT_FORMAT.getMessage(wrappedPlayer.getIslandRole(), wrappedPlayer.getName(),
                    message.toString().substring(1)));
        }

    }

    @Override
    public List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
