package com.ome_r.superiorskyblock.commands.command;

import com.ome_r.superiorskyblock.Locale;
import com.ome_r.superiorskyblock.SuperiorSkyblock;
import com.ome_r.superiorskyblock.commands.ICommand;
import com.ome_r.superiorskyblock.wrappers.WrappedPlayer;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CmdToggle implements ICommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("toggle");
    }

    @Override
    public String getPermission() {
        return "superior.island.toggle";
    }

    @Override
    public String getUsage() {
        return "island toggle <border/blocks>";
    }

    @Override
    public int getMinArgs() {
        return 2;
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
    public void execute(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        WrappedPlayer wrappedPlayer = WrappedPlayer.of(sender);

        if(args[1].equalsIgnoreCase("border")) {
            if (wrappedPlayer.hasWorldBorderEnabled()) {
                Locale.TOGGLED_WORLD_BORDER_OFF.send(wrappedPlayer);
            } else {
                Locale.TOGGLED_WORLD_BORDER_ON.send(wrappedPlayer);
            }

            wrappedPlayer.toggleWorldBorder();
            plugin.getNMSAdapter().setWorldBorder(wrappedPlayer, plugin.getGrid().getIslandAt(wrappedPlayer.getLocation()));
        }

        else if(args[1].equalsIgnoreCase("blocks")){
            if(wrappedPlayer.hasBlocksStackerEnabled()){
                Locale.TOGGLED_STACKED_BLCOKS_OFF.send(wrappedPlayer);
            } else{
                Locale.TOGGLED_STACKED_BLCOKS_ON.send(wrappedPlayer);
            }

            wrappedPlayer.toggleBlocksStacker();
        }

        else{
            Locale.INVALID_TOGGLE_MODE.send(wrappedPlayer, args[1]);
        }

    }

    @Override
    public List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();

        if(args.length == 2){
            list.addAll(Stream.of("border", "blocks")
                    .filter(value -> value.startsWith(args[1].toLowerCase())).collect(Collectors.toList()));
        }

        return list;
    }
}
