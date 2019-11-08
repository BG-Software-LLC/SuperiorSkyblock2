package com.bgsoftware.superiorskyblock.listeners;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("unused")
public final class GeneratorsListener implements Listener {

    private final SuperiorSkyblockPlugin plugin;

    public GeneratorsListener(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockGenerate(BlockFormEvent e){
        if(e.getNewState().getType() != Material.COBBLESTONE)
            return;

        Island island = plugin.getGrid().getIslandAt(e.getNewState().getLocation());

        if(island == null)
            return;

        Map<String, Integer> percentageMap = island.getGeneratorPercentages();

        if(percentageMap.isEmpty())
            return;

        e.setCancelled(true);

        Executor.async(() -> {
            String[] cachedMaterials = new String[100];
            Arrays.fill(cachedMaterials, "COBBLESTONE");

            int slot = 0;

            for(Map.Entry<String, Integer> entry : percentageMap.entrySet()){
                int amount = entry.getValue();
                for(int i = 0; i < amount && slot < 100; i++){
                    cachedMaterials[slot++] = entry.getKey();
                }
            }

            String[] typeSections = shuffleArray(cachedMaterials)[0].split(":");

            Executor.sync(() -> {
                e.getBlock().setType(Material.valueOf(typeSections[0]));
                if(typeSections.length == 2)
                    //noinspection deprecation
                    e.getBlock().setData(Byte.parseByte(typeSections[1]));

                island.handleBlockPlace(Key.of(cachedMaterials[0]), 1);

                plugin.getNMSAdapter().playGeneratorSound(e.getBlock().getLocation());
            });
        });
    }

    private String[] shuffleArray(String[] array) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int i = 0; i < array.length; i++) {
            int randomPosition = random.nextInt(array.length);
            String temp = array[i];
            array[i] = array[randomPosition];
            array[randomPosition] = temp;
        }

        return array;
    }


}
