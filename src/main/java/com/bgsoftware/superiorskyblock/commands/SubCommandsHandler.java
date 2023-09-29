package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand2;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.player.PlayerLocales;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

    public void execute(SuperiorSkyblockPlugin plugin, CommandSender dispatcher, @Nullable String subCommandName, String[] args) {
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
                        CommandContext commandContext = CommandsHelper.parseCommandArgs((SuperiorCommand2) command, dispatcher, args);
                        ((SuperiorCommand2) command).execute(plugin, commandContext);
                    } catch (CommandSyntaxException error) {
                        String usage = CommandsHelper.getCommandUsage(command, locale);
                        Log.debugResult(Debug.EXECUTE_COMMAND, "Return Incorrect Usage", usage);
                        Message.COMMAND_USAGE.send(dispatcher, locale, this.label + " " + usage);
                    }
                } else {
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

        this.unknownCommandHandler.handle(plugin, dispatcher, subCommandName, args);
    }

    public interface UnknownCommandHandler {

        void handle(SuperiorSkyblockPlugin plugin, CommandSender dispatcher, String subCommandName, String[] args);

    }

}
