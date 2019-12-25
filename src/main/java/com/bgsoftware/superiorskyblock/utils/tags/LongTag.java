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

/**
 * The <code>TAG_Long</code> tag.
 *
 * @author Graham Edgecombe
 */
@SuppressWarnings("WeakerAccess")
public final class LongTag extends Tag<Long> {

    static final Class<?> CLASS;
    static final Constructor<?> CONSTRUCTOR;
    static final Method CONSTRUCTOR_METHOD;

    static {
        CLASS = ReflectionUtils.getClass("net.minecraft.server.VERSION.NBTTagLong");
        CONSTRUCTOR = ReflectionUtils.getConstructor(CLASS, long.class);
        CONSTRUCTOR_METHOD = ReflectionUtils.getMethod(CLASS, "a", CLASS, long.class);
    }

    /**
     * Creates the tag.
     *
     * @param value The value.
     */
    public LongTag(long value) {
        super(value);
    }

    @Override
    public String toString() {
        return "TAG_Long: " + value;
    }

    @Override
    public Object toNBT() {
        try {
            if(CONSTRUCTOR_METHOD != null){
                return CONSTRUCTOR_METHOD.invoke(null, value);
            }
            else{
                return CONSTRUCTOR.newInstance(value);
            }
        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    public static LongTag fromNBT(Object tag){
        if(!tag.getClass().equals(CLASS))
            throw new IllegalArgumentException("Cannot convert " + tag.getClass() + " to LongTag!");

        try {
            long value = plugin.getNMSTags().getNBTLongValue(tag);
            return new LongTag(value);
        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

}