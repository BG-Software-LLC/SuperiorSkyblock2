package com.bgsoftware.superiorskyblock.island;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.core.key.BaseKey;
import org.bukkit.Location;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class BlockLimitReservations {

    private static final Map<Island, State> states = new WeakHashMap<>();

    private BlockLimitReservations() {
    }

    @Nullable
    public static Reservation reserve(Island island, Key key, int requested, @Nullable Location location) {
        return reserve(island, key, requested, location, null);
    }

    @Nullable
    public static Reservation reserve(Island island, Key key, int requested, @Nullable Location location,
                                      @Nullable Key replacedKey) {
        return reserve(island, key, requested, location, replacedKey, false);
    }

    @Nullable
    public static Reservation reserveStacked(Island island, Key key, int requested) {
        return reserve(island, key, requested, null, null, true);
    }

    @Nullable
    private static Reservation reserve(Island island, Key key, int requested, @Nullable Location location,
                                       @Nullable Key replacedKey, boolean enforceGlobalLimit) {
        if (requested <= 0)
            return null;
        State state = getState(island);
        Key globalKey = ((BaseKey<?>) key).toGlobalKey();
        Location position = position(location);
        synchronized (state) {
            int exactLimit = island.getExactBlockLimit(key);
            int limit = exactLimit >= 0 ? exactLimit : island.getBlockLimit(globalKey);
            int globalCredit = replacedKey != null && ((BaseKey<?>) replacedKey).toGlobalKey().equals(globalKey) ? requested : 0;
            int amount = requested;
            if (limit >= 0) {
                BigInteger count = exactLimit >= 0 ? island.getExactBlockCountAsBigInteger(key) :
                        island.getBlockCountAsBigInteger(globalKey);
                BigInteger pending = (exactLimit >= 0 ? state.exact : state.global)
                        .getOrDefault(exactLimit >= 0 ? key : globalKey, BigInteger.ZERO);
                BigInteger available = BigInteger.valueOf(limit).subtract(count).subtract(pending);
                if (exactLimit < 0)
                    available = available.add(BigInteger.valueOf(globalCredit));
                amount = available.max(BigInteger.ZERO).min(BigInteger.valueOf(requested)).intValue();
            }
            if (enforceGlobalLimit && exactLimit >= 0 && !key.equals(globalKey)) {
                int globalLimit = island.getBlockLimit(globalKey);
                if (globalLimit >= 0) {
                    BigInteger available = BigInteger.valueOf(globalLimit).subtract(island.getBlockCountAsBigInteger(globalKey))
                            .subtract(state.global.getOrDefault(globalKey, BigInteger.ZERO));
                    amount = available.max(BigInteger.ZERO).min(BigInteger.valueOf(amount)).intValue();
                }
            }
            if (amount == 0)
                return null;
            Reservation reservation = new Reservation(state, key, globalKey, amount,
                    amount, position);
            add(state.exact, key, amount);
            add(state.global, globalKey, reservation.globalAmount);
            if (position != null)
                state.positions.computeIfAbsent(position, ignored -> new ArrayList<>()).add(reservation);
            return reservation;
        }
    }

    public static void recorded(Island island, Key key, Location location, int amount) {
        State state = getState(island);
        synchronized (state) {
            List<Reservation> reservations = state.positions.get(position(location));
            if (reservations == null)
                return;
            for (Reservation reservation : new ArrayList<>(reservations)) {
                if (reservation.key.equals(key)) {
                    int recorded = Math.min(amount, reservation.remaining);
                    reservation.release(recorded);
                    amount -= recorded;
                    if (amount == 0)
                        break;
                }
            }
        }
    }

    private static State getState(Island island) {
        synchronized (states) {
            return states.computeIfAbsent(island, ignored -> new State());
        }
    }

    @Nullable
    private static Location position(@Nullable Location location) {
        return location == null ? null : new Location(location.getWorld(),
                location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    private static void add(Map<Key, BigInteger> counts, Key key, int amount) {
        BigInteger value = counts.getOrDefault(key, BigInteger.ZERO).add(BigInteger.valueOf(amount));
        if (value.signum() == 0)
            counts.remove(key);
        else
            counts.put(key, value);
    }

    private static final class State {

        private final Map<Key, BigInteger> exact = new HashMap<>();
        private final Map<Key, BigInteger> global = new HashMap<>();
        private final Map<Location, List<Reservation>> positions = new HashMap<>();
    }

    public static final class Reservation implements AutoCloseable {

        private final State state;
        private final Key key;
        private final Key globalKey;
        private final int amount;
        private final Location position;
        private int remaining;
        private int globalAmount;

        private Reservation(State state, Key key, Key globalKey, int amount, int globalAmount,
                            @Nullable Location position) {
            this.state = state;
            this.key = key;
            this.globalKey = globalKey;
            this.amount = amount;
            this.remaining = amount;
            this.globalAmount = globalAmount;
            this.position = position;
        }

        public int getAmount() {
            return amount;
        }

        public boolean matches(Key key, Location location) {
            return this.key.equals(key) && this.position != null && this.position.equals(position(location));
        }

        @Override
        public void close() {
            synchronized (state) {
                release(remaining);
            }
        }

        private void release(int amount) {
            if (amount == 0)
                return;
            remaining -= amount;
            add(state.exact, key, -amount);
            int releasedGlobal = Math.min(amount, globalAmount);
            globalAmount -= releasedGlobal;
            add(state.global, globalKey, -releasedGlobal);
            if (remaining == 0 && position != null) {
                List<Reservation> reservations = state.positions.get(position);
                if (reservations != null) {
                    reservations.remove(this);
                    if (reservations.isEmpty())
                        state.positions.remove(position);
                }
            }
        }
    }
}
