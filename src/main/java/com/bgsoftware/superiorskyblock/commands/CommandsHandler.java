package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.command.*;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;

import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class CommandsHandler implements CommandExecutor, TabCompleter {

    private static CommandsHandler instance;

    private SuperiorSkyblockPlugin plugin;
    private List<ICommand> subCommands = new ArrayList<>();

    public CommandsHandler(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;
        instance = this;
        subCommands.add(new CmdAccept());
        subCommands.add(new CmdAdmin());
        subCommands.add(new CmdBan());
        subCommands.add(new CmdBiome());
        subCommands.add(new CmdClose());
        subCommands.add(new CmdCreate());
        subCommands.add(new CmdDelWarp());
        subCommands.add(new CmdDemote());
        subCommands.add(new CmdDeposit());
        subCommands.add(new CmdDisband());
        subCommands.add(new CmdExpel());
        subCommands.add(new CmdHelp());
        subCommands.add(new CmdInvite());
        subCommands.add(new CmdKick());
        subCommands.add(new CmdLeave());
        subCommands.add(new CmdOpen());
        subCommands.add(new CmdPanel());
        subCommands.add(new CmdPardon());
        subCommands.add(new CmdPermissions());
        subCommands.add(new CmdPromote());
        subCommands.add(new CmdRankup());
        subCommands.add(new CmdRecalc());
        subCommands.add(new CmdSetDiscord());
        subCommands.add(new CmdSetPaypal());
        subCommands.add(new CmdSetPermission());
        subCommands.add(new CmdSetRole());
        subCommands.add(new CmdSetTeleport());
        subCommands.add(new CmdSetWarp());
        subCommands.add(new CmdShow());
        subCommands.add(new CmdTeam());
        subCommands.add(new CmdTeamChat());
        subCommands.add(new CmdTeleport());
        subCommands.add(new CmdToggle());
        subCommands.add(new CmdTop());
        subCommands.add(new CmdTransfer());
        subCommands.add(new CmdUpgrade());
        subCommands.add(new CmdValue());
        subCommands.add(new CmdWarp());
        subCommands.add(new CmdWarps());
        subCommands.add(new CmdWithdraw());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length > 0){
            for(ICommand subCommand : subCommands){
                if(subCommand.getAliases().contains(args[0].toLowerCase())){
                    if(!(sender instanceof Player) && !subCommand.canBeExecutedByConsole()){
                        Locale.sendMessage(sender, "&cCan be executed only by players!");
                        return false;
                    }

                    if(!subCommand.getPermission().isEmpty() && !sender.hasPermission(subCommand.getPermission())) {
                        Locale.NO_COMMAND_PERMISSION.send(sender);
                        return false;
                    }

                    if(args.length < subCommand.getMinArgs() || args.length > subCommand.getMaxArgs()){
                        Locale.COMMAND_USAGE.send(sender, subCommand.getUsage());
                        return false;
                    }

                    subCommand.execute(plugin, sender, args);
                    subCommand.tabComplete(plugin, sender, args);
                    return false;
                }
            }
        }

        if(sender instanceof Player){
            SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(sender);

            if(superiorPlayer != null){
                Island island = superiorPlayer.getIsland();

                if(island == null){
                    Bukkit.dispatchCommand(sender, "is create");
                }
                else if(superiorPlayer.hasToggledPanel()){
                    Bukkit.dispatchCommand(sender, "is panel");
                }
                else{
                    Bukkit.dispatchCommand(sender, "is tp");
                }

                return false;
            }
        }

        Locale.NO_COMMAND_PERMISSION.send(sender);

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length > 0){
            for(ICommand subCommand : subCommands) {
                if (subCommand.getAliases().contains(args[0].toLowerCase())){
                    if(subCommand.getPermission() != null && !sender.hasPermission(subCommand.getPermission())){
                        return new ArrayList<>();
                    }
                    return subCommand.tabComplete(plugin, sender, args);
                }
            }
        }

        List<String> list = new ArrayList<>();

        for(ICommand subCommand : subCommands) {
            if (subCommand.getPermission() == null || sender.hasPermission(subCommand.getPermission())) {
                for (String aliases : subCommand.getAliases()) {
                    if (aliases.startsWith(args[0].toLowerCase())) {
                        list.add(aliases);
                        break;
                    }
                }
            }
        }

        return list;
    }

    public static List<ICommand> getSubCommands(){
        return instance.subCommands;
    }


}
