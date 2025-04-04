package com.bgsoftware.superiorskyblock.world.schematic.options;

import com.bgsoftware.superiorskyblock.api.schematic.SchematicOptions;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockOffset;
import com.bgsoftware.superiorskyblock.core.SBlockOffset;

public class SchematicOptionsBuilderImpl implements SchematicOptions.Builder {

    private final String schematicName;
    private BlockOffset blockOffset = SBlockOffset.ZERO;
    private float yaw = 0f;
    private float pitch = 0f;
    private boolean saveAir = false;

    public SchematicOptionsBuilderImpl(String schematicName) {
        this.schematicName = schematicName;
    }

    @Override
    public SchematicOptionsBuilderImpl setOffset(int offsetX, int offsetY, int offsetZ) {
        this.blockOffset = SBlockOffset.fromOffsets(offsetX, offsetY, offsetZ);
        return this;
    }

    @Override
    public SchematicOptionsBuilderImpl setDirection(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
        return this;
    }

    @Override
    public SchematicOptionsBuilderImpl setSaveAir(boolean saveAir) {
        this.saveAir = saveAir;
        return this;
    }

    @Override
    public SchematicOptions build() {
        return new SchematicOptionsImpl(this.schematicName, this.blockOffset, this.yaw, this.pitch, this.saveAir);
    }
}
