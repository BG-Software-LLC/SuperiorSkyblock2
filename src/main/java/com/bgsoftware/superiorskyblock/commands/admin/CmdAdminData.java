package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.commands.arguments.ArgumentsReader;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.commands.CommandsHelper;
import com.bgsoftware.superiorskyblock.commands.CommandsMap;
import com.bgsoftware.superiorskyblock.commands.InternalSuperiorCommand;
import com.bgsoftware.superiorskyblock.commands.SubCommandsHandler;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.StringArrayArgumentType;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.player.PlayerLocales;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdAdminData implements InternalSuperiorCommand {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final String[] EMPTY_ARGS = new String[0];

    private static final String ADMIN_DATA_LABEL = plugin.getCommands().getLabel() + " admin data";

    private final CommandsMap commandsMap = new DataCommandsMap(plugin);
    private final SubCommandsHandler commandsHandler = new SubCommandsHandler(
            ADMIN_DATA_LABEL, commandsMap, this::handleUnknownCommand);

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("data");
    }

    @Override
    public String getPermission() {
        return "superior.admin.data";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_DATA.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder()
                .add(CommandArgument.optional("args", StringArrayArgumentType.withSuggestions(this::handleSuggestions), "get/set/remove"))
                .build();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandContext context) throws CommandSyntaxException {
        String[] args = context.getOptionalArgument("args", String[].class).orElse(EMPTY_ARGS);

        if (args.length == 0) {
            handleUnknownCommand(plugin, context.getDispatcher(), null, ArgumentsReader.EMPTY);
            return;
        }

        String subCommandName = args[0];
        ArgumentsReader subCommandArgs = new ArgumentsReader(args);
        subCommandArgs.setCursor(1);

        this.commandsHandler.execute(plugin, context.getDispatcher(), subCommandName, subCommandArgs);
    }

    private List<String> handleSuggestions(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        String subCommandName = reader.hasNext() ? reader.read() : null;
        return this.commandsHandler.tabComplete((SuperiorSkyblockPlugin) plugin, context.getDispatcher(), subCommandName, reader);
    }

    private void handleUnknownCommand(SuperiorSkyblockPlugin plugin, CommandSender dispatcher, String subCommandName, ArgumentsReader unused) {
        Message.ISLAND_HELP_HEADER.send(dispatcher, 1, 1);

        java.util.Locale locale = PlayerLocales.getLocale(dispatcher);

        for (SuperiorCommand subCommand : commandsMap.getSubCommands(false)) {
            String description = subCommand.getDescription(locale);
            if (description == null)
                new NullPointerException("The description of the command " + subCommand.getAliases().get(0) + " is null.").printStackTrace();
            Message.ISLAND_HELP_LINE.send(dispatcher, ADMIN_DATA_LABEL + " " +
                    CommandsHelper.getCommandUsage(subCommand, locale), description == null ? "" : description);
        }

        Message.ISLAND_HELP_FOOTER.send(dispatcher);
    }

    private static class DataCommandsMap extends CommandsMap {

        DataCommandsMap(SuperiorSkyblockPlugin plugin) {
            super(plugin);
            registerCommand(new CmdAdminDataGet(), false);
            registerCommand(new CmdAdminDataSet(), false);
            registerCommand(new CmdAdminDataRemove(), false);
        }

        @Override
        public void loadDefaultCommands() {

        }

    }

}
