package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalSuperiorCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.StringArgumentType;
import com.bgsoftware.superiorskyblock.core.messages.Message;

import java.util.Arrays;
import java.util.List;

public class CmdAdminSchematic implements InternalSuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("schematic", "schem");
    }

    @Override
    public String getPermission() {
        return "superior.admin.schematic";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_SCHEMATIC.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder()
                .add(CommandArgument.optional("schematic-name", StringArgumentType.INSTANCE, Message.COMMAND_ARGUMENT_SCHEMATIC_NAME))
                .build();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandContext context) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(context.getDispatcher());

        String schematicName = context.getOptionalArgument("schematic-name", String.class).orElse(null);

        if (schematicName == null) {
            if (superiorPlayer.hasSchematicModeEnabled()) {
                Message.TOGGLED_SCHEMATIC_OFF.send(superiorPlayer);
            } else {
                Message.TOGGLED_SCHEMATIC_ON.send(superiorPlayer);
            }

            superiorPlayer.toggleSchematicMode();
        } else {
            if (superiorPlayer.getSchematicPos1() == null || superiorPlayer.getSchematicPos2() == null) {
                Message.SCHEMATIC_NOT_READY.send(superiorPlayer);
                return;
            }

            Message.SCHEMATIC_PROCCESS_REQUEST.send(superiorPlayer);
            plugin.getSchematics().saveSchematic(superiorPlayer, schematicName);
        }

    }

}
