package com.bgsoftware.superiorskyblock.commands.command;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import com.bgsoftware.superiorskyblock.utils.StringUtil;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class CmdBorder implements ICommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("border");
    }

    @Override
    public String getPermission() {
        return "superior.island.border";
    }

    @Override
    public String getUsage() {
        return "island border <color>";
    }

    @Override
    public String getDescription() {
        return Locale.COMMAND_DESCRIPTION_BORDER.getMessage();
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
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(sender);

        BorderColor borderColor;

        try{
            borderColor = BorderColor.valueOf(args[1].toUpperCase());
        }catch(IllegalArgumentException ex){
            Locale.INVALID_BORDER_COLOR.send(sender, args[1]);
            return;
        }

        superiorPlayer.setBorderColor(borderColor);
        plugin.getNMSAdapter().setWorldBorder(superiorPlayer, plugin.getGrid().getIslandAt(superiorPlayer.getLocation()));

        Locale.BORDER_PLAYER_COLOR_UPDATED.send(superiorPlayer, StringUtil.format(borderColor.name()));
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();

        if(args.length == 2){
            Arrays.stream(BorderColor.values())
                    .filter(borderColor -> borderColor.name().toLowerCase().startsWith(args[1]))
                    .forEach(borderColor -> list.add(borderColor.name().toLowerCase()));
        }

        return list;
    }
}
