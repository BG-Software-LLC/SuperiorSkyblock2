package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.core.debug.PluginDebugger;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdAdminDebug implements ISuperiorCommand {

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
        return Message.COMMAND_DESCRIPTION_ADMIN_DEBUG.getMessage(locale);
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
        if (PluginDebugger.isDebugMode() && args.length == 2) {
            Message.DEBUG_MODE_DISABLED.send(sender);
            PluginDebugger.toggleDebugMode();
            PluginDebugger.setDebugFilter("");
            return;
        }

        if (!PluginDebugger.isDebugMode()) {
            Message.DEBUG_MODE_ENABLED.send(sender);
            PluginDebugger.toggleDebugMode();
        }

        if (args.length > 2) {
            StringBuilder debugFilter = new StringBuilder();
            for (int i = 2; i < args.length; i++)
                debugFilter.append(" ").append(args[i]);
            PluginDebugger.setDebugFilter(debugFilter.length() == 0 ? "" : debugFilter.substring(1));
            Message.DEBUG_MODE_FILTER.send(sender);
        }
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

}
