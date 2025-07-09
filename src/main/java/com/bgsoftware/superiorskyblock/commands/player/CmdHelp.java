package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.commands.CommandsHelper;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.collections.CollectionsFactory;
import com.bgsoftware.superiorskyblock.core.collections.view.Int2ObjectMapView;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventType;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsDispatcher;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.player.PlayerLocales;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class CmdHelp implements ISuperiorCommand {

    private static final Int2ObjectMapView<List<SuperiorCommand>> commandsPerPageCache = CollectionsFactory.createInt2ObjectArrayMap();

    public static void registerListeners(PluginEventsDispatcher dispatcher) {
        dispatcher.registerCallback(PluginEventType.SETTINGS_UPDATE_EVENT, CmdHelp::onCommandsRefresh);
        dispatcher.registerCallback(PluginEventType.COMMANDS_UPDATE_EVENT, CmdHelp::onCommandsRefresh);
    }

    private static void onCommandsRefresh() {
        commandsPerPageCache.clear();
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("help");
    }

    @Override
    public String getPermission() {
        return "superior.island.help";
    }

    @Override
    public String getUsage(SuperiorSkyblockPlugin plugin, CommandSender sender, java.util.Locale locale) {
        List<SuperiorCommand> subCommands = new SequentialListBuilder<SuperiorCommand>()
                .filter(subCommand -> CommandsHelper.shouldDisplayCommandForPlayer(subCommand, sender))
                .build(plugin.getCommands().getSubCommands());

        if (getLastPage(plugin, subCommands) != 1) {
            return "help [" + Message.COMMAND_ARGUMENT_PAGE.getMessage(locale) + "]";
        } else {
            return "help";
        }
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
    public int getMaxArgs(SuperiorSkyblockPlugin plugin, CommandSender sender) {
        List<SuperiorCommand> subCommands = new SequentialListBuilder<SuperiorCommand>()
                .filter(subCommand -> CommandsHelper.shouldDisplayCommandForPlayer(subCommand, sender))
                .build(plugin.getCommands().getSubCommands());

        return (getLastPage(plugin, subCommands) != 1) ? 2 : 1;
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
                .filter(subCommand -> CommandsHelper.shouldDisplayCommandForPlayer(subCommand, sender))
                .build(plugin.getCommands().getSubCommands());

        if (subCommands.isEmpty()) {
            Message.NO_COMMAND_PERMISSION.send(sender);
            return;
        }

        int commandsPerPageCount = plugin.getSettings().getCommandsPerPage();

        int lastPage = getLastPage(plugin, subCommands);

        if (page > lastPage) {
            Message.INVALID_AMOUNT.send(sender, page);
            return;
        }

        Message.ISLAND_HELP_HEADER.send(sender, page, lastPage);

        java.util.Locale locale = PlayerLocales.getLocale(sender);

        List<SuperiorCommand> commandsOfPage;
        if (commandsPerPageCount > 0) {
            commandsOfPage = commandsPerPageCache.get(page);
            if (commandsOfPage == null) {
                commandsOfPage = subCommands.subList((page - 1) * commandsPerPageCount, Math.min(subCommands.size(), page * commandsPerPageCount));
                commandsPerPageCache.put(page, commandsOfPage);
            }
        } else {
            commandsOfPage = subCommands;
        }

        for (SuperiorCommand subCommand : commandsOfPage) {
            String description = subCommand.getDescription(locale);
            if (description == null)
                new NullPointerException("The description of the command " + subCommand.getAliases().get(0) + " is null.").printStackTrace();
            Message.ISLAND_HELP_LINE.send(sender, plugin.getCommands().getLabel() + " " + subCommand.getUsage(plugin, sender, locale), description == null ? "" : description);
        }

        if (page != lastPage)
            Message.ISLAND_HELP_NEXT_PAGE.send(sender, page + 1);
        else
            Message.ISLAND_HELP_FOOTER.send(sender);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        if (args.length != 2)
            return Collections.emptyList();

        List<String> list = new LinkedList<>();

        List<SuperiorCommand> subCommands = new SequentialListBuilder<SuperiorCommand>()
                .filter(subCommand -> CommandsHelper.shouldDisplayCommandForPlayer(subCommand, sender))
                .build(plugin.getCommands().getSubCommands());

        int lastPage = getLastPage(plugin, subCommands);

        if (lastPage == 1)
            return Collections.emptyList();

        for (int i = 1; i <= lastPage; i++)
            list.add(i + "");

        return Collections.unmodifiableList(list);
    }

    private static int getLastPage(SuperiorSkyblockPlugin plugin, List<SuperiorCommand> subCommands) {
        int commandsPerPageCount = plugin.getSettings().getCommandsPerPage();

        int lastPage;
        if (commandsPerPageCount > 0) {
            lastPage = subCommands.size() / commandsPerPageCount;
            if (subCommands.size() % commandsPerPageCount != 0) lastPage++;
        } else {
            lastPage = 1;
        }
        return lastPage;
    }

}
