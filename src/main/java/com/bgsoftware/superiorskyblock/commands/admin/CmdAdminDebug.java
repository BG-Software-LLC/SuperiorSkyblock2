package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.core.EnumHelper;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.player.PlayerLocales;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

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
        return 3;
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
        boolean originalDebugMode = Log.isDebugMode();
        Debug debugFilter;

        if (args.length == 2) {
            if (originalDebugMode) {
                debugFilter = null;
            } else {
                Message.COMMAND_USAGE.send(sender, plugin.getCommands().getLabel() + " " + getUsage(PlayerLocales.getLocale(sender)));
                return;
            }
        } else {
            debugFilter = EnumHelper.getEnum(Debug.class, args[2].toUpperCase(Locale.ENGLISH));
        }

        boolean newDebugMode = debugFilter != null;

        if (originalDebugMode != newDebugMode) {
            Log.toggleDebugMode();
            if (newDebugMode) {
                Message.DEBUG_MODE_ENABLED.send(sender);
            } else {
                Message.DEBUG_MODE_DISABLED.send(sender);
            }
        }

        if (debugFilter != null) {
            if (Log.isDebugged(debugFilter)) {
                Message.DEBUG_MODE_FILTER_REMOVE.send(sender, Formatters.CAPITALIZED_FORMATTER.format(debugFilter.name()));
            } else {
                Message.DEBUG_MODE_FILTER_ADD.send(sender, Formatters.CAPITALIZED_FORMATTER.format(debugFilter.name()));
            }
        } else {
            Message.DEBUG_MODE_FILTER_CLEAR.send(sender);
        }

        Log.setDebugFilter(debugFilter);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return args.length <= 2 ? Collections.emptyList() : CommandTabCompletes.getCustomComplete(args[2], Debug.getDebugNames());
    }

}
