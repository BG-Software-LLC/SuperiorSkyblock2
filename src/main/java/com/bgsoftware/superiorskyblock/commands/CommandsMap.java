package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.google.common.base.Preconditions;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public abstract class CommandsMap {

    private final Map<String, SuperiorCommand> subCommands = new LinkedHashMap<>();
    private final Map<String, SuperiorCommand> aliasesToCommand = new HashMap<>();

    protected final SuperiorSkyblockPlugin plugin;

    protected CommandsMap(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    public abstract void loadDefaultCommands();

    public void registerCommand(SuperiorCommand superiorCommand, boolean sort) {
        List<String> aliases = new LinkedList<>(superiorCommand.getAliases());
        String label = aliases.get(0).toLowerCase(Locale.ENGLISH);
        aliases.addAll(plugin.getSettings().getCommandAliases().getOrDefault(label, Collections.emptyList()));

        if (subCommands.containsKey(label)) {
            subCommands.remove(label);
            aliasesToCommand.values().removeIf(sC -> sC.getAliases().get(0).equals(aliases.get(0)));
        }
        subCommands.put(label, superiorCommand);

        for (String alias : aliases) {
            aliasesToCommand.put(alias.toLowerCase(Locale.ENGLISH), superiorCommand);
        }

        if (sort) {
            List<SuperiorCommand> superiorCommands = new LinkedList<>(subCommands.values());
            superiorCommands.sort(Comparator.comparing(o -> o.getAliases().get(0)));
            subCommands.clear();
            superiorCommands.forEach(s -> subCommands.put(s.getAliases().get(0), s));
        }
    }

    public void unregisterCommand(SuperiorCommand superiorCommand) {
        Preconditions.checkNotNull(superiorCommand, "superiorCommand parameter cannot be null.");

        List<String> aliases = new LinkedList<>(superiorCommand.getAliases());
        String label = aliases.get(0).toLowerCase(Locale.ENGLISH);
        aliases.addAll(plugin.getSettings().getCommandAliases().getOrDefault(label, Collections.emptyList()));

        subCommands.remove(label);
        aliasesToCommand.values().removeIf(sC -> sC.getAliases().get(0).equals(aliases.get(0)));
    }

    @Nullable
    public SuperiorCommand getCommand(String label) {
        label = label.toLowerCase(Locale.ENGLISH);
        SuperiorCommand superiorCommand = subCommands.getOrDefault(label, aliasesToCommand.get(label));
        return superiorCommand != null && !isCommandEnabled(superiorCommand) ? null : superiorCommand;
    }

    public List<SuperiorCommand> getSubCommands(boolean includeDisabled) {
        SequentialListBuilder<SuperiorCommand> listBuilder = new SequentialListBuilder<>();

        if (!includeDisabled)
            listBuilder.filter(this::isCommandEnabled);

        return listBuilder.build(this.subCommands.values());
    }

    private boolean isCommandEnabled(SuperiorCommand superiorCommand) {
        return superiorCommand instanceof IAdminIslandCommand || superiorCommand.getAliases().stream()
                .noneMatch(plugin.getSettings().getDisabledCommands()::contains);
    }

}
