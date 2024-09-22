package com.bgsoftware.superiorskyblock.core.zmenu.loader;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.core.zmenu.buttons.border.BorderColorButton;
import fr.maxlego08.menu.api.button.Button;
import fr.maxlego08.menu.api.button.DefaultButtonValue;
import org.bukkit.configuration.file.YamlConfiguration;

public class BorderColorLoader extends SuperiorButtonLoader {

    public BorderColorLoader(SuperiorSkyblockPlugin plugin) {
        super(plugin, "BORDER_COLOR");
    }

    @Override
    public Class<? extends Button> getButton() {
        return BorderColorButton.class;
    }

    @Override
    public Button load(YamlConfiguration configuration, String path, DefaultButtonValue defaultButtonValue) {
        BorderColor borderColor = BorderColor.valueOf(configuration.getString(path + "border-color", BorderColor.BLUE.name()));
        return new BorderColorButton(plugin, borderColor);
    }
}
