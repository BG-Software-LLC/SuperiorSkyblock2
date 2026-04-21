package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

/**
 * IslandTeamChatEvent is called when a player talks in island team chat.
 * This event replaces {@link IslandChatEvent} and currently,
 * extends that deprecated class for backward compatibility.
 */
public class IslandTeamChatEvent extends IslandChatEvent {

    /**
     * The constructor of the event.
     *
     * @param island         The island of the player who sent the message.
     * @param superiorPlayer The player who sent the message.
     * @param message        The message that was sent.
     */
    public IslandTeamChatEvent(Island island, SuperiorPlayer superiorPlayer, String message) {
        super(island, superiorPlayer, message);
    }

}
