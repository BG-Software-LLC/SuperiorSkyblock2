package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdVisitors implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("visitors");
    }

    @Override
    public String getPermission() {
        return "superior.island.panel";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "visitors";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_VISITORS.getMessage(locale);
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
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        plugin.getCommands().dispatchSubCommand(sender, "panel", "visitors");
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

}
