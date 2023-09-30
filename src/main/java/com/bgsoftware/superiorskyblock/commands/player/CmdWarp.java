package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalSuperiorCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.IslandArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.StringArgumentType;
import com.bgsoftware.superiorskyblock.core.menu.Menus;
import com.bgsoftware.superiorskyblock.core.messages.Message;

import java.util.Collections;
import java.util.List;

public class CmdWarp implements InternalSuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("warp");
    }

    @Override
    public String getPermission() {
        return "superior.island.warp";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_WARP.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder()
                .add(CommandArgument.optional("island", IslandArgumentType.INSTANCE, Message.COMMAND_ARGUMENT_PLAYER_NAME, Message.COMMAND_ARGUMENT_ISLAND_NAME))
                .add(CommandArgument.optional("warp-name", StringArgumentType.INSTANCE, Message.COMMAND_ARGUMENT_WARP_NAME))
                .build();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandContext context) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(context.getDispatcher());

        Island targetIsland = context.getOptionalArgument("island", Island.class).orElseGet(superiorPlayer::getIsland);

        if (targetIsland == null) {
            Message.INVALID_ISLAND.send(superiorPlayer);
            return;
        }

        String targetWarpName = context.getOptionalArgument("warp-name", String.class).orElse(null);

        IslandWarp islandWarp = targetWarpName == null ? null : targetIsland.getWarp(targetWarpName);

        if (islandWarp == null) {
            switch (context.getArgumentsCount()) {
                case 1:
                case 2:
                    Menus.MENU_WARP_CATEGORIES.openMenu(superiorPlayer, superiorPlayer.getOpenedView(), targetIsland);
                    break;
                case 3:
                    Message.INVALID_WARP.send(superiorPlayer, targetWarpName);
                    break;
            }

            return;
        }

        if (!targetIsland.isMember(superiorPlayer) && islandWarp.hasPrivateFlag()) {
            Message.INVALID_WARP.send(superiorPlayer, targetWarpName);
            return;
        }

        targetIsland.warpPlayer(superiorPlayer, targetWarpName);

    }

}
