package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.utils.LocationUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdAdminSetSpawn implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setspawn");
    }

    @Override
    public String getPermission() {
        return "superior.admin.setspawn";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin setspawn";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_SET_SPAWN.getMessage(locale);
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
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Location playerLocation = player.getLocation();

        Location spawnLocation = new Location(player.getWorld(), playerLocation.getBlockX(), playerLocation.getBlockY(),
                playerLocation.getBlockZ(), playerLocation.getYaw(), playerLocation.getPitch());

        String newSpawnLocation = LocationUtils.getLocation(spawnLocation).replace(",", ", ");

        try {
            plugin.getSettings().updateValue("spawn.location", newSpawnLocation);
            plugin.getGrid().updateSpawn();
        }catch (Exception ex){
            ex.printStackTrace();
        }

        Locale.SPAWN_SET_SUCCESS.send(sender, newSpawnLocation);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

}
