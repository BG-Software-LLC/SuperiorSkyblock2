package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.missions.IMissionsHolder;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.missions.MissionCategory;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.handlers.MissionsHandler;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.missions.MissionUtils;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class MenuMissionsCategory extends PagedSuperiorMenu<Mission<?>> {

    private static boolean sortByCompletion, removeCompleted;

    private final MissionCategory missionCategory;
    private List<Mission<?>> missions;

    private MenuMissionsCategory(SuperiorPlayer superiorPlayer, MissionCategory missionCategory) {
        super("menuMissionsCategory", superiorPlayer);

        this.missionCategory = missionCategory;

        if(superiorPlayer != null) {
            this.missions = missionCategory.getMissions().stream()
                    .filter(mission -> MissionUtils.canDisplayMission(mission, superiorPlayer, removeCompleted))
                    .collect(Collectors.toList());
            if(sortByCompletion && superiorPlayer.getIsland() != null)
                this.missions.sort(Comparator.comparingInt(this::getCompletionStatus));
        }
    }

    @Override
    protected void onPlayerClick(InventoryClickEvent event, Mission<?> mission) {
        Island island = superiorPlayer.getIsland();

        if(island == null)
            return;

        boolean completed = !island.canCompleteMissionAgain(mission);
        boolean canComplete = plugin.getMissions().canComplete(superiorPlayer, mission);

        SoundWrapper sound = (SoundWrapper) getData(completed ? "sound-completed" : canComplete ? "sound-can-complete" : "sound-not-completed");
        if(sound != null)
            sound.playSound(event.getWhoClicked());

        if(canComplete && plugin.getMissions().hasAllRequiredMissions(superiorPlayer, mission)){
            plugin.getMissions().rewardMission(mission, superiorPlayer, false, false, result -> {
               if(result){
                   previousMove = false;
                   openInventory(superiorPlayer, previousMenu, missionCategory);
               }
            });
        }
    }

    @Override
    protected void cloneAndOpen(SuperiorMenu previousMenu) {
        openInventory(superiorPlayer, previousMenu, missionCategory);
    }

    @Override
    protected Inventory buildInventory(Function<String, String> titleReplacer) {
        return super.buildInventory(title -> title.replace("{0}", missionCategory.getName()));
    }

    @Override
    protected ItemStack getObjectItem(ItemStack clickedItem, Mission<?> mission) {
        try {
            Optional<MissionsHandler.MissionData> missionDataOptional = plugin.getMissions().getMissionData(mission);

            if (!missionDataOptional.isPresent())
                return clickedItem;

            MissionsHandler.MissionData missionData = missionDataOptional.get();
            IMissionsHolder missionsHolder = mission.getIslandMission() ? superiorPlayer.getIsland() : superiorPlayer;

            if (missionsHolder == null)
                return new ItemStack(Material.AIR);

            boolean completed = !missionsHolder.canCompleteMissionAgain(mission);
            int percentage = getPercentage(mission.getProgress(superiorPlayer));
            int progressValue = mission.getProgressValue(superiorPlayer);
            int amountCompleted = missionsHolder.getAmountMissionCompleted(mission);

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
        return superiorPlayer.getIsland() == null ? 0 :
                !superiorPlayer.getIsland().canCompleteMissionAgain(mission) ? 2 :
                plugin.getMissions().canComplete(superiorPlayer, mission) ? 1 : 0;
    }

    public static void init(){
        MenuMissionsCategory menuMissionsCategory = new MenuMissionsCategory(null, null);

        File file = new File(plugin.getDataFolder(), "menus/missions-category.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/missions-category.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        sortByCompletion = cfg.getBoolean("sort-by-completion", false);
        removeCompleted = cfg.getBoolean("remove-completed", false);

        Map<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuMissionsCategory, "missions-category.yml", cfg);

        char slotsChar = cfg.getString("slots", " ").charAt(0);

        if(cfg.contains("sounds." + slotsChar + ".completed"))
            menuMissionsCategory.addData("sound-completed", FileUtils.getSound(cfg.getConfigurationSection("sounds." + slotsChar + ".completed")));
        if(cfg.contains("sounds." + slotsChar + ".not-completed"))
            menuMissionsCategory.addData("sound-not-completed", FileUtils.getSound(cfg.getConfigurationSection("sounds." + slotsChar + ".not-completed")));
        if(cfg.contains("sounds." + slotsChar + ".can-complete"))
            menuMissionsCategory.addData("sound-can-complete", FileUtils.getSound(cfg.getConfigurationSection("sounds." + slotsChar + ".can-complete")));

        menuMissionsCategory.setPreviousSlot(getSlots(cfg, "previous-page", charSlots));
        menuMissionsCategory.setCurrentSlot(getSlots(cfg, "current-page", charSlots));
        menuMissionsCategory.setNextSlot(getSlots(cfg, "next-page", charSlots));
        menuMissionsCategory.setSlots(getSlots(cfg, "slots", charSlots));

        menuMissionsCategory.markCompleted();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, MissionCategory missionCategory){
        new MenuMissionsCategory(superiorPlayer, missionCategory).open(previousMenu);
    }

    public static void refreshMenus(MissionCategory missionCategory){
        refreshMenus(MenuMissionsCategory.class, superiorMenu -> missionCategory.equals(superiorMenu.missionCategory));
    }

}