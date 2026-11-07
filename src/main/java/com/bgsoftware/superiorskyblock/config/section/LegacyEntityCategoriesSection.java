package com.bgsoftware.superiorskyblock.config.section;

import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.api.entity.EntityCategory;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyCategory;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import com.bgsoftware.superiorskyblock.core.key.category.FixedKeyCategory;

import java.util.LinkedList;
import java.util.List;

@Deprecated
public class LegacyEntityCategoriesSection implements SettingsManager.EntityCategories {

    private final SettingsManager.CategoriesMapper mapper;

    public LegacyEntityCategoriesSection(SettingsManager.CategoriesMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<EntityCategory> getCategories() {
        List<EntityCategory> categories = new LinkedList<>();

        mapper.getCategories().forEach(category -> {
            if (category instanceof FixedKeyCategory) {
                categories.add(new LegacyEntityCategory((FixedKeyCategory) category));
            }
        });

        return categories;
    }

    @Override
    public List<EntityCategory> getCategories(Key key) {
        List<EntityCategory> categories = new LinkedList<>();

        mapper.getCategories(key).forEach(category -> {
            if (category instanceof FixedKeyCategory) {
                categories.add(new LegacyEntityCategory((FixedKeyCategory) category));
            }
        });

        return categories;
    }

    @Override
    public EntityCategory getCategoryByName(String name) {
        KeyCategory category = this.mapper.getCategoryByName(name);
        return !(category instanceof FixedKeyCategory) ? null : new LegacyEntityCategory((FixedKeyCategory) category);
    }

    private static class LegacyEntityCategory implements EntityCategory {

        private final FixedKeyCategory category;

        LegacyEntityCategory(FixedKeyCategory category) {
            this.category = category;
        }

        @Override
        public KeySet getEntities() {
            return this.category.getKeys();
        }

        @Override
        public IslandPrivilege getSpawnPrivilege() {
            return getRequiredPrivilege(Action.SPAWN);
        }

        @Override
        public IslandPrivilege getDamagePrivilege() {
            return getRequiredPrivilege(Action.DAMAGE);
        }

        @Override
        public IslandPrivilege getInteractPrivilege() {
            return getRequiredPrivilege(Action.INTERACT);
        }

        @Override
        public IslandFlag getSpawnerSpawningIslandFlag() {
            return getRequiredFlag(Action.SPAWNER_SPAWN);
        }

        @Override
        public IslandFlag getNaturalSpawningIslandFlag() {
            return getRequiredFlag(Action.NATURAL_SPAWN);
        }

        @Override
        public String getName() {
            return this.category.getName();
        }

        @Override
        public boolean isListed(Key key) {
            return this.category.isListed(key);
        }

        @Override
        public IslandPrivilege getRequiredPrivilege(Action action) {
            return this.category.getRequiredPrivilege(action);
        }

        @Override
        public IslandFlag getRequiredFlag(Action action) {
            return this.category.getRequiredFlag(action);
        }
    }

}
