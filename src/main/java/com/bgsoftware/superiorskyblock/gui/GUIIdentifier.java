package com.bgsoftware.superiorskyblock.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class GUIIdentifier implements InventoryHolder {

    private String identifier;

    public GUIIdentifier(String identifier){
        this.identifier = identifier;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof GUIIdentifier && ((GUIIdentifier) obj).identifier.equals(identifier);
    }
}
