package com.bgsoftware.superiorskyblock.core.serialization.impl;

import com.bgsoftware.superiorskyblock.core.serialization.ISerializer;
import com.bgsoftware.superiorskyblock.core.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class StackedBlockSerializer implements ISerializer<Location, String> {

    private static final StackedBlockSerializer INSTANCE = new StackedBlockSerializer();

    public static StackedBlockSerializer getInstance() {
        return INSTANCE;
    }

    private StackedBlockSerializer() {
    }

    @NotNull
    @Override
    public String serialize(@Nullable Location serializable) {
        return serializable == null ? "" :
                serializable.getWorld().getName() + ", " +
                        serializable.getBlockX() + ", " +
                        serializable.getBlockY() + ", " +
                        serializable.getBlockZ();
    }

    @Nullable
    @Override
    public Location deserialize(@Nullable String element) {
        if (Text.isBlank(element))
            return null;

        // Due to issues with commit #feacca9ec, we must parse the values as decimals

        String[] positionSections = element.split(", ");

        return positionSections.length < 4 ? null : new Location(Bukkit.getWorld(positionSections[0]),
                Double.parseDouble(positionSections[1]),
                Double.parseDouble(positionSections[2]),
                Double.parseDouble(positionSections[3]));
    }

}
