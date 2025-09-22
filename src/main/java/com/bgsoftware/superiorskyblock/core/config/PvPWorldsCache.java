package com.bgsoftware.superiorskyblock.core.config;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class PvPWorldsCache {

    private final Set<String> pvpWorldNames = new HashSet<>();
    private final List<Pattern> pvpWorldPatterns = new LinkedList<>();

    public PvPWorldsCache(List<String> pvpWorlds) {
        for (String worldName : pvpWorlds) {
            Pattern worldNamePattern = getPatternRegex(worldName);
            if (worldNamePattern == null) {
                this.pvpWorldNames.add(worldName);
            } else {
                this.pvpWorldPatterns.add(worldNamePattern);
            }
        }
    }

    public boolean isPvPWorld(String worldName) {
        return isPvPWorldByName(worldName) || isPvPWorldByPattern(worldName);
    }

    private boolean isPvPWorldByName(String worldName) {
        return !this.pvpWorldNames.isEmpty() && this.pvpWorldNames.contains(worldName);
    }

    private boolean isPvPWorldByPattern(String worldName) {
        for (Pattern pattern : this.pvpWorldPatterns) {
            if (pattern.matcher(worldName).matches())
                return true;
        }

        return false;
    }

    private static Pattern getPatternRegex(String value) {
        try {
            Pattern pattern = Pattern.compile(value);
            if (value.matches(".*[.*+?^${}()|\\[\\]\\\\].*"))
                return pattern;
        } catch (PatternSyntaxException ignored) {
        }
        return null;
    }

}
