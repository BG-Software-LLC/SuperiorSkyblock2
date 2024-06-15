package com.bgsoftware.superiorskyblock.module.bank.commands;

import com.bgsoftware.common.collections.Lists;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.BaseCommand;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.IslandArgument;
import com.bgsoftware.superiorskyblock.core.menu.view.MenuViewWrapper;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CmdBank extends BaseCommand implements ISuperiorCommand {

    @Override
    protected List<String> aliases() {
        return Lists.singleton("bank");
    }

    @Override
    protected String permission() {
        return "superior.island.bank";
    }

    @Override
    protected String usage(java.util.Locale locale) {
        return "bank [logs]";
    }

    @Override
    protected String description(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_BANK.getMessage(locale);
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
        IslandArgument arguments = CommandArguments.getSenderIsland(plugin, sender);

        Island island = arguments.getIsland();

        if (island == null)
            return;

        SuperiorPlayer superiorPlayer = arguments.getSuperiorPlayer();

        if (args.length == 2 && args[1].equalsIgnoreCase("logs")) {
            plugin.getMenus().openBankLogs(superiorPlayer, MenuViewWrapper.fromView(superiorPlayer.getOpenedView()), island);
        } else {
            plugin.getMenus().openIslandBank(superiorPlayer, MenuViewWrapper.fromView(superiorPlayer.getOpenedView()), island);
        }
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return args.length != 2 ? Lists.emptyList() : CommandTabCompletes.getCustomComplete(args[1], "logs");
    }

}
