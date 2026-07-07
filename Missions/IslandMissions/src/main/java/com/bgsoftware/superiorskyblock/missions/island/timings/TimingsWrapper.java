package com.bgsoftware.superiorskyblock.missions.island.timings;

import co.aikar.timings.Timing;
import co.aikar.timings.Timings;
import co.aikar.timings.TimingsManager;
import com.bgsoftware.common.reflection.ReflectMethod;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;

public class TimingsWrapper implements ITimings {

    private static final ReflectMethod<Timing> START_TIMING = new ReflectMethod<>(Timing.class, "startTiming");

    @Nullable
    private final Timing handle;

    public static ITimings create(Plugin plugin, String name) {
        return Timings.isTimingsEnabled() ? new TimingsWrapper(plugin, name) : DummyTimings.INSTANCE;
    }

    private static final ReflectMethod<Timing> TIMINGS_MANAGER_GET_HANDLER_WITH_PROTECT = new ReflectMethod<>(
            TimingsManager.class, "getHandler", String.class, String.class, Timing.class, boolean.class);
    private static final ReflectMethod<Timing> TIMINGS_MANAGER_GET_HANDLER = new ReflectMethod<>(
            TimingsManager.class, "getHandler", String.class, String.class, Timing.class);

    private TimingsWrapper(Plugin plugin, String name) {
        String pluginName = plugin.getName();

        if (TIMINGS_MANAGER_GET_HANDLER_WITH_PROTECT.isValid()) {
            Timing pluginHandler = TIMINGS_MANAGER_GET_HANDLER_WITH_PROTECT.invoke(null,
                    pluginName, "Combined Total", TimingsManager.PLUGIN_GROUP_HANDLER, false);
            this.handle = TIMINGS_MANAGER_GET_HANDLER_WITH_PROTECT.invoke(null,
                    pluginName, name, pluginHandler, true);
        } else if (TIMINGS_MANAGER_GET_HANDLER.isValid()) {
            Timing pluginHandler = TIMINGS_MANAGER_GET_HANDLER.invoke(null,
                    pluginName, "Combined Total", TimingsManager.PLUGIN_GROUP_HANDLER);
            this.handle = TIMINGS_MANAGER_GET_HANDLER.invoke(null,
                    pluginName, name, pluginHandler);
        } else {
            this.handle = null;
        }
    }

    @Override
    public void startTiming() {
        if (this.handle != null) {
            try {
                this.handle.startTiming();
            } catch (Throwable error) {
                START_TIMING.invoke(this.handle);
            }
        }
    }

    @Override
    public void stopTiming() {
        if (this.handle != null)
            this.handle.stopTiming();
    }

}
