package com.bgsoftware.superiorskyblock.commands.command;

import com.bgsoftware.superiorskyblock.commands.command.admin.*;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdAdmin implements ICommand {

    private SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private List<ICommand> subCommands = new ArrayList<>();

    public CmdAdmin(){
        subCommands.add(new CmdAdminBonus());
        subCommands.add(new CmdAdminBypass());
        subCommands.add(new CmdAdminDemote());
        subCommands.add(new CmdAdminDeposit());
        subCommands.add(new CmdAdminDisband());
        subCommands.add(new CmdAdminJoin());
        subCommands.add(new CmdAdminMsg());
        subCommands.add(new CmdAdminMsgAll());
        subCommands.add(new CmdAdminPromote());
        subCommands.add(new CmdAdminReload());
        subCommands.add(new CmdAdminSave());
        subCommands.add(new CmdAdminSchematic());
        subCommands.add(new CmdAdminSetCropGrowth());
        subCommands.add(new CmdAdminSetHoppersLimit());
        subCommands.add(new CmdAdminSetMobDrops());
        subCommands.add(new CmdAdminSetSize());
        subCommands.add(new CmdAdminSetSpawnerRates());
        subCommands.add(new CmdAdminSetTeamLimit());
        subCommands.add(new CmdAdminSetUpgrade());
        subCommands.add(new CmdAdminWithdraw());
        subCommands.add(new CmdAdminSetDisbands());
        subCommands.add(new CmdAdminGiveDisbands());
        subCommands.add(new CmdAdminSetWarpsLimit());
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
    public String getUsage() {
        return "island admin";
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
        if(args.length > 1){
            for(ICommand subCommand : subCommands){
                if(subCommand.getAliases().contains(args[1].toLowerCase())){
                    if(!(sender instanceof Player) && !subCommand.canBeExecutedByConsole()){
                        Locale.sendMessage(sender, "&cCan be executed only by players!");
                        return;
                    }

                    if(!subCommand.getPermission().isEmpty() && !sender.hasPermission(subCommand.getPermission())) {
                        Locale.NO_COMMAND_PERMISSION.send(sender);
                        return;
                    }

                    if(args.length < subCommand.getMinArgs() || args.length > subCommand.getMaxArgs()){
                        Locale.COMMAND_USAGE.send(sender, subCommand.getUsage());
                        return;
                    }

                    subCommand.execute(plugin, sender, args);
                    subCommand.tabComplete(plugin, sender, args);
                    return;
                }
            }
        }

        for(ICommand subCommand : subCommands){
            if(subCommand.getPermission().isEmpty() || sender.hasPermission(subCommand.getPermission())){
                Locale.ADMIN_HELP_HEADER.send(sender);

                for(ICommand _subCommand : subCommands) {
                    if(_subCommand.getPermission().isEmpty() || sender.hasPermission(_subCommand.getPermission())) {
                        Locale.ADMIN_HELP_LINE.send(sender, _subCommand.getUsage(), getPermissionDescription(_subCommand.getPermission()));
                    }
                }

                Locale.ADMIN_HELP_FOOTER.send(sender);

                return;
            }
        }

        Locale.NO_COMMAND_PERMISSION.send(sender);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();

        if(args.length > 1){
            for(ICommand subCommand : subCommands) {
                if (subCommand.getAliases().contains(args[1].toLowerCase())){
                    if(subCommand.getPermission() != null && !sender.hasPermission(subCommand.getPermission())){
                        return new ArrayList<>();
                    }

                    list = subCommand.tabComplete(plugin, sender, args);

                    if(list == null) {
                        list = new ArrayList<>();
                        if (args.length == 3) {
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                list.add(player.getName());
                            }
                        }
                    }

                    return list;
                }
            }
        }

        if(args.length != 1) {
            for (ICommand subCommand : subCommands) {
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

    private String getPermissionDescription(String permission){
        List<Permission> permissions = plugin.getDescription().getPermissions();

        for(Permission _permission : permissions) {
            if (_permission.getName().equals(permission))
                return _permission.getDescription();
        }

        return "";
    }

}
