package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.core.Text;
import org.bukkit.command.CommandSender;

public class CommandsHelper {

    private CommandsHelper() {

    }

    public static boolean shouldDisplayCommandForPlayer(SuperiorCommand superiorCommand, CommandSender executor) {
        return superiorCommand.displayCommand() && hasCommandAccess(superiorCommand, executor);
    }

    public static boolean hasCommandAccess(SuperiorCommand superiorCommand, CommandSender executor) {
        String permission = superiorCommand.getPermission();
        return Text.isBlank(permission) || executor.hasPermission(permission);
    }

}
