package com.bgsoftware.superiorskyblock.listeners;

import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.listeners.events.BlockGenerateEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("unused")
public final class GeneratorsListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockGenerate(BlockGenerateEvent e){
        Map<String, Integer> percentageMap = e.getIsland().getGeneratorPercentages();

        if(percentageMap.isEmpty())
            return;

        String[] cachedMaterials = new String[100];
        Arrays.fill(cachedMaterials, "COBBLESTONE");

        int slot = 0;

        for(Map.Entry<String, Integer> entry : percentageMap.entrySet()){
            int amount = entry.getValue();
            for(int i = 0; i < amount && slot < 100; i++){
                cachedMaterials[slot++] = entry.getKey();
            }
        }

        e.setNewStateKey(Key.of(shuffleArray(cachedMaterials)[0]));
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
