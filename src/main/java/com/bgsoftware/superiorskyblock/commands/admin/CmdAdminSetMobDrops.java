package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.utils.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.Locale;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public final class CmdAdminSetMobDrops implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setmobdrops");
    }

    @Override
    public String getPermission() {
        return "superior.admin.setmobdrops";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin setmobdrops <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_MULTIPLIER.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_SET_MOB_DROPS.getMessage(locale);
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

        Executor.data(() -> islands.forEach(island -> island.setMobDropsMultiplier(multiplier)));

        if(islands.size() > 1)
            Locale.CHANGED_MOB_DROPS_ALL.send(sender);
        else if(targetPlayer == null)
            Locale.CHANGED_MOB_DROPS_NAME.send(sender, islands.get(0).getName());
        else
            Locale.CHANGED_MOB_DROPS.send(sender, targetPlayer.getName());
    }

}
