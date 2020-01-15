package com.bgsoftware.superiorskyblock.listeners;

import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.listeners.events.BlockGenerateEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("unused")
public final class GeneratorsListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockGenerate(BlockGenerateEvent e){
        Map<String, Integer> amountMap = e.getIsland().getGeneratorAmounts();

        if(amountMap.isEmpty())
            return;

        int size = e.getIsland().getGeneratorTotalAmount();

        boolean onlyOneMaterial = amountMap.values().stream().anyMatch(i -> i == size);

        String[] cachedMaterials = new String[onlyOneMaterial ? 1 : size];

        int slot;

        for(Map.Entry<String, Integer> entry : amountMap.entrySet()){
            int amount = entry.getValue();

            if(onlyOneMaterial){
                cachedMaterials[0] = entry.getKey();
            }
            else {
                for (slot = 0; slot < amount && slot < size; slot++) {
                    cachedMaterials[slot] = entry.getKey();
                }
            }
        }

        if(cachedMaterials.length == 1){
            e.setNewStateKey(Key.of(cachedMaterials[0]));
        }
        else {
            String key = shuffleArray(cachedMaterials)[0];
            e.setNewStateKey(Key.of(key == null ? "COBBLESTONE" : key));
        }
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
