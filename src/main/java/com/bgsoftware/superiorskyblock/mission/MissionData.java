package com.bgsoftware.superiorskyblock.mission;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.core.io.MenuParserImpl;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MissionData {

    private static int currentIndex = 0;

    private final int index;
    private final String missionName;
    private final List<ItemStack> itemRewards = new LinkedList<>();
    private final List<String> commandRewards = new LinkedList<>();
    private final boolean autoReward;
    private final boolean islandMission;
    private final boolean disbandReset;
    private final boolean leaveReset;
    @Nullable
    private final TemplateItem notCompleted;
    @Nullable
    private final TemplateItem canComplete;
    @Nullable
    private final TemplateItem completed;
    private final int resetAmount;

    MissionData(Mission<?> mission, ConfigurationSection section) {
        this.index = currentIndex++;
        this.missionName = mission.getName();
        this.islandMission = section.getBoolean("island", false);
        this.autoReward = section.getBoolean("auto-reward", true);
        this.disbandReset = section.getBoolean("disband-reset", false);
        this.leaveReset = section.getBoolean("leave-reset", false);
        this.resetAmount = section.getInt("reset-amount", 1);

        if (section.contains("rewards.items")) {
            for (String key : section.getConfigurationSection("rewards.items").getKeys(false)) {
                TemplateItem templateItem = MenuParserImpl.getInstance().getItemStack("config.yml", section.getConfigurationSection("rewards.items." + key));
                if (templateItem != null) {
                    ItemStack itemStack = templateItem.build();
                    itemStack.setAmount(section.getInt("rewards.items." + key + ".amount", 1));
                    this.itemRewards.add(itemStack);
                }
            }
        }

        this.commandRewards.addAll(section.getStringList("rewards.commands"));

        this.notCompleted = MenuParserImpl.getInstance().getItemStack("config.yml", section.getConfigurationSection("icons.not-completed"));
        this.canComplete = MenuParserImpl.getInstance().getItemStack("config.yml", section.getConfigurationSection("icons.can-complete"));
        this.completed = MenuParserImpl.getInstance().getItemStack("config.yml", section.getConfigurationSection("icons.completed"));
    }

    public boolean isAutoReward() {
        return autoReward;
    }

    public boolean isIslandMission() {
        return islandMission;
    }

    public List<ItemStack> getItemRewards() {
        return Collections.unmodifiableList(itemRewards);
    }

    public List<String> getCommandRewards() {
        return Collections.unmodifiableList(commandRewards);
    }

    public int getIndex() {
        return index;
    }

    public String getMissionName() {
        return this.missionName;
    }

    public boolean isDisbandReset() {
        return disbandReset;
    }

    public boolean isLeaveReset() {
        return leaveReset;
    }

    public int getResetAmount() {
        return resetAmount;
    }

    public ItemBuilder getCompleted() {
        return (completed == null ? TemplateItem.AIR : completed).getBuilder();
    }

    public ItemBuilder getCanComplete() {
        return (canComplete == null ? TemplateItem.AIR : canComplete).getBuilder();
    }

    public ItemBuilder getNotCompleted() {
        return (notCompleted == null ? TemplateItem.AIR : notCompleted).getBuilder();
    }

    @Override
    public String toString() {
        return "MissionData{name=" + this.missionName + "}";
    }

}
