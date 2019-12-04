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

/**
 * The <code>TAG_Float</code> tag.
 *
 * @author Graham Edgecombe
 */
@SuppressWarnings("WeakerAccess")
public final class FloatTag extends Tag<Float> {

    /**
     * Creates the tag.
     *
     * @param value The value.
     */
    public FloatTag(float value) {
        super(value);
    }

    @Override
    public String toString() {
        return "TAG_Float: " + value;
    }

    @Override
    public Object toNBT() {
        try {
            Class<?> nbtTagClass = ReflectionUtils.getClass("net.minecraft.server.VERSION.NBTTagFloat");
            //noinspection ConstantConditions
            return nbtTagClass.getConstructor(float.class).newInstance((Object) value);
        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    public static FloatTag fromNBT(Object tag){
        Class<?> nbtTagClass = ReflectionUtils.getClass("net.minecraft.server.VERSION.NBTTagFloat");
        if(!tag.getClass().equals(nbtTagClass))
            throw new IllegalArgumentException("Cannot convert " + tag.getClass() + " to FloatTag!");

        try {
            float value = plugin.getNMSTags().getNBTFloatValue(tag);
            return new FloatTag(value);
        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

}