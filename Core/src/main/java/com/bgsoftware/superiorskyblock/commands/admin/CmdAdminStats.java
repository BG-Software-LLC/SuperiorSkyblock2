package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdAdminStats implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("stats");
    }

    @Override
    public String getPermission() {
        return "superior.admin.stats";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin stats";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_STATS.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 2;
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
        sender.sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "SuperiorSkyblock" + ChatColor.GRAY +
                " Stats:\n" + " - Islands: " + plugin.getGrid().getSize() + "\n");
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

}
