package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.key.Key;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CmdAdminRemoveBlockLimit implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("removeblocklimit", "remblocklimit");
    }

    @Override
    public String getPermission() {
        return "superior.admin.removeblocklimit";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin removeblocklimit <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_MATERIAL.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_REMOVE_BLOCK_LIMIT.getMessage(locale);
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
        Key key = Key.of(args[3].toUpperCase());

        Executor.data(() -> islands.forEach(island -> island.removeBlockLimit(key)));

        if(islands.size() > 1)
            Locale.CHANGED_BLOCK_LIMIT_ALL.send(sender, StringUtils.format(key.getGlobalKey()));
        else if(targetPlayer == null)
            Locale.CHANGED_BLOCK_LIMIT_NAME.send(sender, StringUtils.format(key.getGlobalKey()), islands.get(0).getName());
        else
            Locale.CHANGED_BLOCK_LIMIT.send(sender, StringUtils.format(key.getGlobalKey()), targetPlayer.getName());
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        return args.length == 4 ? CommandTabCompletes.getMaterials(args[3]) : new ArrayList<>();
    }

}
