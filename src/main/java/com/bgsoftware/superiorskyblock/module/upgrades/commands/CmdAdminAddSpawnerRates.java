package com.bgsoftware.superiorskyblock.module.upgrades.commands;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public final class CmdAdminAddSpawnerRates implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("addspawnerrates");
    }

    @Override
    public String getPermission() {
        return "superior.admin.addspawnerrates";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin addspawnerrates <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_MULTIPLIER.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_ADD_SPAWNER_RATES.getMessage(locale);
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
    public boolean supportMultipleIslands() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, SuperiorPlayer targetPlayer, List<Island> islands, String[] args) {
        Pair<Double, Boolean> arguments = CommandArguments.getMultiplier(sender, args[3]);

        if(!arguments.getValue())
            return;

        double multiplier = arguments.getKey();

        Executor.data(() -> islands.forEach(island -> island.setSpawnerRatesMultiplier(island.getSpawnerRatesMultiplier() + multiplier)));

        if(islands.size() > 1)
            Locale.CHANGED_SPAWNER_RATES_ALL.send(sender);
        else if(targetPlayer == null)
            Locale.CHANGED_SPAWNER_RATES_NAME.send(sender, islands.get(0).getName());
        else
            Locale.CHANGED_SPAWNER_RATES.send(sender, targetPlayer.getName());
    }

}
