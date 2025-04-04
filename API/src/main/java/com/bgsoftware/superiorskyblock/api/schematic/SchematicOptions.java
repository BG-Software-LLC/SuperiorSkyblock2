package com.bgsoftware.superiorskyblock.api.schematic;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;

/**
 * This class represents options for creating schematics.
 */
public interface SchematicOptions {

    /**
     * Get the name of the schematic.
     */
    String getSchematicName();

    /**
     * Get the offset in the x-axis of the schematic.
     */
    int getOffsetX();

    /**
     * Get the offset in the y-axis of the schematic.
     */
    int getOffsetY();

    /**
     * Get the offset in the z-axis of the schematic.
     */
    int getOffsetZ();

    /**
     * Get the yaw of the schematic.
     */
    float getYaw();

    /**
     * Get the pitch of the schematic.
     */
    float getPitch();

    /**
     * Get whether the schematic should save air blocks.
     */
    boolean shouldSaveAir();

    /**
     * Create a new builder for a {@link SchematicOptions} object.
     *
     * @param schematicName The name of the schematic to create.
     */
    static Builder newBuilder(String schematicName) {
        return SuperiorSkyblockAPI.getFactory().createSchematicOptionsBuilder(schematicName);
    }

    interface Builder {

        default Builder setOffsetX(int offsetX) {
            return setOffset(offsetX, 0, 0);
        }

        default Builder setOffsetY(int offsetY) {
            return setOffset(0, offsetY, 0);
        }

        default Builder setOffsetZ(int offsetZ) {
            return setOffset(0, 0, offsetZ);
        }

        Builder setOffset(int offsetX, int offsetY, int offsetZ);

        default Builder setYaw(float yaw) {
            return setDirection(yaw, 0f);
        }

        default Builder setPitch(float pitch) {
            return setDirection(0f, pitch);
        }

        Builder setDirection(float yaw, float pitch);

        Builder setSaveAir(boolean saveAir);

        SchematicOptions build();

    }

}
