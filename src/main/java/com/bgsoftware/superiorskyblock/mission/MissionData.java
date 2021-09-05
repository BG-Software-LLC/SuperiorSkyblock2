package com.bgsoftware.superiorskyblock.mission;

import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class MissionData {

    private static int currentIndex = 0;

    private final int index;
    private final Mission<?> mission;
    private final List<ItemStack> itemRewards = new ArrayList<>();
    private final List<String> commandRewards = new ArrayList<>();
    private final boolean autoReward, islandMission;
    private final boolean disbandReset, leaveReset;
    private final ItemBuilder notCompleted, canComplete, completed;
    private final int resetAmount;

    MissionData(Mission<?> mission, ConfigurationSection section){
        this.index = currentIndex++;
        this.mission = mission;
        this.islandMission = section.getBoolean("island", false);
        this.autoReward = section.getBoolean("auto-reward", true);
        this.disbandReset = section.getBoolean("disband-reset", false);
        this.leaveReset = section.getBoolean("leave-reset", false);
        this.resetAmount = section.getInt("reset-amount", 1);

        if(section.contains("rewards.items")){
            for(String key : section.getConfigurationSection("rewards.items").getKeys(false)) {
                ItemStack itemStack = FileUtils.getItemStack("config.yml", section.getConfigurationSection("rewards.items." + key)).build();
                itemStack.setAmount(section.getInt("rewards.items." + key + ".amount", 1));
                this.itemRewards.add(itemStack);
            }
        }

        this.commandRewards.addAll(section.getStringList("rewards.commands"));

        this.notCompleted = FileUtils.getItemStack("config.yml", section.getConfigurationSection("icons.not-completed"));
        this.canComplete = FileUtils.getItemStack("config.yml", section.getConfigurationSection("icons.can-complete"));
        this.completed = FileUtils.getItemStack("config.yml", section.getConfigurationSection("icons.completed"));
    }

    public boolean isAutoReward() {
        return autoReward;
    }

    public boolean isIslandMission() {
        return islandMission;
    }

    public List<ItemStack> getItemRewards() {
        return itemRewards;
    }

    public List<String> getCommandRewards() {
        return commandRewards;
    }

    public int getIndex() {
        return index;
    }

    public Mission<?> getMission() {
        return mission;
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
        return completed.clone();
    }

    public ItemBuilder getCanComplete() {
        return canComplete.clone();
    }

    public ItemBuilder getNotCompleted() {
        return notCompleted.clone();
    }

    @Override
    public String toString() {
        return "MissionData{name=" + mission.getName() + "}";
    }

}
