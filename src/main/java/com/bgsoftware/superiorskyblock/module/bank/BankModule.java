package com.bgsoftware.superiorskyblock.module.bank;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.module.bank.commands.CmdAdminDeposit;
import com.bgsoftware.superiorskyblock.module.bank.commands.CmdAdminWithdraw;
import com.bgsoftware.superiorskyblock.module.bank.commands.CmdBalance;
import com.bgsoftware.superiorskyblock.module.bank.commands.CmdBank;
import com.bgsoftware.superiorskyblock.module.bank.commands.CmdDeposit;
import com.bgsoftware.superiorskyblock.module.bank.commands.CmdWithdraw;
import com.bgsoftware.superiorskyblock.module.BuiltinModule;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;

import java.io.File;

public class BankModule extends BuiltinModule {

    public double bankWorthRate = 1000;
    public double disbandRefund = 0;
    public boolean bankLogs = true;
    public boolean cacheAllLogs = false;
    public boolean bankInterestEnabled = true;
    public int bankInterestInterval = 86400;
    public int bankInterestPercentage = 10;
    public int bankInterestRecentActive = 86400;
    private boolean enabled = true;

    public BankModule() {
        super("bank");
    }

    @Override
    protected void onPluginInit(SuperiorSkyblockPlugin plugin) {
        super.onPluginInit(plugin);

        File configFile = new File(plugin.getDataFolder(), "config.yml");
        CommentedConfiguration config = CommentedConfiguration.loadConfiguration(configFile);

        boolean updatedConfig = false;

        if (syncValues("bank-worth-rate", config))
            updatedConfig = true;

        if (syncValues("disband-refund", config))
            updatedConfig = true;

        if (syncValues("bank-logs", config))
            updatedConfig = true;

        if (syncValues("cache-logs", config))
            updatedConfig = true;

        if (syncValues("bank-interest", config))
            updatedConfig = true;

        if (updatedConfig) {
            File moduleConfigFile = new File(getModuleFolder(), "config.yml");

            try {
                super.config.save(moduleConfigFile);
                config.save(configFile);
            } catch (Exception error) {
                Log.entering("BankModule", "onPluginInit", "ENTER");
                Log.error(error, "An error occurred while saving config file:");
            }
        }
    }

    @Override
    public void onEnable(SuperiorSkyblockPlugin plugin) {
        // Do nothing.
    }

    @Override
    public void onDisable(SuperiorSkyblockPlugin plugin) {
        // Do nothing.
    }

    @Override
    public void loadData(SuperiorSkyblockPlugin plugin) {
        // Do nothing.
    }

    @Override
    public Listener[] getModuleListeners(SuperiorSkyblockPlugin plugin) {
        return null;
    }

    @Override
    public SuperiorCommand[] getSuperiorCommands(SuperiorSkyblockPlugin plugin) {
        return !enabled ? null : new SuperiorCommand[]{new CmdBalance(), new CmdBank(), new CmdDeposit(), new CmdWithdraw()};
    }

    @Override
    public SuperiorCommand[] getSuperiorAdminCommands(SuperiorSkyblockPlugin plugin) {
        return !enabled ? null : new SuperiorCommand[]{new CmdAdminDeposit(), new CmdAdminWithdraw()};
    }

    @Override
    public boolean isEnabled() {
        return enabled && isInitialized();
    }

    @Override
    protected void updateConfig(SuperiorSkyblockPlugin plugin) {
        enabled = config.getBoolean("enabled");
        int bankWorthRate = config.getInt("bank-worth-rate", 1000);
        this.bankWorthRate = bankWorthRate == 0 ? 0D : 1D / bankWorthRate;
        disbandRefund = Math.max(0, Math.min(100, config.getDouble("disband-refund"))) / 100D;
        bankLogs = config.getBoolean("bank-logs", true);
        cacheAllLogs = config.getBoolean("cache-logs", true);
        bankInterestEnabled = config.getBoolean("bank-interest.enabled", true);
        bankInterestInterval = config.getInt("bank-interest.interval", 86400);
        bankInterestPercentage = config.getInt("bank-interest.percentage", 10);
        bankInterestRecentActive = config.getInt("bank-interest.recent-active", 86400);
    }

    private boolean syncValues(String section, YamlConfiguration config) {
        if (config.contains(section)) {
            super.config.set(section, config.get(section));
            config.set(section, null);
            return true;
        }

        return false;
    }

}
