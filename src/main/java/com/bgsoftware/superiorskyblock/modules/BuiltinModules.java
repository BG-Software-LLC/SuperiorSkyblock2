package com.bgsoftware.superiorskyblock.modules;

import com.bgsoftware.superiorskyblock.modules.generators.GeneratorsModule;
import com.bgsoftware.superiorskyblock.modules.missions.MissionsModule;

public final class BuiltinModules {

    private BuiltinModules(){

    }

    public static final GeneratorsModule GENERATORS = new GeneratorsModule();
    public static final MissionsModule MISSIONS = new MissionsModule();

}
