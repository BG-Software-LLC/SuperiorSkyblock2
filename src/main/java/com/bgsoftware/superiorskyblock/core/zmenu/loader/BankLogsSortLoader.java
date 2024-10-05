package com.bgsoftware.superiorskyblock.core.zmenu.loader;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.zmenu.buttons.bank.BankLogsSortButton;
import fr.maxlego08.menu.api.button.Button;
import fr.maxlego08.menu.api.button.DefaultButtonValue;
import org.bukkit.configuration.file.YamlConfiguration;

public class BankLogsSortLoader extends SuperiorButtonLoader {

    public BankLogsSortLoader(SuperiorSkyblockPlugin plugin) {
        super(plugin, "BANK_LOGS_SORT");
    }

    @Override
    public Class<? extends Button> getButton() {
        return BankLogsSortButton.class;
    }

    @Override
    public Button load(YamlConfiguration configuration, String path, DefaultButtonValue defaultButtonValue) {
        String sort = configuration.getString(path + "sort");
        return new BankLogsSortButton(this.plugin, sort);
    }
}
