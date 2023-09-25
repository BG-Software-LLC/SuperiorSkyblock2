package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalSuperiorCommand;
import com.bgsoftware.superiorskyblock.core.menu.view.MenuViewWrapper;
import com.bgsoftware.superiorskyblock.core.messages.Message;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CmdLang implements InternalSuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("lang", "language");
    }

    @Override
    public String getPermission() {
        return "superior.island.lang";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_LANG.getMessage(locale);
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
    public void execute(SuperiorSkyblockPlugin plugin, CommandContext context) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(context.getDispatcher());
        plugin.getMenus().openPlayerLanguage(superiorPlayer, MenuViewWrapper.fromView(superiorPlayer.getOpenedView()));
    }

}
