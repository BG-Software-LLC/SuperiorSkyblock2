package com.bgsoftware.superiorskyblock.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.menu.button.impl.menu.OpenMissionCategoryButton;
import com.bgsoftware.superiorskyblock.menu.file.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.menu.pattern.impl.RegularMenuPattern;
import com.bgsoftware.superiorskyblock.threads.Executor;
import com.bgsoftware.superiorskyblock.utils.FileUtils;

public final class MenuMissions extends SuperiorMenu<MenuMissions> {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static RegularMenuPattern<MenuMissions> menuPattern;

    private MenuMissions(SuperiorPlayer superiorPlayer) {
        super(menuPattern, superiorPlayer);
    }

    @Override
    public void cloneAndOpen(ISuperiorMenu previousMenu) {
        openInventory(inventoryViewer, previousMenu);
    }

    public static void init() {
        menuPattern = null;

        RegularMenuPattern.Builder<MenuMissions> patternBuilder = new RegularMenuPattern.Builder<>();

        Pair<MenuPatternSlots, CommentedConfiguration> menuLoadResult = FileUtils.loadMenu(patternBuilder,
                "missions.yml", null);

        if (menuLoadResult == null)
            return;

        Executor.sync(() -> {
            plugin.getMissions().getMissionCategories().forEach(missionCategory -> {
                patternBuilder.mapButton(missionCategory.getSlot(), new OpenMissionCategoryButton.Builder()
                        .setMissionsCategory(missionCategory));
            });

            menuPattern = patternBuilder.build();
        }, 1L);
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu) {
        new MenuMissions(superiorPlayer).open(previousMenu);
    }

    public static MenuMissions createEmptyInstance() {
        return new MenuMissions(null);
    }

}
