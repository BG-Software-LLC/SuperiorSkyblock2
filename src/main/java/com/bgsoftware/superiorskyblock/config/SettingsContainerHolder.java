package com.bgsoftware.superiorskyblock.config;

public abstract class SettingsContainerHolder {

    private SettingsContainer container;

    protected SettingsContainerHolder() {

    }

    public void setContainer(SettingsContainer container) {
        this.container = container;
    }

    protected SettingsContainer getContainer() {
        if (this.container == null)
            throw new IllegalStateException("Cannot access SettingsManager before initialization");
        return this.container;
    }

}
