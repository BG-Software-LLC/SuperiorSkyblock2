package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalSuperiorCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.StringArgumentType;
import com.bgsoftware.superiorskyblock.core.menu.Menus;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.IslandNames;

import java.util.Collections;
import java.util.List;

public class CmdCreate implements InternalSuperiorCommand {

    private final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("create");
    }

    @Override
    public String getPermission() {
        return "superior.island.create";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_CREATE.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        CommandArgumentsBuilder builder = new CommandArgumentsBuilder();

        if (plugin.getSettings().getIslandNames().isRequiredForCreation())
            builder.add(CommandArgument.required("name", StringArgumentType.INSTANCE, Message.COMMAND_ARGUMENT_ISLAND_NAME));

        if (plugin.getSettings().isSchematicNameArgument())
            builder.add(CommandArgument.optional("schematic-name", StringArgumentType.INSTANCE, Message.COMMAND_ARGUMENT_SCHEMATIC_NAME));

        return builder.build();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandContext context) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(context.getDispatcher());

        if (superiorPlayer.getIsland() != null) {
            Message.ALREADY_IN_ISLAND.send(superiorPlayer);
            return;
        }

        if (plugin.getGrid().hasActiveCreateRequest(superiorPlayer)) {
            Message.ISLAND_CREATE_PROCESS_FAIL.send(superiorPlayer);
            return;
        }

        String islandName;
        try {
            islandName = context.getRequiredArgument("name", String.class);
            if (!IslandNames.isValidName(context.getDispatcher(), null, islandName))
                return;
        } catch (IllegalArgumentException error) {
            islandName = "";
        }

        String schematicName = context.getOptionalArgument("schematic-name", String.class).orElse(null);
        if (schematicName != null) {
            Schematic schematic = plugin.getSchematics().getSchematic(schematicName);
            if (schematic == null || schematicName.endsWith("_nether") || schematicName.endsWith("_the_end")) {
                Message.INVALID_SCHEMATIC.send(context.getDispatcher(), schematicName);
                return;
            }
        }

        if (schematicName == null) {
            Menus.MENU_ISLAND_CREATION.openMenu(superiorPlayer, superiorPlayer.getOpenedView(), islandName);
        } else {
            Menus.MENU_ISLAND_CREATION.simulateClick(superiorPlayer, islandName, schematicName, false, superiorPlayer.getOpenedView());
        }
    }

}
