package com.bgsoftware.superiorskyblock.api.player.respawn;

import com.bgsoftware.superiorskyblock.api.objects.Enumerable;
import com.google.common.base.Preconditions;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public abstract class RespawnAction implements Enumerable {

    private static final Map<String, RespawnAction> respawnActions = new HashMap<>();
    private static int ordinalCounter = 0;

    private final String name;
    private final int ordinal;

    protected RespawnAction(String name) {
        Preconditions.checkArgument(!respawnActions.containsKey(name), "name already exists.");
        this.name = name.toUpperCase(Locale.ENGLISH);
        this.ordinal = ordinalCounter++;
    }

    @Override
    public final int ordinal() {
        return this.ordinal;
    }

    /**
     * Get the name of the respawn action.
     */
    public final String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "RespawnAction{name=" + name + "}";
    }

    public abstract boolean canPerform(PlayerRespawnEvent event);

    public abstract void perform(PlayerRespawnEvent event);

    /**
     * Get all the respawn actions.
     */
    public static Collection<RespawnAction> values() {
        return respawnActions.values();
    }

    /**
     * Get a respawn action by its name.
     *
     * @param name The name to check.
     */
    public static RespawnAction getByName(String name) {
        Preconditions.checkNotNull(name, "name parameter cannot be null.");

        RespawnAction respawnAction = respawnActions.get(name.toUpperCase(Locale.ENGLISH));

        Preconditions.checkNotNull(respawnAction, "Couldn't find an RespawnAction with the name " + name + ".");

        return respawnAction;
    }

    /**
     * Register a new respawn action.
     *
     * @param respawnAction The respawn action to register.
     */
    public static void register(RespawnAction respawnAction) {
        Preconditions.checkNotNull(respawnAction, "respawnAction parameter cannot be null.");

        String name = respawnAction.getName().toUpperCase(Locale.ENGLISH);

        Preconditions.checkState(!respawnActions.containsKey(name), "RespawnAction with the name " + name + " already exists.");

        respawnActions.put(name, respawnAction);
    }

}
