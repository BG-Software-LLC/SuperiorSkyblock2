package com.bgsoftware.superiorskyblock.commands.player;

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
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class CmdPanel implements InternalIslandCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("panel", "manager", "cp");
    }

    @Override
    public String getPermission() {
        return "superior.island.panel";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_PANEL.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments()

    {
        return new CommandArgumentsBuilder()
                .add(CommandArgument.optional("action", "members/visitors/toggle", StringArgumentType.INSTANCE))
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
        CommandSender dispatcher = context.getDispatcher();
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(dispatcher);
        Island island = context.getIsland();

        String action = context.getOptionalArgument("action", String.class).orElse(null);
        if (action != null) {
            if (action.equalsIgnoreCase("members")) {
                plugin.getCommands().dispatchSubCommand(dispatcher, "members");
                return;
            } else if (action.equalsIgnoreCase("visitors")) {
                plugin.getCommands().dispatchSubCommand(dispatcher, "visitors");
                return;
            } else if (action.equalsIgnoreCase("toggle")) {
                if (!plugin.getEventsBus().callPlayerTogglePanelEvent(superiorPlayer))
                    return;

                if (superiorPlayer.hasToggledPanel()) {
                    superiorPlayer.setToggledPanel(false);
                    Message.PANEL_TOGGLE_OFF.send(superiorPlayer);
                } else {
                    superiorPlayer.setToggledPanel(true);
                    Message.PANEL_TOGGLE_ON.send(superiorPlayer);
                }
                return;
            }
        }

        plugin.getMenus().openControlPanel(superiorPlayer, MenuViewWrapper.fromView(superiorPlayer.getOpenedView()), island);
    }

}
