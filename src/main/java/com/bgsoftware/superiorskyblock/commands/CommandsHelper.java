package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.core.Text;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CommandsHelper {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private CommandsHelper() {

    }

    public static boolean shouldDisplayCommandForPlayer(SuperiorCommand superiorCommand, CommandSender executor) {
        return superiorCommand.displayCommand() && hasCommandAccess(superiorCommand, executor);
    }

    public static boolean hasCommandAccess(SuperiorCommand superiorCommand, CommandSender executor) {
        String permission = superiorCommand.getPermission();
        return Text.isBlank(permission) || executor.hasPermission(permission);
    }

    public static String getCommandUsage(SuperiorCommand superiorCommand, Locale locale) {
        String pluginLabel = plugin.getCommands().getLabel() + " ";
        String usage = superiorCommand.getUsage(locale);

        if (!plugin.getSettings().isOverrideDefaultAliases())
            return pluginLabel + usage;

        // Plain string operations are used, as labels may contain regex characters.
        String defaultLabel = superiorCommand.getAliases().get(0);

        if (startsWithLabel(usage, defaultLabel))
            return pluginLabel + getCommandLabel(superiorCommand) + usage.substring(defaultLabel.length());

        SuperiorCommand cmdAdmin = plugin.getCommands().getPlayerCommandsMap().findCommand("admin");
        if (cmdAdmin != null) {
            String defaultAdminLabel = cmdAdmin.getAliases().get(0);
            if (startsWithLabel(usage, defaultAdminLabel)) {
                usage = usage.substring(defaultAdminLabel.length()).trim();

                if (startsWithLabel(usage, defaultLabel))
                    usage = getCommandLabel(superiorCommand) + usage.substring(defaultLabel.length());

                return pluginLabel + getCommandLabel(cmdAdmin) + (usage.isEmpty() ? "" : " " + usage);
            }
        }

        return pluginLabel + usage;
    }

    private static boolean startsWithLabel(String usage, String label) {
        return usage.regionMatches(true, 0, label, 0, label.length()) &&
                (usage.length() == label.length() || Character.isWhitespace(usage.charAt(label.length())));
    }

    public static String getCommandLabel(SuperiorCommand superiorCommand) {
        if (!plugin.getSettings().isOverrideDefaultAliases())
            return superiorCommand.getAliases().get(0);

        return getCommandAliases(superiorCommand).get(0);
    }

    public static List<String> getCommandAliases(SuperiorCommand superiorCommand) {
        List<String> defaultAliases = new ArrayList<>(superiorCommand.getAliases());
        List<String> customAliases = new ArrayList<>();

        for (String alias : defaultAliases) {
            List<String> aliases = plugin.getSettings().getCommandAliases()
                    .getOrDefault(alias.toLowerCase(Locale.ENGLISH), Collections.emptyList());
            customAliases.addAll(aliases);
        }

        if (!customAliases.isEmpty() && plugin.getSettings().isOverrideDefaultAliases())
            return customAliases;

        Set<String> combinedAliases = new LinkedHashSet<>(defaultAliases);
        combinedAliases.addAll(customAliases);

        return new ArrayList<>(combinedAliases);
    }

}
