package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.NumberArgument;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class CmdAdminSetChestRow implements IAdminIslandCommand {

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
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <" +
                Message.COMMAND_ARGUMENT_PAGE.getMessage(locale) + "> <" +
                Message.COMMAND_ARGUMENT_ROWS.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_SET_CHEST_ROW.getMessage(locale);
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
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, @Nullable SuperiorPlayer targetPlayer, List<Island> islands, String[] args) {
        NumberArgument<Integer> pageArguments = CommandArguments.getPage(sender, args[3]);

        if (!pageArguments.isSucceed())
            return;

        int page = pageArguments.getNumber();

        NumberArgument<Integer> rowsArguments = CommandArguments.getRows(sender, args[4]);

        if (!rowsArguments.isSucceed())
            return;

        int rows = rowsArguments.getNumber();

        if (rows < 1 || rows > 6) {
            Message.INVALID_ROWS.send(sender, args[4]);
            return;
        }

        BukkitExecutor.data(() -> islands.forEach(island -> island.setChestRows(page - 1, rows)));

        if (islands.size() > 1)
            Message.CHANGED_CHEST_SIZE_ALL.send(sender, page, rows);
        else if (targetPlayer == null)
            Message.CHANGED_CHEST_SIZE_NAME.send(sender, page, rows, islands.get(0).getName());
        else
            Message.CHANGED_CHEST_SIZE.send(sender, page, rows, targetPlayer.getName());
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        return args.length == 4 && island != null ?
                CommandTabCompletes.getCustomComplete(args[3], IntStream.range(1, island.getChestSize() + 1)) :
                args.length == 5 && island != null ?
                        CommandTabCompletes.getCustomComplete(args[4], IntStream.range(1, 7)) :
                        Collections.emptyList();
    }

}
