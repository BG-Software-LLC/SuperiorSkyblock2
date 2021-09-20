package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdToggle implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("toggle");
    }

    @Override
    public String getPermission() {
        return "superior.island.toggle";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "toggle <border/blocks>";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_TOGGLE.getMessage(locale);
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
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);

        if(args[1].equalsIgnoreCase("border")) {
            if(!sender.hasPermission("superior.island.toggle.border")){
                Locale.NO_COMMAND_PERMISSION.send(sender);
                return;
            }

            if (superiorPlayer.hasWorldBorderEnabled()) {
                Locale.TOGGLED_WORLD_BORDER_OFF.send(superiorPlayer);
            } else {
                Locale.TOGGLED_WORLD_BORDER_ON.send(superiorPlayer);
            }

            superiorPlayer.toggleWorldBorder();
            superiorPlayer.updateWorldBorder(plugin.getGrid().getIslandAt(superiorPlayer.getLocation()));
        }

        else if(args[1].equalsIgnoreCase("blocks")){
            if(!sender.hasPermission("superior.island.toggle.blocks")){
                Locale.NO_COMMAND_PERMISSION.send(sender);
                return;
            }

            if(superiorPlayer.hasBlocksStackerEnabled()){
                Locale.TOGGLED_STACKED_BLOCKS_OFF.send(superiorPlayer);
            } else{
                Locale.TOGGLED_STACKED_BLOCKS_ON.send(superiorPlayer);
            }

            superiorPlayer.toggleBlocksStacker();
        }

        else{
            Locale.INVALID_TOGGLE_MODE.send(superiorPlayer, args[1]);
        }

    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return args.length == 2 ? CommandTabCompletes.getCustomComplete(args[1], var ->
                        sender.hasPermission("superior.island.toggle." + var), "border", "blocks") : new ArrayList<>();
    }

}
