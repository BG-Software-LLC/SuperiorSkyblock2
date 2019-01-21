package com.ome_r.superiorskyblock.commands.command.admin;

import com.ome_r.superiorskyblock.Locale;
import com.ome_r.superiorskyblock.SuperiorSkyblock;
import com.ome_r.superiorskyblock.commands.ICommand;
import com.ome_r.superiorskyblock.wrappers.WrappedPlayer;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdAdminBypass implements ICommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("bypass");
    }

    @Override
    public String getPermission() {
        return "superior.admin.bypass";
    }

    @Override
    public String getUsage() {
        return "is admin bypass";
    }

    @Override
    public int getMinArgs() {
        return 2;
    }

    @Override
    public int getMaxArgs() {
        return 2;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        WrappedPlayer wrappedPlayer = WrappedPlayer.of(sender);

        if(wrappedPlayer.hasBypassModeEnabled()){
            Locale.TOGGLED_BYPASS_OFF.send(wrappedPlayer);
        }

        else{
            Locale.TOGGLED_BYPASS_ON.send(wrappedPlayer);
        }

        wrappedPlayer.toggleBypassMode();
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        return null;
    }
}
