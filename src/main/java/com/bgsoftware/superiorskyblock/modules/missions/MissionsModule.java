package com.bgsoftware.superiorskyblock.modules.missions;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.missions.SMissionCategory;
import com.bgsoftware.superiorskyblock.modules.BuiltinModule;
import com.bgsoftware.superiorskyblock.modules.missions.commands.CmdAdminMission;
import com.bgsoftware.superiorskyblock.modules.missions.commands.CmdMission;
import com.bgsoftware.superiorskyblock.modules.missions.commands.CmdMissions;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class MissionsModule extends BuiltinModule {

    private boolean enabled = true;

    public MissionsModule(){
        super("missions");
    }

    @Override
    public void onEnable(SuperiorSkyblockPlugin plugin) {
        if(!enabled)
            return;

        List<Mission<?>> missionsToLoad = new ArrayList<>();

        ConfigurationSection categoriesSection = config.getConfigurationSection("categories");

        if(categoriesSection != null) {
            for (String categoryName : categoriesSection.getKeys(false)) {
                ConfigurationSection categorySection = categoriesSection.getConfigurationSection(categoryName);

                if(categorySection == null)
                    continue;

                List<Mission<?>> categoryMissions = new ArrayList<>();

                if(!canLoadCategory(plugin, categoryName, categoryMissions))
                    continue;

                int slot = categorySection.getInt("slot");

                String formattedCategoryName = categorySection.getString("name", categoryName);

                plugin.getMissions().loadMissionCategory(new SMissionCategory(formattedCategoryName, slot, categoryMissions));

                missionsToLoad.addAll(categoryMissions);
            }
        }

        if(!missionsToLoad.isEmpty()) {
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
        return !enabled ? null : new SuperiorCommand[] {new CmdMission(), new CmdMissions()};
    }

    @Override
    public SuperiorCommand[] getSuperiorAdminCommands(SuperiorSkyblockPlugin plugin) {
        return !enabled ? null : new SuperiorCommand[] {new CmdAdminMission()};
    }

    @Override
    public void onDisable(SuperiorSkyblockPlugin plugin) {
        if(enabled)
            plugin.getMissions().saveMissionsData();
    }

    @Override
    public boolean isEnabled() {
        return enabled && isInitialized();
    }

    @Override
    protected void onPluginInit(SuperiorSkyblockPlugin plugin) {
        super.onPluginInit(plugin);

        FileUtils.copyResource("modules/missions/BlocksMissions");
        FileUtils.copyResource("modules/missions/CraftingMissions");
        FileUtils.copyResource("modules/missions/EnchantingMissions");
        FileUtils.copyResource("modules/missions/IslandMissions");
        FileUtils.copyResource("modules/missions/ItemsMissions");
        FileUtils.copyResource("modules/missions/KillsMissions");
        FileUtils.copyResource("modules/missions/StatisticsMissions");

        File file = new File(getDataFolder(), "config.yml");

        if(!file.exists())
            FileUtils.saveResource("modules/missions/config.yml");

        File categoriesFolder = new File(getDataFolder(), "categories");

        if((!categoriesFolder.exists() || !categoriesFolder.isDirectory()) && categoriesFolder.mkdirs()){
            FileUtils.saveResource("modules/missions/categories/islands/10k-worth.yml");
            FileUtils.saveResource("modules/missions/categories/islands/add-member.yml");
            FileUtils.saveResource("modules/missions/categories/players/birthday.yml");
            FileUtils.saveResource("modules/missions/categories/players/cobble-miner.yml");
            FileUtils.saveResource("modules/missions/categories/players/dragon-killer.yml");
            FileUtils.saveResource("modules/missions/categories/players/enchanter.yml");
            FileUtils.saveResource("modules/missions/categories/players/first-invite.yml");
            FileUtils.saveResource("modules/missions/categories/players/first-island.yml");
            FileUtils.saveResource("modules/missions/categories/players/mobs-killer.yml");
            FileUtils.saveResource("modules/missions/categories/players/woodcutter.yml");
        }

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        try{
            cfg.syncWithConfig(file, FileUtils.getResource("modules/missions/config.yml"), getIgnoredSections());
        }catch (Exception ex){
            ex.printStackTrace();
        }

        convertOldMissions(plugin, file, cfg);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void convertOldMissions(SuperiorSkyblockPlugin plugin, File file, YamlConfiguration config){
        File oldMissionsFolder = new File(plugin.getDataFolder(), "missions");
        if(oldMissionsFolder.exists()) {
            File oldMissionsFile = new File(oldMissionsFolder, "missions.yml");

            if(oldMissionsFile.exists()) {
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

            if(oldMissionsFiles != null) {
                for (File jarFile : oldMissionsFiles) {
                    if (jarFile.getName().endsWith(".jar"))
                        jarFile.renameTo(new File(getDataFolder(), jarFile.getName()));
                }
            }

            File oldDataFile = new File(oldMissionsFolder, "_data.yml");
            if(oldDataFile.exists())
                oldDataFile.renameTo(new File(getDataFolder(), "_data.yml"));

            FileUtils.deleteDirectory(oldMissionsFolder);
        }
    }

    @Override
    protected void updateConfig(SuperiorSkyblockPlugin plugin){
        enabled = config.getBoolean("enabled");
    }

    @Override
    protected String[] getIgnoredSections() {
        return new String[] { "categories" };
    }

    private boolean canLoadCategory(SuperiorSkyblockPlugin plugin, String categoryName, List<Mission<?>> categoryMissions){
        File categoryFolder = new File(getDataFolder(), "categories/" + categoryName);

        if(!categoryFolder.exists()) {
            SuperiorSkyblockPlugin.log("&cThe directory of the mission category " + categoryName + " doesn't exist, skipping...");
            return false;
        }

        if(!categoryFolder.isDirectory()) {
            SuperiorSkyblockPlugin.log("&cThe directory of the mission category " + categoryName + " is not valid, skipping...");
            return false;
        }

        File[] missionFiles = categoryFolder.listFiles(file ->
                file.isFile() && file.getName().endsWith(".yml"));

        if(missionFiles == null || missionFiles.length == 0) {
            SuperiorSkyblockPlugin.log("&cThe mission category " + categoryName + " doesn't have missions, skipping...");
            return false;
        }

        for(File missionFile : missionFiles) {
            String missionName = missionFile.getName().replace(".yml", "");

            YamlConfiguration missionConfigFile = new YamlConfiguration();

            try {
                missionConfigFile.load(missionFile);
            } catch (InvalidConfigurationException ex) {
                SuperiorSkyblockPlugin.log("&cError occurred while parsing mission file " + missionFile.getName() + ":");
                ex.printStackTrace();
                continue;
            } catch (IOException ex){
                SuperiorSkyblockPlugin.log("&cError occurred while opening mission file " + missionFile.getName() + ":");
                ex.printStackTrace();
                continue;
            }

            ConfigurationSection missionSection = missionConfigFile.getConfigurationSection("");

            Mission<?> mission = plugin.getMissions().loadMission(missionName, getDataFolder(), missionSection);
            if (mission != null)
                categoryMissions.add(mission);
        }

        if(categoryMissions.isEmpty()) {
            SuperiorSkyblockPlugin.log("&cThe mission category " + categoryName + " doesn't have missions, skipping...");
            return false;
        }

        return true;
    }

}
