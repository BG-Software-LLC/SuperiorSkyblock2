package com.bgsoftware.superiorskyblock.core;

public enum PluginLoadingStage {

    START,
    API_INITIALIZED,
    SUPPORTED_SERVER_SOFTWARE,
    NMS_INITIALIZED,
    LOADED,
    START_ENABLE,
    SETTINGS_INITIALIZED,
    MODULES_INITIALIZED,
    COMMANDS_INITIALIZED,
    WORLDS_PREPARED,
    MANAGERS_INITIALIZED,
    EVENTS_INITIALIZED,
    CHUNKS_PROVIDER_INITIALIZED,
    ENABLED;

    public boolean isAtLeast(PluginLoadingStage other) {
        return this.ordinal() >= other.ordinal();
    }

    public PluginLoadingStage next() {
        try {
            return PluginLoadingStage.values()[this.ordinal() + 1];
        } catch (ArrayIndexOutOfBoundsException error) {
            return this;
        }
    }

}
