package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdAdminDebug implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("debug");
    }

    @Override
    public String getPermission() {
        return "superior.admin.debug";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin debug [filter]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_DEBUG.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 2;
    }

    @Override
    public int getMaxArgs() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public boolean displayCommand() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        if(plugin.isDebugMode() && args.length == 2){
            Locale.DEBUG_MODE_DISABLED.send(sender);
            plugin.toggleDebugMode();
            return;
        }

        if(!plugin.isDebugMode()){
            Locale.DEBUG_MODE_ENABLED.send(sender);
            plugin.toggleDebugMode();
        }

        if(args.length > 2){
            StringBuilder debugFilter = new StringBuilder();
            for (int i = 2; i < args.length; i++)
                debugFilter.append(" ").append(args[i]);
            plugin.setDebugFilter(debugFilter.length() == 0 ? "" : debugFilter.substring(1));
            Locale.DEBUG_MODE_FILTER.send(sender);
        }
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
