package com.bgsoftware.superiorskyblock.listeners;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import org.bukkit.Bukkit;
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

    private SuperiorSkyblockPlugin plugin;

    public GeneratorsListener(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockFormEvent(BlockFromToEvent e){
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

        block.setType(Material.valueOf(typeSections[0]));

        if(typeSections.length == 2)
            //noinspection deprecation
            block.setData(Byte.parseByte(typeSections[1]));

        plugin.getNMSAdapter().playGeneratorSound(block.getLocation());
    }

    private boolean hasWaterNearby(Block block){
        if(block.getRelative(BlockFace.WEST).getType().name().contains("WATER"))
            return true;
        if(block.getRelative(BlockFace.EAST).getType().name().contains("WATER"))
            return true;
        if(block.getRelative(BlockFace.NORTH).getType().name().contains("WATER"))
            return true;
        if(block.getRelative(BlockFace.SOUTH).getType().name().contains("WATER"))
            return true;

        return false;
    }


}
