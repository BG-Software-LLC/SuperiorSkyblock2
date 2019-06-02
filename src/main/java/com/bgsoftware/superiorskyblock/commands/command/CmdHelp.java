package com.bgsoftware.superiorskyblock.commands.command;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.commands.CommandsHandler;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdHelp implements ICommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("help");
    }

    @Override
    public String getPermission() {
        return "superior.island.help";
    }

    @Override
    public String getUsage() {
        return "island help";
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public int getMaxArgs() {
        return 1;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        List<ICommand> subCommands = CommandsHandler.getSubCommands();

        for(ICommand subCommand : subCommands){
            if(subCommand.getPermission().isEmpty() || sender.hasPermission(subCommand.getPermission())){
                Locale.ISLAND_HELP_HEADER.send(sender);

                for(ICommand _subCommand : subCommands) {
                    if(_subCommand.getPermission().isEmpty() || sender.hasPermission(_subCommand.getPermission())) {
                        Locale.ISLAND_HELP_LINE.send(sender, _subCommand.getUsage(), getPermissionDescription(plugin, _subCommand.getPermission()));
                    }
                }

                Locale.ISLAND_HELP_FOOTER.send(sender);

                return;
            }
        }

        Locale.NO_COMMAND_PERMISSION.send(sender);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

    private String getPermissionDescription(SuperiorSkyblockPlugin plugin, String permission){
        List<Permission> permissions = plugin.getDescription().getPermissions();

        for(Permission _permission : permissions) {
            if (_permission.getName().equals(permission))
                return _permission.getDescription();
        }

        return "";
    }

}
