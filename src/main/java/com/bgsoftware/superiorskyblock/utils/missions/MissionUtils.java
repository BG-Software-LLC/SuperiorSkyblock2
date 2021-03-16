package com.bgsoftware.superiorskyblock.utils.missions;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

public final class MissionUtils {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private MissionUtils(){

    }

    public static boolean canDisplayMission(Mission<?> mission, SuperiorPlayer superiorPlayer, boolean removeCompleted){
        if(mission.isOnlyShowIfRequiredCompleted()) {
            if (!plugin.getMissions().hasAllRequiredMissions(superiorPlayer, mission))
                return false;

            if (!plugin.getMissions().canPassAllChecks(superiorPlayer, mission))
                return false;
        }

        if(removeCompleted){
            if(mission.getIslandMission() ? superiorPlayer.getIsland() != null &&
                    !superiorPlayer.getIsland().canCompleteMissionAgain(mission) :
                    !superiorPlayer.canCompleteMissionAgain(mission))
                return false;
        }

        return true;
    }

}
