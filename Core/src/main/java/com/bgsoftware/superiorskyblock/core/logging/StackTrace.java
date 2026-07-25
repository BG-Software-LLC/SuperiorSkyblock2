package com.bgsoftware.superiorskyblock.core.logging;

public class StackTrace {

    private final StackTraceHolder stackTrace = new StackTraceHolder();

    public void dump() {
        this.stackTrace.printStackTrace();
    }

    private static class StackTraceHolder extends Throwable {

        @Override
        public String toString() {
            return "Original Stacktrace:";
        }
    }

}
