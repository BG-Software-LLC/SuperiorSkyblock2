package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEvent;
import com.bgsoftware.superiorskyblock.core.messages.Message;

import java.util.Locale;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ChangeResult {

    public static final ChangeResult APPLIED = new ChangeResult(null, null);
    public static final ChangeResult CANCELLED = new ChangeResult(Message.CHANGE_SKIP_REASON_CANCELLED, null);
    public static final ChangeResult ABOVE_MAXIMUM = new ChangeResult(Message.CHANGE_SKIP_REASON_ABOVE_MAXIMUM, null);
    public static final ChangeResult BELOW_MINIMUM = new ChangeResult(Message.CHANGE_SKIP_REASON_BELOW_MINIMUM, null);
    public static final ChangeResult UNLIMITED = new ChangeResult(Message.CHANGE_SKIP_REASON_UNLIMITED, null);

    @Nullable
    private final Message reasonMessage;
    @Nullable
    private final String reasonText;

    private ChangeResult(@Nullable Message reasonMessage, @Nullable String reasonText) {
        this.reasonMessage = reasonMessage;
        this.reasonText = reasonText;
    }

    public static ChangeResult cancelled(@Nullable String reason) {
        return Text.isBlank(reason) ? CANCELLED : new ChangeResult(null, reason);
    }

    // The appliers get everything they need as parameters (the event args, or the target and its values), so
    // callers can pass non-capturing lambdas and unbound method references, which aren't allocated per target.

    public static <Args extends PluginEventArgs> ChangeResult ifAllowed(PluginEvent<Args> event, Consumer<Args> applier) {
        if (event.isCancelled())
            return CANCELLED;

        applier.accept(event.getArgs());
        return APPLIED;
    }

    public static <T> ChangeResult ifAllowed(boolean allowed, T target, Consumer<T> applier) {
        if (!allowed)
            return CANCELLED;

        applier.accept(target);
        return APPLIED;
    }

    public static <T, U> ChangeResult ifAllowed(boolean allowed, T target, U value, BiConsumer<T, U> applier) {
        if (!allowed)
            return CANCELLED;

        applier.accept(target, value);
        return APPLIED;
    }

    public static <T, U, V> ChangeResult ifAllowed(boolean allowed, T target, U value1, V value2,
                                                   TriConsumer<T, U, V> applier) {
        if (!allowed)
            return CANCELLED;

        applier.accept(target, value1, value2);
        return APPLIED;
    }

    public boolean isApplied() {
        return this.reasonMessage == null && this.reasonText == null;
    }

    public String getReason(Locale locale) {
        if (this.reasonMessage == null)
            return this.reasonText == null ? "" : this.reasonText;

        String reason = this.reasonMessage.getMessage(locale);
        return reason == null ? "" : reason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChangeResult that = (ChangeResult) o;
        return this.reasonMessage == that.reasonMessage && Objects.equals(this.reasonText, that.reasonText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.reasonMessage, this.reasonText);
    }

    public interface TriConsumer<T, U, V> {

        void accept(T t, U u, V v);

    }

}
