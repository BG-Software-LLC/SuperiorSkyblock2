package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.core.errors.ManagerLoadException;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdAdminReload implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("reload");
    }

    @Override
    public String getPermission() {
        return "superior.admin.reload";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin reload";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_RELOAD.getMessage(locale);
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
        Message.RELOAD_PROCCESS_REQUEST.send(sender);

        try {
            plugin.reloadPlugin(false);
        } catch (ManagerLoadException error) {
            Log.error(error, "An unexpected error occurred while reloading the plugin:");
        }

        Message.RELOAD_COMPLETED.send(sender);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

}
