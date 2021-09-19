package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdAdminAddBonus implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("addbonus");
    }

    @Override
    public String getPermission() {
        return "superior.admin.addbonus";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin addbonus <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <worth/level> <" +
                Locale.COMMAND_ARGUMENT_AMOUNT.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_ADD_BONUS.getMessage(locale);
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
        boolean isWorthBonus = !args[3].equalsIgnoreCase("level");

        BigDecimal bonus = CommandArguments.getBigDecimalAmount(sender, args[4]);

        if(bonus == null)
            return;

        Executor.data(() -> islands.forEach(island -> {
            if(isWorthBonus)
                island.setBonusWorth(island.getBonusWorth().add(bonus));
            else
                island.setBonusLevel(island.getBonusLevel().add(bonus));
        }));

        Locale.BONUS_SET_SUCCESS.send(sender, bonus.toString());
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        return args.length == 4 ? CommandTabCompletes.getCustomComplete(args[3], "worth", "level") : new ArrayList<>();
    }

}
