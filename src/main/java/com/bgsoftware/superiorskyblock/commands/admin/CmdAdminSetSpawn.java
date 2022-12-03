package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.core.serialization.Serializers;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class CmdAdminSetSpawn implements ISuperiorCommand {

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
        return Message.COMMAND_DESCRIPTION_ADMIN_SET_SPAWN.getMessage(locale);
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

        String newSpawnLocation = Serializers.LOCATION_SPACED_SERIALIZER.serialize(spawnLocation);

        try {
            plugin.getSettings().updateValue("spawn.location", newSpawnLocation);
            plugin.getGrid().updateSpawn();
        } catch (Exception error) {
            Log.entering("ENTER", spawnLocation);
            Log.error(error, "An unexpected error occurred while setting spawn:");
        }

        Message.SPAWN_SET_SUCCESS.send(sender, newSpawnLocation);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

}
