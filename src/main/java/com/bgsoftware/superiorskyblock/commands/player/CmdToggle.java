package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalSuperiorCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.StringArgumentType;
import com.bgsoftware.superiorskyblock.core.messages.Message;

import java.util.Collections;
import java.util.List;

public class CmdToggle implements InternalSuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("toggle");
    }

    @Override
    public String getPermission() {
        return "superior.island.toggle";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_TOGGLE.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder()
                .add(CommandArgument.required("action", "border/blocks", StringArgumentType.INSTANCE))
                .build();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandContext context) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(context.getDispatcher());

        String action = context.getRequiredArgument("action", String.class);

        if (action.equalsIgnoreCase("border")) {
            if (!superiorPlayer.hasPermission("superior.island.toggle.border")) {
                Message.NO_COMMAND_PERMISSION.send(superiorPlayer);
                return;
            }

            if (!plugin.getEventsBus().callPlayerToggleBorderEvent(superiorPlayer))
                return;

            if (superiorPlayer.hasWorldBorderEnabled()) {
                Message.TOGGLED_WORLD_BORDER_OFF.send(superiorPlayer);
            } else {
                Message.TOGGLED_WORLD_BORDER_ON.send(superiorPlayer);
            }

            superiorPlayer.toggleWorldBorder();
            superiorPlayer.updateWorldBorder(plugin.getGrid().getIslandAt(superiorPlayer.getLocation()));
        } else if (action.equalsIgnoreCase("blocks")) {
            if (!superiorPlayer.hasPermission("superior.island.toggle.blocks")) {
                Message.NO_COMMAND_PERMISSION.send(superiorPlayer);
                return;
            }

            if (!plugin.getEventsBus().callPlayerToggleBlocksStackerEvent(superiorPlayer))
                return;

            if (superiorPlayer.hasBlocksStackerEnabled()) {
                Message.TOGGLED_STACKED_BLOCKS_OFF.send(superiorPlayer);
            } else {
                Message.TOGGLED_STACKED_BLOCKS_ON.send(superiorPlayer);
            }

            superiorPlayer.toggleBlocksStacker();
        } else {
            Message.INVALID_TOGGLE_MODE.send(superiorPlayer, action);
        }

    }

}
