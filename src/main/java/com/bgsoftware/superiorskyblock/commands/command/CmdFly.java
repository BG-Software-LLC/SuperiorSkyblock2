package com.bgsoftware.superiorskyblock.commands.command;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public final class CmdFly implements ICommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("fly");
    }

    @Override
    public String getPermission() {
        return "superior.island.fly";
    }

    @Override
    public String getUsage() {
        return "is fly";
    }

    @Override
    public String getDescription() {
        return "Toggle island fly.";
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
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(sender);
        Island island = plugin.getGrid().getIslandAt(superiorPlayer.getLocation());
        Player player = superiorPlayer.asPlayer();

        if(superiorPlayer.hasIslandFlyEnabled()){
            Locale.TOGGLED_FLY_OFF.send(superiorPlayer);
            if(island != null) {
                player.setAllowFlight(false);
                player.setFlying(false);
            }
        }
        else{
            Locale.TOGGLED_FLY_ON.send(superiorPlayer);
            if(island != null) {
                player.setAllowFlight(true);
                player.setFlying(true);
            }
        }

        superiorPlayer.toggleIslandFly();
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return null;
    }
}
