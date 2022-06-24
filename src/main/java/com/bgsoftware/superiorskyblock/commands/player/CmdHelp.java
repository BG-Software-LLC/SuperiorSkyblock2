package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.player.PlayerLocales;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class CmdHelp implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("help");
    }

    @Override
    public String getPermission() {
        return "superior.island.help";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "help [" + Message.COMMAND_ARGUMENT_PAGE.getMessage(locale) + "]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_HELP.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public int getMaxArgs() {
        return 2;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        int page = 1;

        if (args.length == 2) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (IllegalArgumentException ex) {
                Message.INVALID_AMOUNT.send(sender, args[1]);
                return;
            }
        }

        if (page <= 0) {
            Message.INVALID_AMOUNT.send(sender, page);
            return;
        }

        List<SuperiorCommand> subCommands = new SequentialListBuilder<SuperiorCommand>()
                .filter(subCommand -> subCommand.displayCommand() && (subCommand.getPermission().isEmpty() ||
                        sender.hasPermission(subCommand.getPermission())))
                .build(plugin.getCommands().getSubCommands());

        if (subCommands.isEmpty()) {
            Message.NO_COMMAND_PERMISSION.send(sender);
            return;
        }

        int lastPage = subCommands.size() / 7;
        if (subCommands.size() % 7 != 0) lastPage++;

        if (page > lastPage) {
            Message.INVALID_AMOUNT.send(sender, page);
            return;
        }

        subCommands = subCommands.subList((page - 1) * 7, Math.min(subCommands.size(), page * 7));

        Message.ISLAND_HELP_HEADER.send(sender, page, lastPage);

        java.util.Locale locale = PlayerLocales.getLocale(sender);

        for (SuperiorCommand _subCommand : subCommands) {
            String description = _subCommand.getDescription(locale);
            if (description == null)
                new NullPointerException("The description of the command " + _subCommand.getAliases().get(0) + " is null.").printStackTrace();
            Message.ISLAND_HELP_LINE.send(sender, plugin.getCommands().getLabel() + " " + _subCommand.getUsage(locale), description == null ? "" : description);
        }

        if (page != lastPage)
            Message.ISLAND_HELP_NEXT_PAGE.send(sender, page + 1);
        else
            Message.ISLAND_HELP_FOOTER.send(sender);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        List<String> list = new LinkedList<>();

        if (args.length == 2) {
            List<SuperiorCommand> subCommands = new SequentialListBuilder<SuperiorCommand>()
                    .filter(subCommand -> subCommand.displayCommand() && (subCommand.getPermission().isEmpty() ||
                            sender.hasPermission(subCommand.getPermission())))
                    .build(plugin.getCommands().getSubCommands());

            int lastPage = subCommands.size() / 7;
            if (subCommands.size() % 7 != 0) lastPage++;

            for (int i = 1; i <= lastPage; i++)
                list.add(i + "");
        }

        return Collections.unmodifiableList(list);
    }

}
