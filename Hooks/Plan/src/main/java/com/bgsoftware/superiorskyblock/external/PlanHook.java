package com.bgsoftware.superiorskyblock.external;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.island.top.SortingTypes;
import com.djrapitops.plan.capability.CapabilityService;
import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.ExtensionService;
import com.djrapitops.plan.extension.annotation.NumberProvider;
import com.djrapitops.plan.extension.annotation.PluginInfo;
import com.djrapitops.plan.extension.annotation.TableProvider;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.table.Table;

import java.math.BigDecimal;
import java.util.List;

public class PlanHook {

    public static void register(SuperiorSkyblockPlugin plugin) {
        try {
            Log.info("Plan detected - attempting to register SuperiorSkyblock DataExtension.");

            if (CapabilityService.getInstance().hasCapability("DATA_EXTENSION_VALUES")) {
                ExtensionService.getInstance().register(new PlanDataExtension(plugin));
                Log.info("Registered SuperiorSkyblock Plan DataExtension.");
            } else {
                Log.warn("Plan installed but DataExtension capability not available - skipping registration.");
            }
        } catch (Throwable t) {
            Log.error(t, "Failed to initialize Plan integration:");
        }
    }

    private PlanHook() {
    }

    @PluginInfo(name = "SuperiorSkyblock")
    private static class PlanDataExtension implements DataExtension {

        private final SuperiorSkyblockPlugin plugin;

        PlanDataExtension(SuperiorSkyblockPlugin plugin) {
            this.plugin = plugin;
        }

        @NumberProvider(
                text = "Total Islands",
                description = "Total number of islands on the server",
                priority = 100
        )
        public long islandCount() {
            return plugin.getGrid().getIslandsContainer().getIslandsAmount();
        }

        @NumberProvider(
                text = "Total Island Level",
                description = "Sum of all island levels",
                priority = 90
        )
        public long totalLevel() {
            return plugin.getGrid().getTotalLevel().toBigInteger().longValue();
        }

        @NumberProvider(
                text = "Total Island Worth",
                description = "Sum of all island worths",
                priority = 80
        )
        public long totalWorth() {
            return plugin.getGrid().getTotalWorth().toBigInteger().longValue();
        }

        @NumberProvider(
                text = "Active Islands",
                description = "Islands that currently have visitors",
                priority = 70
        )
        public long activeIslands() {
            return plugin.getGrid().getIslands().stream()
                    .filter(island -> !island.getIslandVisitors().isEmpty())
                    .count();
        }

        @NumberProvider(
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

        @NumberProvider(
                text = "Average Island Level",
                description = "Average level across all islands",
                priority = 50
        )
        public long averageIslandLevel() {
            List<Island> islands = plugin.getGrid().getIslands();
            if (islands.isEmpty())
                return 0L;

            BigDecimal sum = BigDecimal.ZERO;
            int count = 0;
            for (Island island : islands) {
                BigDecimal lvl = island.getIslandLevel();
                if (lvl != null) {
                    sum = sum.add(lvl);
                    count++;
                }
            }

            if (count == 0)
                return 0L;

            return sum.divide(BigDecimal.valueOf(count), 0, plugin.getSettings().getIslandLevelRoundingMode())
                    .toBigInteger().longValue();
        }

        @TableProvider(
                tableColor = Color.GREEN
        )
        public Table topIslandsTable() {
            List<Island> islands = plugin.getGrid().getIslands(SortingTypes.BY_WORTH);

            Table.Factory table = Table.builder()
                    .columnOne("Pos", Icon.called("list-ol").build())
                    .columnTwo("Owner", Icon.called("user").build())
                    .columnThree("Island", Icon.called("home").build())
                    .columnFour("Level", Icon.called("star").build())
                    .columnFive("Worth", Icon.called("coins").build());

            int pos = 1;
            for (Island island : islands) {
                if (pos > 25)
                    break;

                BigDecimal worth = island.getWorth();
                BigDecimal level = island.getIslandLevel();
                String ownerName = island.getOwner() == null ? "" : island.getOwner().getName();
                String islandName = island.getName() == null || island.getName().isEmpty() ? ownerName : island.getName();

                table.addRow(pos, ownerName, islandName,
                        level == null ? 0L : level.toBigInteger().longValue(),
                        worth == null ? 0L : worth.toBigInteger().longValue()
                );

                pos++;
            }

            return table.build();
        }
    }
}