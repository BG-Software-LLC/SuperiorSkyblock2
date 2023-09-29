package com.bgsoftware.superiorskyblock.module.upgrades.type;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.module.upgrades.commands.CmdAdminAddCropGrowth;
import com.bgsoftware.superiorskyblock.module.upgrades.commands.CmdAdminSetCropGrowth;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;

import java.util.Arrays;
import java.util.List;

public class UpgradeTypeCropGrowth implements IUpgradeType {

    private static final List<ISuperiorCommand> commands = Arrays.asList(new CmdAdminAddCropGrowth(),
            new CmdAdminSetCropGrowth());

    private final SuperiorSkyblockPlugin plugin;

    public UpgradeTypeCropGrowth(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Listener getListener() {
        return new CropGrowthListener();
    }

    @Override
    public List<ISuperiorCommand> getCommands() {
        return commands;
    }

    private class CropGrowthListener implements Listener {

        // Should potentially fix crop growth tile entities "disappearing"
        @EventHandler(priority = EventPriority.LOWEST)
        public void onBlockGrow(BlockGrowEvent e) {
            Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());
            if (island != null && island.isInsideRange(e.getBlock().getLocation()))
                plugin.getNMSChunks().startTickingChunk(island, e.getBlock().getChunk(), false);
        }

    }

}
