package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.commands.CommandsHelper;
import com.bgsoftware.superiorskyblock.commands.CommandsMap;
import com.bgsoftware.superiorskyblock.commands.InternalSuperiorCommand;
import com.bgsoftware.superiorskyblock.commands.SubCommandsHandler;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.StringArgumentType;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
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
        return new CommandArgumentsBuilder().add(CommandArgument.required("sub-command", StringArgumentType.INSTANCE, "get/set/remove")).add(CommandArgument.optional("args", StringArgumentType.MULTIPLE)).build();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandContext context) throws CommandSyntaxException {
        String subCommandName = context.getRequiredArgument("sub-command", String.class);
        String[] args = context.getOptionalArgument("args", String.class).map(s -> s.split(" ")).orElse(EMPTY_ARGS);
        this.commandsHandler.execute(plugin, context.getDispatcher(), subCommandName, args);
    }

    private void handleUnknownCommand(SuperiorSkyblockPlugin plugin, CommandSender dispatcher, String subCommandName, String[] args) {
        List<SuperiorCommand> subCommands = new SequentialListBuilder<SuperiorCommand>()
                .filter(subCommand -> subCommand.displayCommand() && (subCommand.getPermission().isEmpty() || dispatcher.hasPermission(subCommand.getPermission())))
                .build(commandsMap.getSubCommands(false));

        if (subCommands.isEmpty()) {
            Message.NO_COMMAND_PERMISSION.send(dispatcher);
            return;
        }

        Message.ISLAND_HELP_HEADER.send(dispatcher, 1, 1);

        java.util.Locale locale = PlayerLocales.getLocale(dispatcher);

        for (SuperiorCommand subCommand : subCommands) {
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
        }

        @Override
        public void loadDefaultCommands() {
            registerCommand(new CmdAdminDataGet(), false);
            registerCommand(new CmdAdminDataSet(), false);
            registerCommand(new CmdAdminDataRemove(), false);
        }

    }

}
