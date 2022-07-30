package com.bgsoftware.superiorskyblock.api.data;

import com.bgsoftware.superiorskyblock.api.objects.Pair;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

public abstract class DatabaseFilter {

    private static DatabaseFilterEmpty EMPTY_FILTER;

    public static DatabaseFilter fromFilter(String filterKey, Object filterValue) {
        return new DatabaseFilterSingle(filterKey, filterValue);
    }

    public static DatabaseFilter fromFilters(List<Pair<String, Object>> filters) {
        if (filters.isEmpty()) {
            if (EMPTY_FILTER == null)
                EMPTY_FILTER = new DatabaseFilterEmpty();

            return EMPTY_FILTER;
        } else if (filters.size() == 1) {
            Pair<String, Object> filter = filters.get(0);
            return fromFilter(filter.getKey(), filter.getValue());
        } else {
            return new DatabaseFilterList(filters);
        }
    }

    protected DatabaseFilter() {
    }

    public abstract void forEach(BiConsumer<String, Object> consumer);

    public abstract Collection<Pair<String, Object>> getFilters();

    private static class DatabaseFilterList extends DatabaseFilter {

        private final Collection<Pair<String, Object>> filters;

        DatabaseFilterList(Collection<Pair<String, Object>> filters) {
            this.filters = filters;
        }

        @Override
        public void forEach(BiConsumer<String, Object> consumer) {
            filters.forEach(pair -> consumer.accept(pair.getKey(), pair.getValue()));
        }

        @Override
        public Collection<Pair<String, Object>> getFilters() {
            return Collections.unmodifiableCollection(filters);
        }

    }

    private static class DatabaseFilterEmpty extends DatabaseFilter {

        @Override
        public void forEach(BiConsumer<String, Object> consumer) {
            // Do nothing.
        }

        @Override
        public Collection<Pair<String, Object>> getFilters() {
            return Collections.emptyList();
        }

    }

    private static class DatabaseFilterSingle extends DatabaseFilter {

        private final String filterKey;
        private final Object filterValue;

        DatabaseFilterSingle(String filterKey, Object filterValue) {
            this.filterKey = filterKey;
            this.filterValue = filterValue;
        }

        @Override
        public void forEach(BiConsumer<String, Object> consumer) {
            consumer.accept(filterKey, filterValue);
        }

        @Override
        public Collection<Pair<String, Object>> getFilters() {
            return Collections.singleton(new Pair<>(filterKey, filterValue));
        }

    }


}
