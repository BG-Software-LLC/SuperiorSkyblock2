package com.bgsoftware.superiorskyblock.api.player.inventory;

import com.bgsoftware.superiorskyblock.api.objects.Enumerable;
import com.google.common.base.Preconditions;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public abstract class ClearAction implements Enumerable {

    private static final Map<String, ClearAction> clearActions = new HashMap<>();
    private static int ordinalCounter = 0;

    private final String name;
    private final int ordinal;

    protected ClearAction(String name) {
        Preconditions.checkArgument(!clearActions.containsKey(name), "name already exists.");
        this.name = name.toUpperCase(Locale.ENGLISH);
        this.ordinal = ordinalCounter++;
    }

    @Override
    public final int ordinal() {
        return this.ordinal;
    }

    /**
     * Get the name of the clear action.
     */
    public final String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "ClearAction{name=" + name + "}";
    }

    /**
     * Get all the clear actions.
     */
    public static Collection<ClearAction> values() {
        return clearActions.values();
    }

    /**
     * Get a clear action by its name.
     *
     * @param name The name to check.
     */
    public static ClearAction getByName(String name) {
        Preconditions.checkNotNull(name, "name parameter cannot be null.");

        ClearAction clearAction = clearActions.get(name.toUpperCase(Locale.ENGLISH));

        Preconditions.checkNotNull(clearAction, "Couldn't find an ClearAction with the name " + name + ".");

        return clearAction;
    }

    /**
     * Register a new clear action.
     *
     * @param clearAction The clear action to register.
     */
    public static void register(ClearAction clearAction) {
        Preconditions.checkNotNull(clearAction, "clearAction parameter cannot be null.");

        String name = clearAction.getName().toUpperCase(Locale.ENGLISH);

        Preconditions.checkState(!clearActions.containsKey(name), "ClearAction with the name " + name + " already exists.");

        clearActions.put(name, clearAction);
    }

}
