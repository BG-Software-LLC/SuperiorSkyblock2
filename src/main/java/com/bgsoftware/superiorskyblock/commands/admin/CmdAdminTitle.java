package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminPlayerCommand;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.commands.CommandArguments;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class CmdAdminTitle implements IAdminPlayerCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("title");
    }

    @Override
    public String getPermission() {
        return "superior.admin.title";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin title <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_TITLE_FADE_IN.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_TITLE_DURATION.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_TITLE_FADE_OUT.getMessage(locale) + "> " +
                "-title [" + Locale.COMMAND_ARGUMENT_MESSAGE.getMessage(locale) + "] " +
                "-subtitle [" + Locale.COMMAND_ARGUMENT_MESSAGE.getMessage(locale) + "]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_TITLE.getMessage(locale);
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
    public boolean supportMultiplePlayers() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, SuperiorPlayer targetPlayer, String[] args) {
        if(!targetPlayer.isOnline()){
            Locale.PLAYER_NOT_ONLINE.send(sender);
            return;
        }

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

        plugin.getNMSAdapter().sendTitle(targetPlayer.asPlayer(), title == null ? null : StringUtils.translateColors(title),
                subtitle == null ? null : StringUtils.translateColors(subtitle), fadeIn.getKey(), duration.getKey(), fadeOut.getKey());

        Locale.TITLE_SENT.send(sender, targetPlayer.getName());
    }

}
