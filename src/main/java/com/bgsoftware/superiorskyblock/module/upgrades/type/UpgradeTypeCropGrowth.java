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
        return Collections.singletonList(new CropGrowthListener());
    }

    @Override
    public List<ISuperiorCommand> getCommands() {
        return Arrays.asList(new CmdAdminAddCropGrowth(), new CmdAdminSetCropGrowth());
    }

    private class CropGrowthListener implements Listener {

        // Should potentially fix crop growth tile entities "disappearing"
        @EventHandler(priority = EventPriority.LOWEST)
        public void onBlockGrow(BlockGrowEvent e) {
            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                Location blockLocation = e.getBlock().getLocation(wrapper.getHandle());
                Island island = plugin.getGrid().getIslandAt(blockLocation);
                if (island != null && island.isInsideRange(blockLocation))
                    plugin.getNMSChunks().startTickingChunk(island, e.getBlock().getChunk(), false);
            }
        }

    }

}
