package com.bgsoftware.superiorskyblock.modules.missions;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.modules.BuiltinModule;
import com.bgsoftware.superiorskyblock.modules.missions.commands.CmdAdminMission;
import com.bgsoftware.superiorskyblock.modules.missions.commands.CmdMission;
import com.bgsoftware.superiorskyblock.modules.missions.commands.CmdMissions;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;

import java.io.File;
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

        for (String missionName : config.getConfigurationSection("missions").getKeys(false)) {
            ConfigurationSection missionSection = config.getConfigurationSection("missions." + missionName);
            Mission<?> mission = plugin.getMissions().loadMission(missionName, getDataFolder(), missionSection);
            if (mission != null)
                missionsToLoad.add(mission);

        }

        plugin.getMissions().loadMissionsData(missionsToLoad);
    }

    @Override
    public Listener[] getModuleListeners() {
        return null;
    }

    @Override
    public SuperiorCommand[] getSuperiorCommands() {
        return !enabled ? null : new SuperiorCommand[] {new CmdMission(), new CmdMissions()};
    }

    @Override
    public SuperiorCommand[] getSuperiorAdminCommands() {
        return !enabled ? null : new SuperiorCommand[] {new CmdAdminMission()};
    }

    @Override
    public void onDisable() {
        if(enabled)
            plugin.getMissions().saveMissionsData();
    }

    @Override
    public boolean isEnabled() {
        return enabled && isInitialized();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    protected void onPluginInit() {
        super.onPluginInit();

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

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        try{
            cfg.syncWithConfig(file, FileUtils.getResource("modules/missions/config.yml"), "missions");
        }catch (Exception ex){
            ex.printStackTrace();
        }

        File oldMissionsFolder = new File(plugin.getDataFolder(), "missions");
        if(oldMissionsFolder.exists()) {
            File oldMissionsFile = new File(oldMissionsFolder, "missions.yml");

            if(oldMissionsFile.exists()) {
                YamlConfiguration oldConfig = YamlConfiguration.loadConfiguration(oldMissionsFile);
                cfg.set("missions", oldConfig.getConfigurationSection(""));

                try {
                    cfg.save(file);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                oldMissionsFile.delete();
            }

            for(File jarFile : oldMissionsFolder.listFiles()){
                if(jarFile.getName().endsWith(".jar"))
                    jarFile.renameTo(new File(getDataFolder(), jarFile.getName()));
            }

            File oldDataFile = new File(oldMissionsFolder, "_data.yml");
            if(oldDataFile.exists())
                oldDataFile.renameTo(new File(getDataFolder(), "_data.yml"));

            try {
                org.apache.commons.io.FileUtils.deleteDirectory(oldMissionsFolder);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }

    protected void updateConfig(){
        enabled = config.getBoolean("enabled");
    }

}
