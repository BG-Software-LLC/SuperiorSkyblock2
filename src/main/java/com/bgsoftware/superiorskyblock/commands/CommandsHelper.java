package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand2;
import com.bgsoftware.superiorskyblock.api.commands.arguments.ArgumentsReader;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.commands.arguments.ArgumentsMap;
import com.bgsoftware.superiorskyblock.commands.context.CommandContextImpl;
import com.bgsoftware.superiorskyblock.core.Text;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Locale;

public class CommandsHelper {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private CommandsHelper() {

    }

    public static CommandContext parseCommandArgs(SuperiorCommand2 command, CommandSender dispatcher, String[] args) throws CommandSyntaxException {
        ArgumentsMap argumentsMap = new ArgumentsMap();
        ArgumentsReader reader = new ArgumentsReader(args);

        CommandContext context = new CommandContextImpl(dispatcher, argumentsMap);

        for (CommandArgument<?> argument : command.getArguments()) {
            int startCursor = reader.getCursor();

            Object value;
            try {
                value = argument.getType().parse(plugin, context, reader);
            } catch (CommandSyntaxException error) {
                if (argument.isOptional()) {
                    reader.setCursor(startCursor);
                    continue;
                }

                throw error;
            }

            int endCursor = reader.getCursor();
            argumentsMap.setArgument(argument.getIdentifier(), String.join(" ", Arrays.copyOfRange(args, startCursor, endCursor)), value);
        }

        return context;
    }

    public static String getCommandUsage(SuperiorCommand command, Locale locale) {
        if (!(command instanceof SuperiorCommand2)) {
            return command.getUsage(locale);
        }

        StringBuilder usage = new StringBuilder(command.getAliases().get(0));
        for (CommandArgument<?> argument : ((SuperiorCommand2) command).getArguments()) {
            String displayName = argument.getDisplayName(locale);
            if (!Text.isBlank(displayName)) {
                if (argument.isOptional()) {
                    usage.append(" [").append(displayName).append("]");
                } else {
                    usage.append(" <").append(displayName).append(">");
                }
            }
        }

        return usage.toString();
    }

}
