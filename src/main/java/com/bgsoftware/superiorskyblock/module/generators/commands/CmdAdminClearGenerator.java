package com.bgsoftware.superiorskyblock.module.generators.commands;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public final class CmdAdminClearGenerator implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("cleargenerator", "cg");
    }

    @Override
    public String getPermission() {
        return "superior.admin.cleargenerator";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin cleargenerator <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> [" +
                Locale.COMMAND_ARGUMENT_WORLD.getMessage(locale) + "]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_CLEAR_GENERATOR.getMessage(locale);
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
        return true;
    }

    @Override
    public boolean supportMultipleIslands() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, SuperiorPlayer targetPlayer, List<Island> islands, String[] args) {
        World.Environment environment = args.length == 3 ? plugin.getSettings().getWorlds().getDefaultWorld() :
                CommandArguments.getEnvironment(sender, args[3]);

        if(environment == null)
            return;

        Executor.data(() -> islands.forEach(island -> island.clearGeneratorAmounts(environment)));

        if(islands.size() != 1)
            Locale.GENERATOR_CLEARED_ALL.send(sender);
        else if(targetPlayer == null)
            Locale.GENERATOR_CLEARED_NAME.send(sender, islands.get(0).getName());
        else
            Locale.GENERATOR_CLEARED.send(sender, targetPlayer.getName());
    }

}
