package com.bgsoftware.superiorskyblock.mission.store;

import com.bgsoftware.superiorskyblock.api.missions.store.MissionStore;
import com.bgsoftware.superiorskyblock.api.missions.store.UnifiedPayload;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public final class YamlMissionStore implements MissionStore {

    @Override
    public UnifiedPayload read(File file) {
        if (file == null || !file.exists()) return null;

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        if (!yaml.isInt("schema")) return null;
        if (yaml.getInt("schema", -1) < 2) return null;

        UnifiedPayload payload = new UnifiedPayload();
        payload.schema = yaml.getInt("schema", 2);
        payload.mission = yaml.getString("mission", null);
        payload.scope = yaml.getString("scope", null);

        ConfigurationSection globalsSec = yaml.getConfigurationSection("globals");
        if (globalsSec != null) {
            for (String k : globalsSec.getKeys(false)) {
                payload.globals.put(k, globalsSec.get(k));
            }
        }

        ConfigurationSection holdersSec = yaml.getConfigurationSection("holders");
        if (holdersSec != null) {
            for (String holderKey : holdersSec.getKeys(false)) {
                ConfigurationSection hdSec = holdersSec.getConfigurationSection(holderKey);
                if (hdSec == null) continue;

                UnifiedPayload.HolderData hd = new UnifiedPayload.HolderData();

                ConfigurationSection countsSec = hdSec.getConfigurationSection("counts");
                if (countsSec != null) {
                    for (String k : countsSec.getKeys(false)) {
                        hd.counts.put(k, countsSec.getInt(k, 0));
                    }
                }

                ConfigurationSection flagsSec = hdSec.getConfigurationSection("flags");
                if (flagsSec != null) {
                    for (String k : flagsSec.getKeys(false)) {
                        hd.flags.put(k, flagsSec.getBoolean(k, false));
                    }
                }

                if (hdSec.isList("items")) {
                    for (Object o : hdSec.getList("items")) {
                        if (o != null) hd.items.add(String.valueOf(o));
                    }
                }

                ConfigurationSection extrasSec = hdSec.getConfigurationSection("extras");
                if (extrasSec != null) {
                    for (String k : extrasSec.getKeys(false)) {
                        hd.extras.put(k, extrasSec.get(k));
                    }
                }

                payload.holders.put(holderKey, hd);
            }
        }

        return payload;
    }

    @Override
    public void write(File file, UnifiedPayload payload) {
        if (file == null || payload == null) return;

        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("schema", payload.schema);
        yaml.set("mission", payload.mission);
        yaml.set("scope", payload.scope);

        if (payload.globals != null && !payload.globals.isEmpty()) {
            for (Map.Entry<String, Object> g : payload.globals.entrySet()) {
                yaml.set("globals." + g.getKey(), g.getValue());
            }
        }

        for (Map.Entry<String, UnifiedPayload.HolderData> e : payload.holders.entrySet()) {
            String base = "holders." + e.getKey();
            UnifiedPayload.HolderData hd = e.getValue();

            if (!hd.counts.isEmpty()) {
                for (Map.Entry<String, Integer> c : hd.counts.entrySet()) {
                    yaml.set(base + ".counts." + c.getKey(), c.getValue());
                }
            }

            if (!hd.flags.isEmpty()) {
                for (Map.Entry<String, Boolean> f : hd.flags.entrySet()) {
                    yaml.set(base + ".flags." + f.getKey(), f.getValue());
                }
            }

            if (!hd.items.isEmpty()) {
                yaml.set(base + ".items", hd.items);
            }

            if (!hd.extras.isEmpty()) {
                for (Map.Entry<String, Object> x : hd.extras.entrySet()) {
                    yaml.set(base + ".extras." + x.getKey(), x.getValue());
                }
            }
        }

        try {
            if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
            if (!file.exists()) file.createNewFile();
            yaml.save(file);
        } catch (IOException ignored) {
        }
    }

}
