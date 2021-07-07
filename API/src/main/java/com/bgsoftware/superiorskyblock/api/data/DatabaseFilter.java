package com.bgsoftware.superiorskyblock.api.data;

import com.bgsoftware.superiorskyblock.api.objects.Pair;

import java.util.Collection;
import java.util.Collections;

public final class DatabaseFilter {

    private final Collection<Pair<String, Object>> filters;

    public DatabaseFilter(Collection<Pair<String, Object>> filters){
        this.filters = Collections.unmodifiableCollection(filters);
    }

    public Collection<Pair<String, Object>> getFilters() {
        return filters;
    }

}
