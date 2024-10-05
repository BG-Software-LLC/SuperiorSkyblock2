package com.bgsoftware.superiorskyblock.core.zmenu.loader;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.core.zmenu.buttons.RateButton;
import com.bgsoftware.superiorskyblock.core.zmenu.buttons.bank.BankLogsSortButton;
import fr.maxlego08.menu.api.button.Button;
import fr.maxlego08.menu.api.button.DefaultButtonValue;
import org.bukkit.configuration.file.YamlConfiguration;

public class RateLoader extends SuperiorButtonLoader {

    public RateLoader(SuperiorSkyblockPlugin plugin) {
        super(plugin, "RATE");
    }

    @Override
    public Class<? extends Button> getButton() {
        return RateButton.class;
    }

    @Override
    public Button load(YamlConfiguration configuration, String path, DefaultButtonValue defaultButtonValue) {
        Rating rating = Rating.valueOf(configuration.getString(path + "rate", Rating.UNKNOWN.name()).toUpperCase());
        return new RateButton(this.plugin, rating);
    }
}
