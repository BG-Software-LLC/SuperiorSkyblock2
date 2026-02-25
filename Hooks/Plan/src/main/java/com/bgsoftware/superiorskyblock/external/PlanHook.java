package com.bgsoftware.superiorskyblock.external;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import org.bukkit.Bukkit;

import java.math.BigDecimal;
import java.util.*;

public class PlanHook {

    public static void register(SuperiorSkyblockPlugin plugin) {
        if (!Bukkit.getPluginManager().isPluginEnabled("Plan")) {
            return;
        }

        try {
            Log.info("Plan detected - attempting to register SuperiorSkyblock DataExtension.");

            try {
                if (com.djrapitops.plan.capability.CapabilityService.getInstance()
                        .hasCapability("DATA_EXTENSION_VALUES")) {
                    com.djrapitops.plan.extension.ExtensionService.getInstance().register(
                            new PlanDataExtension(plugin)
                    );
                    Log.info("Registered SuperiorSkyblock Plan DataExtension.");
                } else {
                    Log.info("Plan installed but DataExtension capability not available - skipping registration.");
                }
            } catch (NoClassDefFoundError | IllegalStateException ex) {
                Log.error(ex, "Plan API not available or not enabled; skipping Plan integration.");
            }

        } catch (Throwable t) {
            Log.error(t, "Failed to initialize Plan integration:");
        }
    }

    private PlanHook() {
    }

    @com.djrapitops.plan.extension.annotation.PluginInfo(name = "SuperiorSkyblock")
    private static class PlanDataExtension implements com.djrapitops.plan.extension.DataExtension {

        private final SuperiorSkyblockPlugin plugin;

        PlanDataExtension(SuperiorSkyblockPlugin plugin) {
            this.plugin = plugin;
        }

        @com.djrapitops.plan.extension.annotation.NumberProvider(
                text = "Total Islands",
                description = "Total number of islands on the server",
                priority = 100
        )
        public long islandCount() {
            Collection<Island> islands = plugin.getGrid().getIslands();
            return islands == null ? 0L : islands.size();
        }

        @com.djrapitops.plan.extension.annotation.NumberProvider(
                text = "Total Island Level",
                description = "Sum of all island levels",
                priority = 90
        )
        public long totalLevel() {
            BigDecimal total = plugin.getGrid().getTotalLevel();
            return total == null ? 0L : total.toBigInteger().longValue();
        }

        @com.djrapitops.plan.extension.annotation.NumberProvider(
                text = "Total Island Worth",
                description = "Sum of all island worths",
                priority = 80
        )
        public long totalWorth() {
            BigDecimal total = plugin.getGrid().getTotalWorth();
            return total == null ? 0L : total.toBigInteger().longValue();
        }

        @com.djrapitops.plan.extension.annotation.NumberProvider(
                text = "Active Islands",
                description = "Islands that currently have visitors",
                priority = 70
        )
        public long activeIslands() {
            return plugin.getGrid().getIslands().stream()
                    .filter(island -> !island.getIslandVisitors().isEmpty())
                    .count();
        }

        @com.djrapitops.plan.extension.annotation.NumberProvider(
                text = "Islands With Bank Balance",
                description = "Islands that have money in their bank",
                priority = 60
        )
        public long islandsWithBankBalance() {
            return plugin.getGrid().getIslands().stream()
                    .filter(island -> island.getIslandBank() != null
                            && island.getIslandBank().getBalance().compareTo(BigDecimal.ZERO) > 0)
                    .count();
        }

        @com.djrapitops.plan.extension.annotation.NumberProvider(
                text = "Average Island Level",
                description = "Average level across all islands",
                priority = 50
        )
        public long averageIslandLevel() {
            List<Island> islands = new ArrayList<>(plugin.getGrid().getIslands());
            if (islands.isEmpty()) return 0L;
            BigDecimal sum = BigDecimal.ZERO;
            int count = 0;
            for (Island island : islands) {
                BigDecimal lvl = island.getIslandLevel();
                if (lvl != null) {
                    sum = sum.add(lvl);
                    count++;
                }
            }
            if (count == 0) return 0L;
            return sum.divide(BigDecimal.valueOf(count), 0, plugin.getSettings().getIslandLevelRoundingMode())
                    .toBigInteger().longValue();
        }

        @com.djrapitops.plan.extension.annotation.TableProvider(
                tableColor = com.djrapitops.plan.extension.icon.Color.GREEN
        )
        public com.djrapitops.plan.extension.table.Table topIslandsTable() {
            List<Island> islands = new ArrayList<>(plugin.getGrid().getIslands());
            islands.sort((a, b) -> b.getWorth().compareTo(a.getWorth()));

            com.djrapitops.plan.extension.table.Table.Factory table =
                    com.djrapitops.plan.extension.table.Table.builder()
                            .columnOne("Pos", com.djrapitops.plan.extension.icon.Icon.called("list-ol").build())
                            .columnTwo("Owner", com.djrapitops.plan.extension.icon.Icon.called("user").build())
                            .columnThree("Island", com.djrapitops.plan.extension.icon.Icon.called("home").build())
                            .columnFour("Level", com.djrapitops.plan.extension.icon.Icon.called("star").build())
                            .columnFive("Worth", com.djrapitops.plan.extension.icon.Icon.called("coins").build());

            int pos = 1;
            for (Island island : islands) {
                if (pos > 25) break;
                BigDecimal worth = island.getWorth();
                BigDecimal level = island.getIslandLevel();
                String ownerName = island.getOwner() == null ? "" : island.getOwner().getName();
                String islandName = island.getName() == null || island.getName().isEmpty() ? ownerName : island.getName();
                table.addRow(
                        pos,
                        ownerName,
                        islandName,
                        level == null ? 0L : level.toBigInteger().longValue(),
                        worth == null ? 0L : worth.toBigInteger().longValue()
                );
                pos++;
            }
            return table.build();
        }
    }
}