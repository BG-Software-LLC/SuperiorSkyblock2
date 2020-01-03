package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.handlers.MissionsHandler;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class MenuPlayerMissions extends PagedSuperiorMenu<Mission> {

    private List<Mission> missions;

    private MenuPlayerMissions(SuperiorPlayer superiorPlayer){
        super("menuPlayerMissions", superiorPlayer);
        if(superiorPlayer != null) {
            this.missions = plugin.getMissions().getPlayerMissions().stream()
                    .filter(mission -> !mission.isOnlyShowIfRequiredCompleted() || mission.getRequiredMissions().stream().allMatch(_mission ->
                            plugin.getMissions().hasCompleted(superiorPlayer, plugin.getMissions().getMission(_mission)))).collect(Collectors.toList());
        }
    }

    @Override
    protected void onPlayerClick(InventoryClickEvent event, Mission mission) {
        boolean completed = superiorPlayer.hasCompletedMission(mission);
        boolean canComplete = mission.canComplete(superiorPlayer);
        boolean hasAllRequiredMissions = mission.getRequiredMissions().stream().allMatch(_mission ->
                plugin.getMissions().hasCompleted(superiorPlayer, plugin.getMissions().getMission(_mission)));

        SoundWrapper sound = (SoundWrapper) getData(completed ? "sound-completed" : canComplete ? "sound-can-complete" : "sound-not-completed");
        if(sound != null)
            sound.playSound(superiorPlayer.asPlayer());

        if(canComplete && hasAllRequiredMissions){
            plugin.getMissions().rewardMission(mission, superiorPlayer, false);
            previousMove = false;
            open(previousMenu);
        }
    }

    @Override
    protected ItemStack getObjectItem(ItemStack clickedItem, Mission mission) {
        MissionsHandler.MissionData missionData = plugin.getMissions().getMissionData(mission);
        boolean completed = superiorPlayer.hasCompletedMission(mission);
        boolean hasAllRequiredMissions = mission.getRequiredMissions().stream().allMatch(_mission ->
                plugin.getMissions().hasCompleted(superiorPlayer, plugin.getMissions().getMission(_mission)));
        int percentage = getPercentage(mission.getProgress(superiorPlayer));
        int progressValue = mission.getProgressValue(superiorPlayer);
        return completed ? missionData.completed.build(superiorPlayer) :
                mission.canComplete(superiorPlayer) && hasAllRequiredMissions ?
                        missionData.canComplete.clone()
                                .replaceAll("{0}", percentage + "")
                                .replaceAll("{1}", progressValue + "").build(superiorPlayer) :
                        missionData.notCompleted.clone()
                                .replaceAll("{0}", percentage + "")
                                .replaceAll("{1}", progressValue + "").build(superiorPlayer);
    }

    @Override
    protected List<Mission> requestObjects() {
        return missions;
    }

    private int getPercentage(double progress){
        progress = Math.min(1.0, progress);
        return Math.round((float) progress * 100);
    }

    public static void init(){
        MenuPlayerMissions menuPlayerMissions = new MenuPlayerMissions(null);

        File file = new File(plugin.getDataFolder(), "menus/player-missions.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/player-missions.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        Map<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuPlayerMissions, "player-missions.yml", cfg);

        char slotsChar = cfg.getString("slots", "@").charAt(0);

        if(cfg.contains("sounds." + slotsChar + ".completed"))
            menuPlayerMissions.addData("sound-completed", FileUtils.getSound(cfg.getConfigurationSection("sounds." + slotsChar + ".completed")));
        if(cfg.contains("sounds." + slotsChar + ".not-completed"))
            menuPlayerMissions.addData("sound-not-completed", FileUtils.getSound(cfg.getConfigurationSection("sounds." + slotsChar + ".not-completed")));
        if(cfg.contains("sounds." + slotsChar + ".can-complete"))
            menuPlayerMissions.addData("sound-can-complete", FileUtils.getSound(cfg.getConfigurationSection("sounds." + slotsChar + ".can-complete")));

        menuPlayerMissions.setPreviousSlot(charSlots.getOrDefault(cfg.getString("previous-page", "%").charAt(0), Collections.singletonList(-1)).get(0));
        menuPlayerMissions.setCurrentSlot(charSlots.getOrDefault(cfg.getString("current-page", "*").charAt(0), Collections.singletonList(-1)).get(0));
        menuPlayerMissions.setNextSlot(charSlots.getOrDefault(cfg.getString("next-page", "^").charAt(0), Collections.singletonList(-1)).get(0));
        menuPlayerMissions.setSlots(charSlots.getOrDefault(slotsChar, Collections.singletonList(-1)));
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu){
        new MenuPlayerMissions(superiorPlayer).open(previousMenu);
    }

}
