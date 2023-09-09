package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.common.annotations.Nullable;

import java.util.function.Consumer;

public abstract class Either<R, L> {

    public static <R, L> Either<R, L> right(R value) {
        return new Right<>(value);
    }

    public static <R, L> Either<R, L> left(L value) {
        return new Left<>(value);
    }

    public abstract Either<R, L> ifRight(Consumer<R> consumer);

    public abstract Either<R, L> ifLeft(Consumer<L> consumer);

    @Nullable
    public abstract R getRight();

    @Nullable
    public abstract L getLeft();

    private static class Right<R, L> extends Either<R, L> {

        private final R value;

        Right(R value) {
            this.value = value;
        }

        @Override
        public Either<R, L> ifRight(Consumer<R> consumer) {
            consumer.accept(this.value);
            return this;
        }

        @Override
        public Either<R, L> ifLeft(Consumer<L> consumer) {
            // Do nothing
            return this;
        }

        @Override
        public R getRight() {
            return this.value;
        }

        @Override
        public L getLeft() {
            return null;
        }

    }

    private static class Left<R, L> extends Either<R, L> {

        private final L value;

        Left(L value) {
            this.value = value;
        }

        @Override
        public Either<R, L> ifRight(Consumer<R> consumer) {
            // Do nothing
            return this;
        }

        @Override
        public Either<R, L> ifLeft(Consumer<L> consumer) {
            consumer.accept(this.value);
            return this;
        }

        @Override
        public R getRight() {
            return null;
        }

        @Override
        public L getLeft() {
            return this.value;
        }

    }

}
