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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * The <code>TAG_End</code> tag.
 *
 * @author Graham Edgecombe
 */
public class EndTag extends Tag<Object> {

    /*package*/ static final NMSTagConverter TAG_CONVERTER = new NMSTagConverter("NBTTagEnd");

    private static final EndTag INSTANCE = new EndTag();

    private EndTag() {
        super(null);
    }

    @Override
    protected void writeData(DataOutputStream os) {
        // Do nothing.
    }

    @Override
    protected NMSTagConverter getNMSConverter() {
        return TAG_CONVERTER;
    }

    @Override
    public String toString() {
        return "TAG_End";
    }

    public static EndTag of() {
        return INSTANCE;
    }

    public static EndTag fromStream(DataInputStream is, int depth) throws IOException {
        if (depth == 0) {
            throw new IOException("TAG_End found without a TAG_Compound/TAG_List tag preceding it.");
        } else {
            return EndTag.INSTANCE;
        }
    }

}