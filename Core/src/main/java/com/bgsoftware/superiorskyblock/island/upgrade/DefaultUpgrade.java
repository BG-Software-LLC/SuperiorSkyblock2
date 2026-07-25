package com.bgsoftware.superiorskyblock.island.upgrade;

public class DefaultUpgrade extends SUpgrade {

    private static final DefaultUpgrade INSTANCE = new DefaultUpgrade();

    private DefaultUpgrade() {
        super("DEFAULT");
    }

    public static DefaultUpgrade getInstance() {
        return INSTANCE;
    }

    @Override
    public SUpgradeLevel getUpgradeLevel(int level) {
        return DefaultUpgradeLevel.getInstance();
    }

    @Override
    public int getMaxUpgradeLevel() {
        return 1;
    }

    @Override
    public void addUpgradeLevel(int level, SUpgradeLevel upgradeLevel) {
        // Not supported for the default upgrade.
    }
}
