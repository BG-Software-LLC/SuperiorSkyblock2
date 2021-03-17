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
package com.bgsoftware.superiorskyblock.utils.tags;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

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
public final class CompoundTag extends Tag<Map<String, Tag<?>>> implements Iterable<Tag<?>> {

    protected static final Class<?> CLASS = getNNTClass("NBTTagCompound");

    private static final ReflectMethod<Void> SET = new ReflectMethod<>("net.minecraft.server.VERSION.NBTTagCompound",
            "set", String.class, getNNTClass("NBTBase"));
    private static final ReflectMethod<Object> GET = new ReflectMethod<>("net.minecraft.server.VERSION.NBTTagCompound", "get", String.class);

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

    public void setString(String key, String value){
        setTag(key, new StringTag(value));
    }

    public void setInt(String key, int value){
        setTag(key, new IntTag(value));
    }

    public void setShort(String key, short value){
        setTag(key, new ShortTag(value));
    }

    public void setByte(String key, byte value){
        setTag(key, new ByteTag(value));
    }

    public void setTag(String key, Tag<?> value){
        this.value.put(key, value);
    }

    public String getString(String key){
        Tag<?> tag = getTag(key);
        return !(tag instanceof StringTag) ? null : ((StringTag) tag).value;
    }

    public int getInt(String key){
        Tag<?> tag = getTag(key);
        return !(tag instanceof IntTag) ? 0 : ((IntTag) tag).value;
    }


    public CompoundTag getCompound(String key){
        Tag<?> tag = getTag(key);
        return !(tag instanceof CompoundTag) ? null : (CompoundTag) tag;
    }

    public Tag<?> getTag(String key){
        return this.value.get(key);
    }

    public boolean containsKey(String key){
        return this.value.containsKey(key);
    }

    public int size(){
        return value.size();
    }

    public Set<Map.Entry<String, Tag<?>>> entrySet(){
        return value.entrySet();
    }

    @Override
    protected void writeData(DataOutputStream os) throws IOException {
        for(Map.Entry<String, Tag<?>> entry : value.entrySet()) {
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

            for(String key : value.keySet()){
                SET.invoke(nbtTagCompound, key, value.get(key).toNBT());
            }

            return nbtTagCompound;
        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
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
            bldr.append("   ").append(entry.getValue().toString().replaceAll("\r\n", "\r\n   ")).append("\r\n");
        }
        bldr.append("}");
        return bldr.toString();
    }

    public static CompoundTag fromNBT(Object tag){
        Preconditions.checkArgument(tag.getClass().equals(CLASS), "Cannot convert " + tag.getClass() + " to CompoundTag!");

        Map<String, Tag<?>> map = new HashMap<>();

        try {
            Set<String> keySet = plugin.getNMSTags().getNBTCompoundValue(tag);

            for(String key : keySet) {
                map.put(key, Tag.fromNBT(GET.invoke(tag, key)));
            }

            return new CompoundTag(map);
        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    public static CompoundTag fromStream(DataInputStream is, int depth) throws IOException{
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