package com.bgsoftware.superiorskyblock.config.section;

import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockOffset;
import com.bgsoftware.superiorskyblock.config.SettingsContainerHolder;
import com.bgsoftware.superiorskyblock.core.SBlockOffset;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.serialization.Serializers;
import com.bgsoftware.superiorskyblock.world.Dimensions;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Locale;

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
        return (Normal) getDimensionConfig(Dimensions.NORMAL);
    }

    @Override
    public Nether getNether() {
        return (Nether) getDimensionConfig(Dimensions.NETHER);
    }

    @Override
    public End getEnd() {
        return (End) getDimensionConfig(Dimensions.THE_END);
    }

    @Override
    public String getDifficulty() {
        return getContainer().worldsDifficulty;
    }

    @Override
    public DimensionConfig getDimensionConfig(Dimension dimension) {
        return getContainer().dimensionConfigs.get(dimension);
    }

    public static class DefaultDimensionConfig implements DimensionConfig {

        private final boolean isEnabled;
        private final boolean isUnlocked;
        private final boolean isSchematicOffset;
        private final String biome;
        private final String name;

        public DefaultDimensionConfig(ConfigurationSection section, String defaultName) {
            this(section.getBoolean("enabled"), section.getBoolean("unlock"),
                    section.getBoolean("schematic-offset"), section.getString("biome"),
                    section.getString("name"), defaultName);
        }

        public DefaultDimensionConfig(boolean isEnabled, boolean isUnlocked, boolean isSchematicOffset,
                                      String biome, String name, String defaultName) {
            this.isEnabled = isEnabled;
            this.isUnlocked = isUnlocked;
            this.isSchematicOffset = isSchematicOffset;
            this.biome = biome.toUpperCase(Locale.ENGLISH);
            this.name = Text.isBlank(name) ? defaultName : name;
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
    }

    public static class NormalDimensionConfig extends DefaultDimensionConfig implements Normal {

        public NormalDimensionConfig(ConfigurationSection section, String defaultName) {
            super(section, defaultName);
        }

        public NormalDimensionConfig(boolean isEnabled, boolean isUnlocked, boolean isSchematicOffset,
                                     String biome, String name, String defaultName) {
            super(isEnabled, isUnlocked, isSchematicOffset, biome, name, defaultName);
        }

    }

    public static class NetherDimensionConfig extends DefaultDimensionConfig implements Nether {

        public NetherDimensionConfig(ConfigurationSection section, String defaultName) {
            super(section, defaultName + "_nether");
        }

        public NetherDimensionConfig(boolean isEnabled, boolean isUnlocked, boolean isSchematicOffset,
                                     String biome, String name, String defaultName) {
            super(isEnabled, isUnlocked, isSchematicOffset, biome, name, defaultName + "_nether");
        }

    }

    public static class EndDimensionConfig extends DefaultDimensionConfig implements End {

        private final boolean isDragonFlight;
        private final BlockOffset portalOffset;

        public EndDimensionConfig(ConfigurationSection section, String defaultName) {
            super(section, defaultName + "_the_end");
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

        public EndDimensionConfig(boolean isEnabled, boolean isUnlocked, boolean isSchematicOffset,
                                  String biome, String name, String defaultName, boolean isDragonFlight,
                                  BlockOffset portalOffset) {
            super(isEnabled, isUnlocked, isSchematicOffset, biome, name, defaultName + "_the_end");
            this.isDragonFlight = isDragonFlight && ServerVersion.isAtLeast(ServerVersion.v1_9);
            this.portalOffset = portalOffset;
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
