package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.handlers.MissionsHandler;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class MenuPlayerMissions extends SuperiorMenu {

    private static int previousSlot, currentSlot, nextSlot;
    private static List<Integer> slots = new ArrayList<>();

    private List<Mission> missions;
    private int currentPage;

    private MenuPlayerMissions(SuperiorPlayer superiorPlayer, int currentPage){
        super("menuPlayerMissions", superiorPlayer);
        this.currentPage = currentPage;
        if(superiorPlayer != null) {
            this.missions = plugin.getMissions().getPlayerMissions().stream()
                    .filter(mission -> !mission.isOnlyShowIfRequiredCompleted() || mission.getRequiredMissions().stream().allMatch(_mission ->
                            plugin.getMissions().hasCompleted(superiorPlayer, plugin.getMissions().getMission(_mission)))).collect(Collectors.toList());
        }
    }

    @Override
    public void onPlayerClick(InventoryClickEvent e) {
        int clickedSlot = e.getRawSlot();

        if(clickedSlot == previousSlot || clickedSlot == nextSlot || clickedSlot == currentSlot){
            boolean nextPage = slots.size() * currentPage < slots.size();

            if((!nextPage && e.getRawSlot() == nextSlot) || (currentPage == 1 && e.getRawSlot() == previousSlot))
                return;

            currentPage = e.getRawSlot() == nextSlot ? currentPage + 1 : currentPage - 1;

            previousMove = false;
            open(previousMenu);
        }

        else{
            if(e.getCurrentItem() == null)
                return;

            int indexOf = slots.indexOf(e.getRawSlot());

            if(indexOf < 0 || indexOf >= missions.size())
                return;

            Mission mission = missions.get(indexOf);

            if (mission != null) {
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
        }
    }

    @Override
    public Inventory getInventory() {
        Inventory inventory = super.getInventory();

        for (int i = 0; i < slots.size() && (i + (slots.size() * (currentPage - 1))) < missions.size(); i++) {
            Mission mission = missions.get(i + (slots.size() * (currentPage - 1)));
            MissionsHandler.MissionData missionData = plugin.getMissions().getMissionData(mission);
            boolean completed = superiorPlayer.hasCompletedMission(mission);
            boolean hasAllRequiredMissions = mission.getRequiredMissions().stream().allMatch(_mission ->
                    plugin.getMissions().hasCompleted(superiorPlayer, plugin.getMissions().getMission(_mission)));
            int percentage = getPercentage(mission.getProgress(superiorPlayer));
            int progressValue = mission.getProgressValue(superiorPlayer);
            inventory.setItem(i, completed ? missionData.completed.build(superiorPlayer) :
                    mission.canComplete(superiorPlayer) && hasAllRequiredMissions ?
                        missionData.canComplete.clone()
                                .replaceAll("{0}", percentage + "")
                                .replaceAll("{1}", progressValue + "").build(superiorPlayer) :
                        missionData.notCompleted.clone()
                                .replaceAll("{0}", percentage + "")
                                .replaceAll("{1}", progressValue + "").build(superiorPlayer));
        }

        inventory.setItem(previousSlot, new ItemBuilder(inventory.getItem(previousSlot))
                .replaceAll("{0}", (currentPage == 1 ? "&c" : "&a")).build(superiorPlayer));

        inventory.setItem(currentSlot, new ItemBuilder(inventory.getItem(currentSlot))
                .replaceAll("{0}", currentPage + "").build(superiorPlayer));

        inventory.setItem(nextSlot, new ItemBuilder(inventory.getItem(nextSlot))
                .replaceAll("{0}", (missions.size() > currentPage * slots.size() ? "&a" : "&c")).build(superiorPlayer));

        return inventory;
    }

    private int getPercentage(double progress){
        progress = Math.min(1.0, progress);
        return Math.round((float) progress * 100);
    }

    public static void init(){
        MenuPlayerMissions menuPlayerMissions = new MenuPlayerMissions(null, 1);

        File file = new File(plugin.getDataFolder(), "menus/player-missions.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/player-missions.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        Map<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuPlayerMissions, cfg);

        previousSlot = charSlots.getOrDefault(cfg.getString("previous-page", "%").charAt(0), Collections.singletonList(-1)).get(0);
        currentSlot = charSlots.getOrDefault(cfg.getString("current-page", "*").charAt(0), Collections.singletonList(-1)).get(0);
        nextSlot = charSlots.getOrDefault(cfg.getString("next-page", "^").charAt(0), Collections.singletonList(-1)).get(0);

        char slotsChar = cfg.getString("slots", "@").charAt(0);

        slots = charSlots.getOrDefault(slotsChar, Collections.singletonList(-1));
        slots.sort(Integer::compareTo);

        if(cfg.contains("sounds." + slotsChar + ".completed"))
            menuPlayerMissions.addData("sound-completed", FileUtils.getSound(cfg.getConfigurationSection("sounds." + slotsChar + ".completed")));
        if(cfg.contains("sounds." + slotsChar + ".not-completed"))
            menuPlayerMissions.addData("sound-not-completed", FileUtils.getSound(cfg.getConfigurationSection("sounds." + slotsChar + ".not-completed")));
        if(cfg.contains("sounds." + slotsChar + ".can-complete"))
            menuPlayerMissions.addData("sound-can-complete", FileUtils.getSound(cfg.getConfigurationSection("sounds." + slotsChar + ".can-complete")));
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, int currentPage, SuperiorMenu previousMenu){
        new MenuPlayerMissions(superiorPlayer, currentPage).open(previousMenu);
    }

}
