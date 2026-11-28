package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventType;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsDispatcher;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.google.common.base.Preconditions;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public abstract class CommandsMap {

    // Lower-cased default labels (first default alias) of the disabled commands.
    // Labels and not instances are stored, as reloading the plugin creates new command instances.
    private static Set<String> DISABLED_COMMANDS_CACHE = new HashSet<>();

    public static void registerListeners(PluginEventsDispatcher dispatcher) {
        // Commands are re-created when the plugin is reloaded, and modules register their commands
        // later than the built-in ones; therefore, the disabled commands are resolved on both events.
        dispatcher.registerCallback(PluginEventType.SETTINGS_UPDATE_EVENT, CommandsMap::refreshDisabledCommands);
        dispatcher.registerCallback(PluginEventType.COMMANDS_UPDATE_EVENT, CommandsMap::refreshDisabledCommands);
    }

    private static void refreshDisabledCommands() {
        Set<String> disabledCommands = new HashSet<>();
        SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
        plugin.getSettings().getDisabledCommands().forEach(commandLabel -> {
            SuperiorCommand superiorCommand = plugin.getCommands().getPlayerCommandsMap().findCommand(commandLabel);
            if (superiorCommand != null && !(superiorCommand instanceof IAdminIslandCommand)
                    && !(superiorCommand instanceof IAdminPlayerCommand))
                disabledCommands.add(getDefaultLabel(superiorCommand));
        });
        DISABLED_COMMANDS_CACHE = disabledCommands;
    }

    private static String getDefaultLabel(SuperiorCommand superiorCommand) {
        return superiorCommand.getAliases().get(0).toLowerCase(Locale.ENGLISH);
    }

    // All the registered commands, by their default labels.
    private final Map<String, SuperiorCommand> subCommands = new TreeMap<>();

    // Labels the players can type: the label and the aliases of each command, the label first.
    // When override-default-aliases is enabled, these contain only the custom aliases of the commands that have them.
    private final Map<String, List<SuperiorCommand>> typedLabelsToCommand = new HashMap<>();

    // All the default aliases of each command, lower-cased.
    // Used to find commands from code, menus and the console even when their default aliases are overridden.
    private final Map<String, List<SuperiorCommand>> defaultAliasesToCommand = new HashMap<>();

    protected final SuperiorSkyblockPlugin plugin;

    protected CommandsMap(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    public abstract void loadDefaultCommands();

    public void registerCommand(SuperiorCommand superiorCommand) {
        Preconditions.checkNotNull(superiorCommand, "superiorCommand parameter cannot be null.");

        String defaultLabel = getDefaultLabel(superiorCommand);

        // Replace the command that was registered with the same default label.
        SuperiorCommand existingCommand = subCommands.get(defaultLabel);
        if (existingCommand != null)
            removeCommand(existingCommand);

        subCommands.put(defaultLabel, superiorCommand);

        boolean isLabel = true;
        for (String alias : CommandsHelper.getCommandAliases(superiorCommand)) {
            List<SuperiorCommand> commands = typedLabelsToCommand.computeIfAbsent(alias.toLowerCase(Locale.ENGLISH), a -> new LinkedList<>());

            for (SuperiorCommand otherCommand : commands) {
                if (plugin.getSettings().isOverrideDefaultAliases() && !getDefaultLabel(otherCommand).equals(defaultLabel))
                    Log.warn("The alias '", alias, "' is used by both the '", getDefaultLabel(otherCommand),
                            "' and '", defaultLabel, "' commands; players will run the '",
                            getDefaultLabel(isLabel ? superiorCommand : commands.get(0)), "' command.");
            }

            // The label of a command takes priority over aliases of other commands.
            if (isLabel)
                commands.add(0, superiorCommand);
            else
                commands.add(superiorCommand);

            isLabel = false;
        }

        for (String defaultAlias : superiorCommand.getAliases())
            defaultAliasesToCommand.computeIfAbsent(defaultAlias.toLowerCase(Locale.ENGLISH), a -> new LinkedList<>()).add(superiorCommand);

        PluginEventsFactory.callCommandsUpdateEvent();
    }

    public void unregisterCommand(SuperiorCommand superiorCommand) {
        Preconditions.checkNotNull(superiorCommand, "superiorCommand parameter cannot be null.");

        // Commands may be unregistered with another instance of the same command, therefore
        // the registered instance is looked up by its default label.
        SuperiorCommand registeredCommand = subCommands.get(getDefaultLabel(superiorCommand));
        if (registeredCommand != null)
            removeCommand(registeredCommand);

        PluginEventsFactory.callCommandsUpdateEvent();
    }

    /**
     * Get a command by a label typed by a player.
     * When override-default-aliases is enabled, default aliases that were overridden are not accepted.
     */
    @Nullable
    public SuperiorCommand getTypedCommand(String label) {
        return getFirstEnabled(typedLabelsToCommand.get(label.toLowerCase(Locale.ENGLISH)));
    }

    /**
     * Get a command by any of its labels, preferring default aliases over custom ones.
     * This is used by code, menus and the console, which refer to commands by their default labels.
     * If a custom alias of a command equals a default alias of another command, the latter is returned.
     */
    @Nullable
    public SuperiorCommand getCommand(String label) {
        SuperiorCommand superiorCommand = getFirstEnabled(defaultAliasesToCommand.get(label.toLowerCase(Locale.ENGLISH)));
        return superiorCommand != null ? superiorCommand : getTypedCommand(label);
    }

    /**
     * Same as {@link #getCommand(String)}, but also returns disabled commands.
     */
    @Nullable
    public SuperiorCommand findCommand(String label) {
        label = label.toLowerCase(Locale.ENGLISH);

        List<SuperiorCommand> commands = defaultAliasesToCommand.get(label);
        if (commands == null || commands.isEmpty())
            commands = typedLabelsToCommand.get(label);

        return commands == null || commands.isEmpty() ? null : commands.get(0);
    }

    public List<SuperiorCommand> getSubCommands(boolean includeDisabled) {
        SequentialListBuilder<SuperiorCommand> listBuilder = new SequentialListBuilder<>();

        if (!includeDisabled)
            listBuilder.filter(this::isCommandEnabled);

        return listBuilder.build(this.subCommands.values());
    }

    protected void clearCommands() {
        this.subCommands.clear();
        this.typedLabelsToCommand.clear();
        this.defaultAliasesToCommand.clear();
    }

    private boolean isCommandEnabled(SuperiorCommand superiorCommand) {
        return superiorCommand instanceof IAdminIslandCommand || superiorCommand instanceof IAdminPlayerCommand ||
                !DISABLED_COMMANDS_CACHE.contains(getDefaultLabel(superiorCommand));
    }

    @Nullable
    private SuperiorCommand getFirstEnabled(@Nullable List<SuperiorCommand> commands) {
        if (commands != null) {
            for (SuperiorCommand superiorCommand : commands) {
                if (isCommandEnabled(superiorCommand))
                    return superiorCommand;
            }
        }

        return null;
    }

    private void removeCommand(SuperiorCommand superiorCommand) {
        subCommands.values().removeIf(sC -> sC == superiorCommand);
        removeFromAliases(typedLabelsToCommand, superiorCommand);
        removeFromAliases(defaultAliasesToCommand, superiorCommand);
    }

    private static void removeFromAliases(Map<String, List<SuperiorCommand>> aliases, SuperiorCommand superiorCommand) {
        aliases.values().forEach(commandsList -> commandsList.removeIf(sC -> sC == superiorCommand));
        aliases.values().removeIf(List::isEmpty);
    }

}
