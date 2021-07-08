package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;

import com.bgsoftware.superiorskyblock.api.handlers.UpgradesManager;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCost;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCostLoadException;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCostLoader;
import com.bgsoftware.superiorskyblock.upgrades.DefaultUpgrade;
import com.bgsoftware.superiorskyblock.upgrades.SUpgrade;
import com.bgsoftware.superiorskyblock.upgrades.SUpgradeLevel;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.key.KeyMap;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.bgsoftware.superiorskyblock.utils.upgrades.UpgradeValue;
import com.google.common.base.Preconditions;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nullable;
import java.io.File;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class UpgradesHandler extends AbstractHandler implements UpgradesManager {

    private final Registry<String, SUpgrade> upgrades = Registry.createRegistry();
    private final Registry<String, UpgradeCostLoader> upgradeCostLoaders = Registry.createRegistry();

    public UpgradesHandler(SuperiorSkyblockPlugin plugin){
        super(plugin);
    }

    @Override
    public void loadData(){
        File file = new File(plugin.getDataFolder(), "upgrades.yml");

        if(!file.exists())
            plugin.saveResource("upgrades.yml", false);

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection upgrades = cfg.getConfigurationSection("upgrades");

        for(String upgradeName : upgrades.getKeys(false)){
            SUpgrade upgrade = new SUpgrade(upgradeName);
            for(String _level : upgrades.getConfigurationSection(upgradeName).getKeys(false)){
                ConfigurationSection levelSection = upgrades.getConfigurationSection(upgradeName + "." + _level);
                int level = Integer.parseInt(_level);

                String priceType = levelSection.getString("price-type", "money");
                UpgradeCostLoader costLoader = getUpgradeCostLoader(priceType);

                if(costLoader == null){
                    SuperiorSkyblockPlugin.log("&cUpgrade by name " + upgrade.getName() + " (level " + level + ") has invalid price-type. Skipping...");
                    continue;
                }

                UpgradeCost upgradeCost;

                try{
                    upgradeCost = costLoader.loadCost(levelSection);
                }catch (UpgradeCostLoadException ex){
                    SuperiorSkyblockPlugin.log("&cUpgrade by name " + upgrade.getName() + " (level " + level + ") failed to initialize because: " + ex.getMessage() + ". Skipping...");
                    continue;
                }

                List<String> commands = levelSection.getStringList("commands");
                String permission = levelSection.getString("permission", "");
                Set<Pair<String, String>> requirements = new HashSet<>();
                for(String line : levelSection.getStringList("required-checks")){
                    String[] sections = line.split(";");
                    requirements.add(new Pair<>(sections[0], StringUtils.translateColors(sections[1])));
                }
                UpgradeValue<Double> cropGrowth = new UpgradeValue<>(levelSection.getDouble("crop-growth", -1D), true);
                UpgradeValue<Double> spawnerRates = new UpgradeValue<>(levelSection.getDouble("spawner-rates", -1D), true);
                UpgradeValue<Double> mobDrops = new UpgradeValue<>(levelSection.getDouble("mob-drops", -1D), true);
                UpgradeValue<Integer> teamLimit = new UpgradeValue<>(levelSection.getInt("team-limit", -1), true);
                UpgradeValue<Integer> warpsLimit = new UpgradeValue<>(levelSection.getInt("warps-limit", -1), true);
                UpgradeValue<Integer> coopLimit = new UpgradeValue<>(levelSection.getInt("coop-limit", -1), true);
                UpgradeValue<Integer> borderSize = new UpgradeValue<>(levelSection.getInt("border-size", -1), true);
                UpgradeValue<BigDecimal> bankLimit = new UpgradeValue<>(new BigDecimal(levelSection.getString("bank-limit", "-1")), true);
                KeyMap<UpgradeValue<Integer>> blockLimits = new KeyMap<>();
                if(levelSection.contains("block-limits")){
                    for(String block : levelSection.getConfigurationSection("block-limits").getKeys(false))
                        blockLimits.put(block, new UpgradeValue<>(levelSection.getInt("block-limits." + block), true));
                }
                KeyMap<UpgradeValue<Integer>> entityLimits = new KeyMap<>();
                if(levelSection.contains("entity-limits")){
                    for(String entity : levelSection.getConfigurationSection("entity-limits").getKeys(false))
                        entityLimits.put(entity.toUpperCase(), new UpgradeValue<>(levelSection.getInt("entity-limits." + entity), true));
                }
                KeyMap<UpgradeValue<Integer>>[] generatorRates = new KeyMap[3];
                if(levelSection.contains("generator-rates")){
                    for(String blockOrEnv : levelSection.getConfigurationSection("generator-rates").getKeys(false)) {
                        try{
                            int index = World.Environment.valueOf(blockOrEnv.toUpperCase()).ordinal();
                            for(String block : levelSection.getConfigurationSection("generator-rates." + blockOrEnv).getKeys(false)) {
                                if(generatorRates[index] == null)
                                    generatorRates[index] = new KeyMap<>();
                                generatorRates[index].put(block, new UpgradeValue<>(levelSection.getInt("generator-rates." + blockOrEnv + "." + block), true));
                            }
                        }catch (Exception ex) {
                            if(generatorRates[0] == null)
                                generatorRates[0] = new KeyMap<>();
                            generatorRates[0].put(blockOrEnv, new UpgradeValue<>(levelSection.getInt("generator-rates." + blockOrEnv), true));
                        }
                    }
                }
                Map<PotionEffectType, UpgradeValue<Integer>> islandEffects = new HashMap<>();
                if(levelSection.contains("island-effects")){
                    for(String effect : levelSection.getConfigurationSection("island-effects").getKeys(false)) {
                        PotionEffectType potionEffectType = PotionEffectType.getByName(effect);
                        if(potionEffectType != null)
                            islandEffects.put(potionEffectType, new UpgradeValue<>(levelSection.getInt("island-effects." + effect) - 1, true));
                    }
                }
                Map<Integer, UpgradeValue<Integer>> rolesLimits = new HashMap<>();
                if(levelSection.contains("role-limits")){
                    for(String roleId : levelSection.getConfigurationSection("role-limits").getKeys(false)) {
                        try {
                            rolesLimits.put(Integer.parseInt(roleId), new UpgradeValue<>(levelSection.getInt("role-limits." + roleId), true));
                        }catch (NumberFormatException ignored){}
                    }
                }
                upgrade.addUpgradeLevel(level, new SUpgradeLevel(level, upgradeCost, commands, permission, requirements,
                        cropGrowth, spawnerRates, mobDrops, teamLimit, warpsLimit, coopLimit, borderSize, blockLimits,
                        entityLimits, generatorRates, islandEffects, bankLimit, rolesLimits));
            }
            this.upgrades.add(upgradeName, upgrade);
        }
    }

    @Override
    public SUpgrade getUpgrade(String upgradeName){
        Preconditions.checkNotNull(upgradeName, "upgradeName parameter cannot be null.");
        return upgrades.get(upgradeName.toLowerCase());
    }

    @Override
    public SUpgrade getUpgrade(int slot){
        return upgrades.values().stream().filter(upgrade -> upgrade.getMenuSlot() == slot).findFirst().orElse(null);
    }

    @Override
    public Upgrade getDefaultUpgrade() {
        return DefaultUpgrade.getInstance();
    }

    @Override
    public boolean isUpgrade(String upgradeName){
        Preconditions.checkNotNull(upgradeName, "upgradeName parameter cannot be null.");
        return upgrades.containsKey(upgradeName.toLowerCase());
    }

    @Override
    public Collection<Upgrade> getUpgrades() {
        return Collections.unmodifiableCollection(upgrades.values());
    }

    @Override
    public void registerUpgradeCostLoader(String id, UpgradeCostLoader costLoader) {
        id = id.toLowerCase();
        Preconditions.checkArgument(!upgradeCostLoaders.containsKey(id), "A loader with the id " + id + " already exists.");
        upgradeCostLoaders.add(id, costLoader);
    }

    @Override
    public Collection<UpgradeCostLoader> getUpgradesCostLoaders() {
        return Collections.unmodifiableCollection(upgradeCostLoaders.values());
    }

    @Nullable
    @Override
    public UpgradeCostLoader getUpgradeCostLoader(String id) {
        Preconditions.checkNotNull(id, "id parameter cannot be null.");
        return upgradeCostLoaders.get(id.toLowerCase());
    }

}
