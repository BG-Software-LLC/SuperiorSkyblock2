package com.ome_r.superiorskyblock.commands;

import com.ome_r.superiorskyblock.Locale;
import com.ome_r.superiorskyblock.SuperiorSkyblock;
import com.ome_r.superiorskyblock.commands.command.CmdAccept;
import com.ome_r.superiorskyblock.commands.command.CmdAdmin;
import com.ome_r.superiorskyblock.commands.command.CmdBan;
import com.ome_r.superiorskyblock.commands.command.CmdDelWarp;
import com.ome_r.superiorskyblock.commands.command.CmdSetDiscord;
import com.ome_r.superiorskyblock.commands.command.CmdSetPaypal;
import com.ome_r.superiorskyblock.commands.command.CmdSetWarp;
import com.ome_r.superiorskyblock.commands.command.CmdTeam;
import com.ome_r.superiorskyblock.commands.command.CmdTeamChat;
import com.ome_r.superiorskyblock.commands.command.CmdToggle;
import com.ome_r.superiorskyblock.commands.command.CmdCreate;
import com.ome_r.superiorskyblock.commands.command.CmdDemote;
import com.ome_r.superiorskyblock.commands.command.CmdDeposit;
import com.ome_r.superiorskyblock.commands.command.CmdDisband;
import com.ome_r.superiorskyblock.commands.command.CmdExpel;
import com.ome_r.superiorskyblock.commands.command.CmdInvite;
import com.ome_r.superiorskyblock.commands.command.CmdKick;
import com.ome_r.superiorskyblock.commands.command.CmdLeave;
import com.ome_r.superiorskyblock.commands.command.CmdPanel;
import com.ome_r.superiorskyblock.commands.command.CmdPermissions;
import com.ome_r.superiorskyblock.commands.command.CmdPromote;
import com.ome_r.superiorskyblock.commands.command.CmdRecalc;
import com.ome_r.superiorskyblock.commands.command.CmdBiome;
import com.ome_r.superiorskyblock.commands.command.CmdSetPermission;
import com.ome_r.superiorskyblock.commands.command.CmdSetRole;
import com.ome_r.superiorskyblock.commands.command.CmdShow;
import com.ome_r.superiorskyblock.commands.command.CmdTeleport;
import com.ome_r.superiorskyblock.commands.command.CmdTop;
import com.ome_r.superiorskyblock.commands.command.CmdUpgrade;
import com.ome_r.superiorskyblock.commands.command.CmdValue;
import com.ome_r.superiorskyblock.commands.command.CmdWarp;
import com.ome_r.superiorskyblock.commands.command.CmdWithdraw;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import java.util.ArrayList;
import java.util.List;

public class CommandsHandler implements CommandExecutor, TabCompleter {

    private SuperiorSkyblock plugin;
    private List<ICommand> subCommands = new ArrayList<>();

    public CommandsHandler(SuperiorSkyblock plugin){
        this.plugin = plugin;
        subCommands.add(new CmdAccept());
        subCommands.add(new CmdAdmin());
        subCommands.add(new CmdBan());
        subCommands.add(new CmdBiome());
        subCommands.add(new CmdCreate());
        subCommands.add(new CmdDelWarp());
        subCommands.add(new CmdDemote());
        subCommands.add(new CmdDeposit());
        subCommands.add(new CmdDisband());
        subCommands.add(new CmdExpel());
        subCommands.add(new CmdInvite());
        subCommands.add(new CmdKick());
        subCommands.add(new CmdLeave());
        subCommands.add(new CmdPanel());
        subCommands.add(new CmdPermissions());
        subCommands.add(new CmdPromote());
        subCommands.add(new CmdRecalc());
        subCommands.add(new CmdSetDiscord());
        subCommands.add(new CmdSetPaypal());
        subCommands.add(new CmdSetPermission());
        subCommands.add(new CmdSetRole());
        subCommands.add(new CmdSetWarp());
        subCommands.add(new CmdShow());
        subCommands.add(new CmdTeam());
        subCommands.add(new CmdTeamChat());
        subCommands.add(new CmdTeleport());
        subCommands.add(new CmdToggle());
        subCommands.add(new CmdTop());
        subCommands.add(new CmdUpgrade());
        subCommands.add(new CmdValue());
        subCommands.add(new CmdWarp());
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

        for(ICommand subCommand : subCommands){
            if(subCommand.getPermission().isEmpty() || sender.hasPermission(subCommand.getPermission())){
                Locale.ISLAND_HELP_HEADER.send(sender);

                for(ICommand _subCommand : subCommands) {
                    if(_subCommand.getPermission().isEmpty() || sender.hasPermission(_subCommand.getPermission())) {
                        Locale.ISLAND_HELP_LINE.send(sender, _subCommand.getUsage(), getPermissionDescription(_subCommand.getPermission()));
                    }
                }

                Locale.ISLAND_HELP_FOOTER.send(sender);

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

    private String getPermissionDescription(String permission){
        List<Permission> permissions = plugin.getDescription().getPermissions();

        for(Permission _permission : permissions) {
            if (_permission.getName().equals(permission))
                return _permission.getDescription();
        }

        return "";
    }

}
