package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.common.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class SequentialListBuilder<E> {

    @Nullable
    private Comparator<? super E> comparator;
    @Nullable
    private Predicate<? super E> predicate;
    private boolean mutable = false;

    public SequentialListBuilder<E> sorted(@Nullable Comparator<? super E> comparator) {
        this.comparator = comparator;
        return this;
    }

    public SequentialListBuilder<E> filter(@Nullable Predicate<? super E> predicate) {
        this.predicate = predicate;
        return this;
    }

    public SequentialListBuilder<E> mutable() {
        this.mutable = true;
        return this;
    }

    public <O> List<E> build(Collection<O> collection, Function<O, E> mapper) {
        LinkedList<E> sequentialList = new LinkedList<>();

        collection.forEach(element -> {
            E mappedElement = mapper.apply(element);
            if (predicate == null || predicate.test(mappedElement))
                sequentialList.add(mappedElement);
        });

        return completeBuild(sequentialList);
    }


    public <O> List<O> map(Collection<E> collection, Function<E, O> mapper) {
        LinkedList<O> sequentialList = new LinkedList<>();

        collection.forEach(element -> {
            if (predicate == null || predicate.test(element))
                sequentialList.add(mapper.apply(element));
        });

        return completeBuild(sequentialList, null, this.mutable);
    }

    public List<E> build(Stream<E> stream) {
        LinkedList<E> sequentialList = new LinkedList<>();

        stream.forEach(element -> {
            if (predicate == null || predicate.test(element))
                sequentialList.add(element);
        });

        return completeBuild(sequentialList);
    }

    public List<E> build(Collection<E> collection) {
        LinkedList<E> sequentialList;

        if (predicate == null) {
            sequentialList = new LinkedList<>(collection);
        } else {
            sequentialList = new LinkedList<>();
            collection.forEach(element -> {
                if (predicate.test(element))
                    sequentialList.add(element);
            });
        }

        return completeBuild(sequentialList);
    }

    private List<E> completeBuild(List<E> sequentialList) {
        return completeBuild(sequentialList, this.comparator, this.mutable);
    }

    private static <E> List<E> completeBuild(List<E> sequentialList, @Nullable Comparator<? super E> comparator, boolean mutable) {
        if (sequentialList.isEmpty())
            return mutable ? sequentialList : Collections.emptyList();

        if (comparator != null)
            sequentialList.sort(comparator);

        return mutable ? sequentialList : Collections.unmodifiableList(sequentialList);
    }


}
