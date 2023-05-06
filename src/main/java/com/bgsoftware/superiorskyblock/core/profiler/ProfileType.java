package com.bgsoftware.superiorskyblock.core.profiler;

import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;

public enum ProfileType {

    CREATE_ISLAND,
    DISBAND_ISLAND,
    SCHEMATIC_PLACE;

    private final LazyReference<String> prettyName = new LazyReference<String>() {
        @Override
        protected String create() {
            return Formatters.CAPITALIZED_FORMATTER.format(ProfileType.this.name());
        }
    };

    ProfileType() {

    }

    public String getPrettyName() {
        return this.prettyName.get();
    }

}
