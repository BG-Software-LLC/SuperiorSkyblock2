package com.bgsoftware.superiorskyblock.commands.arguments;

public class NumberArgument<N> extends Argument<N, Boolean> {

    public NumberArgument(N number, boolean succeed) {
        super(number, succeed);
    }

    public N getNumber() {
        return super.k;
    }

    public boolean isSucceed() {
        return super.v;
    }

}
