package com.bgsoftware.superiorskyblock.commands.command.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public final class CmdAdminReload implements ICommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("reload");
    }

    @Override
    public String getPermission() {
        return "superior.admin.reload";
    }

    @Override
    public String getUsage() {
        return "island admin reload";
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
        new Thread(() -> {
            Locale.RELOAD_PROCCESS_REQUEST.send(sender);
            plugin.reloadPlugin(false);
            Locale.RELOAD_COMPLETED.send(sender);
        }).start();
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return null;
    }
}
