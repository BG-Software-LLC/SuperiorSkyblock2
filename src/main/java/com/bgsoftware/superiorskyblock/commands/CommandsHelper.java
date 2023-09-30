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
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.Text;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CommandsHelper {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private CommandsHelper() {

    }

    public static CommandContext parseCommandArgs(SuperiorCommand2 command, CommandSender dispatcher, ArgumentsReader reader) throws CommandSyntaxException {
        ArgumentsMap argumentsMap = new ArgumentsMap();

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
            argumentsMap.setArgument(argument.getIdentifier(), joinArray(" ", reader.getRaw(), startCursor, endCursor), value);
        }

        return context;
    }

    public static List<String> parseCommandSuggestions(SuperiorCommand2 command, CommandSender dispatcher, ArgumentsReader reader) throws CommandSyntaxException {
        ArgumentsMap argumentsMap = new ArgumentsMap();

        CommandContext context = new CommandContextImpl(dispatcher, argumentsMap);

        for (CommandArgument<?> argument : command.getArguments()) {
            int startCursor = reader.getCursor();

            List<String> suggestions;

            try {
                suggestions = argument.getType().getSuggestions(plugin, context, reader);
            } catch (CommandSyntaxException error) {
                if (argument.isOptional()) {
                    reader.setCursor(startCursor);
                    continue;
                }

                return Collections.emptyList();
            }

            if (!reader.hasNext()) {
                return suggestions;
            }

            int endCursor = reader.getCursor();

            Object value = new LazyReference<Object>() {
                @Override
                protected Object create() {
                    int currentCursor = reader.getCursor();
                    try {
                        reader.setCursor(startCursor);
                        return argument.getType().parse(plugin, context, reader);
                    } catch (CommandSyntaxException error) {
                        return null;
                    } finally {
                        reader.setCursor(currentCursor);
                    }
                }
            };

            argumentsMap.setArgument(argument.getIdentifier(), joinArray(" ", reader.getRaw(), startCursor, endCursor), value);
        }

        return Collections.emptyList();
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

    private static String joinArray(String delimiter, String[] arr, int start, int end) {
        if (arr.length == 0 || start >= arr.length) return "";

        StringBuilder str = new StringBuilder(arr[start]);

        for (int i = start + 1; i < Math.min(end, arr.length); ++i)
            str.append(delimiter).append(arr[i]);

        return str.toString();
    }

}
