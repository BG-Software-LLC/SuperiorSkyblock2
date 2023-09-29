package com.bgsoftware.superiorskyblock.module.upgrades.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalIslandCommand;
import com.bgsoftware.superiorskyblock.commands.context.IslandCommandContext;
import com.bgsoftware.superiorskyblock.core.menu.view.MenuViewWrapper;
import com.bgsoftware.superiorskyblock.core.messages.Message;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CmdUpgrade implements InternalIslandCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("upgrade", "upgrades");
    }

    @Override
    public String getPermission() {
        return "superior.island.upgrade";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_UPGRADE.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments()

    {
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
        plugin.getMenus().openUpgrades(superiorPlayer, MenuViewWrapper.fromView(superiorPlayer.getOpenedView()), island);
    }

}
