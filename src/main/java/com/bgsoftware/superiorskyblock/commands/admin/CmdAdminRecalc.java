package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class CmdAdminRecalc implements IAdminIslandCommand {

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
        return "admin recalc <" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_RECALC.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 3;
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
    public boolean supportMultipleIslands() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, @Nullable SuperiorPlayer targetPlayer, List<Island> islands, String[] args) {
        if (islands.size() > 1) {
            Message.RECALC_ALL_ISLANDS.send(sender);
            plugin.getGrid().calcAllIslands(() -> Message.RECALC_ALL_ISLANDS_DONE.send(sender));
        } else {
            Island island = islands.get(0);

            if (island.isBeingRecalculated()) {
                Message.RECALC_ALREADY_RUNNING_OTHER.send(sender);
                return;
            }

            Message.RECALC_PROCCESS_REQUEST.send(sender);
            island.calcIslandWorth(sender instanceof Player ? plugin.getPlayers().getSuperiorPlayer(sender) : null);
        }
    }

}
