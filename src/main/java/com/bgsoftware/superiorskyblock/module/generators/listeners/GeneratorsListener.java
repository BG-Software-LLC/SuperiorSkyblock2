package com.bgsoftware.superiorskyblock.module.generators.listeners;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.module.generators.GeneratorsModule;
import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import com.bgsoftware.superiorskyblock.key.Key;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("unused")
public final class GeneratorsListener implements Listener {

    private static final BlockFace[] nearbyFaces = new BlockFace[] {
            BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH
    };

    private final SuperiorSkyblockPlugin plugin;
    private final GeneratorsModule module;

    public GeneratorsListener(SuperiorSkyblockPlugin plugin, GeneratorsModule module){
        this.plugin = plugin;
        this.module = module;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockFormEvent(BlockFromToEvent e){
        if(!module.isEnabled())
            return;

        Block block = e.getToBlock();

        Island island = plugin.getGrid().getIslandAt(block.getLocation());

        if(island == null)
            return;

        if(!e.getBlock().getType().name().contains("LAVA") || !hasWaterNearby(block))
            return;

        World.Environment environment = block.getWorld().getEnvironment();
        Map<String, Integer> generatorAmounts = island.getGeneratorAmounts(environment);

        int totalGeneratorAmounts = island.getGeneratorTotalAmount(environment);

        if(totalGeneratorAmounts == 0)
            return;

        String newState = "COBBLESTONE";

        if(totalGeneratorAmounts == 1){
            newState = generatorAmounts.keySet().iterator().next();
        }
        else{
            int generatedIndex = ThreadLocalRandom.current().nextInt(totalGeneratorAmounts);
            int currentIndex = 0;
            for(Map.Entry<String, Integer> entry : generatorAmounts.entrySet()){
                currentIndex += entry.getValue();
                if(generatedIndex < currentIndex){
                    newState = entry.getKey();
                    break;
                }
            }
        }

        String[] typeSections = newState.split(":");

        /* Block is being placed in BlocksListener#onBlockFromToMonitor
            island.handleBlockPlace(Key.of(newState), 1); */

        if(typeSections[0].contains("COBBLESTONE"))
            return;

        e.setCancelled(true);

        // If the block is a custom block, and the event was cancelled - we need to call the handleBlockPlace manually.
        island.handleBlockPlace(Key.of(newState), 1);

        byte blockData = typeSections.length == 2 ? Byte.parseByte(typeSections[1]) : 0;

        plugin.getNMSWorld().setBlock(block.getLocation(), Material.valueOf(typeSections[0]), blockData);

        plugin.getNMSWorld().playGeneratorSound(block.getLocation());
    }

    private boolean hasWaterNearby(Block block){
        if(ServerVersion.isAtLeast(ServerVersion.v1_16) &&
                block.getWorld().getEnvironment() == World.Environment.NETHER) {
            for (BlockFace blockFace : nearbyFaces) {
                if (block.getRelative(blockFace).getType().name().equals("BLUE_ICE"))
                    return true;
            }
        }
        else{
            for (BlockFace blockFace : nearbyFaces) {
                if (plugin.getNMSWorld().isWaterLogged(block.getRelative(blockFace)))
                    return true;
            }
        }

        return false;
    }


}
