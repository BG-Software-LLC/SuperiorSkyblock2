package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.lang.Message;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CmdAdminRecalc implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("recalc", "recalculate", "level");
    }

    @Override
    public String getPermission() {
        return "superior.admin.recalc";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin recalc [" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_RECALC.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 2;
    }

    @Override
    public int getMaxArgs() {
        return 3;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        if (args.length == 2) {
            Message.RECALC_ALL_ISLANDS.send(sender);
            plugin.getGrid().calcAllIslands(() -> Message.RECALC_ALL_ISLANDS_DONE.send(sender));
        } else {
            SuperiorPlayer targetPlayer = plugin.getPlayers().getSuperiorPlayer(args[2]);
            Island island = targetPlayer == null ? plugin.getGrid().getIsland(args[2]) : targetPlayer.getIsland();

            if (island == null) {
                if (args[2].equalsIgnoreCase(sender.getName()))
                    Message.INVALID_ISLAND.send(sender);
                else if (targetPlayer == null)
                    Message.INVALID_ISLAND_OTHER_NAME.send(sender, StringUtils.stripColors(args[2]));
                else
                    Message.INVALID_ISLAND_OTHER.send(sender, targetPlayer.getName());
                return;
            }

            if (island.isBeingRecalculated()) {
                Message.RECALC_ALREADY_RUNNING_OTHER.send(sender);
                return;
            }

            Message.RECALC_PROCCESS_REQUEST.send(sender);
            island.calcIslandWorth(sender instanceof Player ? plugin.getPlayers().getSuperiorPlayer(sender) : null);
        }
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return args.length == 3 ? CommandTabCompletes.getOnlinePlayersWithIslands(plugin, args[2], false) : new ArrayList<>();
    }

}
