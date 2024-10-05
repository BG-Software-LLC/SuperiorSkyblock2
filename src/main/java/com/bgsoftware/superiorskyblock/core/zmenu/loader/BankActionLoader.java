package com.bgsoftware.superiorskyblock.core.zmenu.loader;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.zmenu.buttons.bank.BankActionButton;
import fr.maxlego08.menu.api.button.Button;
import fr.maxlego08.menu.api.button.DefaultButtonValue;
import org.bukkit.configuration.file.YamlConfiguration;

import java.math.BigDecimal;
import java.util.List;

public class BankActionLoader extends SuperiorButtonLoader {

    public BankActionLoader(SuperiorSkyblockPlugin plugin) {
        super(plugin, "BANK_ACTION");
    }

    @Override
    public Class<? extends Button> getButton() {
        return BankActionButton.class;
    }

    @Override
    public Button load(YamlConfiguration configuration, String path, DefaultButtonValue defaultButtonValue) {
        BankActionButton.BankAction bankAction = BankActionButton.BankAction.valueOf(configuration.getString(path + "bank-action", BankActionButton.BankAction.DEPOSIT.name()));
        double value = configuration.getDouble(path + "percentage", 0);
        List<String> withdrawCommands = configuration.getStringList(path + "bank-action.withdraw");
        return new BankActionButton(plugin, new BigDecimal(value), bankAction, withdrawCommands);
    }
}
