package com.bgsoftware.superiorskyblock.world.schematic.options;

import com.bgsoftware.superiorskyblock.api.schematic.SchematicOptions;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockOffset;

public class SchematicOptionsImpl implements SchematicOptions {

    private final String schematicName;
    private final BlockOffset blockOffset;
    private final float yaw;
    private final float pitch;
    private final boolean saveAir;

    public SchematicOptionsImpl(String schematicName, BlockOffset blockOffset, float yaw, float pitch, boolean saveAir) {
        this.schematicName = schematicName;
        this.blockOffset = blockOffset;
        this.yaw = yaw;
        this.pitch = pitch;
        this.saveAir = saveAir;
    }

    @Override
    public String getSchematicName() {
        return this.schematicName;
    }

    @Override
    public int getOffsetX() {
        return this.blockOffset.getOffsetX();
    }

    @Override
    public int getOffsetY() {
        return this.blockOffset.getOffsetY();
    }

    @Override
    public int getOffsetZ() {
        return this.blockOffset.getOffsetZ();
    }

    @Override
    public float getYaw() {
        return this.yaw;
    }

    @Override
    public float getPitch() {
        return this.pitch;
    }

    @Override
    public boolean shouldSaveAir() {
        return this.saveAir;
    }

}
