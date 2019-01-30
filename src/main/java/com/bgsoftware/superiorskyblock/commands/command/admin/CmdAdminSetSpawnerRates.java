package com.bgsoftware.superiorskyblock.commands.command.admin;

import com.bgsoftware.superiorskyblock.wrappers.WrappedPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import com.bgsoftware.superiorskyblock.island.Island;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdAdminSetSpawnerRates implements ICommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setspawnerrates");
    }

    @Override
    public String getPermission() {
        return "superior.admin.setspawnerrates";
    }

    @Override
    public String getUsage() {
        return "island admin setspawnerrates <player-name> <multiplier>";
    }

    @Override
    public int getMinArgs() {
        return 4;
    }

    @Override
    public int getMaxArgs() {
        return 4;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        WrappedPlayer targetPlayer = WrappedPlayer.of(args[2]);

        if(targetPlayer == null){
            Locale.INVALID_PLAYER.send(sender, args[2]);
            return;
        }

        Island island = targetPlayer.getIsland();

        if(island == null){
            Locale.INVALID_ISLAND_OTHER.send(sender, targetPlayer.getName());
            return;
        }

        double multiplier;

        try{
            multiplier = Double.valueOf(args[3]);
        }catch(IllegalArgumentException ex){
            Locale.INVALID_MULTIPLIER.send(sender, args[3]);
            return;
        }

        island.setSpawnerRatesMultiplier(multiplier);
        Locale.CHANGED_SPAWNER_RATES.send(sender, targetPlayer.getName());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        return null;
    }
}
