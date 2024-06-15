package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.common.collections.Lists;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.BaseCommand;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class CmdAdminSchematic extends BaseCommand implements ISuperiorCommand {

    @Override
    protected List<String> aliases() {
        return Lists.newLinkedList("schematic", "schem");
    }

    @Override
    protected String permission() {
        return "superior.admin.schematic";
    }

    @Override
    protected String usage(java.util.Locale locale) {
        return "admin schematic [" + Message.COMMAND_ARGUMENT_SCHEMATIC_NAME.getMessage(locale) + "]";
    }

    @Override
    protected String description(java.util.Locale locale) {
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
        return Lists.emptyList();
    }

}
