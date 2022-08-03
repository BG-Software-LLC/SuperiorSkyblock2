package com.bgsoftware.superiorskyblock.nms.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Remapped {

    private final EnumMap<Remap.Type, Map<String, List<String>>> mappings = new EnumMap<>(Remap.Type.class);

    public List<String> getObfuscatedNames(String name, Remap.Type type) {
        Map<String, List<String>> typeMappings = mappings.get(type);

        if (typeMappings != null) {
            List<String> obfuscatedNames = typeMappings.get(name);
            if (obfuscatedNames != null)
                return obfuscatedNames;
        }

        return Collections.emptyList();
    }

    public void put(Remap.Type type, String name, String obfuscated) {
        mappings.computeIfAbsent(type, t -> new HashMap<>())
                .computeIfAbsent(name, n -> new ArrayList<>())
                .add(obfuscated);
    }

}
