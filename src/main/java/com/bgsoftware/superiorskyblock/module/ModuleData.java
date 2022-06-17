package com.bgsoftware.superiorskyblock.module;

import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import org.bukkit.event.Listener;

public class ModuleData {

    private final Listener[] listeners;
    private final SuperiorCommand[] commands;
    private final SuperiorCommand[] adminCommands;

    public ModuleData(Listener[] listeners, SuperiorCommand[] commands, SuperiorCommand[] adminCommands) {
        this.listeners = listeners;
        this.commands = commands;
        this.adminCommands = adminCommands;
    }

    public Listener[] getListeners() {
        return listeners;
    }

    public SuperiorCommand[] getCommands() {
        return commands;
    }

    public SuperiorCommand[] getAdminCommands() {
        return adminCommands;
    }

}
