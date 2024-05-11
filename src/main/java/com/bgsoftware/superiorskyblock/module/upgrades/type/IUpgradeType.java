package com.bgsoftware.superiorskyblock.module.upgrades.type;

import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import org.bukkit.event.Listener;

import java.util.List;

public interface IUpgradeType {

    List<Listener> getListeners();

    List<ISuperiorCommand> getCommands();

}
