package com.bgsoftware.superiorskyblock.commands.command.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public final class CmdAdminSchematic implements ICommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("schematic", "schem");
    }

    @Override
    public String getPermission() {
        return "superior.admin.schematic";
    }

    @Override
    public String getUsage() {
        return "island admin schematic [schematic-name]";
    }

    @Override
    public String getDescription() {
        return Locale.COMMAND_DESCRIPTION_ADMIN_SCHEMATIC.getMessage();
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
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(sender);

        if(args.length == 2) {
            if (superiorPlayer.hasSchematicModeEnabled()) {
                Locale.TOGGLED_SCHEMATIC_OFF.send(superiorPlayer);
            } else {
                Locale.TOGGLED_SCHEMATIC_ON.send(superiorPlayer);
            }

            superiorPlayer.toggleSchematicMode();
        }

        else{
            if(superiorPlayer.getSchematicPos1() == null || superiorPlayer.getSchematicPos2() == null){
                Locale.SCHEMATIC_NOT_READY.send(superiorPlayer);
                return;
            }

            String schematicName = args[2];

            Locale.SCHEMATIC_PROCCESS_REQUEST.send(superiorPlayer);
            plugin.getSchematics().saveSchematic(superiorPlayer, schematicName);
        }
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return null;
    }
}
