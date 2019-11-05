package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.handlers.KeysManager;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class KeysHandler implements KeysManager {

    private final SuperiorSkyblockPlugin plugin;

    public KeysHandler(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean isSpawner(Material type) {
        return Materials.SPAWNER.toBukkitType() == type;
    }

    @Override
    public Key getSpawnerKey(ItemStack itemStack) {
        return plugin.getProviders().getSpawnerKey(itemStack);
    }

}
