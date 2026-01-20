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

        if (!plugin.getSettings().isOverrideDefaultAliases())
            return pluginLabel + superiorCommand.getUsage(locale);

        String usage = superiorCommand.getUsage(locale);
        String defaultLabel = superiorCommand.getAliases().get(0);

        if (usage.startsWith(defaultLabel))
            return pluginLabel + usage.replaceFirst(defaultLabel, getCommandLabel(superiorCommand));

        SuperiorCommand cmdAdmin = plugin.getCommands().getCommand("admin");
        String defaultAdminLabel = cmdAdmin.getAliases().get(0);

        if (usage.startsWith(defaultAdminLabel)) {
            usage = usage.substring(defaultAdminLabel.length() + 1);

            if (usage.startsWith(defaultLabel))
                usage = usage.replaceFirst(defaultLabel, getCommandLabel(superiorCommand));

            return pluginLabel + getCommandLabel(cmdAdmin) + " " + usage;
        }

        return pluginLabel + usage;
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
            List<String> aliases = plugin.getSettings().getCommandAliases().getOrDefault(alias, Collections.emptyList());
            customAliases.addAll(aliases);
        }

        if (!customAliases.isEmpty() && plugin.getSettings().isOverrideDefaultAliases())
            return customAliases;

        Set<String> combinedAliases = new LinkedHashSet<>(defaultAliases);
        combinedAliases.addAll(customAliases);

        return new ArrayList<>(combinedAliases);
    }

}
