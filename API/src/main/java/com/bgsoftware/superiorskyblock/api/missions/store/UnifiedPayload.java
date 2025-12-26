package com.bgsoftware.superiorskyblock.api.missions.store;

import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public final class UnifiedPayload {
    public int schema;
    public String mission;
    public String scope;

    public Map<String, HolderData> holders = new LinkedHashMap<>();
    public Map<String, Object> globals = new LinkedHashMap<>();

    public static final class HolderData {
        public Map<String, Integer> counts = new LinkedHashMap<>();
        public Map<String, Boolean> flags = new LinkedHashMap<>();
        public List<String> items = new ArrayList<>();
        public Map<String, Object> extras = new LinkedHashMap<>();
    }
}
