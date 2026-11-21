package com.bgsoftware.superiorskyblock.core;

import java.util.function.Function;

public class Computed<E> {

    private E value;
    private boolean computed = false;

    public <T> E tryCompute(T arg, Function<T, E> function) {
        if (!this.computed) {
            this.value = function.apply(arg);
            this.computed = true;
        }

        return this.value;
    }

}
