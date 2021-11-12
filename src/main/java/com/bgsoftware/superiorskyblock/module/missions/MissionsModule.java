package com.bgsoftware.superiorskyblock.module.missions;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.menu.file.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.menu.impl.MenuMissions;
import com.bgsoftware.superiorskyblock.mission.SMissionCategory;
import com.bgsoftware.superiorskyblock.module.BuiltinModule;
import com.bgsoftware.superiorskyblock.module.missions.commands.CmdAdminMission;
import com.bgsoftware.superiorskyblock.module.missions.commands.CmdMission;
import com.bgsoftware.superiorskyblock.module.missions.commands.CmdMissions;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class MissionsModule extends BuiltinModule {

    private boolean enabled = true;

    public MissionsModule() {
        super("missions");
    }

    @Override
    public void onEnable(SuperiorSkyblockPlugin plugin) {
        if (!enabled)
            return;

        List<Mission<?>> missionsToLoad = new ArrayList<>();

        ConfigurationSection categoriesSection = config.getConfigurationSection("categories");

        if (categoriesSection != null) {
            for (String categoryName : categoriesSection.getKeys(false)) {
                ConfigurationSection categorySection = categoriesSection.getConfigurationSection(categoryName);

                if (categorySection == null)
                    continue;

                List<Mission<?>> categoryMissions = new ArrayList<>();

                if (!canLoadCategory(plugin, categoryName, categoryMissions))
                    continue;

                int slot = categorySection.getInt("slot");

                String formattedCategoryName = categorySection.getString("name", categoryName);

                plugin.getMissions().loadMissionCategory(new SMissionCategory(formattedCategoryName, slot, categoryMissions));

                missionsToLoad.addAll(categoryMissions);
            }
        }

        if (!missionsToLoad.isEmpty()) {
            // Should be running in 1-tick delay so players and their islands will be loaded
            // before loading data of missions, as they depend on this data.
            Executor.sync(() -> plugin.getMissions().loadMissionsData(missionsToLoad), 1L);
        }
    }

    @Override
    public Listener[] getModuleListeners(SuperiorSkyblockPlugin plugin) {
        return null;
    }

    @Override
    public SuperiorCommand[] getSuperiorCommands(SuperiorSkyblockPlugin plugin) {
        return !enabled ? null : new SuperiorCommand[]{new CmdMission(), new CmdMissions()};
    }

    @Override
    public SuperiorCommand[] getSuperiorAdminCommands(SuperiorSkyblockPlugin plugin) {
        return !enabled ? null : new SuperiorCommand[]{new CmdAdminMission()};
    }

    @Override
    public void onDisable(SuperiorSkyblockPlugin plugin) {
        if (enabled)
            plugin.getMissions().saveMissionsData();
    }

    @Override
    public boolean isEnabled() {
        return enabled && isInitialized();
    }

    private void generateDefaultFiles() {
        FileUtils.copyResource("modules/missions/BlocksMissions");
        FileUtils.copyResource("modules/missions/BrewingMissions");
        FileUtils.copyResource("modules/missions/CraftingMissions");
        FileUtils.copyResource("modules/missions/EnchantingMissions");
        FileUtils.copyResource("modules/missions/FarmingMissions");
        FileUtils.copyResource("modules/missions/FishingMissions");
        FileUtils.copyResource("modules/missions/IslandMissions");
        FileUtils.copyResource("modules/missions/ItemsMissions");
        FileUtils.copyResource("modules/missions/KillsMissions");
        FileUtils.copyResource("modules/missions/StatisticsMissions");

        File categoriesFolder = new File(getDataFolder(), "categories");

        if ((!categoriesFolder.exists() || !categoriesFolder.isDirectory()) && categoriesFolder.mkdirs()) {
            FileUtils.saveResource("modules/missions/categories/farmer/farmer_1.yml");
            FileUtils.saveResource("modules/missions/categories/farmer/farmer_2.yml");
            FileUtils.saveResource("modules/missions/categories/farmer/farmer_3.yml");
            FileUtils.saveResource("modules/missions/categories/farmer/farmer_4.yml");
            FileUtils.saveResource("modules/missions/categories/farmer/farmer_5.yml");
            FileUtils.saveResource("modules/missions/categories/miner/miner_1.yml");
            FileUtils.saveResource("modules/missions/categories/miner/miner_2.yml");
            FileUtils.saveResource("modules/missions/categories/miner/miner_3.yml");
            FileUtils.saveResource("modules/missions/categories/miner/miner_4.yml");
            FileUtils.saveResource("modules/missions/categories/miner/miner_5.yml");
            FileUtils.saveResource("modules/missions/categories/slayer/slayer_1.yml");
            FileUtils.saveResource("modules/missions/categories/slayer/slayer_2.yml");
            FileUtils.saveResource("modules/missions/categories/slayer/slayer_3.yml");
            FileUtils.saveResource("modules/missions/categories/slayer/slayer_4.yml");
            FileUtils.saveResource("modules/missions/categories/fisherman/fisherman_1.yml");
            FileUtils.saveResource("modules/missions/categories/fisherman/fisherman_2.yml");
            FileUtils.saveResource("modules/missions/categories/fisherman/fisherman_3.yml");
            FileUtils.saveResource("modules/missions/categories/explorer/explorer_1.yml");
            FileUtils.saveResource("modules/missions/categories/explorer/explorer_2.yml");
        }
    }

    @Override
    protected void onPluginInit(SuperiorSkyblockPlugin plugin) {
        File file = new File(getDataFolder(), "config.yml");

        if (!file.exists())
            FileUtils.saveResource("modules/missions/config.yml");

        config = CommentedConfiguration.loadConfiguration(file);

        convertOldMissions(plugin, file, config);
        convertNonCategorizedMissions(plugin, file, config);
        generateDefaultFiles();

        try {
            config.syncWithConfig(file, FileUtils.getResource("modules/missions/config.yml"), getIgnoredSections());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        updateConfig(plugin);
    }

    @Override
    protected void updateConfig(SuperiorSkyblockPlugin plugin) {
        enabled = config.getBoolean("enabled");
    }

    @Override
    protected String[] getIgnoredSections() {
        return new String[]{"categories"};
    }

    private boolean canLoadCategory(SuperiorSkyblockPlugin plugin, String categoryName, List<Mission<?>> categoryMissions) {
        File categoryFolder = new File(getDataFolder(), "categories/" + categoryName);

        if (!categoryFolder.exists()) {
            SuperiorSkyblockPlugin.log("&cThe directory of the mission category " + categoryName + " doesn't exist, skipping...");
            return false;
        }

        if (!categoryFolder.isDirectory()) {
            SuperiorSkyblockPlugin.log("&cThe directory of the mission category " + categoryName + " is not valid, skipping...");
            return false;
        }

        File[] missionFiles = categoryFolder.listFiles(file ->
                file.isFile() && file.getName().endsWith(".yml"));

        if (missionFiles == null || missionFiles.length == 0) {
            SuperiorSkyblockPlugin.log("&cThe mission category " + categoryName + " doesn't have missions, skipping...");
            return false;
        }

        for (File missionFile : missionFiles) {
            String missionName = missionFile.getName().replace(".yml", "");

            YamlConfiguration missionConfigFile = new YamlConfiguration();

            try {
                missionConfigFile.load(missionFile);
            } catch (InvalidConfigurationException ex) {
                SuperiorSkyblockPlugin.log("&cError occurred while parsing mission file " + missionFile.getName() + ":");
                ex.printStackTrace();
                continue;
            } catch (IOException ex) {
                SuperiorSkyblockPlugin.log("&cError occurred while opening mission file " + missionFile.getName() + ":");
                ex.printStackTrace();
                continue;
            }

            ConfigurationSection missionSection = missionConfigFile.getConfigurationSection("");

            Mission<?> mission = plugin.getMissions().loadMission(missionName, getDataFolder(), missionSection);
            if (mission != null)
                categoryMissions.add(mission);
        }

        if (categoryMissions.isEmpty()) {
            SuperiorSkyblockPlugin.log("&cThe mission category " + categoryName + " doesn't have missions, skipping...");
            return false;
        }

        // Sort missions by their names.
        categoryMissions.sort(Comparator.comparing(Mission::getName));

        return true;
    }

    private YamlConfiguration loadMissionsMenuFile(SuperiorSkyblockPlugin plugin) {
        File missionsMenuFile = new File(plugin.getDataFolder(), "menus/missions.yml");

        if (!missionsMenuFile.exists())
            FileUtils.saveResource("menus/missions.yml");

        return CommentedConfiguration.loadConfiguration(missionsMenuFile);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void convertNonCategorizedMissions(SuperiorSkyblockPlugin plugin, File file, YamlConfiguration config) {
        ConfigurationSection missionsSection = config.getConfigurationSection("missions");

        if (missionsSection == null)
            return;

        ConfigurationSection categoriesSection = config.createSection("categories");
        YamlConfiguration missionsMenuConfig = loadMissionsMenuFile(plugin);
        MenuPatternSlots menuPatternSlots = FileUtils.loadGUI(MenuMissions.createEmptyInstance(), "missions.yml", missionsMenuConfig);

        int islandsCategorySlot = menuPatternSlots.getSlot(missionsMenuConfig.getString("island-missions", ""));
        if (islandsCategorySlot != -1) {
            categoriesSection.set("islands.name", "Islands");
            categoriesSection.set("islands.slot", islandsCategorySlot);
        }

        int playersCategorySlot = menuPatternSlots.getSlot(missionsMenuConfig.getString("player-missions", ""));
        if (playersCategorySlot != -1) {
            categoriesSection.set("players.name", "Players");
            categoriesSection.set("players.slot", playersCategorySlot);
        }

        File islandsCategoryFile = new File(getDataFolder(), "categories/islands");
        File playersCategoryFile = new File(getDataFolder(), "categories/players");

        islandsCategoryFile.mkdirs();
        playersCategoryFile.mkdirs();

        for (String missionName : missionsSection.getKeys(false)) {
            ConfigurationSection missionSection = missionsSection.getConfigurationSection(missionName);

            if (missionSection == null)
                continue;

            boolean islandsMission = missionSection.getBoolean("island", false);

            File missionFile = new File(islandsMission ? islandsCategoryFile : playersCategoryFile, missionName + ".yml");

            try {
                missionFile.createNewFile();
            } catch (IOException error) {
                error.printStackTrace();
                continue;
            }

            YamlConfiguration missionConfigFile = new YamlConfiguration();
            missionSection.getValues(true).forEach(missionConfigFile::set);

            try {
                missionConfigFile.save(missionFile);
            } catch (Exception error) {
                error.printStackTrace();
            }
        }

        config.set("missions", "");

        try {
            config.save(file);
        } catch (Exception error) {
            error.printStackTrace();
        }

        copyOldMissionsMenuFile(plugin);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void convertOldMissions(SuperiorSkyblockPlugin plugin, File file, YamlConfiguration config) {
        File oldMissionsFolder = new File(plugin.getDataFolder(), "missions");
        if (oldMissionsFolder.exists()) {
            File oldMissionsFile = new File(oldMissionsFolder, "missions.yml");

            if (oldMissionsFile.exists()) {
                YamlConfiguration oldConfig = YamlConfiguration.loadConfiguration(oldMissionsFile);
                config.set("missions", oldConfig.getConfigurationSection(""));

                try {
                    config.save(file);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                oldMissionsFile.delete();
            }

            File[] oldMissionsFiles = oldMissionsFolder.listFiles();

            if (oldMissionsFiles != null) {
                for (File jarFile : oldMissionsFiles) {
                    if (jarFile.getName().endsWith(".jar"))
                        jarFile.renameTo(new File(getDataFolder(), jarFile.getName()));
                }
            }

            File oldDataFile = new File(oldMissionsFolder, "_data.yml");
            if (oldDataFile.exists())
                oldDataFile.renameTo(new File(getDataFolder(), "_data.yml"));

            FileUtils.deleteDirectory(oldMissionsFolder);
        }
    }

    private void copyOldMissionsMenuFile(SuperiorSkyblockPlugin plugin) {
        File oldMissionsMenuFile = new File(plugin.getDataFolder(), "menus/island-missions.yml");
        File newMissionsCategoryMenuFile = new File(plugin.getDataFolder(), "menus/missions-category.yml");

        try {
            Files.copy(Paths.get(oldMissionsMenuFile.toURI()), Paths.get(newMissionsCategoryMenuFile.toURI()));
        } catch (IOException error) {
            SuperiorSkyblockPlugin.log("&cError occurred while copying old missions-menu to the new format, skipping...");
            return;
        }

        YamlConfiguration newMissionsCategoryMenuConfig = YamlConfiguration.loadConfiguration(newMissionsCategoryMenuFile);
        newMissionsCategoryMenuConfig.set("title", "&l{0} Missions");

        try {
            newMissionsCategoryMenuConfig.save(newMissionsCategoryMenuFile);
        } catch (IOException ignored) {
        }
    }

}
