package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalIslandCommand;
import com.bgsoftware.superiorskyblock.commands.context.IslandCommandContext;
import com.bgsoftware.superiorskyblock.core.menu.view.MenuViewWrapper;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.IslandUtils;

import java.util.Collections;
import java.util.List;

public class CmdLeave implements InternalIslandCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("leave");
    }

    @Override
    public String getPermission() {
        return "superior.island.leave";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_LEAVE.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return Collections.emptyList();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public boolean isSelfIsland() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, IslandCommandContext context) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(context.getDispatcher());
        Island island = context.getIsland();

        if (superiorPlayer.getPlayerRole().getNextRole() == null) {
            Message.LEAVE_ISLAND_AS_LEADER.send(superiorPlayer);
            return;
        }

        if (plugin.getSettings().isLeaveConfirm()) {
            plugin.getMenus().openConfirmLeave(superiorPlayer, MenuViewWrapper.fromView(superiorPlayer.getOpenedView()));
            return;
        }

        if (!plugin.getEventsBus().callIslandQuitEvent(superiorPlayer, island))
            return;

        island.kickMember(superiorPlayer);

        IslandUtils.sendMessage(island, Message.LEAVE_ANNOUNCEMENT, Collections.emptyList(), superiorPlayer.getName());

        Message.LEFT_ISLAND.send(superiorPlayer);
    }

}
