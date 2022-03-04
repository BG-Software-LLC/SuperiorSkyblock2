package com.bgsoftware.superiorskyblock.module.generators.listeners;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.key.ConstantKeys;
import com.bgsoftware.superiorskyblock.module.generators.GeneratorsModule;
import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;

@SuppressWarnings("unused")
public final class GeneratorsListener implements Listener {

    private static final BlockFace[] nearbyFaces = new BlockFace[]{
            BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH
    };
    private static final Material BLUE_ICE_MATERIAL = Materials.getMaterialSafe("BLUE_ICE");
    private static final Material SOUL_SOIL_MATERIAL = Materials.getMaterialSafe("SOUL_SOIL");
    private static final Material BASALT_MATERIAL = Materials.getMaterialSafe("BASALT");
    private static final Material LAVA_MATERIAL = Materials.getMaterialSafe("STATIONARY_LAVA", "LAVA");

    private final SuperiorSkyblockPlugin plugin;
    private final GeneratorsModule module;

    public GeneratorsListener(SuperiorSkyblockPlugin plugin, GeneratorsModule module) {
        this.plugin = plugin;
        this.module = module;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockFormEvent(BlockFormEvent e) {
        if (!module.isEnabled())
            return;

        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());

        if (island == null)
            return;

        if (e.getBlock().getType() != LAVA_MATERIAL || e.getNewState().getType() != BASALT_MATERIAL)
            return;

        Key generatedBlock = island.generateBlock(e.getBlock().getLocation(), true);

        if (generatedBlock != null && !generatedBlock.equals(ConstantKeys.COBBLESTONE))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockFromToEvent(BlockFromToEvent e) {
        if (!module.isEnabled())
            return;

        Block block = e.getToBlock();

        Island island = plugin.getGrid().getIslandAt(block.getLocation());

        if (island == null)
            return;

        if (e.getBlock().getType() != LAVA_MATERIAL || !canGenerateBlock(block))
            return;

        // Should fix solid blocks from generating custom blocks
        // https://github.com/BG-Software-LLC/SuperiorSkyblock2/issues/837
        if (block.getType().isSolid())
            return;

        Key generatedBlock = island.generateBlock(block.getLocation(), true);

        if (generatedBlock != null && !generatedBlock.equals(ConstantKeys.COBBLESTONE))
            e.setCancelled(true);
    }

    private boolean canGenerateBlock(Block block) {
        if (ServerVersion.isAtLeast(ServerVersion.v1_16) &&
                block.getWorld().getEnvironment() == World.Environment.NETHER) {
            for (BlockFace blockFace : nearbyFaces) {
                if (block.getRelative(blockFace).getType() == BLUE_ICE_MATERIAL &&
                        block.getRelative(BlockFace.DOWN).getType() == SOUL_SOIL_MATERIAL)
                    return true;
            }
        } else {
            for (BlockFace blockFace : nearbyFaces) {
                if (plugin.getNMSWorld().isWaterLogged(block.getRelative(blockFace)))
                    return true;
            }
        }

        return false;
    }


}
