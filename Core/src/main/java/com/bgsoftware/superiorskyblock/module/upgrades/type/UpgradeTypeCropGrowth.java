package com.bgsoftware.superiorskyblock.module.upgrades.type;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.module.upgrades.commands.CmdAdminAddCropGrowth;
import com.bgsoftware.superiorskyblock.module.upgrades.commands.CmdAdminSetCropGrowth;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UpgradeTypeCropGrowth implements IUpgradeType {

    private final SuperiorSkyblockPlugin plugin;

    public UpgradeTypeCropGrowth(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<Listener> getListeners() {
        return Collections.emptyList();
    }

    @Override
    public List<ISuperiorCommand> getCommands() {
        return Arrays.asList(new CmdAdminAddCropGrowth(), new CmdAdminSetCropGrowth());
    }

}
