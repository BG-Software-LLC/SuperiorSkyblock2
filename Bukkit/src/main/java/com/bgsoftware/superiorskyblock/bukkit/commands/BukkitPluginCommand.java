package com.bgsoftware.superiorskyblock.bukkit.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventType;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

import java.util.List;

public class BukkitPluginCommand extends BukkitCommand {

    private final SuperiorSkyblockPlugin plugin;

    public BukkitPluginCommand(SuperiorSkyblockPlugin plugin, String islandCommandLabel) {
        super(islandCommandLabel);
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] commandArgs) {
        PluginEventArgs.CommandExecute args = new PluginEventArgs.CommandExecute();
        args.sender = sender;
        args.args = commandArgs;
        plugin.getPluginEventsDispatcher().fireEvent(PluginEventType.COMMAND_EXECUTE_EVENT, args);
        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String label, String[] commandArgs) {
        PluginEventArgs.CommandTabComplete args = new PluginEventArgs.CommandTabComplete();
        args.sender = sender;
        args.args = commandArgs;
        plugin.getPluginEventsDispatcher().fireEvent(PluginEventType.COMMAND_TAB_COMPLETE_EVENT, args);
        return args.tabCompletes;
    }

}
