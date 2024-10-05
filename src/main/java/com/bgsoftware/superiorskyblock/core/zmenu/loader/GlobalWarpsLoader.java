package com.bgsoftware.superiorskyblock.core.zmenu.loader;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.zmenu.buttons.GlobalWarpsButton;
import com.bgsoftware.superiorskyblock.core.zmenu.buttons.bank.BankLogsSortButton;
import fr.maxlego08.menu.api.button.Button;
import fr.maxlego08.menu.api.button.DefaultButtonValue;
import org.bukkit.configuration.file.YamlConfiguration;

public class GlobalWarpsLoader extends SuperiorButtonLoader {

    public GlobalWarpsLoader(SuperiorSkyblockPlugin plugin) {
        super(plugin, "GLOBAL_WARPS");
    }

    @Override
    public Class<? extends Button> getButton() {
        return BankLogsSortButton.class;
    }

    @Override
    public Button load(YamlConfiguration configuration, String path, DefaultButtonValue defaultButtonValue) {
        boolean visitorWarps = configuration.getBoolean("visitor-warps");
        return new GlobalWarpsButton(this.plugin, visitorWarps);
    }
}
