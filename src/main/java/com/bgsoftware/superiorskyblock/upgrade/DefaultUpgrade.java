package com.bgsoftware.superiorskyblock.upgrade;

public final class DefaultUpgrade extends SUpgrade {

    private static final DefaultUpgrade INSTANCE = new DefaultUpgrade();

    private DefaultUpgrade(){
        super("DEFAULT");
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

    public static DefaultUpgrade getInstance() {
        return INSTANCE;
    }
}
