package com.bgsoftware.superiorskyblock.listeners;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.utils.key.Key;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("unused")
public final class GeneratorsListener implements Listener {

    private static final BlockFace[] nearbyFaces = new BlockFace[] {
            BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH
    };

    private final SuperiorSkyblockPlugin plugin;

    public GeneratorsListener(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockFormEvent(BlockFromToEvent e){
        if(!plugin.getSettings().generators)
            return;
        
        Block block = e.getToBlock();

        Island island = plugin.getGrid().getIslandAt(block.getLocation());

        if(island == null)
            return;

        if(!e.getBlock().getType().name().contains("LAVA") || !hasWaterNearby(block))
            return;

        String[] cachedMaterials = island.getGeneratorArray();

        if(cachedMaterials.length == 0)
            return;

        boolean onlyOneMaterial = Arrays.stream(cachedMaterials).allMatch(s -> cachedMaterials[0].equals(s));

        String newState;

        if(cachedMaterials.length == 1){
            newState = cachedMaterials[0];
        }
        else {
            newState = cachedMaterials[ThreadLocalRandom.current().nextInt(cachedMaterials.length)];
        }

        String[] typeSections = newState.split(":");

        island.handleBlockPlace(Key.of(newState), 1);

        if(typeSections[0].contains("COBBLESTONE"))
            return;

        e.setCancelled(true);


        byte blockData = typeSections.length == 2 ? Byte.parseByte(typeSections[1]) : 0;

        plugin.getNMSBlocks().setBlock(block.getLocation(), Material.valueOf(typeSections[0]), blockData);

        plugin.getNMSAdapter().playGeneratorSound(block.getLocation());
    }

    private boolean hasWaterNearby(Block block){
        for(BlockFace blockFace : nearbyFaces) {
            if (block.getRelative(blockFace).getType().name().contains("WATER"))
                return true;
        }

        return false;
    }


}
