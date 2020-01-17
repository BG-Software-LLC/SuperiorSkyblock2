package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.handlers.MissionsHandler;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.menus.MenuConverter;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class MenuIslandMissions extends PagedSuperiorMenu<Mission> {

    private List<Mission> missions;

    private MenuIslandMissions(SuperiorPlayer superiorPlayer){
        super("menuIslandMissions", superiorPlayer);
        if(superiorPlayer != null) {
            this.missions = plugin.getMissions().getIslandMissions().stream()
                    .filter(mission -> !mission.isOnlyShowIfRequiredCompleted() || plugin.getMissions().hasAllRequiredMissions(superiorPlayer, mission))
                    .collect(Collectors.toList());
        }
    }

    @Override
    protected void onPlayerClick(InventoryClickEvent event, Mission mission) {
        Island island = superiorPlayer.getIsland();
        boolean completed = !island.canCompleteMissionAgain(mission);
        boolean canComplete = plugin.getMissions().canComplete(superiorPlayer, mission);

        SoundWrapper sound = (SoundWrapper) getData(completed ? "sound-completed" : canComplete ? "sound-can-complete" : "sound-not-completed");
        if(sound != null)
            sound.playSound(superiorPlayer.asPlayer());

        if(canComplete && plugin.getMissions().hasAllRequiredMissions(superiorPlayer, mission)){
            plugin.getMissions().rewardMission(mission, superiorPlayer, false);
            previousMove = false;
            open(previousMenu);
        }
    }

    @Override
    protected ItemStack getObjectItem(ItemStack clickedItem, Mission mission) {
        Island island = superiorPlayer.getIsland();
        MissionsHandler.MissionData missionData = plugin.getMissions().getMissionData(mission);
        boolean completed = !island.canCompleteMissionAgain(mission);
        int percentage = getPercentage(mission.getProgress(superiorPlayer));
        int progressValue = mission.getProgressValue(superiorPlayer);
        int amountCompleted = island.getAmountMissionCompleted(mission);
        return completed ? missionData.completed.clone().build(superiorPlayer) :
                plugin.getMissions().canComplete(superiorPlayer, mission) ?
                        missionData.canComplete.clone()
                                .replaceAll("{0}", percentage + "")
                                .replaceAll("{1}", progressValue + "")
                                .replaceAll("{2}", amountCompleted + "").build(superiorPlayer) :
                        missionData.notCompleted.clone()
                                .replaceAll("{0}", percentage + "")
                                .replaceAll("{1}", progressValue + "")
                                .replaceAll("{2}", amountCompleted + "").build(superiorPlayer);
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
        MenuIslandMissions menuIslandMissions = new MenuIslandMissions(null);

        File file = new File(plugin.getDataFolder(), "menus/island-missions.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/island-missions.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        if(convertOldGUI(cfg)){
            cfg.save(file);
        }

        Map<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuIslandMissions, "island-missions.yml", cfg);

        char slotsChar = cfg.getString("slots", "@").charAt(0);

        if(cfg.contains("sounds." + slotsChar + ".completed"))
            menuIslandMissions.addData("sound-completed", FileUtils.getSound(cfg.getConfigurationSection("sounds." + slotsChar + ".completed")));
        if(cfg.contains("sounds." + slotsChar + ".not-completed"))
            menuIslandMissions.addData("sound-not-completed", FileUtils.getSound(cfg.getConfigurationSection("sounds." + slotsChar + ".not-completed")));
        if(cfg.contains("sounds." + slotsChar + ".can-complete"))
            menuIslandMissions.addData("sound-can-complete", FileUtils.getSound(cfg.getConfigurationSection("sounds." + slotsChar + ".can-complete")));

        menuIslandMissions.setPreviousSlot(charSlots.getOrDefault(cfg.getString("previous-page", "%").charAt(0), Collections.singletonList(-1)).get(0));
        menuIslandMissions.setCurrentSlot(charSlots.getOrDefault(cfg.getString("current-page", "*").charAt(0), Collections.singletonList(-1)).get(0));
        menuIslandMissions.setNextSlot(charSlots.getOrDefault(cfg.getString("next-page", "^").charAt(0), Collections.singletonList(-1)).get(0));
        menuIslandMissions.setSlots(charSlots.getOrDefault(slotsChar, Collections.singletonList(-1)));
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu){
        new MenuIslandMissions(superiorPlayer).open(previousMenu);
    }

    public static void refreshMenus(){
        refreshMenus(MenuIslandMissions.class);
    }

    private static boolean convertOldGUI(YamlConfiguration newMenu){
        File oldFile = new File(plugin.getDataFolder(), "guis/missions-gui.yml");

        if(!oldFile.exists())
            return false;

        //We want to reset the items of newMenu.
        ConfigurationSection itemsSection = newMenu.createSection("items");
        ConfigurationSection soundsSection = newMenu.createSection("sounds");
        ConfigurationSection commandsSection = newMenu.createSection("commands");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(oldFile);

        newMenu.set("title", cfg.getString("missions-panel.island-title"));

        int size = cfg.getInt("missions-panel.size");

        char[] patternChars = new char[size * 9];
        Arrays.fill(patternChars, '\n');

        int charCounter = 0;

        if(cfg.contains("missions-panel.fill-items")) {
            charCounter = MenuConverter.convertFillItems(cfg.getConfigurationSection("missions-panel.fill-items"),
                    charCounter, patternChars, itemsSection, commandsSection, soundsSection);
        }

        char slotsChar = itemChars[charCounter++];

        MenuConverter.convertPagedButtons(cfg.getConfigurationSection("missions-panel"), newMenu, patternChars,
                slotsChar, itemChars[charCounter++], itemChars[charCounter++], itemChars[charCounter++],
                itemsSection, commandsSection, soundsSection);

        if(cfg.contains("missions-panel.sounds"))
            newMenu.set("sounds." + slotsChar, cfg.getConfigurationSection("missions-panel.sounds"));

        newMenu.set("pattern", MenuConverter.buildPattern(size, patternChars, itemChars[charCounter]));

        return true;
    }

}