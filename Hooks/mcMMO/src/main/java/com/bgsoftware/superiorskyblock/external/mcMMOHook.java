package com.bgsoftware.superiorskyblock.external;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.gmail.nossr50.datatypes.skills.SubSkillType;
import com.gmail.nossr50.events.skills.secondaryabilities.SubSkillBlockEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.lang.reflect.Method;

public class mcMMOHook {

    private static Object trackerInstance = null;
    private static Method trackMethod = null;

    private static SuperiorSkyblockPlugin plugin;

    public static void register(SuperiorSkyblockPlugin plugin) {
        mcMMOHook.plugin = plugin;

        try {
            Class<?> trackerClass = Class.forName("com.bgsoftware.superiorskyblock.missions.farming.PlantsTracker");
            trackerInstance = trackerClass.getField("INSTANCE").get(null);
            trackMethod = trackerClass.getMethod("track", org.bukkit.block.Block.class, java.util.UUID.class);

            Bukkit.getPluginManager().registerEvents(new mcMMOListener(), plugin);
        } catch (Exception ignored) {
        }
    }

    private static class mcMMOListener implements Listener {

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSubSkillBlock(SubSkillBlockEvent e) {
            if (e.getSubSkillType() != SubSkillType.HERBALISM_GREEN_THUMB) {
                return;
            }

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                try {
                    trackMethod.invoke(trackerInstance, e.getBlock(), e.getPlayer().getUniqueId());
                } catch (Exception ignored) {
                }
            }, 1L);
        }

    }

}
