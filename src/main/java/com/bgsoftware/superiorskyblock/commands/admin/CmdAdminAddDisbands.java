package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminPlayerCommand;
import com.bgsoftware.superiorskyblock.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;

import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public final class CmdAdminAddDisbands implements IAdminPlayerCommand {
    @Override
    public List<String> getAliases() {
        return Arrays.asList("adddisbands", "givedisbands");
    }

    @Override
    public String getPermission() {
        return "superior.admin.givedisbands";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin adddisbands <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_AMOUNT.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_GIVE_DISBANDS.getMessage(locale);
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
        Pair<Integer, Boolean> arguments = CommandArguments.getLimit(sender, args[3]);

        if(!arguments.getValue())
            return;

        int amount = arguments.getKey();

        Executor.data(() -> targetPlayers.forEach(superiorPlayer -> superiorPlayer.setDisbands(superiorPlayer.getDisbands() + amount)));

        if(targetPlayers.size() > 1){
            Locale.DISBAND_GIVE_ALL.send(sender, amount);
        }
        else if (!sender.equals(targetPlayers.get(0).asPlayer()))
            Locale.DISBAND_GIVE_OTHER.send(sender, targetPlayers.get(0).getName(), amount);

        targetPlayers.forEach(superiorPlayer -> {
            if(superiorPlayer.isOnline()){
                Locale.DISBAND_GIVE.send(superiorPlayer, amount);
            }
        });
    }

}
