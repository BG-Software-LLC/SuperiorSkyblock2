package com.bgsoftware.superiorskyblock.api.island;

public interface PlayerRole {

    int getWeight();

    boolean isHigherThan(PlayerRole role);

    boolean isLessThan(PlayerRole role);

    boolean isFirstRole();

    boolean isLastRole();

    boolean isRoleLadder();

    PlayerRole getNextRole();

    PlayerRole getPreviousRole();


}
