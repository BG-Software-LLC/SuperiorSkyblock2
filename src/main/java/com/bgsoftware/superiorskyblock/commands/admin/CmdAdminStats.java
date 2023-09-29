package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.commands.InternalSuperiorCommand;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.ChatColor;

import java.util.Collections;
import java.util.List;

public class CmdAdminStats implements InternalSuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("stats");
    }

    @Override
    public String getPermission() {
        return "superior.admin.stats";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_STATS.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments()

    {
        return Collections.emptyList();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandContext context) {
        context.getDispatcher().sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "SuperiorSkyblock" + ChatColor.GRAY +
                " Stats:\n" + " - Islands: " + plugin.getGrid().getSize() + "\n");
    }

}
