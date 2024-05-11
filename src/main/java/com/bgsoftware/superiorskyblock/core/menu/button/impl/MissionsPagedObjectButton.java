package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.missions.IMissionsHolder;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSoundImpl;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractPagedMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.button.PagedMenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuMissionsCategory;
import com.bgsoftware.superiorskyblock.mission.MissionData;
import com.bgsoftware.superiorskyblock.mission.MissionReference;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

public class MissionsPagedObjectButton extends AbstractPagedMenuButton<MenuMissionsCategory.View, MissionReference> {

    private MissionsPagedObjectButton(MenuTemplateButton<MenuMissionsCategory.View> templateButton, MenuMissionsCategory.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public Template getTemplate() {
        return (Template) super.getTemplate();
    }

    @Override
    public void onButtonClick(InventoryClickEvent clickEvent) {
        SuperiorPlayer clickedPlayer = menuView.getInventoryViewer();
        Island island = clickedPlayer.getIsland();

        if (island == null)
            return;

        Mission<?> mission = pagedObject.getMission();
        if (mission == null)
            return;

        boolean completed = !island.canCompleteMissionAgain(mission);
        boolean canComplete = plugin.getMissions().canComplete(clickedPlayer, mission);

        GameSound soundToPlay = completed ? getTemplate().completedSound : canComplete ?
                getTemplate().canCompleteSound : getTemplate().notCompletedSound;
        GameSoundImpl.playSound(clickEvent.getWhoClicked(), soundToPlay);

        if (!canComplete || !plugin.getMissions().hasAllRequiredMissions(clickedPlayer, mission))
            return;

        plugin.getMissions().rewardMission(mission, clickedPlayer, false, false, result -> {
            if (result)
                menuView.refreshView();
        });
    }

    @Override
    public ItemStack modifyViewItem(ItemStack buttonItem) {
        Mission<?> mission = pagedObject.getMission();
        if (mission == null)
            return buttonItem;

        Optional<MissionData> missionDataOptional = plugin.getMissions().getMissionData(mission);

        if (!missionDataOptional.isPresent())
            return buttonItem;

        SuperiorPlayer inventoryViewer = menuView.getInventoryViewer();

        MissionData missionData = missionDataOptional.get();
        IMissionsHolder missionsHolder = mission.getIslandMission() ? inventoryViewer.getIsland() : inventoryViewer;

        if (missionsHolder == null)
            return new ItemStack(Material.AIR);

        boolean completed = !missionsHolder.canCompleteMissionAgain(mission);
        int percentage = calculatePercentage(mission.getProgress(inventoryViewer));
        int progressValue = mission.getProgressValue(inventoryViewer);
        int amountCompleted = missionsHolder.getAmountMissionCompleted(mission);

        ItemStack itemStack = completed ? missionData.getCompleted().build(inventoryViewer) :
                plugin.getMissions().canComplete(inventoryViewer, mission) ?
                        missionData.getCanComplete()
                                .replaceAll("{0}", percentage + "")
                                .replaceAll("{1}", progressValue + "")
                                .replaceAll("{2}", amountCompleted + "")
                                .build(inventoryViewer) :
                        missionData.getNotCompleted()
                                .replaceAll("{0}", percentage + "")
                                .replaceAll("{1}", progressValue + "")
                                .replaceAll("{2}", amountCompleted + "")
                                .build(inventoryViewer);

        mission.formatItem(inventoryViewer, itemStack);

        return itemStack;
    }

    private static int calculatePercentage(double progress) {
        return Math.round((float) Math.min(1.0, progress) * 100);
    }

    public static class Builder extends PagedMenuTemplateButtonImpl.AbstractBuilder<MenuMissionsCategory.View, MissionReference> {

        private GameSound notCompletedSound = null;
        private GameSound canCompleteSound = null;

        public Builder setCompletedSound(GameSound completedSound) {
            this.clickSound = completedSound;
            return this;
        }

        public Builder setNotCompletedSound(GameSound notCompletedSound) {
            this.notCompletedSound = notCompletedSound;
            return this;
        }

        public Builder setCanCompleteSound(GameSound canCompleteSound) {
            this.canCompleteSound = canCompleteSound;
            return this;
        }

        @Override
        public PagedMenuTemplateButton<MenuMissionsCategory.View, MissionReference> build() {
            return new Template(buttonItem, commands, requiredPermission, lackPermissionSound, nullItem,
                    getButtonIndex(), clickSound, notCompletedSound, canCompleteSound);
        }

    }

    public static class Template extends PagedMenuTemplateButtonImpl<MenuMissionsCategory.View, MissionReference> {

        private final GameSound completedSound;
        private final GameSound notCompletedSound;
        private final GameSound canCompleteSound;

        Template(TemplateItem buttonItem, List<String> commands, String requiredPermission,
                 GameSound lackPermissionSound, TemplateItem nullItem, int buttonIndex,
                 GameSound completedSound, GameSound notCompletedSound, GameSound canCompleteSound) {
            super(buttonItem, null, commands, requiredPermission, lackPermissionSound, nullItem, buttonIndex,
                    MissionsPagedObjectButton.class, MissionsPagedObjectButton::new);
            this.completedSound = completedSound;
            this.notCompletedSound = notCompletedSound;
            this.canCompleteSound = canCompleteSound;
        }

    }

}
