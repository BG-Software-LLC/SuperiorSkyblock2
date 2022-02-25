package com.bgsoftware.superiorskyblock.module.generators.listeners;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.key.Key;
import com.bgsoftware.superiorskyblock.module.generators.GeneratorsModule;
import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import com.bgsoftware.superiorskyblock.utils.debug.PluginDebugger;
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

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

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

        if (performBlockGeneration(e.getBlock(), island))
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

        if (performBlockGeneration(block, island))
            e.setCancelled(true);
    }

    private boolean performBlockGeneration(Block block, Island island) {
        World.Environment environment = block.getWorld().getEnvironment();
        Map<String, Integer> generatorAmounts = island.getGeneratorAmounts(environment);

        int totalGeneratorAmounts = island.getGeneratorTotalAmount(environment);

        if (totalGeneratorAmounts == 0)
            return false;

        String newState = "COBBLESTONE";

        if (totalGeneratorAmounts == 1) {
            newState = generatorAmounts.keySet().iterator().next();
        } else {
            int generatedIndex = ThreadLocalRandom.current().nextInt(totalGeneratorAmounts);
            int currentIndex = 0;
            for (Map.Entry<String, Integer> entry : generatorAmounts.entrySet()) {
                currentIndex += entry.getValue();
                if (generatedIndex < currentIndex) {
                    newState = entry.getKey();
                    break;
                }
            }
        }

        String[] typeSections = newState.split(":");

        /* Block is being placed in BlocksListener#onBlockFromToMonitor
            island.handleBlockPlace(Key.of(newState), 1); */

        if (typeSections[0].contains("COBBLESTONE"))
            return false;

        // If the block is a custom block, and the event was cancelled - we need to call the handleBlockPlace manually.
        island.handleBlockPlace(Key.of(newState), 1);

        Material generateBlockType = Material.valueOf(typeSections[0]);
        byte blockData = typeSections.length == 2 ? Byte.parseByte(typeSections[1]) : 0;
        int combinedId = plugin.getNMSAlgorithms().getCombinedId(generateBlockType, blockData);

        if (combinedId == -1) {
            SuperiorSkyblockPlugin.log("&cFailed to generate block for type " + generateBlockType + ":" + blockData);
            generateBlockType = Material.COBBLESTONE;
            blockData = 0;
            combinedId = plugin.getNMSAlgorithms().getCombinedId(generateBlockType, blockData);
        }

        PluginDebugger.debug("Action: Generate Block, Island: " + island.getOwner().getName() +
                ", Block: " + generateBlockType + ":" + blockData);

        plugin.getNMSWorld().setBlock(block.getLocation(), combinedId);

        plugin.getNMSWorld().playGeneratorSound(block.getLocation());

        return true;
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
