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

import com.bgsoftware.superiorskyblock.utils.reflections.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The <code>TAG_Compound</code> tag.
 *
 * @author Graham Edgecombe
 */
public final class CompoundTag extends Tag<Map<String, Tag<?>>> {

    static final Class<?> CLASS;
    static final Constructor<?> CONSTRUCTOR;
    static final Method COMPOUND_SET, COMPOUND_GET;

    static {
        CLASS = ReflectionUtils.getClass("net.minecraft.server.VERSION.NBTTagCompound");
        CONSTRUCTOR = ReflectionUtils.getConstructor(CLASS);
        COMPOUND_SET = ReflectionUtils.getMethod(CLASS, "set", null, String.class, Tag.CLASS);
        COMPOUND_GET = ReflectionUtils.getMethod(CLASS, "get", null, String.class);
    }

    /**
     * Creates the tag.
     *
     * @param value The value.
     */
    public CompoundTag(Map<String, Tag<?>> value) {
        super(new HashMap<>(value));
    }

    public void setString(String key, String value){
        this.value.put(key, new StringTag(value));
    }

    public void setTag(String key, Tag<?> value){
        this.value.put(key, value);
    }

    @Override
    public Map<String, Tag<?>> getValue() {
        return Collections.unmodifiableMap(value);
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

    @Override
    public Object toNBT() {
        try {
            Object nbtTagCompound = CONSTRUCTOR.newInstance();

            for(String key : value.keySet()){
                COMPOUND_SET.invoke(nbtTagCompound, key, value.get(key).toNBT());
            }

            return nbtTagCompound;
        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    public static CompoundTag fromNBT(Object tag){
        if(!tag.getClass().equals(CLASS))
            throw new IllegalArgumentException("Cannot convert " + tag.getClass() + " to CompoundTag!");

        Map<String, Tag<?>> map = new HashMap<>();

        try {
            Set<String> keySet = plugin.getNMSTags().getNBTCompoundValue(tag);

            for(String key : keySet) {
                map.put(key, Tag.fromNBT(COMPOUND_GET.invoke(tag, key)));
            }

            return new CompoundTag(map);
        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

}