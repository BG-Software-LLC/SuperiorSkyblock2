package com.bgsoftware.superiorskyblock.module.missions;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.core.collections.ArrayMap;
import com.bgsoftware.superiorskyblock.core.io.Files;
import com.bgsoftware.superiorskyblock.core.io.MenuParserImpl;
import com.bgsoftware.superiorskyblock.core.io.Resources;
import com.bgsoftware.superiorskyblock.core.io.loader.FilesLookup;
import com.bgsoftware.superiorskyblock.core.io.loader.FilesLookupFactory;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuIslandMembers;
import com.bgsoftware.superiorskyblock.mission.SMissionCategory;
import com.bgsoftware.superiorskyblock.module.BuiltinModule;
import com.bgsoftware.superiorskyblock.module.IModuleConfiguration;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MissionsModule extends BuiltinModule<MissionsModule.Configuration> {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final String[] IGNORED_SECTIONS = new String[]{"categories"};
    private static final int MAX_MISSIONS_NAME_LENGTH = 255;

    public MissionsModule() {
        super("missions");
    }

    @Override
    protected boolean onConfigCreate(SuperiorSkyblockPlugin plugin, CommentedConfiguration config, boolean firstTime) {
        boolean updatedConfig = false;

        if (convertOldMissions(plugin, config))
            updatedConfig = true;
        if (convertNonCategorizedMissions(plugin, config))
            updatedConfig = true;

        generateDefaultMissionJars();

        if (firstTime) {
            generateDefaultFiles();
        }

        return updatedConfig;
    }

    @Override
    protected void onEnable(SuperiorSkyblockPlugin plugin) {
        // Do nothing
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
        if (isEnabled())
            plugin.getMissions().saveMissionsData();
    }

    @Override
    public void loadData(SuperiorSkyblockPlugin plugin) {
        List<Mission<?>> missionsToLoad = this.configuration.missionsToLoad;
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
        return new SuperiorCommand[]{new CmdMission(), new CmdMissions()};
    }

    @Override
    public SuperiorCommand[] getSuperiorAdminCommands(SuperiorSkyblockPlugin plugin) {
        return new SuperiorCommand[]{new CmdAdminMission()};
    }

    @Override
    protected String[] getIgnoredSections() {
        return IGNORED_SECTIONS;
    }

    @Override
    protected Configuration createConfigFile(CommentedConfiguration config) {
        return new Configuration(config);
    }

    public class Configuration implements IModuleConfiguration {

        private final boolean enabled;
        private final boolean autoRewardOutsideIslands;
        private final List<Mission<?>> missionsToLoad = new LinkedList<>();

        Configuration(CommentedConfiguration config) {
            this.enabled = config.getBoolean("enabled");
            this.autoRewardOutsideIslands = config.getBoolean("auto-reward-outside-islands");
            if (this.enabled) {
                loadMissionCategories(config);
            }
        }

        @Override
        public boolean isEnabled() {
            return this.enabled;
        }

        public boolean isAutoRewardOutsideIslands() {
            return this.autoRewardOutsideIslands;
        }

        private void loadMissionCategories(CommentedConfiguration config) {
            ConfigurationSection categoriesSection = config.getConfigurationSection("categories");

            if (categoriesSection != null) {
                for (String categoryName : categoriesSection.getKeys(false)) {
                    ConfigurationSection categorySection = categoriesSection.getConfigurationSection(categoryName);

                    if (categorySection == null)
                        continue;

                    List<Mission<?>> categoryMissions = new LinkedList<>();

                    if (!canLoadCategory(categoryName, categoryMissions))
                        continue;

                    int slot = categorySection.getInt("slot");

                    String formattedCategoryName = categorySection.getString("name", categoryName);

                    plugin.getMissions().loadMissionCategory(new SMissionCategory(formattedCategoryName, slot, categoryMissions));

                    missionsToLoad.addAll(categoryMissions);
                }
            }
        }

        private boolean canLoadCategory(String categoryName, List<Mission<?>> categoryMissions) {
            File categoryFolder = new File(getModuleFolder(), "categories/" + categoryName);

            if (!categoryFolder.exists()) {
                logger().w("The directory of the mission category " + categoryName + " doesn't exist, skipping...");
                return false;
            }

            if (!categoryFolder.isDirectory()) {
                logger().w("The directory of the mission category " + categoryName + " is not valid, skipping...");
                return false;
            }

            File[] missionFiles = categoryFolder.listFiles(file ->
                    file.isFile() && file.getName().endsWith(".yml"));

            if (missionFiles == null || missionFiles.length == 0) {
                logger().w("The mission category " + categoryName + " doesn't have missions, skipping...");
                return false;
            }

            Map<Mission<?>, Integer> missionWeights = new ArrayMap<>();

            try (FilesLookup filesLookup = FilesLookupFactory.getInstance().lookupFolder(getModuleFolder())) {
                for (File missionFile : missionFiles) {
                    String missionName = missionFile.getName().replace(".yml", "");

                    if (missionName.length() > MAX_MISSIONS_NAME_LENGTH)
                        missionName = missionName.substring(0, MAX_MISSIONS_NAME_LENGTH);

                    YamlConfiguration missionConfigFile = new YamlConfiguration();

                    try {
                        missionConfigFile.load(missionFile);
                    } catch (InvalidConfigurationException error) {
                        logger().e("A format-error occurred while parsing the mission file " + missionFile.getName() + ":", error);
                        continue;
                    } catch (IOException error) {
                        logger().e("An unexpected error occurred while parsing the mission file " + missionFile.getName() + ":", error);
                        continue;
                    }

                    ConfigurationSection missionSection = missionConfigFile.getConfigurationSection("");

                    Mission<?> mission = plugin.getMissions().loadMission(missionName, categoryName, filesLookup, missionSection);

                    if (mission != null) {
                        categoryMissions.add(mission);
                        missionWeights.put(mission, missionSection.getInt("weight", 0));
                    }
                }
            }

            if (categoryMissions.isEmpty()) {
                logger().w("The mission category " + categoryName + " doesn't have missions, skipping...");
                return false;
            }

            // Sort missions by their names and weights.
            categoryMissions.sort(new MissionsComparator(missionWeights));

            return true;
        }

    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private boolean convertNonCategorizedMissions(SuperiorSkyblockPlugin plugin, YamlConfiguration config) {
        ConfigurationSection missionsSection = config.getConfigurationSection("missions");

        if (missionsSection == null)
            return false;

        ConfigurationSection categoriesSection = config.createSection("categories");

        MenuParseResult<MenuIslandMembers.View> menuLoadResult = MenuParserImpl.getInstance().loadMenu("missions.yml",
                null);

        if (menuLoadResult == null)
            return false;

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
                logger().e("An unexpected error occurred while converting non-categorized mission " + missionName + ":", error);
                continue;
            }

            YamlConfiguration missionConfigFile = new YamlConfiguration();
            missionSection.getValues(true).forEach(missionConfigFile::set);

            try {
                missionConfigFile.save(missionFile);
            } catch (Exception error) {
                logger().e("An unexpected error occurred while saving non-categorized mission " + missionName + ":", error);
            }
        }

        config.set("missions", "");

        copyOldMissionsMenuFile(plugin);

        return true;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private boolean convertOldMissions(SuperiorSkyblockPlugin plugin, YamlConfiguration config) {
        boolean updatedConfig = false;

        File oldMissionsFolder = new File(plugin.getDataFolder(), "missions");
        if (oldMissionsFolder.exists()) {
            File oldMissionsFile = new File(oldMissionsFolder, "missions.yml");

            if (oldMissionsFile.exists()) {
                YamlConfiguration oldConfig = YamlConfiguration.loadConfiguration(oldMissionsFile);
                config.set("missions", oldConfig.getConfigurationSection(""));

                updatedConfig = true;

                oldMissionsFile.delete();
            }

            for (File jarFile : Files.listFolderFiles(oldMissionsFolder, false, f -> f.getName().endsWith(".jar"))) {
                jarFile.renameTo(new File(getModuleFolder(), jarFile.getName()));
            }

            File oldDataFile = new File(oldMissionsFolder, "_data.yml");
            if (oldDataFile.exists())
                oldDataFile.renameTo(new File(getModuleFolder(), "_data.yml"));

            Files.deleteDirectory(oldMissionsFolder);
        }

        return updatedConfig;
    }

    private void copyOldMissionsMenuFile(SuperiorSkyblockPlugin plugin) {
        File oldMissionsMenuFile = new File(plugin.getDataFolder(), "menus/island-missions.yml");
        File newMissionsCategoryMenuFile = new File(plugin.getDataFolder(), "menus/missions-category.yml");

        try {
            java.nio.file.Files.copy(Paths.get(oldMissionsMenuFile.toURI()), Paths.get(newMissionsCategoryMenuFile.toURI()));
        } catch (IOException error) {
            logger().e("An unexpected error occurred while copying old missions-menu to the new format:", error);
            return;
        }

        YamlConfiguration newMissionsCategoryMenuConfig = YamlConfiguration.loadConfiguration(newMissionsCategoryMenuFile);
        newMissionsCategoryMenuConfig.set("title", "&l{0} Missions");

        try {
            newMissionsCategoryMenuConfig.save(newMissionsCategoryMenuFile);
        } catch (IOException ignored) {
        }
    }

    private void generateDefaultFiles() {
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

    private void generateDefaultMissionJars() {
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
