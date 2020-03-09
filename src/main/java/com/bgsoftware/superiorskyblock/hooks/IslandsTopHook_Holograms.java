package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.sainttx.holograms.api.Hologram;
import com.sainttx.holograms.api.HologramManager;
import com.sainttx.holograms.api.line.HologramLine;
import org.bukkit.plugin.java.JavaPlugin;

public final class IslandsTopHook_Holograms extends IslandsTopHook {

    private HologramManager hologramManager;

    public IslandsTopHook_Holograms(){
        hologramManager =  JavaPlugin.getPlugin(com.sainttx.holograms.HologramPlugin.class).getHologramManager();
    }

    @Override
    public void refresh(SortingType sortingType) {
        for(Hologram hologram : hologramManager.getActiveHolograms().values()){
            for(HologramLine hologramLine : hologram.getLines()){
                if(hasPlaceholders(hologramLine.getRaw()) && !hologramLine.isHidden()){
                    hologramLine.hide();
                    hologramLine.show();
                }
            }
        }
    }

}
