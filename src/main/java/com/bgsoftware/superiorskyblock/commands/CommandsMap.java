package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventType;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsDispatcher;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
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

    private static Set<SuperiorCommand> DISABLED_COMMANDS_CACHE;

    private static void onSettingsUpdate() {
        DISABLED_COMMANDS_CACHE = new HashSet<>();
        SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
        plugin.getSettings().getDisabledCommands().forEach(commandLabel -> {
            SuperiorCommand superiorCommand = plugin.getCommands().getCommand(commandLabel);
            if (superiorCommand != null && !(superiorCommand instanceof IAdminIslandCommand)
                    && !(superiorCommand instanceof IAdminPlayerCommand))
                DISABLED_COMMANDS_CACHE.add(superiorCommand);
        });
    }

    public static void registerListeners(PluginEventsDispatcher dispatcher) {
        dispatcher.registerCallback(PluginEventType.SETTINGS_UPDATE_EVENT, CommandsMap::onSettingsUpdate);
    }

    //This map allows to specify a command in code when its default aliases are disabled.
    private final Map<String, SuperiorCommand> defaultLabels = new HashMap<>();

    private final Map<String, SuperiorCommand> subCommands = new TreeMap<>();
    private final Map<String, List<SuperiorCommand>> aliasesToCommand = new HashMap<>();

    protected final SuperiorSkyblockPlugin plugin;

    protected CommandsMap(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    public abstract void loadDefaultCommands();

    public void registerCommand(SuperiorCommand superiorCommand) {
        Preconditions.checkNotNull(superiorCommand, "superiorCommand parameter cannot be null.");

        List<String> aliases = new LinkedList<>(CommandsHelper.getCommandAliases(superiorCommand));
        String label = aliases.remove(0).toLowerCase(Locale.ENGLISH);

        defaultLabels.put(superiorCommand.getAliases().get(0), superiorCommand);
        subCommands.put(label, superiorCommand);

        for (String alias : aliases)
            aliasesToCommand.computeIfAbsent(alias.toLowerCase(Locale.ENGLISH), a -> new LinkedList<>()).add(superiorCommand);

        PluginEventsFactory.callCommandsUpdateEvent();
    }

    public void unregisterCommand(SuperiorCommand superiorCommand) {
        Preconditions.checkNotNull(superiorCommand, "superiorCommand parameter cannot be null.");

        String label = CommandsHelper.getCommandLabel(superiorCommand);
        removeCommand(label);

        PluginEventsFactory.callCommandsUpdateEvent();
    }

    @Nullable
    public SuperiorCommand getCommand(String label) {
        label = label.toLowerCase(Locale.ENGLISH);
        SuperiorCommand superiorCommand = subCommands.get(label);
        if (superiorCommand != null && isCommandEnabled(superiorCommand))
            return superiorCommand;

        List<SuperiorCommand> commandAliases = aliasesToCommand.getOrDefault(label, Collections.emptyList());
        for (SuperiorCommand commandAlias : commandAliases) {
            if (isCommandEnabled(commandAlias)) {
                return commandAlias;
            }
        }

        superiorCommand = defaultLabels.get(label);
        if (superiorCommand != null && isCommandEnabled(superiorCommand))
            return superiorCommand;

        return null;
    }

    public List<SuperiorCommand> getSubCommands(boolean includeDisabled) {
        SequentialListBuilder<SuperiorCommand> listBuilder = new SequentialListBuilder<>();

        if (!includeDisabled)
            listBuilder.filter(this::isCommandEnabled);

        return listBuilder.build(this.subCommands.values());
    }

    protected void clearCommands() {
        this.subCommands.clear();
        this.aliasesToCommand.clear();
    }

    private boolean isCommandEnabled(SuperiorCommand superiorCommand) {
        return superiorCommand instanceof IAdminIslandCommand || superiorCommand instanceof IAdminPlayerCommand ||
                !DISABLED_COMMANDS_CACHE.contains(superiorCommand);
    }

    private void removeCommand(String label) {
        if (subCommands.remove(label) != null) {
            aliasesToCommand.values().forEach(commandsList ->
                    commandsList.removeIf(sC -> sC.getAliases().get(0).equalsIgnoreCase(label)));
        }
    }

}
