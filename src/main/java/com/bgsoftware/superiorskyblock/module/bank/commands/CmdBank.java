package com.bgsoftware.superiorskyblock.module.bank.commands;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.commands.CommandArguments;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdBank implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("bank");
    }

    @Override
    public String getPermission() {
        return "superior.island.bank";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "bank";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_BANK.getMessage(locale) + "[logs]";
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public int getMaxArgs() {
        return 2;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        Pair<Island, SuperiorPlayer> arguments = CommandArguments.getSenderIsland(plugin, sender);

        Island island = arguments.getKey();

        if(island == null)
            return;

        SuperiorPlayer superiorPlayer = arguments.getValue();

        if(args.length == 2 && args[1].equalsIgnoreCase("logs")){
            plugin.getMenus().openBankLogs(superiorPlayer, null, island);
        }
        else {
            plugin.getMenus().openIslandBank(superiorPlayer, null, island);
        }
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

}
