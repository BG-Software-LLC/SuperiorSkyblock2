package com.bgsoftware.superiorskyblock.commands.command;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
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
        return "fly";
    }

    @Override
    public String getDescription() {
        return Locale.COMMAND_DESCRIPTION_FLY.getMessage();
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
            player.setAllowFlight(false);
            player.setFlying(false);

            Locale.TOGGLED_FLY_OFF.send(superiorPlayer);
        }
        else{
            if(island == null || !island.equals(superiorPlayer.getIsland())){
                Locale.TOGGLE_FLY_OUTSIDE_ISLAND.send(superiorPlayer);
                return;
            }

            player.setAllowFlight(true);
            player.setFlying(true);

            Locale.TOGGLED_FLY_ON.send(superiorPlayer);
        }

        superiorPlayer.toggleIslandFly();
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
