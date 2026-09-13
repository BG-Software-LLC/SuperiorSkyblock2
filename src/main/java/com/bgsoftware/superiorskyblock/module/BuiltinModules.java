package com.bgsoftware.superiorskyblock.module;

import com.bgsoftware.superiorskyblock.api.modules.PluginModule;
import com.bgsoftware.superiorskyblock.module.bank.BankModule;
import com.bgsoftware.superiorskyblock.module.generators.GeneratorsModule;
import com.bgsoftware.superiorskyblock.module.missions.MissionsModule;
import com.bgsoftware.superiorskyblock.module.upgrades.UpgradesModule;
import com.bgsoftware.superiorskyblock.module.visit.VisitModule;
import com.bgsoftware.superiorskyblock.module.warps.WarpsModule;

import java.util.Locale;

public class BuiltinModules {

    public static final BankModule BANK = new BankModule();
    public static final GeneratorsModule GENERATORS = new GeneratorsModule();
    public static final MissionsModule MISSIONS = new MissionsModule();
    public static final UpgradesModule UPGRADES = new UpgradesModule();
    public static final VisitModule VISIT = new VisitModule();
    public static final WarpsModule WARPS = new WarpsModule();

    private BuiltinModules() {

    }

    public static PluginModule getBuiltinModule(String name) {
        switch (name.toLowerCase(Locale.ENGLISH)) {
            case "bank":
                return BANK;
            case "generators":
                return GENERATORS;
            case "missions":
                return MISSIONS;
            case "upgrades":
                return UPGRADES;
            case "visit":
                return VISIT;
            case "warps":
                return WARPS;
            default:
                return null;
        }
    }

}
