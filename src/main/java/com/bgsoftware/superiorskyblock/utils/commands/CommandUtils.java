package com.bgsoftware.superiorskyblock.utils.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

public final class CommandUtils {

    public static void dispatchCommand(CommandSender sender, String commandLine){
        Event event;
        if(sender instanceof Player){
            event = new PlayerCommandPreprocessEvent((Player) sender, commandLine);
        }
        else{
            event = new ServerCommandEvent(sender, commandLine);
        }

        Bukkit.getPluginManager().callEvent(event);

        if(!((Cancellable) event).isCancelled())
            Bukkit.dispatchCommand(sender, commandLine);
    }

}
