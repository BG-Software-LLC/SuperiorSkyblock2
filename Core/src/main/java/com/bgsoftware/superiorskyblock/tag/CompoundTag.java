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
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;

/**
 * The <code>TAG_Compound</code> tag.
 *
 * @author Graham Edgecombe
 */
public class CompoundTag extends Tag<Map<String, Tag<?>>> implements Iterable<Tag<?>> {

    /*package*/ static final NMSTagConverter TAG_CONVERTER = NMSTagConverter.choice("NBTTagCompound", "CompoundTag");

    private CompoundTag(Map<String, Tag<?>> value, boolean cloneMap) {
        super(cloneMap ? new HashMap<>(value) : value);
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

    public CompoundTag copy() {
        return of(this.value);
    }

    public Optional<Tag<?>> getTag(String key) {
        return Optional.ofNullable(getTagInternal(key));
    }

    public Optional<byte[]> getByteArray(String key) {
        return getTagInternal(key, ByteArrayTag.class).map(tag -> tag.value);
    }

    public OptionalInt getByte(String key) {
        NumberTag<?> tag = getTagInternal(key, NumberTag.class).orElse(null);
        return tag == null ? OptionalInt.empty() : OptionalInt.of(tag.value.byteValue());
    }

    public Optional<CompoundTag> getCompound(String key) {
        return getTagInternal(key, CompoundTag.class);
    }

    public OptionalDouble getDouble(String key) {
        NumberTag<?> tag = getTagInternal(key, NumberTag.class).orElse(null);
        return tag == null ? OptionalDouble.empty() : OptionalDouble.of(tag.value.doubleValue());
    }

    public OptionalDouble getFloat(String key) {
        NumberTag<?> tag = getTagInternal(key, NumberTag.class).orElse(null);
        return tag == null ? OptionalDouble.empty() : OptionalDouble.of(tag.value.floatValue());
    }

    public Optional<int[]> getIntArray(String key) {
        return getTagInternal(key, IntArrayTag.class).map(tag -> tag.value);
    }

    public OptionalInt getInt(String key) {
        NumberTag<?> tag = getTagInternal(key, NumberTag.class).orElse(null);
        return tag == null ? OptionalInt.empty() : OptionalInt.of(tag.value.intValue());
    }

    public Optional<ListTag> getList(String key) {
        return getTagInternal(key, ListTag.class);
    }

    public OptionalLong getLong(String key) {
        NumberTag<?> tag = getTagInternal(key, NumberTag.class).orElse(null);
        return tag == null ? OptionalLong.empty() : OptionalLong.of(tag.value.longValue());
    }

    public Optional<Number> getNumber(String key) {
        return getTagInternal(key, NumberTag.class).map(tag -> (Number) tag.value);
    }

    public OptionalInt getShort(String key) {
        NumberTag<?> tag = getTagInternal(key, NumberTag.class).orElse(null);
        return tag == null ? OptionalInt.empty() : OptionalInt.of(tag.value.shortValue());
    }

    public Optional<String> getString(String key) {
        return getTagInternal(key, StringTag.class).map(tag -> tag.value);
    }

    @Nullable
    public Tag<?> setTag(String key, Tag<?> value) {
        return this.value.put(key, value);
    }

    public void setByteArray(String key, byte[] value) {
        setTag(key, ByteArrayTag.of(value));
    }

    public void setByte(String key, byte value) {
        setTag(key, ByteTag.of(value));
    }

    public void setDouble(String key, double value) {
        setTag(key, DoubleTag.of(value));
    }

    public void setFloat(String key, float value) {
        setTag(key, FloatTag.of(value));
    }

    public void setIntArray(String key, int[] value) {
        setTag(key, IntArrayTag.of(value));
    }

    public void setInt(String key, int value) {
        setTag(key, IntTag.of(value));
    }

    public void setLong(String key, long value) {
        setTag(key, LongTag.of(value));
    }

    public void setShort(String key, short value) {
        setTag(key, ShortTag.of(value));
    }

    public void setString(String key, String value) {
        setTag(key, StringTag.of(value));
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
    public Object toNBT() {
        try {
            Object nbtTagCompound = TAG_CONVERTER.toNBT();

            for (Map.Entry<String, Tag<?>> entry : value.entrySet()) {
                plugin.getNMSTags().setNBTCompoundTagValue(nbtTagCompound, entry.getKey(), entry.getValue().toNBT());
            }

            return nbtTagCompound;
        } catch (Exception error) {
            Log.error(error, "An unexpected error occurred while converting tag compound to NMS:");
            return null;
        }
    }

    @Nullable
    private Tag<?> getTagInternal(String key) {
        return this.value.get(key);
    }

    @Nullable
    private <T extends Tag<?>> Optional<T> getTagInternal(String key, Class<T> valueType) {
        Tag<?> tag = this.value.get(key);
        return tag != null && valueType.isAssignableFrom(tag.getClass()) ? Optional.of(valueType.cast(tag)) : Optional.empty();
    }

    public static CompoundTag of() {
        return new CompoundTag(new HashMap<>(), false);
    }

    public static CompoundTag of(Map<String, Tag<?>> value) {
        return new CompoundTag(value, true);
    }

    public static CompoundTag fromNBT(Object tag) {
        Preconditions.checkArgument(tag.getClass().equals(TAG_CONVERTER.getNBTClass()), "Cannot convert " + tag.getClass() + " to CompoundTag!");

        Map<String, Tag<?>> map = new HashMap<>();

        try {
            Set<String> keySet = plugin.getNMSTags().getNBTCompoundValue(tag);

            for (String key : keySet) {
                map.put(key, Tag.fromNBT(plugin.getNMSTags().getNBTCompoundTag(tag, key)));
            }

            return new CompoundTag(map, false);
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

        return new CompoundTag(tagMap, false);
    }

}