package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.commands.arguments.ArgumentsReader;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.commands.CommandsHelper;
import com.bgsoftware.superiorskyblock.commands.InternalSuperiorCommand;
import com.bgsoftware.superiorskyblock.commands.SubCommandsHandler;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.StringArrayArgumentType;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.player.PlayerLocales;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CmdAdmin implements InternalSuperiorCommand {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final String[] EMPTY_ARGS = new String[0];

    private static final String ADMIN_LABEL = plugin.getCommands().getLabel() + " admin";


    private final SubCommandsHandler commandsHandler = new SubCommandsHandler(ADMIN_LABEL,
            plugin.getCommands().getAdminCommandsMap(), CmdAdmin::handleUnknownCommand);

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("admin");
    }

    @Override
    public String getPermission() {
        return "superior.admin";
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder()
                .add(CommandArgument.optional("args", StringArrayArgumentType.withSuggestions(this::handleSuggestions), Message.COMMAND_ARGUMENT_PAGE))
                .build();
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN.getMessage(locale);
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

    private static void handleUnknownCommand(SuperiorSkyblockPlugin plugin, CommandSender dispatcher,
                                             @Nullable String subCommandName, ArgumentsReader unused) {
        Locale locale = PlayerLocales.getLocale(dispatcher);

        int page = 1;

        if (subCommandName != null) {
            try {
                page = Integer.parseInt(subCommandName);
            } catch (Throwable ignored) {
            }
        }

        if (page <= 0) {
            Message.INVALID_AMOUNT.send(dispatcher, locale, page);
            return;
        }

        List<SuperiorCommand> subCommands = new SequentialListBuilder<SuperiorCommand>().filter(subCommand -> subCommand.getPermission().isEmpty() || dispatcher.hasPermission(subCommand.getPermission())).build(plugin.getCommands().getAdminSubCommands());

        if (subCommands.isEmpty()) {
            Message.NO_COMMAND_PERMISSION.send(dispatcher, locale);
            return;
        }

        int lastPage = subCommands.size() / 7;
        if (subCommands.size() % 7 != 0) lastPage++;

        if (page > lastPage) {
            Message.INVALID_AMOUNT.send(dispatcher, locale, page);
            return;
        }

        subCommands = subCommands.subList((page - 1) * 7, Math.min(subCommands.size(), page * 7));

        Message.ADMIN_HELP_HEADER.send(dispatcher, locale, page, lastPage);

        for (SuperiorCommand subCommand : subCommands) {
            if (subCommand.displayCommand() && (subCommand.getPermission().isEmpty() || dispatcher.hasPermission(subCommand.getPermission()))) {
                String description = subCommand.getDescription(locale);
                if (description == null)
                    new NullPointerException("The description of the command " + subCommand.getAliases().get(0) + " is null.").printStackTrace();
                Message.ADMIN_HELP_LINE.send(dispatcher, locale, ADMIN_LABEL + " " + CommandsHelper.getCommandUsage(subCommand, locale), description);
            }
        }

        if (page != lastPage) Message.ADMIN_HELP_NEXT_PAGE.send(dispatcher, locale, page + 1);
        else Message.ADMIN_HELP_FOOTER.send(dispatcher, locale);
    }

}
