package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.line.HologramLine;
import com.gmail.filoghost.holographicdisplays.object.CraftHologram;
import com.gmail.filoghost.holographicdisplays.object.NamedHologramManager;
import com.gmail.filoghost.holographicdisplays.object.line.CraftTextLine;

public class IslandsTopHook_HolographicDisplays extends IslandsTopHook {

    @Override
    public void refresh(SortingType sortingType) {
        for(Hologram hologram : NamedHologramManager.getHolograms()){
            if(hasPlaceholders(hologram)) {
                ((CraftHologram) hologram).refreshAll();
            }
        }
    }

    private boolean hasPlaceholders(Hologram hologram){
        for(int i = 0; i < hologram.size(); i++){
            HologramLine hologramLine = hologram.getLine(i);
            if(hologramLine instanceof CraftTextLine && !hasPlaceholders(((CraftTextLine) hologramLine).getText())){
                return false;
            }
        }

        return true;
    }

}
