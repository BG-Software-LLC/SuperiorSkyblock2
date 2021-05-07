package com.bgsoftware.superiorskyblock.modules;

import com.bgsoftware.superiorskyblock.modules.bank.BankModule;
import com.bgsoftware.superiorskyblock.modules.generators.GeneratorsModule;
import com.bgsoftware.superiorskyblock.modules.missions.MissionsModule;

public final class BuiltinModules {

    private BuiltinModules(){

    }

    public static final GeneratorsModule GENERATORS = new GeneratorsModule();
    public static final MissionsModule MISSIONS = new MissionsModule();
    public static final BankModule BANK = new BankModule();

}
