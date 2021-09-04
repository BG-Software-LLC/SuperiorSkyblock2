package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.utils.LocaleUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class CmdAdmin implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("admin");
    }

    @Override
    public String getPermission() {
        return "superior.admin";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin [" + Locale.COMMAND_ARGUMENT_PAGE.getMessage(locale) + "]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public int getMaxArgs() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        java.util.Locale locale = LocaleUtils.getLocale(sender);

        if(args.length > 1 && !isNumber(args[1])){
            SuperiorCommand command = plugin.getCommands().getAdminCommand(args[1]);
            if(command != null){
                if(!(sender instanceof Player) && !command.canBeExecutedByConsole()){
                    Locale.sendMessage(sender, "&cCan be executed only by players!", true);
                    return;
                }

                if(!command.getPermission().isEmpty() && !sender.hasPermission(command.getPermission())) {
                    Locale.NO_COMMAND_PERMISSION.send(sender, locale);
                    return;
                }

                if(args.length < command.getMinArgs() || args.length > command.getMaxArgs()){
                    Locale.COMMAND_USAGE.send(sender, locale, plugin.getCommands().getLabel() + " " + command.getUsage(locale));
                    return;
                }

                command.execute(plugin, sender, args);
                return;
            }
        }

        int page = 1;

        if(args.length == 2){
            try {
                page = Integer.parseInt(args[1]);
            }catch(Throwable ignored){}
        }

        if(page <= 0){
            Locale.INVALID_AMOUNT.send(sender, locale, page);
            return;
        }

        List<SuperiorCommand> subCommands = plugin.getCommands().getAdminSubCommands().stream()
                .filter(subCommand -> subCommand.getPermission().isEmpty() || sender.hasPermission(subCommand.getPermission()))
                .collect(Collectors.toList());

        if(subCommands.isEmpty()){
            Locale.NO_COMMAND_PERMISSION.send(sender, locale);
            return;
        }

        int lastPage = subCommands.size() / 7;
        if(subCommands.size() % 7 != 0) lastPage++;

        if(page > lastPage){
            Locale.INVALID_AMOUNT.send(sender, locale, page);
            return;
        }

        subCommands = subCommands.subList((page - 1) * 7, Math.min(subCommands.size(), page * 7));

        Locale.ADMIN_HELP_HEADER.send(sender, locale, page, lastPage);

        for(SuperiorCommand _subCommand : subCommands) {
            if(_subCommand.displayCommand() && (_subCommand.getPermission().isEmpty() || sender.hasPermission(_subCommand.getPermission()))) {
                String description = _subCommand.getDescription(locale);
                if(description == null)
                    new NullPointerException("The description of the command " + _subCommand.getAliases().get(0) + " is null.").printStackTrace();
                Locale.ADMIN_HELP_LINE.send(sender, locale, plugin.getCommands().getLabel() + " " + _subCommand.getUsage(locale), description);
            }
        }

        if(page != lastPage)
            Locale.ADMIN_HELP_NEXT_PAGE.send(sender, locale, page + 1);
        else
            Locale.ADMIN_HELP_FOOTER.send(sender, locale);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();

        if(args.length > 1){
            SuperiorCommand command = plugin.getCommands().getAdminCommand(args[1]);
            if(command != null){
                return command.getPermission() != null && !sender.hasPermission(command.getPermission()) ?
                        new ArrayList<>() : command.tabComplete(plugin, sender, args);
            }
        }

        if(args.length != 1) {
            for (SuperiorCommand subCommand : plugin.getCommands().getAdminSubCommands()) {
                if (subCommand.displayCommand() && (subCommand.getPermission() == null || sender.hasPermission(subCommand.getPermission()))) {
                    List<String> aliases = new ArrayList<>(subCommand.getAliases());
                    aliases.addAll(plugin.getSettings().getCommandAliases().getOrDefault(aliases.get(0).toLowerCase(), new ArrayList<>()));
                    for (String _aliases : aliases) {
                        if (_aliases.contains(args[1].toLowerCase())) {
                            list.add(_aliases);
                        }
                    }
                }
            }
        }

        return list;
    }

    private boolean isNumber(String str){
        try{
            Integer.valueOf(str);
            return true;
        }catch(NumberFormatException ex){
            return false;
        }
    }

}
