package com.bgsoftware.superiorskyblock.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.missions.IMissionsHolder;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.missions.MissionCategory;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.PagedSuperiorMenu;
import com.bgsoftware.superiorskyblock.menu.button.impl.menu.MissionsPagedObjectButton;
import com.bgsoftware.superiorskyblock.menu.file.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.menu.pattern.impl.PagedMenuPattern;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.debug.PluginDebugger;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class MenuMissionsCategory extends PagedSuperiorMenu<MenuMissionsCategory, Mission<?>> {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static PagedMenuPattern<MenuMissionsCategory, Mission<?>> menuPattern;

    private static boolean sortByCompletion, removeCompleted;

    private final MissionCategory missionCategory;
    private List<Mission<?>> missions;

    private MenuMissionsCategory(SuperiorPlayer superiorPlayer, MissionCategory missionCategory) {
        super(menuPattern, superiorPlayer);

        this.missionCategory = missionCategory;

        if (superiorPlayer != null) {
            this.missions = missionCategory.getMissions().stream()
                    .filter(mission -> plugin.getMissions().canDisplayMission(mission, superiorPlayer, removeCompleted))
                    .collect(Collectors.toList());
            if (sortByCompletion)
                this.missions.sort(Comparator.comparingInt(this::getCompletionStatus));
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

        Pair<MenuPatternSlots, CommentedConfiguration> menuLoadResult = FileUtils.loadMenu(patternBuilder,
                "missions-category.yml", null);

        if (menuLoadResult == null)
            return;

        MenuPatternSlots menuPatternSlots = menuLoadResult.getKey();
        CommentedConfiguration cfg = menuLoadResult.getValue();

        sortByCompletion = cfg.getBoolean("sort-by-completion", false);
        removeCompleted = cfg.getBoolean("remove-completed", false);

        ConfigurationSection soundsSection = cfg.getConfigurationSection("sounds");

        if (soundsSection != null) {
            for (char slotChar : cfg.getString("slots", "").toCharArray()) {
                ConfigurationSection soundSection = soundsSection.getConfigurationSection(slotChar + "");

                if (soundSection == null)
                    continue;

                SoundWrapper completedSound = FileUtils.getSound(soundSection.getConfigurationSection("completed"));
                SoundWrapper notCompletedSound = FileUtils.getSound(soundSection.getConfigurationSection("not-completed"));
                SoundWrapper canCompleteSound = FileUtils.getSound(soundSection.getConfigurationSection("can-complete"));

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