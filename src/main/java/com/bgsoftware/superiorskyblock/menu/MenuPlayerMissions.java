package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.handlers.MissionsHandler;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.menus.MenuConverter;
import com.bgsoftware.superiorskyblock.utils.missions.MissionUtils;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class MenuPlayerMissions extends PagedSuperiorMenu<Mission<?>> {

    private static boolean sortByCompletion, removeCompleted;

    private List<Mission<?>> missions;

    private MenuPlayerMissions(SuperiorPlayer superiorPlayer){
        super("menuPlayerMissions", superiorPlayer);
        if(superiorPlayer != null) {
            this.missions = plugin.getMissions().getPlayerMissions().stream()
                    .filter(mission -> MissionUtils.canDisplayMission(mission, superiorPlayer, removeCompleted))
                    .collect(Collectors.toList());
            if(sortByCompletion)
                this.missions.sort(Comparator.comparingInt(this::getCompletionStatus));
        }
    }

    @Override
    protected void onPlayerClick(InventoryClickEvent event, Mission<?> mission) {
        boolean completed = !superiorPlayer.canCompleteMissionAgain(mission);
        boolean canComplete = plugin.getMissions().canComplete(superiorPlayer, mission);

        SoundWrapper sound = (SoundWrapper) getData(completed ? "sound-completed" : canComplete ? "sound-can-complete" : "sound-not-completed");
        if(sound != null)
            sound.playSound(event.getWhoClicked());

        if(canComplete){
            plugin.getMissions().rewardMission(mission, superiorPlayer, false, false, result -> {
                if(result) {
                    previousMove = false;
                    openInventory(superiorPlayer, previousMenu);
                }
            });
        }
    }

    @Override
    protected void cloneAndOpen(SuperiorMenu previousMenu) {
        openInventory(superiorPlayer, previousMenu);
    }

    @Override
    protected ItemStack getObjectItem(ItemStack clickedItem, Mission<?> mission) {
        try {
            Optional<MissionsHandler.MissionData> missionDataOptional = plugin.getMissions().getMissionData(mission);

            if (!missionDataOptional.isPresent())
                return clickedItem;

            MissionsHandler.MissionData missionData = missionDataOptional.get();
            boolean completed = !superiorPlayer.canCompleteMissionAgain(mission);
            int percentage = getPercentage(mission.getProgress(superiorPlayer));
            int progressValue = mission.getProgressValue(superiorPlayer);
            int amountCompleted = superiorPlayer.getAmountMissionCompleted(mission);

            ItemStack itemStack = completed ? missionData.completed.clone().build(superiorPlayer) :
                    plugin.getMissions().canComplete(superiorPlayer, mission) ?
                            missionData.canComplete.clone()
                                    .replaceAll("{0}", percentage + "")
                                    .replaceAll("{1}", progressValue + "")
                                    .replaceAll("{2}", amountCompleted + "").build(superiorPlayer) :
                            missionData.notCompleted.clone()
                                    .replaceAll("{0}", percentage + "")
                                    .replaceAll("{1}", progressValue + "")
                                    .replaceAll("{2}", amountCompleted + "").build(superiorPlayer);

            mission.formatItem(superiorPlayer, itemStack);

            return itemStack;
        }catch(Exception ex){
            SuperiorSkyblockPlugin.log("Failed to load menu because of mission: " + mission.getName());
            throw ex;
        }
    }

    @Override
    protected List<Mission<?>> requestObjects() {
        return missions;
    }

    private int getPercentage(double progress){
        progress = Math.min(1.0, progress);
        return Math.round((float) progress * 100);
    }

    private int getCompletionStatus(Mission<?> mission){
        return !superiorPlayer.canCompleteMissionAgain(mission) ? 2 :
                plugin.getMissions().canComplete(superiorPlayer, mission) ? 1 : 0;
    }

    public static void init(){
        MenuPlayerMissions menuPlayerMissions = new MenuPlayerMissions(null);

        File file = new File(plugin.getDataFolder(), "menus/player-missions.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/player-missions.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        if(convertOldGUI(cfg)){
            try {
                cfg.save(file);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        sortByCompletion = cfg.getBoolean("sort-by-completion", false);
        removeCompleted = cfg.getBoolean("remove-completed", false);

        Registry<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuPlayerMissions, "player-missions.yml", cfg);

        char slotsChar = cfg.getString("slots", " ").charAt(0);

        if(cfg.contains("sounds." + slotsChar + ".completed"))
            menuPlayerMissions.addData("sound-completed", FileUtils.getSound(cfg.getConfigurationSection("sounds." + slotsChar + ".completed")));
        if(cfg.contains("sounds." + slotsChar + ".not-completed"))
            menuPlayerMissions.addData("sound-not-completed", FileUtils.getSound(cfg.getConfigurationSection("sounds." + slotsChar + ".not-completed")));
        if(cfg.contains("sounds." + slotsChar + ".can-complete"))
            menuPlayerMissions.addData("sound-can-complete", FileUtils.getSound(cfg.getConfigurationSection("sounds." + slotsChar + ".can-complete")));

        menuPlayerMissions.setPreviousSlot(getSlots(cfg, "previous-page", charSlots));
        menuPlayerMissions.setCurrentSlot(getSlots(cfg, "current-page", charSlots));
        menuPlayerMissions.setNextSlot(getSlots(cfg, "next-page", charSlots));
        menuPlayerMissions.setSlots(getSlots(cfg, "slots", charSlots));

        charSlots.delete();

        menuPlayerMissions.markCompleted();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu){
        new MenuPlayerMissions(superiorPlayer).open(previousMenu);
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

        newMenu.set("title", cfg.getString("missions-panel.player-title"));

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
