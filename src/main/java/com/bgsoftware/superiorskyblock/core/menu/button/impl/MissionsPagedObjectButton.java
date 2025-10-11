package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.missions.IMissionsHolder;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSoundImpl;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
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
        Mission<?> mission = pagedObject.getMission();

        if (mission == null)
            return;

        Optional<MissionData> missionDataOptional = plugin.getMissions().getMissionData(mission);

        if (!missionDataOptional.isPresent())
            return;

        SuperiorPlayer inventoryViewer = menuView.getInventoryViewer();

        MissionData missionData = missionDataOptional.get();
        IMissionsHolder missionsHolder = mission.getIslandMission() ? inventoryViewer.getIsland() : inventoryViewer;

        if (missionsHolder == null)
            return;

        boolean canComplete = plugin.getMissions().canComplete(inventoryViewer, mission);

        GameSound gameSound;

        if (!missionsHolder.canCompleteMissionAgain(mission))
            gameSound = getTemplate().completedSound;
        else if (getTemplate().lockedSound != null && missionData.hasLocked()
                && !plugin.getMissions().hasAllRequirements(mission, inventoryViewer))
            gameSound = getTemplate().lockedSound;
        else if (canComplete)
            gameSound = getTemplate().canCompleteSound;
        else
            gameSound = getTemplate().notCompletedSound;

        GameSoundImpl.playSound(clickEvent.getWhoClicked(), gameSound);

        if (!canComplete)
            return;

        plugin.getMissions().rewardMission(mission, inventoryViewer, false, false, result -> {
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

        int percentage = calculatePercentage(mission.getProgress(inventoryViewer));
        int progressValue = mission.getProgressValue(inventoryViewer);
        int amountCompleted = missionsHolder.getAmountMissionCompleted(mission);

        ItemBuilder itemBuilder;

        if (!missionsHolder.canCompleteMissionAgain(mission))
            itemBuilder = missionData.getCompleted();
        else if (missionData.hasLocked() && !plugin.getMissions().hasAllRequirements(mission, inventoryViewer))
            itemBuilder = missionData.getLocked();
        else if (plugin.getMissions().canComplete(inventoryViewer, mission))
            itemBuilder = missionData.getCanComplete();
        else
            itemBuilder = missionData.getNotCompleted();

        ItemStack itemStack = itemBuilder
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
        private GameSound lockedSound = null;

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

        public Builder setLockedSound(GameSound lockedSound) {
            this.lockedSound = lockedSound;
            return this;
        }

        @Override
        public PagedMenuTemplateButton<MenuMissionsCategory.View, MissionReference> build() {
            return new Template(buttonItem, commands, requiredPermission, lackPermissionSound, nullItem,
                    getButtonIndex(), clickSound, notCompletedSound, canCompleteSound, lockedSound);
        }

    }

    public static class Template extends PagedMenuTemplateButtonImpl<MenuMissionsCategory.View, MissionReference> {

        private final GameSound completedSound;
        private final GameSound notCompletedSound;
        private final GameSound canCompleteSound;
        private final GameSound lockedSound;

        Template(TemplateItem buttonItem, List<String> commands, String requiredPermission,
                 GameSound lackPermissionSound, TemplateItem nullItem, int buttonIndex,
                 GameSound completedSound, GameSound notCompletedSound, GameSound canCompleteSound, GameSound lockedSound) {
            super(buttonItem, null, commands, requiredPermission, lackPermissionSound, nullItem, buttonIndex,
                    MissionsPagedObjectButton.class, MissionsPagedObjectButton::new);
            this.completedSound = completedSound;
            this.notCompletedSound = notCompletedSound;
            this.canCompleteSound = canCompleteSound;
            this.lockedSound = lockedSound;
        }

    }

}
