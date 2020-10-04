package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.utils.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public final class CmdAdminAddSize implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("addsize");
    }

    @Override
    public String getPermission() {
        return "superior.admin.addsize";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin addsize <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_SIZE.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_ADD_SIZE.getMessage(locale);
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
        Pair<Integer, Boolean> arguments = CommandArguments.getSize(sender, args[3]);

        if(!arguments.getValue())
            return;

        int size = arguments.getKey();

        if(size > plugin.getSettings().maxIslandSize){
            Locale.SIZE_BIGGER_MAX.send(sender);
            return;
        }

        Executor.data(() -> islands.forEach(island -> island.setIslandSize(island.getIslandSize() + size)));

        if(islands.size() > 1)
            Locale.CHANGED_ISLAND_SIZE_ALL.send(sender);
        else if(targetPlayer == null)
            Locale.CHANGED_ISLAND_SIZE_NAME.send(sender, islands.get(0).getName());
        else
            Locale.CHANGED_ISLAND_SIZE.send(sender, targetPlayer.getName());

        if(plugin.getSettings().buildOutsideIsland)
            Locale.CHANGED_ISLAND_SIZE_BUILD_OUTSIDE.send(sender);

        islands.forEach(Island::updateBorder);
    }

}
