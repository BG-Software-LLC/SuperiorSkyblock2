package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.commands.CommandsHelper;
import com.bgsoftware.superiorskyblock.commands.InternalSuperiorCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.IntArgumentType;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.player.PlayerLocales;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdHelp implements InternalSuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("help");
    }

    @Override
    public String getPermission() {
        return "superior.island.help";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_HELP.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder()
                .add(CommandArgument.optional("page", IntArgumentType.PAGE, Message.COMMAND_ARGUMENT_PAGE))
                .build();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandContext context) {
        CommandSender dispatcher = context.getDispatcher();

        int page = context.getOptionalArgument("page", Integer.class).orElse(1);

        List<SuperiorCommand> subCommands = new SequentialListBuilder<SuperiorCommand>()
                .filter(subCommand -> subCommand.displayCommand() && (subCommand.getPermission().isEmpty() ||
                        dispatcher.hasPermission(subCommand.getPermission())))
                .build(plugin.getCommands().getSubCommands());

        if (subCommands.isEmpty()) {
            Message.NO_COMMAND_PERMISSION.send(dispatcher);
            return;
        }

        int lastPage = subCommands.size() / 7;
        if (subCommands.size() % 7 != 0) lastPage++;

        if (page > lastPage) {
            Message.INVALID_AMOUNT.send(dispatcher, page);
            return;
        }

        subCommands = subCommands.subList((page - 1) * 7, Math.min(subCommands.size(), page * 7));

        Message.ISLAND_HELP_HEADER.send(dispatcher, page, lastPage);

        java.util.Locale locale = PlayerLocales.getLocale(dispatcher);

        for (SuperiorCommand subCommand : subCommands) {
            String description = subCommand.getDescription(locale);
            if (description == null)
                new NullPointerException("The description of the command " + subCommand.getAliases().get(0) + " is null.").printStackTrace();
            Message.ISLAND_HELP_LINE.send(dispatcher, plugin.getCommands().getLabel() + " " +
                            CommandsHelper.getCommandUsage(subCommand, locale),
                    description == null ? "" : description);
        }

        if (page != lastPage)
            Message.ISLAND_HELP_NEXT_PAGE.send(dispatcher, page + 1);
        else
            Message.ISLAND_HELP_FOOTER.send(dispatcher);
    }

}
