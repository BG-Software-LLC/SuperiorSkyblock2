package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.IslandArgument;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.menu.view.MenuViewWrapper;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.player.inventory.ClearActions;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdLeave implements ISuperiorCommand {

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
        return Message.COMMAND_DESCRIPTION_LEAVE.getMessage(locale);
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
        IslandArgument arguments = CommandArguments.getSenderIsland(plugin, sender);

        Island island = arguments.getIsland();

        if (island == null)
            return;

        SuperiorPlayer superiorPlayer = arguments.getSuperiorPlayer();

        if (superiorPlayer.getPlayerRole().getNextRole() == null) {
            Message.LEAVE_ISLAND_AS_LEADER.send(superiorPlayer);
            return;
        }

        if (plugin.getSettings().isLeaveConfirm()) {
            plugin.getMenus().openConfirmLeave(superiorPlayer, MenuViewWrapper.fromView(superiorPlayer.getOpenedView()));
        } else {
            IslandUtils.handleLeaveIsland(superiorPlayer, island);
        }
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

}
