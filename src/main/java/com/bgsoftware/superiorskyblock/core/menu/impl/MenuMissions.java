package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.menu.OpenMissionCategoryButton;
import com.bgsoftware.superiorskyblock.core.menu.pattern.impl.RegularMenuPattern;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.core.io.MenuParser;

public class MenuMissions extends SuperiorMenu<MenuMissions> {

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

        MenuParseResult menuLoadResult = MenuParser.loadMenu(patternBuilder, "missions.yml", null);

        if (menuLoadResult == null)
            return;

        BukkitExecutor.sync(() -> {
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
