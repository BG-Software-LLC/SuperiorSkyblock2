package com.bgsoftware.superiorskyblock.module.upgrades.type;

import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import org.bukkit.event.Listener;

import java.util.List;

public interface IUpgradeType {

    Listener getListener();

    List<ISuperiorCommand> getCommands();

}
