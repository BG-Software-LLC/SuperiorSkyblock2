package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.common.nmsloader.NMSLoadException;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;

public class NMSDragonFightChooser implements NMSDragonFight {

    private final SuperiorSkyblockPlugin plugin;
    private final NMSDragonFightSupplier enabledInstanceSupplier;
    private NMSDragonFight delegate;

    public NMSDragonFightChooser(SuperiorSkyblockPlugin plugin, NMSDragonFightSupplier enabledInstanceSupplier) {
        this.plugin = plugin;
        this.enabledInstanceSupplier = enabledInstanceSupplier;
    }

    @Override
    public void prepareEndWorld(World bukkitWorld) {
        getDelegate().prepareEndWorld(bukkitWorld);
    }

    @Override
    public EnderDragon getEnderDragon(Island island) {
        return getDelegate().getEnderDragon(island);
    }

    @Override
    public void startDragonBattle(Island island, Location location) {
        getDelegate().startDragonBattle(island, location);
    }

    @Override
    public void removeDragonBattle(Island island) {
        getDelegate().removeDragonBattle(island);
    }

    @Override
    public void awardTheEndAchievement(Player player) {
        getDelegate().awardTheEndAchievement(player);
    }

    private NMSDragonFight getDelegate() {
        if (this.delegate == null) {
            if (plugin.getSettings() == null)
                throw new RuntimeException("Called NMSDragonFightChooser#getDelegate before settings initialized");

            try {
                this.delegate = plugin.getSettings().getWorlds().getEnd().isDragonFight() ?
                        this.enabledInstanceSupplier.get() : new NMSDragonFightImpl();
            } catch (NMSLoadException error) {
                Log.error(error, "Failed to load NMSDragonFight, disabling it...");
                this.delegate = new NMSDragonFightImpl();
            }
        }


        return this.delegate;
    }

    public interface NMSDragonFightSupplier {

        NMSDragonFight get() throws NMSLoadException;

    }

}
