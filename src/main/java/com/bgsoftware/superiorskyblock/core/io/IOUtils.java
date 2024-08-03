package com.bgsoftware.superiorskyblock.core.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtils {

    private static final int EOF = -1;

    private IOUtils() {

    }

    public static byte[] toByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        copy(inputStream, outputStream);
        return outputStream.toByteArray();
    }

    public static int copy(InputStream inputStream, OutputStream outputStream) throws IOException {
        long bytesCopied = copyLarge(inputStream, outputStream);
        return bytesCopied > 0x7FFFFFFFL ? -1 : (int) bytesCopied;
    }

    public static long copyLarge(InputStream inputStream, OutputStream outputStream) throws IOException {
        return copyLarge(inputStream, outputStream, new byte[4096]);
    }

    public static long copyLarge(InputStream inputStream, OutputStream outputStream, byte[] buf) throws IOException {
        long totalBytesCopied = 0;

        int bytesCopied;
        while ((bytesCopied = inputStream.read(buf)) != EOF) {
            outputStream.write(buf, 0, bytesCopied);
            totalBytesCopied += bytesCopied;
        }

        return totalBytesCopied;
    }

}
