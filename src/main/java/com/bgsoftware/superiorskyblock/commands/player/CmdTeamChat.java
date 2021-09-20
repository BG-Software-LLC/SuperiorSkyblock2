package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.Locale;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CmdTeamChat implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("teamchat", "chat", "tc");
    }

    @Override
    public String getPermission() {
        return "superior.island.teamchat";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "teamchat [" + Locale.COMMAND_ARGUMENT_MESSAGE.getMessage(locale) + "]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_TEAM_CHAT.getMessage(locale);
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
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        Pair<Island, SuperiorPlayer> arguments = CommandArguments.getSenderIsland(plugin, sender);

        Island island = arguments.getKey();

        if(island == null)
            return;

        SuperiorPlayer superiorPlayer = arguments.getValue();

        if(args.length == 1){
            if(superiorPlayer.hasTeamChatEnabled()){
                Locale.TOGGLED_TEAM_CHAT_OFF.send(superiorPlayer);
            }else{
                Locale.TOGGLED_TEAM_CHAT_ON.send(superiorPlayer);
            }
            superiorPlayer.toggleTeamChat();
        }

        else{
            String message = CommandArguments.buildLongString(args, 1, true);
            IslandUtils.sendMessage(island, Locale.TEAM_CHAT_FORMAT, new ArrayList<>(), superiorPlayer.getPlayerRole(),
                    superiorPlayer.getName(), message);
            Locale.SPY_TEAM_CHAT_FORMAT.send(Bukkit.getConsoleSender(), superiorPlayer.getPlayerRole(), superiorPlayer.getName(), message);
            for(Player _onlinePlayer : Bukkit.getOnlinePlayers()){
                SuperiorPlayer onlinePlayer = plugin.getPlayers().getSuperiorPlayer(_onlinePlayer);
                if(onlinePlayer.hasAdminSpyEnabled())
                    Locale.SPY_TEAM_CHAT_FORMAT.send(onlinePlayer, superiorPlayer.getPlayerRole(), superiorPlayer.getName(), message);
            }
        }
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

}
