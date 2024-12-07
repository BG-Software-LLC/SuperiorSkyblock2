package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.IPermissibleCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class CmdExpel implements IPermissibleCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("expel");
    }

    @Override
    public String getPermission() {
        return "superior.island.expel";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "expel <" + Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_EXPEL.getMessage(locale);
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
    public IslandPrivilege getPrivilege() {
        return IslandPrivileges.EXPEL_PLAYERS;
    }

    @Override
    public Message getPermissionLackMessage() {
        return Message.NO_EXPEL_PERMISSION;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island playerIsland, String[] args) {
        CommandSender sender = superiorPlayer == null ? Bukkit.getConsoleSender() : superiorPlayer.asPlayer();
        SuperiorPlayer targetPlayer = CommandArguments.getPlayer(plugin, sender, args[1]);

        if (targetPlayer == null || sender == null)
            return;

        Player target = targetPlayer.asPlayer();

        if (target == null) {
            Message.INVALID_PLAYER.send(sender, args[1]);
            return;
        }

        Island targetIsland;
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            targetIsland = plugin.getGrid().getIslandAt(target.getLocation(wrapper.getHandle()));
        }

        if (targetIsland == null) {
            Message.PLAYER_NOT_INSIDE_ISLAND.send(sender);
            return;
        }

        // Checking requirements for players
        if (superiorPlayer != null) {
            if (!targetIsland.equals(playerIsland)) {
                Message.PLAYER_NOT_INSIDE_ISLAND.send(sender);
                return;
            }

            if (targetIsland.hasPermission(targetPlayer, IslandPrivileges.EXPEL_BYPASS)) {
                Message.PLAYER_EXPEL_BYPASS.send(sender);
                return;
            }
        }

        targetPlayer.teleport(plugin.getGrid().getSpawnIsland());
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            target.getLocation(wrapper.getHandle()).setDirection(plugin.getGrid().getSpawnIsland()
                    .getCenter(plugin.getSettings().getWorlds().getDefaultWorldDimension()).getDirection());
        }
        Message.EXPELLED_PLAYER.send(sender, targetPlayer.getName());
        Message.GOT_EXPELLED.send(targetPlayer, sender.getName());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        if (args.length != 2)
            return Collections.emptyList();

        if (island != null)
            return CommandTabCompletes.getIslandVisitors(island, args[1], plugin.getSettings().isTabCompleteHideVanished());

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            return CommandTabCompletes.getOnlinePlayers(plugin, args[1], plugin.getSettings().isTabCompleteHideVanished(),
                    onlinePlayer -> plugin.getGrid().getIslandAt(onlinePlayer.getLocation(wrapper.getHandle())) != null);
        }
    }

}
