package com.bgsoftware.superiorskyblock.nms.player_detector;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventType;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsDispatcher;
import com.bgsoftware.superiorskyblock.core.key.Keys;

import java.util.function.Supplier;

public class IslandPlayerDetectorHelper {

    @Nullable
    private static IslandPrivilege CACHED_TRIAL_VAULT_PRIVILEGE;

    public static void registerListeners(PluginEventsDispatcher dispatcher) {
        dispatcher.registerCallback(PluginEventType.SETTINGS_UPDATE_EVENT, IslandPlayerDetectorHelper::onSettingsUpdate);
    }

    private static void onSettingsUpdate() {
        SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
        // Do not call in static-initializator, otherwise will cause errors
        Key vaultKey = Keys.ofMaterialAndData("VAULT");
        CACHED_TRIAL_VAULT_PRIVILEGE = plugin.getSettings().getInteractablesMap().getRequiredPrivilege(vaultKey);
    }

    public static Supplier<IslandPrivilege> getTrialVaultIslandPrivilege() {
        return () -> CACHED_TRIAL_VAULT_PRIVILEGE;
    }

    private IslandPlayerDetectorHelper() {

    }

}
