package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.utils.LocaleUtils;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class CmdHelp implements ISuperiorCommand {

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
        return "help [" + Locale.COMMAND_ARGUMENT_PAGE.getMessage(locale) + "]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_HELP.getMessage(locale);
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

        if(args.length == 2){
            try{
                page = Integer.parseInt(args[1]);
            }catch(IllegalArgumentException ex){
                Locale.INVALID_AMOUNT.send(sender, args[1]);
                return;
            }
        }

        if(page <= 0){
            Locale.INVALID_AMOUNT.send(sender, page);
            return;
        }

        List<SuperiorCommand> subCommands = plugin.getCommands().getSubCommands().stream()
                .filter(subCommand -> subCommand.getPermission().isEmpty() || sender.hasPermission(subCommand.getPermission()))
                .collect(Collectors.toList());

        if(subCommands.isEmpty()){
            Locale.NO_COMMAND_PERMISSION.send(sender);
            return;
        }

        int lastPage = subCommands.size() / 7;
        if(subCommands.size() % 7 != 0) lastPage++;

        if(page > lastPage){
            Locale.INVALID_AMOUNT.send(sender, page);
            return;
        }

        subCommands = subCommands.subList((page - 1) * 7, Math.min(subCommands.size(), page * 7));

        Locale.ISLAND_HELP_HEADER.send(sender, page, lastPage);

        java.util.Locale locale = LocaleUtils.getLocale(sender);

        for(SuperiorCommand _subCommand : subCommands) {
            if(_subCommand.displayCommand() && (_subCommand.getPermission().isEmpty() || sender.hasPermission(_subCommand.getPermission()))) {
                String description = _subCommand.getDescription(locale);
                if(description == null)
                    new NullPointerException("The description of the command " + _subCommand.getAliases().get(0) + " is null.").printStackTrace();
                Locale.ISLAND_HELP_LINE.send(sender, plugin.getCommands().getLabel() + " " + _subCommand.getUsage(locale), description == null ? "" : description);
            }
        }

        if(page != lastPage)
            Locale.ISLAND_HELP_NEXT_PAGE.send(sender, page + 1);
        else
            Locale.ISLAND_HELP_FOOTER.send(sender);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();

        if(args.length == 2){
            List<SuperiorCommand> subCommands = plugin.getCommands().getSubCommands().stream()
                    .filter(subCommand -> subCommand.displayCommand() && (subCommand.getPermission().isEmpty() || sender.hasPermission(subCommand.getPermission())))
                    .collect(Collectors.toList());

            int lastPage = subCommands.size() / 7;
            if(subCommands.size() % 7 != 0) lastPage++;

            for(int i = 1; i <= lastPage; i++)
                list.add(i + "");
        }

        return list;
    }

}
