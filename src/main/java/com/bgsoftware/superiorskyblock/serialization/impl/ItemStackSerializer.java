package com.bgsoftware.superiorskyblock.serialization.impl;

import com.bgsoftware.superiorskyblock.serialization.ISerializer;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.tag.Tag;
import com.bgsoftware.superiorskyblock.tag.TagUtils;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.debug.PluginDebugger;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.math.BigInteger;

public final class ItemStackSerializer implements ISerializer<ItemStack, String> {

    private static final ItemStackSerializer INSTANCE = new ItemStackSerializer();

    public static ItemStackSerializer getInstance() {
        return INSTANCE;
    }

    private ItemStackSerializer() {
    }

    @NotNull
    @Override
    public String serialize(@Nullable ItemStack serializable) {
        if (serializable == null)
            return "";

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutput = new DataOutputStream(outputStream);

        try {
            TagUtils.itemToCompound(serializable).write(dataOutput);
        } catch (Exception ex) {
            ex.printStackTrace();
            PluginDebugger.debug(ex);
            return "";
        }

        return new BigInteger(1, outputStream.toByteArray()).toString(32);
    }

    @Nullable
    @Override
    public ItemStack deserialize(@Nullable String deserializable) {
        if (StringUtils.isBlank(deserializable))
            return null;

        ByteArrayInputStream inputStream = new ByteArrayInputStream(new BigInteger(deserializable, 32).toByteArray());

        try {
            CompoundTag compoundTag = (CompoundTag) Tag.fromStream(new DataInputStream(inputStream), 0);
            return TagUtils.compoundToItem(compoundTag);
        } catch (Exception ex) {
            ex.printStackTrace();
            PluginDebugger.debug(ex);
            return null;
        }
    }
}
