package com.bgsoftware.superiorskyblock.core.serialization.impl;

import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.tag.Tag;
import com.bgsoftware.superiorskyblock.core.serialization.ISerializer;
import com.bgsoftware.superiorskyblock.core.serialization.Serializers;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.debug.PluginDebugger;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.math.BigInteger;

public class InventorySerializer implements ISerializer<ItemStack[], String> {

    private static final ItemStack[] EMPTY_CONTENTS = new ItemStack[0];

    private static final InventorySerializer INSTANCE = new InventorySerializer();

    public static InventorySerializer getInstance() {
        return INSTANCE;
    }

    private InventorySerializer() {
    }

    @NotNull
    @Override
    public String serialize(@Nullable ItemStack[] serializable) {
        if (serializable == null || serializable.length == 0)
            return "";

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutput = new DataOutputStream(outputStream);

        CompoundTag compoundTag = new CompoundTag();
        compoundTag.setInt("Length", serializable.length);

        for (int i = 0; i < serializable.length; i++) {
            if (serializable[i] != null && serializable[i].getType() != Material.AIR)
                compoundTag.setTag(i + "", Serializers.ITEM_STACK_TO_TAG_SERIALIZER.serialize(serializable[i]));
        }

        try {
            compoundTag.write(dataOutput);
        } catch (Exception ex) {
            ex.printStackTrace();
            PluginDebugger.debug(ex);
            return "";
        }

        return new BigInteger(1, outputStream.toByteArray()).toString(32);
    }

    @Override
    public ItemStack[] deserialize(@Nullable String deserializable) {
        if (Text.isBlank(deserializable))
            return EMPTY_CONTENTS;

        ByteArrayInputStream inputStream = new ByteArrayInputStream(new BigInteger(deserializable, 32).toByteArray());
        CompoundTag compoundTag;

        try {
            compoundTag = (CompoundTag) Tag.fromStream(new DataInputStream(inputStream), 0);
        } catch (Exception ex) {
            ex.printStackTrace();
            PluginDebugger.debug(ex);
            return EMPTY_CONTENTS;
        }

        ItemStack[] contents = new ItemStack[compoundTag.getInt("Length")];

        for (int i = 0; i < contents.length; i++) {
            CompoundTag itemCompound = compoundTag.getCompound(i + "");
            if (itemCompound != null)
                contents[i] = Serializers.ITEM_STACK_TO_TAG_SERIALIZER.deserialize(itemCompound);
        }

        return contents;
    }
}
