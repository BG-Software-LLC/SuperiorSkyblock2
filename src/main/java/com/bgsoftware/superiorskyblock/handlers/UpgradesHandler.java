package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;

import com.bgsoftware.superiorskyblock.api.handlers.UpgradesManager;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.upgrades.DefaultUpgrade;
import com.bgsoftware.superiorskyblock.upgrades.SUpgrade;
import com.bgsoftware.superiorskyblock.upgrades.SUpgradeLevel;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.key.KeyMap;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class UpgradesHandler extends AbstractHandler implements UpgradesManager {

    private final Registry<String, SUpgrade> upgrades = Registry.createRegistry();

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
                double price = levelSection.getDouble("price");
                List<String> commands = levelSection.getStringList("commands");
                String permission = levelSection.getString("permission", "");
                Set<Pair<String, String>> requirements = new HashSet<>();
                for(String line : levelSection.getStringList("required-checks")){
                    String[] sections = line.split(";");
                    requirements.add(new Pair<>(sections[0], StringUtils.translateColors(sections[1])));
                }
                double cropGrowth = levelSection.getDouble("crop-growth", -1D);
                double spawnerRates = levelSection.getDouble("spawner-rates", -1D);
                double mobDrops = levelSection.getDouble("mob-drops", -1D);
                int teamLimit = levelSection.getInt("team-limit", -1);
                int warpsLimit = levelSection.getInt("warps-limit", -1);
                int coopLimit = levelSection.getInt("coop-limit", -1);
                int borderSize = levelSection.getInt("border-size", -1);
                KeyMap<Integer> blockLimits = new KeyMap<>();
                if(levelSection.contains("block-limits")){
                    for(String block : levelSection.getConfigurationSection("block-limits").getKeys(false))
                        blockLimits.put(block, levelSection.getInt("block-limits." + block));
                }
                Map<EntityType, Integer> entityLimits = new HashMap<>();
                if(levelSection.contains("entity-limits")){
                    for(String entity : levelSection.getConfigurationSection("entity-limits").getKeys(false)) {
                        try {
                            entityLimits.put(EntityType.valueOf(entity.toUpperCase()), levelSection.getInt("entity-limits." + entity));
                        }catch(IllegalArgumentException ignored){}
                    }
                }
                KeyMap<Integer> generatorRates = new KeyMap<>();
                if(levelSection.contains("generator-rates")){
                    for(String block : levelSection.getConfigurationSection("generator-rates").getKeys(false))
                        generatorRates.put(block, levelSection.getInt("generator-rates." + block));
                }
                Map<PotionEffectType, Integer> islandEffects = new HashMap<>();
                if(levelSection.contains("island-effects")){
                    for(String effect : levelSection.getConfigurationSection("island-effects").getKeys(false)) {
                        PotionEffectType potionEffectType = PotionEffectType.getByName(effect);
                        if(potionEffectType != null)
                            islandEffects.put(potionEffectType, levelSection.getInt("island-effects." + effect) - 1);
                    }
                }
                upgrade.addUpgradeLevel(level, new SUpgradeLevel(level, price, commands, permission, requirements,
                        cropGrowth, spawnerRates, mobDrops, teamLimit, warpsLimit, coopLimit, borderSize, blockLimits,
                        entityLimits, generatorRates, islandEffects));
            }
            this.upgrades.add(upgradeName, upgrade);
        }
    }

    @Override
    public SUpgrade getUpgrade(String upgradeName){
        return upgrades.get(upgradeName);
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
        return upgrades.containsKey(upgradeName.toLowerCase());
    }

    @Override
    public Collection<Upgrade> getUpgrades() {
        return Collections.unmodifiableCollection(upgrades.values());
    }

}
