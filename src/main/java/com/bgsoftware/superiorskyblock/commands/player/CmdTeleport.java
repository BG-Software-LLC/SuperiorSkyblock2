package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.IslandArgument;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import org.bukkit.command.CommandSender;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CmdTeleport implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("tp", "teleport", "go", "home");
    }

    @Override
    public String getPermission() {
        return "superior.island.teleport";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "teleport";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_TELEPORT.getMessage(locale);
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
        IslandArgument arguments = CommandArguments.getSenderIsland(plugin, sender);

        Island island = arguments.getIsland();

        if (island == null)
            return;

        SuperiorPlayer superiorPlayer = arguments.getSuperiorPlayer();

        if (plugin.getSettings().getHomeWarmup() > 0 && !superiorPlayer.hasBypassModeEnabled() &&
                !superiorPlayer.hasPermission("superior.admin.bypass.warmup")) {
            Message.TELEPORT_WARMUP.send(superiorPlayer, Formatters.TIME_FORMATTER.format(
                    Duration.ofMillis(plugin.getSettings().getHomeWarmup()), superiorPlayer.getUserLocale()));
            superiorPlayer.setTeleportTask(BukkitExecutor.sync(() ->
                    teleportToIsland(superiorPlayer, island), plugin.getSettings().getHomeWarmup() / 50));
        } else {
            teleportToIsland(superiorPlayer, island);
        }
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    private void teleportToIsland(SuperiorPlayer superiorPlayer, Island island) {
        superiorPlayer.setTeleportTask(null);
        superiorPlayer.teleport(island, result -> {
            if (result)
                Message.TELEPORTED_SUCCESS.send(superiorPlayer);
            else
                Message.TELEPORTED_FAILED.send(superiorPlayer);
        });
    }

}
