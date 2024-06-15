package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.common.collections.Lists;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.BaseCommand;
import com.bgsoftware.superiorskyblock.commands.IAdminPlayerCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.NumberArgument;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CmdAdminSetDisbands extends BaseCommand implements IAdminPlayerCommand {
    @Override
    protected List<String> aliases() {
        return Lists.singleton("setdisbands");
    }

    @Override
    protected String permission() {
        return "superior.admin.setdisbands";
    }

    @Override
    protected String usage(java.util.Locale locale) {
        return "admin setdisbands <" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ALL_PLAYERS.getMessage(locale) + "> <" +
                Message.COMMAND_ARGUMENT_AMOUNT.getMessage(locale) + ">";
    }

    @Override
    protected String description(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_SET_DISBANDS.getMessage(locale);
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
    public boolean supportMultiplePlayers() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, List<SuperiorPlayer> targetPlayers, String[] args) {
        NumberArgument<Integer> arguments = CommandArguments.getLimit(sender, args[3]);

        if (!arguments.isSucceed())
            return;

        int amount = arguments.getNumber();

        BukkitExecutor.data(() -> targetPlayers.forEach(superiorPlayer -> superiorPlayer.setDisbands(amount)));

        if (targetPlayers.size() > 1) {
            Message.DISBAND_SET_ALL.send(sender, amount);
        } else if (!sender.equals(targetPlayers.get(0).asPlayer()))
            Message.DISBAND_SET_OTHER.send(sender, targetPlayers.get(0).getName(), amount);

        targetPlayers.forEach(superiorPlayer -> Message.DISBAND_SET.send(superiorPlayer, amount));
    }

}
