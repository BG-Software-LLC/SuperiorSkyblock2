package com.bgsoftware.superiorskyblock.core.zmenu.buttons;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.zmenu.PlayerCache;
import com.bgsoftware.superiorskyblock.core.zmenu.ZMenuManager;
import fr.maxlego08.menu.button.ZButton;
import org.bukkit.entity.Player;

public abstract class SuperiorButton extends ZButton {

    protected final SuperiorSkyblockPlugin plugin;
    protected final ZMenuManager menuManager;

    public SuperiorButton(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
        this.menuManager = plugin.getZMenumanager();
    }

    protected SuperiorPlayer getSuperiorPlayer(Player player) {
        return this.plugin.getPlayers().getSuperiorPlayer(player);
    }

    protected PlayerCache getCache(Player player) {
        return this.menuManager.getCache(player);
    }
}
