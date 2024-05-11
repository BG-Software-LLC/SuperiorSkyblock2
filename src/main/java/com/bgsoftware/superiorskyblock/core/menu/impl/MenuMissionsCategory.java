package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.menu.Menu;
import com.bgsoftware.superiorskyblock.api.menu.layout.PagedMenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.menu.view.ViewArgs;
import com.bgsoftware.superiorskyblock.api.missions.IMissionsHolder;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.missions.MissionCategory;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.io.MenuParserImpl;
import com.bgsoftware.superiorskyblock.core.menu.AbstractPagedMenu;
import com.bgsoftware.superiorskyblock.core.menu.MenuIdentifiers;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.MissionsPagedObjectButton;
import com.bgsoftware.superiorskyblock.core.menu.view.AbstractPagedMenuView;
import com.bgsoftware.superiorskyblock.mission.MissionReference;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MenuMissionsCategory extends AbstractPagedMenu<MenuMissionsCategory.View, MenuMissionsCategory.Args, MissionReference> {

    private final boolean sortByCompletion;
    private final boolean removeCompleted;

    private MenuMissionsCategory(MenuParseResult<View> parseResult, boolean sortByCompletion, boolean removeCompleted) {
        super(MenuIdentifiers.MENU_MISSIONS_CATEGORY, parseResult, false);
        this.sortByCompletion = sortByCompletion;
        this.removeCompleted = removeCompleted;
    }

    @Override
    protected View createViewInternal(SuperiorPlayer superiorPlayer, Args args,
                                      @Nullable MenuView<?, ?> previousMenuView) {
        return new View(superiorPlayer, previousMenuView, this, args);
    }

    public void refreshViews(MissionCategory missionCategory) {
        refreshViews(view -> missionCategory.equals(view.missionCategory));
    }

    @Nullable
    public static MenuMissionsCategory createInstance() {
        MenuParseResult<View> menuParseResult = MenuParserImpl.getInstance().loadMenu("missions-category.yml", null,
                new MissionsPagedObjectButton.Builder());

        if (menuParseResult == null) {
            return null;
        }

        MenuPatternSlots menuPatternSlots = menuParseResult.getPatternSlots();
        YamlConfiguration cfg = menuParseResult.getConfig();
        PagedMenuLayout.Builder<View, MissionReference> patternBuilder = (PagedMenuLayout.Builder<View, MissionReference>) menuParseResult.getLayoutBuilder();

        boolean sortByCompletion = cfg.getBoolean("sort-by-completion", false);
        boolean removeCompleted = cfg.getBoolean("remove-completed", false);

        ConfigurationSection soundsSection = cfg.getConfigurationSection("sounds");
        if (soundsSection != null) {
            for (char slotChar : cfg.getString("slots", "").toCharArray()) {
                ConfigurationSection soundSection = soundsSection.getConfigurationSection(slotChar + "");

                if (soundSection == null)
                    continue;

                GameSound completedSound = MenuParserImpl.getInstance().getSound(soundSection.getConfigurationSection("completed"));
                GameSound notCompletedSound = MenuParserImpl.getInstance().getSound(soundSection.getConfigurationSection("not-completed"));
                GameSound canCompleteSound = MenuParserImpl.getInstance().getSound(soundSection.getConfigurationSection("can-complete"));

                patternBuilder.setPagedObjectSlots(menuPatternSlots.getSlots(slotChar), new MissionsPagedObjectButton.Builder()
                        .setCompletedSound(completedSound)
                        .setNotCompletedSound(notCompletedSound)
                        .setCanCompleteSound(canCompleteSound));
            }
        }

        return new MenuMissionsCategory(menuParseResult, sortByCompletion, removeCompleted);
    }

    public static class Args implements ViewArgs {

        private final MissionCategory missionCategory;

        public Args(MissionCategory missionCategory) {
            this.missionCategory = missionCategory;
        }

    }

    public class View extends AbstractPagedMenuView<View, Args, MissionReference> {

        private final MissionCategory missionCategory;
        private final List<MissionReference> missions;

        View(SuperiorPlayer inventoryViewer, @Nullable MenuView<?, ?> previousMenuView,
             Menu<View, Args> menu, Args args) {
            super(inventoryViewer, previousMenuView, menu);

            this.missionCategory = args.missionCategory;

            if (inventoryViewer == null) {
                this.missions = Collections.emptyList();
            } else {
                SequentialListBuilder<Mission<?>> listBuilder = new SequentialListBuilder<>();

                if (sortByCompletion)
                    listBuilder.sorted(Comparator.comparingInt(this::getCompletionStatus));

                this.missions = listBuilder
                        .filter(mission -> plugin.getMissions().canDisplayMission(mission, inventoryViewer, removeCompleted))
                        .map(args.missionCategory.getMissions(), MissionReference::new);
            }
        }

        @Override
        public String replaceTitle(String title) {
            return title.replace("{0}", missionCategory.getName());
        }

        @Override
        protected List<MissionReference> requestObjects() {
            return missions;
        }

        private int getCompletionStatus(Mission<?> mission) {
            SuperiorPlayer inventoryViewer = getInventoryViewer();
            IMissionsHolder missionsHolder = mission.getIslandMission() ? inventoryViewer.getIsland() : inventoryViewer;
            return missionsHolder == null ? 0 :
                    !missionsHolder.canCompleteMissionAgain(mission) ? 2 :
                            plugin.getMissions().canComplete(inventoryViewer, mission) ? 1 : 0;
        }

    }

}
