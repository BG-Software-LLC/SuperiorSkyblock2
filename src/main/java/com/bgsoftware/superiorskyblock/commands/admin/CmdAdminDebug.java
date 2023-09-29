package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.commands.InternalSuperiorCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.StringArgumentType;
import com.bgsoftware.superiorskyblock.core.EnumHelper;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CmdAdminDebug implements InternalSuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("debug");
    }

    @Override
    public String getPermission() {
        return "superior.admin.debug";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_DEBUG.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder()
                .add(CommandArgument.optional("filter", StringArgumentType.INSTANCE))
                .build();
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
    public void execute(SuperiorSkyblockPlugin plugin, CommandContext context) throws CommandSyntaxException {
        CommandSender dispatcher = context.getDispatcher();

        boolean originalDebugMode = Log.isDebugMode();
        String debugFilterName = context.getOptionalArgument("filter", String.class).orElse(null);
        Debug debugFilter;

        if (debugFilterName == null) {
            if (originalDebugMode) {
                debugFilter = null;
            } else {
                throw new CommandSyntaxException("Invalid usage");
            }
        } else {
            debugFilter = EnumHelper.getEnum(Debug.class, debugFilterName.toUpperCase(Locale.ENGLISH));
        }

        boolean newDebugMode = debugFilter != null;

        if (originalDebugMode != newDebugMode) {
            Log.toggleDebugMode();
            if (newDebugMode) {
                Message.DEBUG_MODE_ENABLED.send(dispatcher);
            } else {
                Message.DEBUG_MODE_DISABLED.send(dispatcher);
            }
        }

        if (debugFilter != null) {
            if (Log.isDebugged(debugFilter)) {
                Message.DEBUG_MODE_FILTER_REMOVE.send(dispatcher, Formatters.CAPITALIZED_FORMATTER.format(debugFilter.name()));
            } else {
                Message.DEBUG_MODE_FILTER_ADD.send(dispatcher, Formatters.CAPITALIZED_FORMATTER.format(debugFilter.name()));
            }
        } else {
            Message.DEBUG_MODE_FILTER_CLEAR.send(dispatcher);
        }

        Log.setDebugFilter(debugFilter);
    }

}
