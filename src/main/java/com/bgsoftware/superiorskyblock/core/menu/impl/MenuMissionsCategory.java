package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.missions.IMissionsHolder;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.missions.MissionCategory;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.io.MenuParser;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.core.menu.PagedSuperiorMenu;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.menu.MissionsPagedObjectButton;
import com.bgsoftware.superiorskyblock.core.menu.pattern.impl.PagedMenuPattern;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MenuMissionsCategory extends PagedSuperiorMenu<MenuMissionsCategory, Mission<?>> {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static PagedMenuPattern<MenuMissionsCategory, Mission<?>> menuPattern;

    private static boolean sortByCompletion;
    private static boolean removeCompleted;

    private final MissionCategory missionCategory;
    private final List<Mission<?>> missions;

    private MenuMissionsCategory(SuperiorPlayer superiorPlayer, MissionCategory missionCategory) {
        super(menuPattern, superiorPlayer);

        this.missionCategory = missionCategory;

        if (superiorPlayer == null) {
            this.missions = Collections.emptyList();
        } else {
            SequentialListBuilder<Mission<?>> listBuilder = new SequentialListBuilder<>();

            if (sortByCompletion)
                listBuilder.sorted(Comparator.comparingInt(this::getCompletionStatus));

            this.missions = listBuilder
                    .filter(mission -> plugin.getMissions().canDisplayMission(mission, superiorPlayer, removeCompleted))
                    .build(missionCategory.getMissions());
        }
    }

    @Override
    public void cloneAndOpen(ISuperiorMenu previousMenu) {
        openInventory(inventoryViewer, previousMenu, missionCategory);
    }

    @Override
    protected String replaceTitle(String title) {
        return title.replace("{0}", missionCategory.getName());
    }

    @Override
    protected List<Mission<?>> requestObjects() {
        return missions;
    }

    private int getCompletionStatus(Mission<?> mission) {
        IMissionsHolder missionsHolder = mission.getIslandMission() ? inventoryViewer.getIsland() : inventoryViewer;
        return missionsHolder == null ? 0 :
                !missionsHolder.canCompleteMissionAgain(mission) ? 2 :
                        plugin.getMissions().canComplete(inventoryViewer, mission) ? 1 : 0;
    }

    public static void init() {
        menuPattern = null;

        PagedMenuPattern.Builder<MenuMissionsCategory, Mission<?>> patternBuilder = new PagedMenuPattern.Builder<>();

        MenuParseResult menuLoadResult = MenuParser.loadMenu(patternBuilder, "missions-category.yml", null);

        if (menuLoadResult == null)
            return;

        MenuPatternSlots menuPatternSlots = menuLoadResult.getPatternSlots();
        CommentedConfiguration cfg = menuLoadResult.getConfig();

        sortByCompletion = cfg.getBoolean("sort-by-completion", false);
        removeCompleted = cfg.getBoolean("remove-completed", false);

        ConfigurationSection soundsSection = cfg.getConfigurationSection("sounds");

        if (soundsSection != null) {
            for (char slotChar : cfg.getString("slots", "").toCharArray()) {
                ConfigurationSection soundSection = soundsSection.getConfigurationSection(slotChar + "");

                if (soundSection == null)
                    continue;

                GameSound completedSound = MenuParser.getSound(soundSection.getConfigurationSection("completed"));
                GameSound notCompletedSound = MenuParser.getSound(soundSection.getConfigurationSection("not-completed"));
                GameSound canCompleteSound = MenuParser.getSound(soundSection.getConfigurationSection("can-complete"));

                patternBuilder.setPagedObjectSlots(menuPatternSlots.getSlots(slotChar), new MissionsPagedObjectButton.Builder()
                        .setCompletedSound(completedSound)
                        .setNotCompletedSound(notCompletedSound)
                        .setCanCompleteSound(canCompleteSound));
            }
        }

        menuPattern = patternBuilder
                .setPreviousPageSlots(getSlots(cfg, "previous-page", menuPatternSlots))
                .setCurrentPageSlots(getSlots(cfg, "current-page", menuPatternSlots))
                .setNextPageSlots(getSlots(cfg, "next-page", menuPatternSlots))
                .build();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu, MissionCategory missionCategory) {
        new MenuMissionsCategory(superiorPlayer, missionCategory).open(previousMenu);
    }

    public static void refreshMenus(MissionCategory missionCategory) {
        refreshMenus(MenuMissionsCategory.class, superiorMenu -> missionCategory.equals(superiorMenu.missionCategory));
    }

}