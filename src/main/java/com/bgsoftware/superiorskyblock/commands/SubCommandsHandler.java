package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand2;
import com.bgsoftware.superiorskyblock.api.commands.arguments.ArgumentsReader;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.player.PlayerLocales;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class SubCommandsHandler {

    private final String label;
    private final CommandsMap commandsMap;
    private final UnknownCommandHandler unknownCommandHandler;

    public SubCommandsHandler(String label, CommandsMap commandsMap, UnknownCommandHandler unknownCommandHandler) {
        this.label = label;
        this.commandsMap = commandsMap;
        this.unknownCommandHandler = unknownCommandHandler;
    }

    public void execute(SuperiorSkyblockPlugin plugin, CommandSender dispatcher, @Nullable String subCommandName,
                        ArgumentsReader reader) {
        java.util.Locale locale = PlayerLocales.getLocale(dispatcher);

        if (subCommandName != null) {
            Log.debug(Debug.EXECUTE_COMMAND, dispatcher.getName(), subCommandName);

            SuperiorCommand command = this.commandsMap.getCommand(subCommandName);

            if (command != null) {
                if (!(dispatcher instanceof Player) && !command.canBeExecutedByConsole()) {
                    Message.CUSTOM.send(dispatcher, "&cCan be executed only by players!", true);
                    return;
                }

                String permission = command.getPermission();
                if (!Text.isBlank(permission) && !dispatcher.hasPermission(permission)) {
                    Log.debugResult(Debug.EXECUTE_COMMAND, "Return Missing Permission", permission);
                    Message.NO_COMMAND_PERMISSION.send(dispatcher, locale);
                    return;
                }

                if (command instanceof SuperiorCommand2) {
                    try {
                        CommandContext context = CommandsHelper.parseCommandArgs((SuperiorCommand2) command, dispatcher, reader);
                        ((SuperiorCommand2) command).execute(plugin, context);
                    } catch (CommandSyntaxException error) {
                        String usage = CommandsHelper.getCommandUsage(command, locale);
                        Log.debugResult(Debug.EXECUTE_COMMAND, "Return Incorrect Usage", usage);
                        Message.COMMAND_USAGE.send(dispatcher, locale, this.label + " " + usage);
                    }
                } else {
                    String[] args = reader.getRaw();

                    if (args.length < command.getMinArgs() || args.length > command.getMaxArgs()) {
                        String usage = CommandsHelper.getCommandUsage(command, locale);
                        Log.debugResult(Debug.EXECUTE_COMMAND, "Return Incorrect Usage", usage);
                        Message.COMMAND_USAGE.send(dispatcher, locale, this.label + " " + usage);
                        return;
                    }

                    command.execute(plugin, dispatcher, args);
                }

                return;
            }
        }

        this.unknownCommandHandler.handle(plugin, dispatcher, subCommandName, reader);
    }

    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender dispatcher,
                                    @Nullable String subCommandName, ArgumentsReader reader) {
        if (subCommandName == null)
            return Collections.emptyList();

        SuperiorCommand command = this.commandsMap.getCommand(subCommandName);

        if (command != null) {
            if (hasCommandAccess(command, dispatcher)) {
                if (command instanceof SuperiorCommand2) {
                    try {
                        return CommandsHelper.parseCommandSuggestions((SuperiorCommand2) command, dispatcher, reader);
                    } catch (CommandSyntaxException ignored) {
                        return Collections.emptyList();
                    }
                } else {
                    return command.tabComplete(plugin, dispatcher, reader.getRaw());
                }
            }

            return Collections.emptyList();
        }

        subCommandName = subCommandName.toLowerCase(Locale.ENGLISH);

        List<String> suggestions = new LinkedList<>();

        for (SuperiorCommand subCommand : this.commandsMap.getSubCommands(false)) {
            if (hasCommandAccess(subCommand, dispatcher)) {
                List<String> aliases = new LinkedList<>(subCommand.getAliases());
                aliases.addAll(plugin.getSettings().getCommandAliases().getOrDefault(aliases.get(0).toLowerCase(Locale.ENGLISH), Collections.emptyList()));
                for (String alias : aliases) {
                    if (alias.contains(subCommandName.toLowerCase(Locale.ENGLISH))) {
                        suggestions.add(alias);
                    }
                }
            }
        }

        return suggestions;
    }

    private static boolean hasCommandAccess(SuperiorCommand command, CommandSender dispatcher) {
        if (!(dispatcher instanceof Player) && !command.canBeExecutedByConsole()) {
            return false;
        }

        String permission = command.getPermission();
        if (!Text.isBlank(permission) && !dispatcher.hasPermission(permission)) {
            return false;
        }

        return true;
    }

    public interface UnknownCommandHandler {

        void handle(SuperiorSkyblockPlugin plugin, CommandSender dispatcher, String subCommandName, ArgumentsReader reader);

    }

}
