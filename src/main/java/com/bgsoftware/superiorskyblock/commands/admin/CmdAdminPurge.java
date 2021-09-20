package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class CmdAdminPurge implements ISuperiorCommand {

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
        return "admin purge <cancel/" + Locale.COMMAND_ARGUMENT_TIME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_PURGE.getMessage(locale);
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
        if(args[2].equalsIgnoreCase("cancel")){
            plugin.getGrid().getIslandsToPurge().forEach(island -> plugin.getGrid().removeIslandFromPurge(island));
            Locale.PURGE_CLEAR.send(sender);
        }

        else {
            long timeToPurge = StringUtils.parseLong(args[2]), currentTime = System.currentTimeMillis() / 1000;

            List<Island> islands = plugin.getGrid().getIslands().stream().filter(island -> {
                long lastTimeUpdate = island.getLastTimeUpdate();
                return lastTimeUpdate != -1 && currentTime - lastTimeUpdate >= timeToPurge;
            }).collect(Collectors.toList());

            if(islands.isEmpty()){
                Locale.NO_ISLANDS_TO_PURGE.send(sender);
            }

            else {
                Executor.async(() -> islands.forEach(island -> plugin.getGrid().addIslandToPurge(island)));
                Locale.PURGED_ISLANDS.send(sender, islands.size());
            }
        }
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return args.length == 3 ? CommandTabCompletes.getCustomComplete(args[2], "cancel") : new ArrayList<>();
    }

}
