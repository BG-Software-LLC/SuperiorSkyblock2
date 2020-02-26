package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.handlers.CommandsHandler;
import com.bgsoftware.superiorskyblock.commands.admin.*;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.utils.LocaleUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class CmdAdmin implements ISuperiorCommand {

    private final Map<String, SuperiorCommand> subCommands = new LinkedHashMap<>();
    private final Map<String, SuperiorCommand> aliasesToCommand = new HashMap<>();
    private final CommandsHandler commandsHandler;

    public CmdAdmin(CommandsHandler commandsHandler){
        this.commandsHandler = commandsHandler;
        registerCommand(new CmdAdminBonus(), false);
        registerCommand(new CmdAdminBypass(), false);
        registerCommand(new CmdAdminClose(), false);
        registerCommand(new CmdAdminDemote(), false);
        registerCommand(new CmdAdminDeposit(), false);
        registerCommand(new CmdAdminDisband(), false);
        registerCommand(new CmdAdminGiveDisbands(), false);
        registerCommand(new CmdAdminIgnore(), false);
        registerCommand(new CmdAdminJoin(), false);
        registerCommand(new CmdAdminMission(), false);
        registerCommand(new CmdAdminMsg(), false);
        registerCommand(new CmdAdminMsgAll(), false);
        registerCommand(new CmdAdminName(), false);
        registerCommand(new CmdAdminOpen(), false);
        registerCommand(new CmdAdminPromote(), false);
        registerCommand(new CmdAdminPurge(), false);
        registerCommand(new CmdAdminRecalc(), false);
        registerCommand(new CmdAdminReload(), false);
        registerCommand(new CmdAdminRemoveRatings(), false);
        registerCommand(new CmdAdminSchematic(), false);
        registerCommand(new CmdAdminSetBlockLimit(), false);
        registerCommand(new CmdAdminSetCropGrowth(), false);
        registerCommand(new CmdAdminSetLeader(), false);
        registerCommand(new CmdAdminSetMobDrops(), false);
        registerCommand(new CmdAdminSetPermission(), false);
        registerCommand(new CmdAdminSetDisbands(), false);
        registerCommand(new CmdAdminSetGenerator(), false);
        registerCommand(new CmdAdminSetRate(), false);
        registerCommand(new CmdAdminSetSettings(), false);
        registerCommand(new CmdAdminSetSize(), false);
        registerCommand(new CmdAdminSetSpawnerRates(), false);
        registerCommand(new CmdAdminSetTeamLimit(), false);
        registerCommand(new CmdAdminSetUpgrade(), false);
        registerCommand(new CmdAdminSetWarpsLimit(), false);
        registerCommand(new CmdAdminShow(), false);
        registerCommand(new CmdAdminSpy(), false);
        registerCommand(new CmdAdminTeleport(), false);
        registerCommand(new CmdAdminUnignore(), false);
        registerCommand(new CmdAdminUnlockWorld(), false);
        registerCommand(new CmdAdminWithdraw(), false);
    }

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
            SuperiorCommand command = getCommand(args[1]);
            if(command != null){
                if(!(sender instanceof Player) && !command.canBeExecutedByConsole()){
                    Locale.sendMessage(sender, "&cCan be executed only by players!");
                    return;
                }

                if(!command.getPermission().isEmpty() && !sender.hasPermission(command.getPermission())) {
                    Locale.NO_COMMAND_PERMISSION.send(sender, locale);
                    return;
                }

                if(args.length < command.getMinArgs() || args.length > command.getMaxArgs()){
                    Locale.COMMAND_USAGE.send(sender, locale, commandsHandler.getLabel() + " " + command.getUsage(locale));
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

        List<SuperiorCommand> subCommands = this.subCommands.values().stream()
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
            if(_subCommand.getPermission().isEmpty() || sender.hasPermission(_subCommand.getPermission())) {
                String description = _subCommand.getDescription(locale);
                if(description == null)
                    new NullPointerException("The description of the command " + _subCommand.getAliases().get(0) + " is null.").printStackTrace();
                Locale.ADMIN_HELP_LINE.send(sender, locale, commandsHandler.getLabel() + " " + _subCommand.getUsage(locale), description);
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
            SuperiorCommand command = getCommand(args[1]);
            if(command != null){
                return command.getPermission() != null && !sender.hasPermission(command.getPermission()) ?
                        new ArrayList<>() : command.tabComplete(plugin, sender, args);
            }
        }

        if(args.length != 1) {
            for (SuperiorCommand subCommand : subCommands.values()) {
                if (subCommand.getPermission() == null || sender.hasPermission(subCommand.getPermission())) {
                    for (String aliases : subCommand.getAliases()) {
                        if (aliases.startsWith(args[1].toLowerCase())) {
                            list.add(aliases);
                            break;
                        }
                    }
                }
            }
        }

        return list;
    }

    public void registerCommand(SuperiorCommand superiorCommand, boolean sort) {
        List<String> aliases = superiorCommand.getAliases();
        subCommands.put(aliases.get(0).toLowerCase(), superiorCommand);
        for(int i = 1; i < aliases.size(); i++){
            aliasesToCommand.put(aliases.get(i).toLowerCase(), superiorCommand);
        }
        if(sort){
            List<SuperiorCommand> superiorCommands = new ArrayList<>(subCommands.values());
            superiorCommands.sort(Comparator.comparing(o -> o.getAliases().get(0)));
            subCommands.clear();
            superiorCommands.forEach(s -> subCommands.put(s.getAliases().get(0), s));
        }
    }

    public List<SuperiorCommand> getSubCommands() {
        return Collections.unmodifiableList(new ArrayList<>(subCommands.values()));
    }

    private boolean isNumber(String str){
        try{
            Integer.valueOf(str);
            return true;
        }catch(NumberFormatException ex){
            return false;
        }
    }

    private SuperiorCommand getCommand(String label){
        label = label.toLowerCase();
        return subCommands.getOrDefault(label, aliasesToCommand.get(label));
    }

}
