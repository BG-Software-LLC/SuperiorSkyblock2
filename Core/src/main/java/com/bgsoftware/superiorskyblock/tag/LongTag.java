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

import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.google.common.base.Preconditions;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * The <code>TAG_Long</code> tag.
 *
 * @author Graham Edgecombe
 */
public class LongTag extends NumberTag<Long> {

    /*package*/ static final NMSTagConverter TAG_CONVERTER = NMSTagConverter.choice(
            new String[]{"NBTTagLong", "LongTag"}, long.class);

    private static final LongTag[] CACHE = new LongTag[100];

    private LongTag(long value) {
        super(value);
    }

    @Override
    protected void writeData(DataOutputStream os) throws IOException {
        os.writeLong(value);
    }

    @Override
    protected NMSTagConverter getNMSConverter() {
        return TAG_CONVERTER;
    }

    public static LongTag of(long value) {
        if (value >= 0 && value < CACHE.length) {
            LongTag tag = CACHE[(int) value];
            if (tag == null)
                tag = CACHE[(int) value] = new LongTag(value);
            return tag;
        } else {
            return new LongTag(value);
        }
    }

    public static LongTag fromNBT(Object tag) {
        Preconditions.checkArgument(tag.getClass().equals(TAG_CONVERTER.getNBTClass()), "Cannot convert " + tag.getClass() + " to LongTag!");

        try {
            long value = plugin.getNMSTags().getNBTLongValue(tag);
            return LongTag.of(value);
        } catch (Exception error) {
            Log.error(error, "An unexpected error occurred while converting tag long from NMS:");
            return null;
        }
    }

    public static LongTag fromStream(DataInputStream is) throws IOException {
        return LongTag.of(is.readLong());
    }

}