package com.bgsoftware.superiorskyblock.database.sql.session;

import javax.annotation.Nullable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;

public final class QueryResult<T> {

    public static final QueryResult<Void> VOID = QueryResult.ofSuccess(null);
    public static final QueryResult<ResultSet> RESULT_SET_ERROR = QueryResult.ofFail(new Exception("Session was not initialized."));
    public static final QueryResult<PreparedStatement> PREPARED_STATEMENT_ERROR = QueryResult.ofFail(new Exception("Session was not initialized."));

    public static final Consumer<Throwable> PRINT_ERROR = Throwable::printStackTrace;

    @Nullable
    private final T successValue;
    private final Throwable error;
    @Nullable
    private Consumer<T> onFinish;

    private QueryResult(@Nullable T value, Throwable error) {
        this.successValue = value;
        this.error = error;
    }

    public QueryResult<T> ifSuccess(QueryConsumer<T> consumer) {
        try {
            if (error == null) {
                try {
                    consumer.accept(successValue);
                } catch (SQLException error) {
                    return QueryResult.ofFail(error);
                }
            }
        } finally {
            if (onFinish != null)
                onFinish.accept(successValue);
        }
        return this;
    }

    public QueryResult<T> ifFail(Consumer<Throwable> consumer) {
        if (error != null)
            consumer.accept(error);

        return this;
    }

    public QueryResult<T> onFinish(Consumer<T> onFinish) {
        this.onFinish = onFinish;
        return this;
    }

    public static <T> QueryResult<T> ofSuccess(T value) {
        return new QueryResult<>(value, null);
    }

    public static <T> QueryResult<T> ofFail(Throwable error) {
        return new QueryResult<>(null, error);
    }

    public interface QueryConsumer<T> {

        void accept(T value) throws SQLException;

    }

}
