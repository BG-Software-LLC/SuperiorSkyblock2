package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CmdPanel implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("panel", "manager", "cp");
    }

    @Override
    public String getPermission() {
        return "superior.island.panel";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "panel [members/visitors] [toggle]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_PANEL.getMessage(locale);
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

        if(args.length > 1){
            if(args[1].equalsIgnoreCase("members")){
                plugin.getMenus().openMembers(superiorPlayer, null, island);
                return;
            }else if(args[1].equalsIgnoreCase("visitors")){
                plugin.getMenus().openVisitors(superiorPlayer, null, island);
                return;
            }else if(args[1].equalsIgnoreCase("toggle")){
                if(superiorPlayer.hasToggledPanel()){
                    superiorPlayer.setToggledPanel(false);
                    Locale.PANEL_TOGGLE_OFF.send(superiorPlayer);
                }else{
                    superiorPlayer.setToggledPanel(true);
                    Locale.PANEL_TOGGLE_ON.send(superiorPlayer);
                }
                return;
            }
        }

        plugin.getMenus().openControlPanel(superiorPlayer, null, island);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return args.length == 2 ? CommandTabCompletes.getCustomComplete(args[1], "members", "visitors", "toggle") : new ArrayList<>();
    }

}
