package com.bgsoftware.superiorskyblock.module.missions;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.core.io.Files;
import com.bgsoftware.superiorskyblock.core.io.MenuParserImpl;
import com.bgsoftware.superiorskyblock.core.io.Resources;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuIslandMembers;
import com.bgsoftware.superiorskyblock.mission.SMissionCategory;
import com.bgsoftware.superiorskyblock.module.BuiltinModule;
import com.bgsoftware.superiorskyblock.module.missions.commands.CmdAdminMission;
import com.bgsoftware.superiorskyblock.module.missions.commands.CmdMission;
import com.bgsoftware.superiorskyblock.module.missions.commands.CmdMissions;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MissionsModule extends BuiltinModule {

    private static final int MAX_MISSIONS_NAME_LENGTH = 255;

    private final List<Mission<?>> missionsToLoad = new LinkedList<>();

    private boolean enabled = true;

    public MissionsModule() {
        super("missions");
    }

    private void generateDefaultFiles() {
        Resources.copyResource("modules/missions/BlocksMissions");
        Resources.copyResource("modules/missions/BrewingMissions");
        Resources.copyResource("modules/missions/CraftingMissions");
        Resources.copyResource("modules/missions/EnchantingMissions");
        Resources.copyResource("modules/missions/FarmingMissions");
        Resources.copyResource("modules/missions/FishingMissions");
        Resources.copyResource("modules/missions/IslandMissions");
        Resources.copyResource("modules/missions/ItemsMissions");
        Resources.copyResource("modules/missions/KillsMissions");
        Resources.copyResource("modules/missions/StatisticsMissions");

        File categoriesFolder = new File(getModuleFolder(), "categories");

        if ((!categoriesFolder.exists() || !categoriesFolder.isDirectory()) && categoriesFolder.mkdirs()) {
            Resources.saveResource("modules/missions/categories/farmer/farmer_1.yml");
            Resources.saveResource("modules/missions/categories/farmer/farmer_2.yml");
            Resources.saveResource("modules/missions/categories/farmer/farmer_3.yml");
            Resources.saveResource("modules/missions/categories/farmer/farmer_4.yml");
            Resources.saveResource("modules/missions/categories/farmer/farmer_5.yml");
            Resources.saveResource("modules/missions/categories/miner/miner_1.yml");
            Resources.saveResource("modules/missions/categories/miner/miner_2.yml");
            Resources.saveResource("modules/missions/categories/miner/miner_3.yml");
            Resources.saveResource("modules/missions/categories/miner/miner_4.yml");
            Resources.saveResource("modules/missions/categories/miner/miner_5.yml");
            Resources.saveResource("modules/missions/categories/slayer/slayer_1.yml");
            Resources.saveResource("modules/missions/categories/slayer/slayer_2.yml");
            Resources.saveResource("modules/missions/categories/slayer/slayer_3.yml");
            Resources.saveResource("modules/missions/categories/slayer/slayer_4.yml");
            Resources.saveResource("modules/missions/categories/fisherman/fisherman_1.yml");
            Resources.saveResource("modules/missions/categories/fisherman/fisherman_2.yml");
            Resources.saveResource("modules/missions/categories/fisherman/fisherman_3.yml");
            Resources.saveResource("modules/missions/categories/explorer/explorer_1.yml");
            Resources.saveResource("modules/missions/categories/explorer/explorer_2.yml");
        }
    }

    @Override
    protected void onPluginInit(SuperiorSkyblockPlugin plugin) {
        File file = new File(getModuleFolder(), "config.yml");

        if (!file.exists())
            Resources.saveResource("modules/missions/config.yml");

        config = CommentedConfiguration.loadConfiguration(file);

        convertOldMissions(plugin, file, config);
        convertNonCategorizedMissions(plugin, file, config);
        generateDefaultFiles();

        try {
            config.syncWithConfig(file, Resources.getResource("modules/missions/config.yml"), getIgnoredSections());
        } catch (Exception error) {
            Log.entering("MissionsModule", "onPluginInit", "ENTER");
            Log.error(error, "An error occurred while saving config file:");
        }

        updateConfig(plugin);
    }

    @Override
    public void onEnable(SuperiorSkyblockPlugin plugin) {
        if (enabled)
            loadMissionCategories(plugin);
    }

    @Override
    public void onReload(SuperiorSkyblockPlugin plugin) {
        plugin.getMissions().saveMissionsData();
        // Before we continue with the reload, we want to unload all the missions.
        plugin.getMissions().clearData();
        super.onReload(plugin);
        onEnable(plugin);
        plugin.getMissions().loadMissionsData();
    }

    @Override
    public void onDisable(SuperiorSkyblockPlugin plugin) {
        if (enabled)
            plugin.getMissions().saveMissionsData();
    }

    @Override
    public void loadData(SuperiorSkyblockPlugin plugin) {
        if (!missionsToLoad.isEmpty()) {
            plugin.getMissions().loadMissionsData(missionsToLoad);
            missionsToLoad.clear();
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
    public boolean isEnabled() {
        return enabled && isInitialized();
    }

    @Override
    protected void updateConfig(SuperiorSkyblockPlugin plugin) {
        enabled = config.getBoolean("enabled");
    }

    @Override
    protected String[] getIgnoredSections() {
        return new String[]{"categories"};
    }

    private void loadMissionCategories(SuperiorSkyblockPlugin plugin) {
        ConfigurationSection categoriesSection = config.getConfigurationSection("categories");

        if (categoriesSection != null) {
            for (String categoryName : categoriesSection.getKeys(false)) {
                ConfigurationSection categorySection = categoriesSection.getConfigurationSection(categoryName);

                if (categorySection == null)
                    continue;

                List<Mission<?>> categoryMissions = new LinkedList<>();

                if (!canLoadCategory(plugin, categoryName, categoryMissions))
                    continue;

                int slot = categorySection.getInt("slot");

                String formattedCategoryName = categorySection.getString("name", categoryName);

                plugin.getMissions().loadMissionCategory(new SMissionCategory(formattedCategoryName, slot, categoryMissions));

                missionsToLoad.addAll(categoryMissions);
            }
        }
    }

    private boolean canLoadCategory(SuperiorSkyblockPlugin plugin, String categoryName, List<Mission<?>> categoryMissions) {
        File categoryFolder = new File(getModuleFolder(), "categories/" + categoryName);

        if (!categoryFolder.exists()) {
            Log.warn("The directory of the mission category ", categoryName, " doesn't exist, skipping...");
            return false;
        }

        if (!categoryFolder.isDirectory()) {
            Log.warn("The directory of the mission category ", categoryName, " is not valid, skipping...");
            return false;
        }

        File[] missionFiles = categoryFolder.listFiles(file ->
                file.isFile() && file.getName().endsWith(".yml"));

        if (missionFiles == null || missionFiles.length == 0) {
            Log.warn("The mission category ", categoryName, " doesn't have missions, skipping...");
            return false;
        }

        Map<Mission<?>, Integer> missionWeights = new HashMap<>();

        for (File missionFile : missionFiles) {
            String missionName = missionFile.getName().replace(".yml", "");

            if (missionName.length() > MAX_MISSIONS_NAME_LENGTH)
                missionName = missionName.substring(0, MAX_MISSIONS_NAME_LENGTH);

            YamlConfiguration missionConfigFile = new YamlConfiguration();

            try {
                missionConfigFile.load(missionFile);
            } catch (InvalidConfigurationException error) {
                Log.error(error, "A format-error occurred while parsing the mission file ", missionFile.getName() + ":");
                continue;
            } catch (IOException error) {
                Log.error(error, "An unexpected error occurred while parsing the mission file ", missionFile.getName() + ":");
                continue;
            }

            ConfigurationSection missionSection = missionConfigFile.getConfigurationSection("");

            Mission<?> mission = plugin.getMissions().loadMission(missionName, getModuleFolder(), missionSection);

            if (mission != null) {
                categoryMissions.add(mission);
                missionWeights.put(mission, missionSection.getInt("weight", 0));
            }
        }

        if (categoryMissions.isEmpty()) {
            Log.warn("The mission category ", categoryName, " doesn't have missions, skipping...");
            return false;
        }

        // Sort missions by their names and weights.
        categoryMissions.sort(new MissionsComparator(missionWeights));

        return true;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void convertNonCategorizedMissions(SuperiorSkyblockPlugin plugin, File file, YamlConfiguration config) {
        ConfigurationSection missionsSection = config.getConfigurationSection("missions");

        if (missionsSection == null)
            return;

        ConfigurationSection categoriesSection = config.createSection("categories");

        MenuParseResult<MenuIslandMembers.View> menuLoadResult = MenuParserImpl.getInstance().loadMenu("missions.yml",
                null);

        if (menuLoadResult == null)
            return;

        MenuPatternSlots menuPatternSlots = menuLoadResult.getPatternSlots();
        YamlConfiguration missionsMenuConfig = menuLoadResult.getConfig();

        List<Integer> islandsCategorySlot = menuPatternSlots.getSlots(missionsMenuConfig.getString("island-missions", ""));
        if (islandsCategorySlot.isEmpty()) {
            categoriesSection.set("islands.name", "Islands");
            categoriesSection.set("islands.slot", islandsCategorySlot.get(0));
        }

        List<Integer> playersCategorySlot = menuPatternSlots.getSlots(missionsMenuConfig.getString("player-missions", ""));
        if (playersCategorySlot.isEmpty()) {
            categoriesSection.set("players.name", "Players");
            categoriesSection.set("players.slot", playersCategorySlot.get(0));
        }

        File islandsCategoryFile = new File(getModuleFolder(), "categories/islands");
        File playersCategoryFile = new File(getModuleFolder(), "categories/players");

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
                Log.error(error, "An unexpected error occurred while converting non-categorized mission ", missionName, ":");
                continue;
            }

            YamlConfiguration missionConfigFile = new YamlConfiguration();
            missionSection.getValues(true).forEach(missionConfigFile::set);

            try {
                missionConfigFile.save(missionFile);
            } catch (Exception error) {
                Log.error(error, "An unexpected error occurred while saving non-categorized mission ", missionName, ":");
            }
        }

        config.set("missions", "");

        try {
            config.save(file);
        } catch (Exception error) {
            Log.error(error, "An unexpected error occurred while saving missions file:");
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
                } catch (Exception error) {
                    Log.error(error, "An unexpected error occurred while saving old missions file ", file.getName(), ":");
                }

                oldMissionsFile.delete();
            }

            File[] oldMissionsFiles = oldMissionsFolder.listFiles();

            if (oldMissionsFiles != null) {
                for (File jarFile : oldMissionsFiles) {
                    if (jarFile.getName().endsWith(".jar"))
                        jarFile.renameTo(new File(getModuleFolder(), jarFile.getName()));
                }
            }

            File oldDataFile = new File(oldMissionsFolder, "_data.yml");
            if (oldDataFile.exists())
                oldDataFile.renameTo(new File(getModuleFolder(), "_data.yml"));

            Files.deleteDirectory(oldMissionsFolder);
        }
    }

    private void copyOldMissionsMenuFile(SuperiorSkyblockPlugin plugin) {
        File oldMissionsMenuFile = new File(plugin.getDataFolder(), "menus/island-missions.yml");
        File newMissionsCategoryMenuFile = new File(plugin.getDataFolder(), "menus/missions-category.yml");

        try {
            java.nio.file.Files.copy(Paths.get(oldMissionsMenuFile.toURI()), Paths.get(newMissionsCategoryMenuFile.toURI()));
        } catch (IOException error) {
            Log.error(error, "An unexpected error occurred while copying old missions-menu to the new format:");
            return;
        }

        YamlConfiguration newMissionsCategoryMenuConfig = YamlConfiguration.loadConfiguration(newMissionsCategoryMenuFile);
        newMissionsCategoryMenuConfig.set("title", "&l{0} Missions");

        try {
            newMissionsCategoryMenuConfig.save(newMissionsCategoryMenuFile);
        } catch (IOException ignored) {
        }
    }

    private static class MissionsComparator implements Comparator<Mission<?>> {

        private final Map<Mission<?>, Integer> missionWeights;

        public MissionsComparator(Map<Mission<?>, Integer> missionWeights) {
            this.missionWeights = missionWeights;
        }

        @Override
        public int compare(Mission<?> o1, Mission<?> o2) {
            int firstWeight = this.missionWeights.getOrDefault(o1, 0);
            int secondWeight = this.missionWeights.getOrDefault(o2, 0);
            return firstWeight == secondWeight ? o1.getName().compareTo(o2.getName()) : Integer.compare(firstWeight, secondWeight);
        }
    }

}
