package com.bgsoftware.superiorskyblock.missions.common;

import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.missions.common.requirements.KeyRequirements;
import com.bgsoftware.superiorskyblock.missions.common.requirements.Requirements;
import com.bgsoftware.superiorskyblock.missions.common.tracker.DataTracker;
import org.bukkit.ChatColor;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Placeholders {

    private static final Pattern PERCENTAGE_PATTERN = Pattern.compile("\\{percentage_(.+?)}");
    private static final Pattern VALUES_PATTERN = Pattern.compile("\\{value_(.+?)}");

    private static final Function<String, Key> MATERIAL_KEY_CREATION = Key::ofMaterialAndData;
    private static final Function<String, Key> ENTITY_KEY_CREATION = Key::ofEntityType;

    private Placeholders() {

    }

    public static String parseKeyPlaceholders(Map<KeyRequirements, Integer> requirements,
                                              DataTracker<Key, KeyRequirements> dataTracker,
                                              String line, boolean isMaterial) {
        Function<String, Key> creationMethod;

        if (isMaterial) {
            creationMethod = MATERIAL_KEY_CREATION;
        } else {
            creationMethod = ENTITY_KEY_CREATION;
        }

        return parsePlaceholders(line, new PlaceholdersFunctions<KeyRequirements>() {
            @Override
            public KeyRequirements getRequirementFromKey(String key) {
                Key requirementKey = creationMethod.apply(key);

                for (KeyRequirements requirements : requirements.keySet()) {
                    if (requirements.contains(requirementKey)) {
                        return requirements;
                    }
                }

                return null;
            }

            @Override
            public Optional<Integer> lookupRequirement(KeyRequirements requirementsKey) {
                return Optional.ofNullable(requirements.get(requirementsKey));
            }

            @Override
            public int getCountForRequirement(KeyRequirements requirementsKey) {
                int totalCount = 0;

                for (Key requirementKey : requirementsKey) {
                    totalCount += dataTracker.getCount(requirementKey);
                }

                return totalCount;
            }
        });
    }

    public static String parsePlaceholders(Map<Requirements, Integer> requirements, DataTracker<String, Requirements> dataTracker, String line) {
        return parsePlaceholders(line, new PlaceholdersFunctions<Requirements>() {
            @Override
            public Requirements getRequirementFromKey(String key) {
                for (Requirements requirements : requirements.keySet()) {
                    if (requirements.contains(key))
                        return requirements;
                }

                return null;
            }

            @Override
            public Optional<Integer> lookupRequirement(Requirements requirementsKey) {
                return Optional.ofNullable(requirements.get(requirementsKey));
            }

            @Override
            public int getCountForRequirement(Requirements requirementsKey) {
                int totalCount = 0;

                for (String requirementKey : requirementsKey) {
                    totalCount += dataTracker.getCount(requirementKey);
                }

                return totalCount;
            }
        });
    }

    public static <E> String parsePlaceholders(String line, PlaceholdersFunctions<E> functions) {
        Matcher matcher = PERCENTAGE_PATTERN.matcher(line);
        if (matcher.find()) {
            String requirementKey = matcher.group(1).toUpperCase(Locale.ENGLISH);
            E requirement = functions.getRequirementFromKey(requirementKey);
            Optional<Integer> entry = requirement == null ? Optional.empty() : functions.lookupRequirement(requirement);

            if (entry.isPresent()) {
                int count = Math.min(functions.getCountForRequirement(requirement), entry.get());
                line = matcher.replaceAll("" + (count * 100) / entry.get());
            }
        }

        if ((matcher = VALUES_PATTERN.matcher(line)).find()) {
            String requirementKey = matcher.group(1).toUpperCase(Locale.ENGLISH);
            E requirement = functions.getRequirementFromKey(requirementKey);
            Optional<Integer> entry = requirement == null ? Optional.empty() : functions.lookupRequirement(requirement);

            if (entry.isPresent()) {
                int count = Math.min(functions.getCountForRequirement(requirement), entry.get());
                line = matcher.replaceAll(String.valueOf(count));
            }
        }

        return ChatColor.translateAlternateColorCodes('&', line);
    }

    public static abstract class PlaceholdersFunctions<E> {

        @Nullable
        public abstract E getRequirementFromKey(String key);

        public abstract Optional<Integer> lookupRequirement(E requirement);

        public abstract int getCountForRequirement(E requirement);

    }

}
