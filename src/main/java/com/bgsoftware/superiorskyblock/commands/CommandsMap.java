package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.google.common.base.Preconditions;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class CommandsMap {

    private final Map<String, SuperiorCommand> subCommands = new LinkedHashMap<>();
    private final Map<String, SuperiorCommand> aliasesToCommand = new HashMap<>();

    private final SuperiorSkyblockPlugin plugin;

    protected CommandsMap(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    public abstract void loadDefaultCommands();

    public void registerCommand(SuperiorCommand superiorCommand, boolean sort) {
        List<String> aliases = new ArrayList<>(superiorCommand.getAliases());
        String label = aliases.get(0).toLowerCase();
        aliases.addAll(plugin.getSettings().getCommandAliases().getOrDefault(label, new ArrayList<>()));

        if(subCommands.containsKey(label)){
            subCommands.remove(label);
            aliasesToCommand.values().removeIf(sC -> sC.getAliases().get(0).equals(aliases.get(0)));
        }
        subCommands.put(label, superiorCommand);

        for(int i = 1; i < aliases.size(); i++){
            aliasesToCommand.put(aliases.get(i).toLowerCase(), superiorCommand);
        }

        if(sort){
            List<SuperiorCommand> superiorCommands = new ArrayList<>(subCommands.values());
            superiorCommands.sort(Comparator.comparing(o -> o.getAliases().get(0)));
            subCommands.clear();
            superiorCommands.forEach(s -> subCommands.put(s.getAliases().get(0), s));
        }
    }

    public void unregisterCommand(SuperiorCommand superiorCommand) {
        Preconditions.checkNotNull(superiorCommand, "superiorCommand parameter cannot be null.");

        List<String> aliases = new ArrayList<>(superiorCommand.getAliases());
        String label = aliases.get(0).toLowerCase();
        aliases.addAll(plugin.getSettings().getCommandAliases().getOrDefault(label, new ArrayList<>()));

        subCommands.remove(label);
        aliasesToCommand.values().removeIf(sC -> sC.getAliases().get(0).equals(aliases.get(0)));
    }

    @Nullable
    public SuperiorCommand getCommand(String label){
        label = label.toLowerCase();
        return subCommands.getOrDefault(label, aliasesToCommand.get(label));
    }

    public List<SuperiorCommand> getSubCommands() {
        return Collections.unmodifiableList(new ArrayList<>(subCommands.values()));
    }

}
