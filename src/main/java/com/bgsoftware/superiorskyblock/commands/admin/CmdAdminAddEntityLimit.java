package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.key.Key;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdAdminAddEntityLimit implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("addentitylimit");
    }

    @Override
    public String getPermission() {
        return "superior.admin.addentitylimit";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin addentitylimit <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_ENTITY.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_LIMIT.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_ADD_ENTITY_LIMIT.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 5;
    }

    @Override
    public int getMaxArgs() {
        return 5;
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
        Key entityKey = Key.of(args[3].toUpperCase());

        Pair<Integer, Boolean> arguments = CommandArguments.getLimit(sender, args[4]);

        if(!arguments.getValue())
            return;

        int limit = arguments.getKey();

        Executor.data(() -> islands.forEach(island -> island.setEntityLimit(entityKey, island.getEntityLimit(entityKey) + limit)));

        if(islands.size() > 1)
            Locale.CHANGED_ENTITY_LIMIT_ALL.send(sender, StringUtils.format(entityKey.getGlobalKey()));
        else if(targetPlayer == null)
            Locale.CHANGED_ENTITY_LIMIT_NAME.send(sender, StringUtils.format(entityKey.getGlobalKey()), islands.get(0).getName());
        else
            Locale.CHANGED_ENTITY_LIMIT.send(sender, StringUtils.format(entityKey.getGlobalKey()), targetPlayer.getName());
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        return args.length == 4 ? CommandTabCompletes.getEntitiesForLimit(args[3]) : new ArrayList<>();
    }

}
