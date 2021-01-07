package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.commands.CommandArguments;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class CmdAdminTitleAll implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("titleall");
    }

    @Override
    public String getPermission() {
        return "superior.admin.titleall";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin titleall <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_TITLE_FADE_IN.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_TITLE_DURATION.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_TITLE_FADE_OUT.getMessage(locale) + "> " +
                "-title [" + Locale.COMMAND_ARGUMENT_MESSAGE.getMessage(locale) + "] " +
                "-subtitle [" + Locale.COMMAND_ARGUMENT_MESSAGE.getMessage(locale) + "]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_TITLE_ALL.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 8;
    }

    @Override
    public int getMaxArgs() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public boolean supportMultipleIslands() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, SuperiorPlayer targetPlayer, List<Island> islands, String[] args) {
        Pair<Integer, Boolean> fadeIn = CommandArguments.getInterval(sender, args[3]);

        if(!fadeIn.getValue())
            return;

        Pair<Integer, Boolean> duration = CommandArguments.getInterval(sender, args[4]);

        if(!duration.getValue())
            return;

        Pair<Integer, Boolean> fadeOut = CommandArguments.getInterval(sender, args[5]);

        if(!fadeOut.getValue())
            return;

        Map<String, String> parsedArguments = CommandArguments.parseArguments(args);

        String title = parsedArguments.get("title"), subtitle = parsedArguments.get("subtitle");

        if(title == null && subtitle == null){
            Locale.INVALID_TITLE.send(sender);
            return;
        }

        islands.forEach(island -> island.sendTitle(title == null ? null : StringUtils.translateColors(title),
                subtitle == null ? null : StringUtils.translateColors(subtitle), fadeIn.getKey(), duration.getKey(), fadeOut.getKey()));

        if(targetPlayer == null)
            Locale.GLOBAL_TITLE_SENT_NAME.send(sender, islands.size() == 1 ? islands.get(0).getName() : "all");
        else
            Locale.GLOBAL_TITLE_SENT.send(sender, targetPlayer.getName());
    }

}
