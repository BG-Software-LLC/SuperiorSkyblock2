package com.bgsoftware.superiorskyblock.core.database.sql.session;

import com.bgsoftware.common.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;

public class QueryResult<T> {

    public static final Consumer<Throwable> PRINT_ERROR = Throwable::printStackTrace;
    public static final QueryResult<ResultSet> EMPTY_QUERY_RESULT = new QueryResult<>();
    public static final QueryResult<Void> EMPTY_VOID_QUERY_RESULT = new QueryResult<>();

    @Nullable
    private QueryConsumer<T> onSuccess;
    private Consumer<Throwable> onFail;

    public QueryResult() {

    }

    public QueryResult<T> onSuccess(QueryConsumer<T> onSuccess) {
        this.onSuccess = onSuccess;
        return this;
    }

    public QueryResult<T> onFail(Consumer<Throwable> onFail) {
        this.onFail = onFail;
        return this;
    }

    public void complete(T success) throws SQLException {
        if (onSuccess != null)
            onSuccess.accept(success);
    }

    public void fail(Throwable error) {
        if (onFail != null)
            onFail.accept(error);
    }

    public interface QueryConsumer<T> {

        void accept(T value) throws SQLException;

    }

}
