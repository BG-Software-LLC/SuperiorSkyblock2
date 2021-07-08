package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.listeners.PlayersListener;
import com.bgsoftware.superiorskyblock.utils.commands.CommandTabCompletes;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CmdAdminTeleport implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("tp", "teleport", "go", "visit");
    }

    @Override
    public String getPermission() {
        return "superior.admin.teleport";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin teleport <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "> [nether/the_end]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_TELEPORT.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 3;
    }

    @Override
    public int getMaxArgs() {
        return 4;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public boolean supportMultipleIslands() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, SuperiorPlayer targetPlayer, Island island, String[] args) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);

        World.Environment environment = args.length == 4 ? World.Environment.valueOf(args[3].toUpperCase()) : World.Environment.NORMAL;

        if(environment != World.Environment.NORMAL){
            if(!island.wasSchematicGenerated(environment)) {
                PlayersListener.handlePlayerPortal(plugin, (Player) sender, ((Player) sender).getLocation(),
                        environment == World.Environment.NETHER ? PlayerTeleportEvent.TeleportCause.NETHER_PORTAL :
                                PlayerTeleportEvent.TeleportCause.END_PORTAL, null);
                return;
            }
        }

        Location visitLocation = island.getTeleportLocation(environment);
        superiorPlayer.teleport(visitLocation);
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        return args.length == 4 ? CommandTabCompletes.getEnvironments(args[3]) : new ArrayList<>();
    }

}
