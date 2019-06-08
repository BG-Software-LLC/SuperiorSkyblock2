package com.bgsoftware.superiorskyblock.gui;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.utils.FileUtil;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public enum MenuTemplate {

    PANEL("guis/panel/panel-gui.yml"),
    MEMBERS("guis/panel/members-gui.yml"),
    VISITORS("guis/panel/visitors-gui.yml"),
    MEMBER("guis/panel/member-gui.yml"),
    MEMBER_ROLE("guis/panel/member-role-gui.yml");

    private static SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private String path;

    MenuTemplate(String path) {
        this.path = path;
    }

    public YamlConfiguration getFile() {
        File file = new File(plugin.getDataFolder(), path);

        if (!file.exists())
            FileUtil.saveResource(path);

        return YamlConfiguration.loadConfiguration(file);
    }

}
