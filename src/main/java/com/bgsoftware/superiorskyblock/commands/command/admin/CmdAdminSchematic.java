package com.bgsoftware.superiorskyblock.commands.command.admin;

import com.bgsoftware.superiorskyblock.wrappers.WrappedPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class CmdAdminSchematic implements ICommand {

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
    public void execute(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        WrappedPlayer wrappedPlayer = WrappedPlayer.of(sender);

        if(args.length == 2) {
            if (wrappedPlayer.hasSchematicModeEnabled()) {
                Locale.TOGGLED_SCHEMATIC_OFF.send(wrappedPlayer);
            } else {
                Locale.TOGGLED_SCHEMATIC_ON.send(wrappedPlayer);
            }

            wrappedPlayer.toggleSchematicMode();
        }

        else{
            if(wrappedPlayer.getSchematicPos1() == null || wrappedPlayer.getSchematicPos2() == null){
                Locale.SCHEMATIC_NOT_READY.send(wrappedPlayer);
                return;
            }

            String schematicName = args[2];

            Locale.SCHEMATIC_PROCCESS_REQUEST.send(wrappedPlayer);
            plugin.getSchematics().saveSchematic(schematicName, wrappedPlayer);
        }
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        return null;
    }
}
