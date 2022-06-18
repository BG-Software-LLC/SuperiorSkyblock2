package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CmdAdminSchematic implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("schematic", "schem");
    }

    @Override
    public String getPermission() {
        return "superior.admin.schematic";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin schematic [" + Message.COMMAND_ARGUMENT_SCHEMATIC_NAME.getMessage(locale) + "]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_SCHEMATIC.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 2;
    }

    @Override
    public int getMaxArgs() {
        return 3;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);

        if (args.length == 2) {
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

            String schematicName = args[2];

            Message.SCHEMATIC_PROCCESS_REQUEST.send(superiorPlayer);
            plugin.getSchematics().saveSchematic(superiorPlayer, schematicName);
        }
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

}
