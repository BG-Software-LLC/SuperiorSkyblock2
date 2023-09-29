package com.bgsoftware.superiorskyblock.module.bank.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalIslandCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.StringArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.IslandCommandContext;
import com.bgsoftware.superiorskyblock.core.menu.view.MenuViewWrapper;
import com.bgsoftware.superiorskyblock.core.messages.Message;

import java.util.Collections;
import java.util.List;

public class CmdBank implements InternalIslandCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("bank");
    }

    @Override
    public String getPermission() {
        return "superior.island.bank";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_BANK.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments()

    {
        return new CommandArgumentsBuilder()
                .add(CommandArgument.optional("logs", StringArgumentType.INSTANCE))
                .build();
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
        boolean openLogs = context.getOptionalArgument("logs", String.class)
                .map(x -> x.equalsIgnoreCase("logs")).orElse(false);

        if (openLogs) {
            plugin.getMenus().openBankLogs(superiorPlayer, MenuViewWrapper.fromView(superiorPlayer.getOpenedView()), island);
        } else {
            plugin.getMenus().openIslandBank(superiorPlayer, MenuViewWrapper.fromView(superiorPlayer.getOpenedView()), island);
        }
    }

}
