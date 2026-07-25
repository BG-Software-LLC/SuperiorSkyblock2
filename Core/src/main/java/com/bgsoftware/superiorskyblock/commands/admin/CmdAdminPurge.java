package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CmdAdminPurge implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("purge");
    }

    @Override
    public String getPermission() {
        return "superior.admin.purge";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin purge <cancel/" + Message.COMMAND_ARGUMENT_TIME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_PURGE.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 3;
    }

    @Override
    public int getMaxArgs() {
        return 3;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        if (args[2].equalsIgnoreCase("cancel")) {
            plugin.getGrid().getIslandsToPurge().forEach(island -> plugin.getGrid().removeIslandFromPurge(island));
            Message.PURGE_CLEAR.send(sender);
        } else {
            long timeToPurge = parseLongSafe(args[2]);
            long currentTime = System.currentTimeMillis() / 1000;

            List<Island> islands = new SequentialListBuilder<Island>().filter(island -> {
                long lastTimeUpdate = island.getLastTimeUpdate();
                return lastTimeUpdate != -1 && currentTime - lastTimeUpdate >= timeToPurge;
            }).build(plugin.getGrid().getIslands());

            if (islands.isEmpty()) {
                Message.NO_ISLANDS_TO_PURGE.send(sender);
            } else {
                BukkitExecutor.async(() -> islands.forEach(island -> plugin.getGrid().addIslandToPurge(island)));
                Message.PURGED_ISLANDS.send(sender, islands.size());
            }
        }
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return args.length == 3 ? CommandTabCompletes.getCustomComplete(args[2], "cancel") : Collections.emptyList();
    }

    private static long parseLongSafe(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception error) {
            return 0;
        }
    }

}
