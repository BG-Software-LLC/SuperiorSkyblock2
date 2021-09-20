package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public final class CmdAdminSetChestRow implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setchestrow");
    }

    @Override
    public String getPermission() {
        return "superior.admin.setchestrow";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin setchestrow <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_PAGE.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_ROWS.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_SET_CHEST_ROW.getMessage(locale);
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
        Pair<Integer, Boolean> pageArguments = CommandArguments.getPage(sender, args[3]);

        if(!pageArguments.getValue())
            return;

        int page = pageArguments.getKey();

        Pair<Integer, Boolean> rowsArguments = CommandArguments.getRows(sender, args[4]);

        if(!rowsArguments.getValue())
            return;

        int rows = rowsArguments.getKey();

        if(rows < 1 || rows > 6){
            Locale.INVALID_ROWS.send(sender, args[4]);
            return;
        }

        Executor.data(() -> islands.forEach(island -> island.setChestRows(page - 1, rows)));

        if(islands.size() > 1)
            Locale.CHANGED_CHEST_SIZE_ALL.send(sender, page, rows);
        else if(targetPlayer == null)
            Locale.CHANGED_CHEST_SIZE_NAME.send(sender, page, rows, islands.get(0).getName());
        else
            Locale.CHANGED_CHEST_SIZE.send(sender, page, rows, targetPlayer.getName());
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        return args.length == 4 && island != null ?
                CommandTabCompletes.getCustomComplete(args[3], IntStream.range(1, island.getChestSize() + 1)) :
                args.length == 5 && island != null ?
                CommandTabCompletes.getCustomComplete(args[4], IntStream.range(1, 7)) :
                new ArrayList<>();
    }

}
