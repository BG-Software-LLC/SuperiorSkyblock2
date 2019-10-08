package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.handlers.MissionsHandler;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class IslandMissionsMenu extends SuperiorMenu {

    private static Inventory inventory = null;
    private static String playerTitle = "", islandTitle = "";
    private static ItemStack previousButton, currentButton, nextButton;
    private static int previousSlot, currentSlot, nextSlot;
    private static List<Integer> slots = new ArrayList<>();

    private boolean islandMissions;
    private int page;

    private IslandMissionsMenu(boolean islandMissions){
        super("missionsPage");
        this.islandMissions = islandMissions;
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        super.onClick(e);
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getWhoClicked());
        Island island = superiorPlayer.getIsland();
        int clickedSlot = e.getRawSlot();

        List<Mission> missions = islandMissions ? plugin.getMissions().getIslandMissions() : plugin.getMissions().getPlayerMissions();

        if(clickedSlot == previousSlot || clickedSlot == nextSlot || clickedSlot == currentSlot){
            int nextPage;

            if(clickedSlot == previousSlot){
                nextPage = page == 1 ? -1 : page - 1;
            }
            else if(clickedSlot == nextSlot){
                nextPage = missions.size() > page * slots.size() ? page + 1 : -1;
            }
            else return;

            if(nextPage == -1)
                return;

            open(superiorPlayer, nextPage, previousMenu);
        }

        else{
            if(e.getCurrentItem() == null)
                return;

            if(islandMissions && island == null)
                return;

            int indexOf = slots.indexOf(e.getRawSlot());

            if(indexOf < 0 || indexOf >= missions.size())
                return;

            Mission mission = missions.get(indexOf);

            if (mission != null) {
                boolean completed = islandMissions ? island.hasCompletedMission(mission) : superiorPlayer.hasCompletedMission(mission);
                boolean canComplete = mission.canComplete(superiorPlayer);
                boolean hasAllRequiredMissions = mission.getRequiredMissions().stream().allMatch(_mission ->
                        plugin.getMissions().hasCompleted(superiorPlayer, plugin.getMissions().getMission(_mission)));

                SoundWrapper sound = getSound(completed ? -1 : canComplete ? -3 : -2);
                if(sound != null)
                    sound.playSound(superiorPlayer.asPlayer());

                if(canComplete && hasAllRequiredMissions){
                    plugin.getMissions().rewardMission(mission, superiorPlayer, false);
                    open(superiorPlayer, page, previousMenu);
                }
            }
        }
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    @Override
    public void open(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu) {
        open(superiorPlayer, 1, previousMenu);
    }

    private void open(SuperiorPlayer superiorPlayer, int page, SuperiorMenu previousMenu){
        if(Bukkit.isPrimaryThread()){
            Executor.async(() -> open(superiorPlayer, page, previousMenu));
            return;
        }

        Island island = superiorPlayer.getIsland();

        this.page = page;

        List<Mission> missions = (islandMissions ? plugin.getMissions().getIslandMissions() : plugin.getMissions().getPlayerMissions()).stream()
                .filter(mission -> !mission.isOnlyShowIfRequiredCompleted() || mission.getRequiredMissions().stream().allMatch(_mission ->
                        plugin.getMissions().hasCompleted(superiorPlayer, plugin.getMissions().getMission(_mission)))).collect(Collectors.toList());

        Inventory inv = Bukkit.createInventory(this, inventory.getSize(), islandMissions ? islandTitle : playerTitle);
        inv.setContents(inventory.getContents());

        if(!islandMissions || island != null) {
            for (int i = 0; i < slots.size() && (i + (slots.size() * (page - 1))) < missions.size(); i++) {
                Mission mission = missions.get(i + (slots.size() * (page - 1)));
                MissionsHandler.MissionData missionData = plugin.getMissions().getMissionData(mission);
                boolean completed = islandMissions ? island.hasCompletedMission(mission) : superiorPlayer.hasCompletedMission(mission);
                boolean hasAllRequiredMissions = mission.getRequiredMissions().stream().allMatch(_mission ->
                        plugin.getMissions().hasCompleted(superiorPlayer, plugin.getMissions().getMission(_mission)));
                inv.setItem(i, completed ? missionData.completed : mission.canComplete(superiorPlayer) && hasAllRequiredMissions ? missionData.canComplete : missionData.notCompleted);
            }
        }

        inv.setItem(previousSlot, new ItemBuilder(previousButton)
                .replaceAll("{0}", (page == 1 ? "&c" : "&a")).build());

        inv.setItem(currentSlot, new ItemBuilder(currentButton)
                .replaceAll("{0}", page + "").build());

        inv.setItem(nextSlot, new ItemBuilder(nextButton)
                .replaceAll("{0}", (missions.size() > page * slots.size() ? "&a" : "&c")).build());

        this.previousMenu = null;

        Executor.sync(() -> {
            superiorPlayer.asPlayer().openInventory(inv);
            this.previousMenu = previousMenu;
        });
    }

    public static void init(){
        IslandMissionsMenu islandMissionsMenu = new IslandMissionsMenu(false);

        File file = new File(plugin.getDataFolder(), "guis/missions-gui.yml");

        if(!file.exists())
            FileUtils.saveResource("guis/missions-gui.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        inventory = FileUtils.loadGUI(islandMissionsMenu, cfg.getConfigurationSection("missions-panel"), 6, "&lYour Missions");
        playerTitle = ChatColor.translateAlternateColorCodes('&', cfg.getString("missions-panel.player-title"));
        islandTitle = ChatColor.translateAlternateColorCodes('&', cfg.getString("missions-panel.island-title"));

        previousButton = FileUtils.getItemStack(cfg.getConfigurationSection("missions-panel.previous-page"));
        currentButton = FileUtils.getItemStack(cfg.getConfigurationSection("missions-panel.current-page"));
        nextButton = FileUtils.getItemStack(cfg.getConfigurationSection("missions-panel.next-page"));

        previousSlot = cfg.getInt("missions-panel.previous-page.slot");
        currentSlot = cfg.getInt("missions-panel.current-page.slot");
        nextSlot = cfg.getInt("missions-panel.next-page.slot");

        islandMissionsMenu.addSound(previousSlot, FileUtils.getSound(cfg.getConfigurationSection("missions-panel.previous-page.sound")));
        islandMissionsMenu.addSound(currentSlot, FileUtils.getSound(cfg.getConfigurationSection("missions-panel.current-page.sound")));
        islandMissionsMenu.addSound(nextSlot, FileUtils.getSound(cfg.getConfigurationSection("missions-panel.next-page.sound")));
        islandMissionsMenu.addSound(-1, FileUtils.getSound(cfg.getConfigurationSection("missions-panel.sounds.completed")));
        islandMissionsMenu.addSound(-2, FileUtils.getSound(cfg.getConfigurationSection("missions-panel.sounds.not-completed")));
        islandMissionsMenu.addSound(-3, FileUtils.getSound(cfg.getConfigurationSection("missions-panel.sounds.can-complete")));
        islandMissionsMenu.addCommands(previousSlot, cfg.getStringList("missions-panel.previous-page.commands"));
        islandMissionsMenu.addCommands(currentSlot, cfg.getStringList("missions-panel.current-page.commands"));
        islandMissionsMenu.addCommands(nextSlot, cfg.getStringList("missions-panel.next-page.commands"));

        inventory.setItem(previousSlot, previousButton);
        inventory.setItem(currentSlot, currentButton);
        inventory.setItem(nextSlot, nextButton);

        Arrays.stream(cfg.getString("missions-panel.slots").split(","))
                .forEach(slot -> slots.add(Integer.valueOf(slot)));
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, boolean islandMissions){
        new IslandMissionsMenu(islandMissions).open(superiorPlayer, previousMenu);
    }

}
