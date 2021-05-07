package com.bgsoftware.superiorskyblock.modules.bank;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.modules.BuiltinModule;
import com.bgsoftware.superiorskyblock.modules.bank.commands.CmdAdminDeposit;
import com.bgsoftware.superiorskyblock.modules.bank.commands.CmdAdminWithdraw;
import com.bgsoftware.superiorskyblock.modules.bank.commands.CmdBalance;
import com.bgsoftware.superiorskyblock.modules.bank.commands.CmdBank;
import com.bgsoftware.superiorskyblock.modules.bank.commands.CmdDeposit;
import com.bgsoftware.superiorskyblock.modules.bank.commands.CmdWithdraw;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public final class BankModule extends BuiltinModule {

    private boolean enabled = true;

    public double bankWorthRate = 1000;
    public double disbandRefund = 0;
    public boolean bankLogs = true;
    public boolean bankInterestEnabled = true;
    public int bankInterestInterval = 86400;
    public int bankInterestPercentage = 10;
    public int bankInterestRecentActive = 86400;

    public BankModule(){
        super("bank");
    }

    @Override
    public void onEnable(SuperiorSkyblockPlugin plugin) {
        if(!enabled)
            return;

        plugin.getCommands().registerAdminCommand(new CmdAdminDeposit());
        plugin.getCommands().registerAdminCommand(new CmdAdminWithdraw());
        plugin.getCommands().registerCommand(new CmdBalance());
        plugin.getCommands().registerCommand(new CmdBank());
        plugin.getCommands().registerCommand(new CmdDeposit());
        plugin.getCommands().registerCommand(new CmdWithdraw());
    }

    @Override
    public void onDisable() {

    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    protected void updateConfig() {
        enabled = config.getBoolean("enabled");
        int bankWorthRate = config.getInt("bank-worth-rate", 1000);
        this.bankWorthRate = bankWorthRate == 0 ? 0D : 1D / bankWorthRate;
        disbandRefund = Math.max(0, Math.min(100, config.getDouble("disband-refund"))) / 100D;
        bankLogs = config.getBoolean("bank-logs", true);
        bankInterestEnabled = config.getBoolean("bank-interest.enabled", true);
        bankInterestInterval = config.getInt("bank-interest.interval", 86400);
        bankInterestPercentage = config.getInt("bank-interest.percentage", 10);
        bankInterestRecentActive = config.getInt("bank-interest.recent-active", 86400);
    }

    @Override
    protected void onPluginInit() {
        super.onPluginInit();

        File configFile = new File(plugin.getDataFolder(), "config.yml");
        CommentedConfiguration config = CommentedConfiguration.loadConfiguration(configFile);

        boolean updatedConfig = false;

        if(syncValues("bank-worth-rate", config))
            updatedConfig = true;

        if(syncValues("disband-refund", config))
            updatedConfig = true;

        if(syncValues("bank-logs", config))
            updatedConfig = true;

        if(syncValues("bank-interest", config))
            updatedConfig = true;

        if(updatedConfig) {
            File moduleConfigFile = new File(getDataFolder(), "config.yml");

            try {
                super.config.save(moduleConfigFile);
                config.save(configFile);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private boolean syncValues(String section, YamlConfiguration config){
        if(config.contains(section)){
            super.config.set(section, config.get(section));
            config.set(section, null);
            return true;
        }

        return false;
    }

}
