package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.common.nmsloader.NMSLoadException;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
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
    public EnderDragon getEnderDragon(Island island, Dimension dimension) {
        return getDelegate().getEnderDragon(island, dimension);
    }

    @Override
    public void startDragonBattle(Island island, Location location) {
        getDelegate().startDragonBattle(island, location);
    }

    @Override
    public void removeDragonBattle(Island island, Dimension dimension) {
        getDelegate().removeDragonBattle(island, dimension);
    }

    @Override
    public void awardTheEndAchievement(Player player) {
        getDelegate().awardTheEndAchievement(player);
    }

    private NMSDragonFight getDelegate() {
        if (this.delegate == null) {
            if (plugin.getSettings() == null)
                throw new RuntimeException("Called NMSDragonFightChooser#getDelegate before settings initialized");

            for (Dimension dimension : Dimension.values()) {
                if (dimension.getEnvironment() == World.Environment.THE_END) {
                    SettingsManager.Worlds.DimensionConfig dimensionConfig = plugin.getSettings().getWorlds().getDimensionConfig(dimension);
                    if (dimensionConfig instanceof SettingsManager.Worlds.End &&
                            ((SettingsManager.Worlds.End) dimensionConfig).isDragonFight()) {
                        try {
                            this.delegate = this.enabledInstanceSupplier.get();
                        } catch (NMSLoadException error) {
                            Log.error(error, "Failed to load NMSDragonFight, disabling it...");
                            this.delegate = new NMSDragonFightImpl();
                        }

                        return this.delegate;
                    }
                }
            }

            this.delegate = new NMSDragonFightImpl();
        }


        return this.delegate;
    }

    public interface NMSDragonFightSupplier {

        NMSDragonFight get() throws NMSLoadException;

    }

}
