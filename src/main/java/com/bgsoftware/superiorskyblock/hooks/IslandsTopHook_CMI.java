package com.bgsoftware.superiorskyblock.hooks;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Modules.Holograms.CMIHologram;
import com.Zrips.CMI.Modules.Holograms.HologramManager;
import com.bgsoftware.superiorskyblock.api.island.SortingType;

public final class IslandsTopHook_CMI extends IslandsTopHook {

    private HologramManager hologramManager = CMI.getInstance().getHologramManager();

    @Override
    public void refresh(SortingType sortingType) {
        for(CMIHologram hologram : hologramManager.getHolograms().values()){
            for(String line : hologram.getLines()){
                if(hasPlaceholders(line)){
                    hologram.update();
                    break;
                }
            }
        }
    }

}
