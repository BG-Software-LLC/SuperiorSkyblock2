package com.bgsoftware.superiorskyblock.config.section;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockOffset;
import com.bgsoftware.superiorskyblock.config.SettingsContainerHolder;
import com.bgsoftware.superiorskyblock.core.SBlockOffset;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.serialization.Serializers;
import org.bukkit.PortalType;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class WorldsSection extends SettingsContainerHolder implements SettingsManager.Worlds {

    @Override
    public Dimension getDefaultWorldDimension() {
        return getContainer().defaultWorldDimension;
    }

    @Override
    @Deprecated
    public World.Environment getDefaultWorld() {
        return getDefaultWorldDimension().getEnvironment();
    }

    @Override
    public String getWorldName() {
        return getContainer().islandWorldName;
    }

    @Override
    public String getDefaultWorldName() {
        return getContainer().defaultWorldName;
    }

    @Override
    public Normal getNormal() {
        return (Normal) getDimensionConfig(Dimension.getByName("NORMAL"));
    }

    @Override
    public Nether getNether() {
        return (Nether) getDimensionConfig(Dimension.getByName("NETHER"));
    }

    @Override
    public End getEnd() {
        return (End) getDimensionConfig(Dimension.getByName("THE_END"));
    }

    @Override
    public String getDifficulty() {
        return getContainer().worldsDifficulty;
    }

    @Override
    public int getSeaLevelHeight() {
        return getContainer().seaLevelHeight;
    }

    @Override
    public DimensionConfig getDimensionConfig(Dimension dimension) {
        return getContainer().dimensionConfigs.get(dimension);
    }

    public static abstract class BaseDimensionConfig implements DimensionConfig {

        private final boolean isEnabled;
        private final boolean isUnlocked;
        private final boolean isSchematicOffset;
        private final String biome;
        private final String name;
        private final Map<PortalType, Dimension> portalAgents;

        protected BaseDimensionConfig(ConfigurationSection section, Dimension dimension, String defaultName) {
            this.isEnabled = section.getBoolean("enabled");
            this.isUnlocked = section.getBoolean("unlock");
            this.isSchematicOffset = section.getBoolean("schematic-offset");
            this.biome = section.getString("biome");
            String name = section.getString("name");
            this.name = Text.isBlank(name) ? defaultName : name;
            this.portalAgents = loadPortalAgents(section.getConfigurationSection("portals"), dimension);
        }

        private static Map<PortalType, Dimension> loadPortalAgents(@Nullable ConfigurationSection portals, Dimension dimension) {
            EnumMap<PortalType, Dimension> portalAgents = new EnumMap<>(PortalType.class);
            if (portals == null) {
                // Map environment to dimension
                Dimension normalDimension;
                Dimension netherDimension;
                Dimension endDimension;
                {
                    EnumMap<World.Environment, Dimension> environmentToDimension = new EnumMap<>(World.Environment.class);
                    for (Dimension dim : Dimension.values()) {
                        Dimension currentDimension = environmentToDimension.get(dim.getEnvironment());
                        if (currentDimension == null || dim.ordinal() < currentDimension.ordinal())
                            environmentToDimension.put(dim.getEnvironment(), dim);
                    }
                    normalDimension = environmentToDimension.get(World.Environment.NORMAL);
                    netherDimension = environmentToDimension.get(World.Environment.NETHER);
                    endDimension = environmentToDimension.get(World.Environment.THE_END);
                }
                // Set portal agent
                switch (dimension.getEnvironment()) {
                    case NORMAL:
                        if (netherDimension != null)
                            portalAgents.put(PortalType.NETHER, netherDimension);
                        if (endDimension != null)
                            portalAgents.put(PortalType.ENDER, endDimension);
                        break;
                    case NETHER:
                        if (normalDimension != null)
                            portalAgents.put(PortalType.NETHER, normalDimension);
                        if (endDimension != null)
                            portalAgents.put(PortalType.ENDER, endDimension);
                        break;
                    case THE_END:
                        if (netherDimension != null)
                            portalAgents.put(PortalType.NETHER, netherDimension);
                        if (normalDimension != null)
                            portalAgents.put(PortalType.ENDER, normalDimension);
                        break;
                }
            } else {
                for (PortalType portalType : PortalType.values()) {
                    String destination = portals.getString(portalType.name());
                    if (destination != null) {
                        try {
                            portalAgents.put(portalType, Dimension.getByName(destination));
                        } catch (NullPointerException error) {
                            Log.warnFromFile("config.yml", "Cannot find valid dimension ", destination, " - skipping...");
                        }
                    }
                }
            }
            return portalAgents.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(portalAgents);
        }

        @Override
        public boolean isEnabled() {
            return this.isEnabled;
        }

        @Override
        public boolean isUnlocked() {
            return this.isUnlocked;
        }

        @Override
        public boolean isSchematicOffset() {
            return this.isSchematicOffset;
        }

        @Override
        public String getBiome() {
            return this.biome;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public Dimension getPortalDestination(PortalType portalType) {
            return this.portalAgents.get(portalType);
        }
    }

    public static class NormalDimensionConfig extends BaseDimensionConfig implements Normal {

        public NormalDimensionConfig(ConfigurationSection section, Dimension dimension, String defaultName) {
            super(section, dimension, defaultName);
        }

    }

    public static class NetherDimensionConfig extends BaseDimensionConfig implements Nether {

        public NetherDimensionConfig(ConfigurationSection section, Dimension dimension, String defaultName) {
            super(section, dimension, defaultName);
        }

    }

    public static class EndDimensionConfig extends BaseDimensionConfig implements End {

        private final boolean isDragonFlight;
        private final BlockOffset portalOffset;

        public EndDimensionConfig(ConfigurationSection section, Dimension dimension, String defaultName) {
            super(section, dimension, defaultName);
            this.isDragonFlight = section.getBoolean("dragon-fight.enabled") && ServerVersion.isAtLeast(ServerVersion.v1_9);
            String portalOffset = section.getString("dragon-fight.portal-offset");
            BlockOffset endDragonFightPortalOffset = Serializers.OFFSET_SPACED_SERIALIZER.deserialize(portalOffset);
            if (endDragonFightPortalOffset == null) {
                Log.warnFromFile("config.yml", "Cannot parse portal-offset '", portalOffset, "' to a valid offset, skipping...");
                this.portalOffset = SBlockOffset.ZERO;
            } else {
                this.portalOffset = endDragonFightPortalOffset;
            }
        }

        @Override
        public boolean isDragonFight() {
            return this.isDragonFlight;
        }

        @Override
        public BlockOffset getPortalOffset() {
            return this.portalOffset;
        }
    }

}
