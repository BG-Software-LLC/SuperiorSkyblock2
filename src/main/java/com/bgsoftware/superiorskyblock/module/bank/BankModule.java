package com.bgsoftware.superiorskyblock.module.bank;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.module.BuiltinModule;
import com.bgsoftware.superiorskyblock.module.IModuleConfiguration;
import com.bgsoftware.superiorskyblock.module.bank.commands.CmdAdminDeposit;
import com.bgsoftware.superiorskyblock.module.bank.commands.CmdAdminWithdraw;
import com.bgsoftware.superiorskyblock.module.bank.commands.CmdBalance;
import com.bgsoftware.superiorskyblock.module.bank.commands.CmdBank;
import com.bgsoftware.superiorskyblock.module.bank.commands.CmdDeposit;
import com.bgsoftware.superiorskyblock.module.bank.commands.CmdWithdraw;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;

import java.io.File;
import java.math.BigDecimal;

public class BankModule extends BuiltinModule<BankModule.Configuration> {

    public BankModule() {
        super("bank");
    }

    @Override
    protected boolean onConfigCreate(SuperiorSkyblockPlugin plugin, CommentedConfiguration config, boolean firstTime) {
        File oldConfigFile = new File(plugin.getDataFolder(), "config.yml");
        if (!oldConfigFile.exists())
            return false;

        CommentedConfiguration oldConfig = CommentedConfiguration.loadConfiguration(oldConfigFile);
        boolean updatedConfig = false;

        if (syncValues("bank-worth-rate", config, oldConfig))
            updatedConfig = true;

        if (syncValues("disband-refund", config, oldConfig))
            updatedConfig = true;

        if (syncValues("bank-logs", config, oldConfig))
            updatedConfig = true;

        if (syncValues("cache-logs", config, oldConfig))
            updatedConfig = true;

        if (syncValues("bank-interest", config, oldConfig))
            updatedConfig = true;

        return updatedConfig;
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
        return new SuperiorCommand[]{new CmdBalance(), new CmdBank(), new CmdDeposit(), new CmdWithdraw()};
    }

    @Override
    public SuperiorCommand[] getSuperiorAdminCommands(SuperiorSkyblockPlugin plugin) {
        return new SuperiorCommand[]{new CmdAdminDeposit(), new CmdAdminWithdraw()};
    }

    @Override
    protected Configuration createConfigFile(CommentedConfiguration config) {
        return new Configuration(config);
    }

    private static boolean syncValues(String section, YamlConfiguration newConfig, YamlConfiguration oldConfig) {
        if (oldConfig.contains(section)) {
            newConfig.set(section, oldConfig.get(section));
            return true;
        }

        return false;
    }

    public static class Configuration implements IModuleConfiguration {

        private final boolean enabled;
        private final double bankWorthRate;
        private final boolean hasDisbandRefund;
        private final BigDecimal disbandRefund;
        private final boolean bankLogs;
        private final boolean cacheAllLogs;
        private final boolean bankInterestEnabled;
        private final int bankInterestInterval;
        private final int bankInterestPercentage;
        private final int bankInterestRecentActive;

        Configuration(CommentedConfiguration config) {
            this.enabled = config.getBoolean("enabled");
            int bankWorthRate = config.getInt("bank-worth-rate", 1000);
            this.bankWorthRate = bankWorthRate == 0 ? 0D : 1D / bankWorthRate;
            double disbandRefund = Math.max(0, Math.min(100, config.getDouble("disband-refund"))) / 100D;
            this.hasDisbandRefund = disbandRefund > 0;
            this.disbandRefund = BigDecimal.valueOf(disbandRefund);
            this.bankLogs = config.getBoolean("bank-logs", true);
            this.cacheAllLogs = config.getBoolean("cache-logs", true);
            this.bankInterestEnabled = config.getBoolean("bank-interest.enabled", true);
            this.bankInterestInterval = config.getInt("bank-interest.interval", 86400);
            this.bankInterestPercentage = config.getInt("bank-interest.percentage", 10);
            this.bankInterestRecentActive = config.getInt("bank-interest.recent-active", 86400);
        }

        @Override
        public boolean isEnabled() {
            return this.enabled;
        }

        public double getBankWorthRate() {
            return bankWorthRate;
        }

        public boolean hasDisbandRefund() {
            return hasDisbandRefund;
        }

        public BigDecimal getDisbandRefund() {
            return disbandRefund;
        }

        public boolean isBankLogs() {
            return bankLogs;
        }

        public boolean isCacheAllLogs() {
            return cacheAllLogs;
        }

        public boolean isBankInterestEnabled() {
            return bankInterestEnabled;
        }

        public int getBankInterestInterval() {
            return bankInterestInterval;
        }

        public int getBankInterestPercentage() {
            return bankInterestPercentage;
        }

        public int getBankInterestRecentActive() {
            return bankInterestRecentActive;
        }
    }

}
