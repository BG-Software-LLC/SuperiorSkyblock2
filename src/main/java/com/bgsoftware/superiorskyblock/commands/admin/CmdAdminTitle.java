package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.lang.Message;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.IAdminPlayerCommand;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import org.bukkit.command.CommandSender;

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
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "> <" +
                Message.COMMAND_ARGUMENT_TITLE_FADE_IN.getMessage(locale) + "> <" +
                Message.COMMAND_ARGUMENT_TITLE_DURATION.getMessage(locale) + "> <" +
                Message.COMMAND_ARGUMENT_TITLE_FADE_OUT.getMessage(locale) + "> " +
                "-title [" + Message.COMMAND_ARGUMENT_MESSAGE.getMessage(locale) + "] " +
                "-subtitle [" + Message.COMMAND_ARGUMENT_MESSAGE.getMessage(locale) + "]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_TITLE.getMessage(locale);
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
        if (!targetPlayer.isOnline()) {
            Message.PLAYER_NOT_ONLINE.send(sender);
            return;
        }

        Pair<Integer, Boolean> fadeIn = CommandArguments.getInterval(sender, args[3]);

        if (!fadeIn.getValue())
            return;

        Pair<Integer, Boolean> duration = CommandArguments.getInterval(sender, args[4]);

        if (!duration.getValue())
            return;

        Pair<Integer, Boolean> fadeOut = CommandArguments.getInterval(sender, args[5]);

        if (!fadeOut.getValue())
            return;

        Map<String, String> parsedArguments = CommandArguments.parseArguments(args);

        String title = parsedArguments.get("title");
        String subtitle = parsedArguments.get("subtitle");

        if (title == null && subtitle == null) {
            Message.INVALID_TITLE.send(sender);
            return;
        }

        plugin.getNMSPlayers().sendTitle(targetPlayer.asPlayer(),
                title == null ? null : StringUtils.translateColors(title),
                subtitle == null ? null : StringUtils.translateColors(subtitle),
                fadeIn.getKey(),
                duration.getKey(),
                fadeOut.getKey());

        Message.TITLE_SENT.send(sender, targetPlayer.getName());
    }

}
