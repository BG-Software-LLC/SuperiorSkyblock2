/*
 * JNBT License

Copyright (c) 2010 Graham Edgecombe
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.
      
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
      
    * Neither the name of the JNBT team nor the names of its
      contributors may be used to endorse or promote products derived from
      this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE. 
 */
package com.bgsoftware.superiorskyblock.tag;

import com.bgsoftware.common.annotations.NotNull;
import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.google.common.base.Preconditions;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * The <code>TAG_Compound</code> tag.
 *
 * @author Graham Edgecombe
 */
public class CompoundTag extends Tag<Map<String, Tag<?>>> implements Iterable<Tag<?>> {

    /*package*/ static final Class<?> CLASS = getNNTClass("NBTTagCompound");

    public CompoundTag() {
        this(new HashMap<>());
    }

    public CompoundTag(CompoundTag other) {
        super(new HashMap<>(other.value), CLASS);
    }


    /**
     * Creates the tag.
     *
     * @param value The value.
     */
    public CompoundTag(Map<String, Tag<?>> value) {
        super(value, CLASS);
    }

    public Tag<?> getTag(String key) {
        return getTag(key, null);
    }

    public Tag<?> getTag(String key, Tag<?> def) {
        return this.value.getOrDefault(key, def);
    }

    public byte[] getByteArray(String key) {
        Tag<?> tag = getTag(key);
        return tag instanceof ByteArrayTag ? (byte[]) tag.value : null;
    }

    public byte getByte(String key) {
        Number number = getNumber(key);
        return number == null ? 0 : number.byteValue();
    }

    public CompoundTag getCompound(String key) {
        return getCompound(key, null);
    }

    public CompoundTag getCompound(String key, CompoundTag def) {
        Tag<?> tag = getTag(key);
        return tag instanceof CompoundTag ? (CompoundTag) tag : def;
    }

    public double getDouble(String key) {
        Number number = getNumber(key);
        return number == null ? 0D : number.doubleValue();
    }

    public float getFloat(String key) {
        Number number = getNumber(key);
        return number == null ? 0F : number.floatValue();
    }

    public int[] getIntArray(String key) {
        Tag<?> tag = getTag(key);
        return tag instanceof IntArrayTag ? (int[]) tag.value : null;
    }

    public int getInt(String key) {
        return getInt(key, 0);
    }

    public int getInt(String key, int def) {
        return getNumber(key, def).intValue();
    }

    public ListTag getList(String key) {
        Tag<?> tag = getTag(key);
        return tag instanceof ListTag ? (ListTag) tag : null;
    }

    public long getLong(String key) {
        Number number = getNumber(key);
        return number == null ? 0L : number.longValue();
    }

    public Number getNumber(String key) {
        return getNumber(key, null);
    }

    public Number getNumber(String key, Number def) {
        Tag<?> tag = getTag(key);
        return tag instanceof NumberTag ? (Number) tag.value : def;
    }

    public short getShort(String key) {
        Number number = getNumber(key);
        return number == null ? 0 : number.shortValue();
    }

    public String getString(String key) {
        Tag<?> tag = getTag(key);
        return tag instanceof StringTag ? (String) tag.value : null;
    }

    @Nullable
    public Tag<?> setTag(String key, Tag<?> value) {
        return this.value.put(key, value);
    }

    public void setByteArray(String key, byte[] value) {
        setTag(key, new ByteArrayTag(value));
    }

    public void setByte(String key, byte value) {
        setTag(key, new ByteTag(value));
    }

    public void setDouble(String key, double value) {
        setTag(key, new DoubleTag(value));
    }

    public void setFloat(String key, float value) {
        setTag(key, new FloatTag(value));
    }

    public void setIntArray(String key, int[] value) {
        setTag(key, new IntArrayTag(value));
    }

    public void setInt(String key, int value) {
        setTag(key, new IntTag(value));
    }

    public void setLong(String key, long value) {
        setTag(key, new LongTag(value));
    }

    public void setShort(String key, short value) {
        setTag(key, new ShortTag(value));
    }

    public void setString(String key, String value) {
        setTag(key, new StringTag(value));
    }

    public void putAll(CompoundTag other) {
        this.value.putAll(other.value);
    }

    public boolean containsKey(String key) {
        return this.value.containsKey(key);
    }

    public Tag<?> remove(String key) {
        return this.value.remove(key);
    }

    public int size() {
        return value.size();
    }

    public boolean isEmpty() {
        return value.isEmpty();
    }

    public Set<Map.Entry<String, Tag<?>>> entrySet() {
        return value.entrySet();
    }

    @NotNull
    @Override
    public Iterator<Tag<?>> iterator() {
        return value.values().iterator();
    }

    @Override
    public String toString() {
        StringBuilder bldr = new StringBuilder();
        bldr.append("TAG_Compound: ").append(value.size()).append(" entries\r\n{\r\n");
        for (Map.Entry<String, Tag<?>> entry : value.entrySet()) {
            bldr.append("   ").append((entry.getValue() + "").replaceAll("\r\n", "\r\n   ")).append("\r\n");
        }
        bldr.append("}");
        return bldr.toString();
    }

    @Override
    protected void writeData(DataOutputStream os) throws IOException {
        for (Map.Entry<String, Tag<?>> entry : value.entrySet()) {
            entry.getValue().write(os);

            byte[] keyBytes = entry.getKey().getBytes(StandardCharsets.UTF_8);
            os.writeShort(keyBytes.length);
            os.write(keyBytes);
        }

        os.writeByte((byte) 0);
    }

    @Override
    public Object toNBT() {
        try {
            Object nbtTagCompound = CONSTRUCTOR.newInstance();

            for (Map.Entry<String, Tag<?>> entry : value.entrySet()) {
                plugin.getNMSTags().setNBTCompoundTagValue(nbtTagCompound, entry.getKey(), entry.getValue().toNBT());
            }

            return nbtTagCompound;
        } catch (Exception error) {
            Log.error(error, "An unexpected error occurred while converting tag compound to NMS:");
            return null;
        }
    }

    public static CompoundTag fromNBT(Object tag) {
        Preconditions.checkArgument(tag.getClass().equals(CLASS), "Cannot convert " + tag.getClass() + " to CompoundTag!");

        Map<String, Tag<?>> map = new HashMap<>();

        try {
            Set<String> keySet = plugin.getNMSTags().getNBTCompoundValue(tag);

            for (String key : keySet) {
                map.put(key, Tag.fromNBT(plugin.getNMSTags().getNBTCompoundTag(tag, key)));
            }

            return new CompoundTag(map);
        } catch (Exception error) {
            Log.error(error, "An unexpected error occurred while converting tag compound from NMS:");
            return null;
        }
    }

    public static CompoundTag fromStream(DataInputStream is, int depth) throws IOException {
        Map<String, Tag<?>> tagMap = new HashMap<>();
        Tag<?> tag;

        while (!((tag = Tag.fromStream(is, depth + 1)) instanceof EndTag)) {
            int keyLength = is.readShort() & 0xFFFF;
            byte[] keyBytes = new byte[keyLength];
            is.readFully(keyBytes);
            String key = new String(keyBytes, StandardCharsets.UTF_8);
            tagMap.put(key, tag);
        }

        return new CompoundTag(tagMap);
    }

}