package com.bgsoftware.superiorskyblock.module.generators.listeners;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.core.EnumHelper;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import com.bgsoftware.superiorskyblock.world.Dimensions;
import com.bgsoftware.superiorskyblock.world.GeneratorType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;

import java.util.Optional;

@SuppressWarnings("unused")
public class GeneratorsListener implements Listener {

    private static final Material BASALT_MATERIAL = EnumHelper.getEnum(Material.class, "BASALT");
    private static final Material LAVA_MATERIAL = EnumHelper.getEnum(Material.class, "STATIONARY_LAVA", "LAVA");

    private final SuperiorSkyblockPlugin plugin;

    public GeneratorsListener(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockFormEvent(BlockFormEvent e) {
        if (!BuiltinModules.GENERATORS.isEnabled())
            return;

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location blockLocation = e.getBlock().getLocation(wrapper.getHandle());

            Island island = plugin.getGrid().getIslandAt(blockLocation);

            if (island == null)
                return;

            GeneratorType generatorType = e.getNewState().getType() == BASALT_MATERIAL ?
                    GeneratorType.BASALT : GeneratorType.NORMAL;

            if (e.getBlock().getType() != LAVA_MATERIAL || generatorType != GeneratorType.BASALT)
                return;

            Dimension dimension;
            if (BuiltinModules.GENERATORS.getConfiguration().isMatchGeneratorWorld()) {
                dimension = Dimensions.NETHER;
            } else {
                World blockWorld = blockLocation.getWorld();
                dimension = Optional.ofNullable(plugin.getProviders().getWorldsProvider().getIslandsWorldDimension(blockWorld))
                        .orElseGet(() -> Dimensions.fromEnvironment(blockWorld.getEnvironment()));
            }

            Key generatedBlock = island.generateBlock(blockLocation, dimension, true);

            if (generatedBlock != null && !generatedBlock.equals(generatorType.getDefaultBlock()))
                e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockFromToEvent(BlockFromToEvent e) {
        if (!BuiltinModules.GENERATORS.isEnabled())
            return;

        Block block = e.getToBlock();

        // Should fix solid blocks from generating custom blocks
        // https://github.com/BG-Software-LLC/SuperiorSkyblock2/issues/837
        if (block.getType().isSolid())
            return;

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location blockLocation = block.getLocation(wrapper.getHandle());
            World blockWorld = blockLocation.getWorld();

            Island island = plugin.getGrid().getIslandAt(blockLocation);

            if (island == null)
                return;

            if (e.getBlock().getType() != LAVA_MATERIAL)
                return;

            GeneratorType generatorType = GeneratorType.detectGenerator(block);
            if (generatorType == GeneratorType.NONE)
                return;

            Dimension dimension;
            if (BuiltinModules.GENERATORS.getConfiguration().isMatchGeneratorWorld()) {
                dimension = generatorType == GeneratorType.BASALT ? Dimensions.NETHER : Dimensions.NORMAL;
            } else {
                dimension = Optional.ofNullable(plugin.getProviders().getWorldsProvider().getIslandsWorldDimension(blockWorld))
                        .orElseGet(() -> Dimensions.fromEnvironment(blockWorld.getEnvironment()));
            }

            Key generatedBlock = island.generateBlock(blockLocation, dimension, true);

            if (generatedBlock != null && !generatedBlock.equals(generatorType.getDefaultBlock()))
                e.setCancelled(true);
        }
    }


}
