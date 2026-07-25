package com.bgsoftware.superiorskyblock.core.database.loader;

import com.bgsoftware.superiorskyblock.core.errors.ManagerLoadException;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public abstract class MachineStateDatabaseLoader implements DatabaseLoader {

    private final Map<State, StateAction> STATE_MACHINE_HANDLES = Collections.unmodifiableMap(new EnumMap<State, StateAction>(State.class) {{
        put(State.INITIALIZE, MachineStateDatabaseLoader.this::handleInitialize);
        put(State.POST_INITIALIZE, MachineStateDatabaseLoader.this::handlePostInitialize);
        put(State.PRE_LOAD_DATA, MachineStateDatabaseLoader.this::handlePreLoadData);
        put(State.POST_LOAD_DATA, MachineStateDatabaseLoader.this::handlePostLoadData);
        put(State.SHUTDOWN, MachineStateDatabaseLoader.this::handleShutdown);
    }});

    @Override
    public void setState(State state) throws ManagerLoadException {
        StateAction action = STATE_MACHINE_HANDLES.get(state);
        if (action != null) {
            try {
                action.run();
            } catch (Throwable error) {
                if (error instanceof ManagerLoadException)
                    throw error;

                throw new ManagerLoadException(error, ManagerLoadException.ErrorLevel.SERVER_SHUTDOWN);
            }
        }
    }

    protected abstract void handleInitialize() throws ManagerLoadException;

    protected abstract void handlePostInitialize() throws ManagerLoadException;

    protected abstract void handlePreLoadData() throws ManagerLoadException;

    protected abstract void handlePostLoadData() throws ManagerLoadException;

    protected abstract void handleShutdown() throws ManagerLoadException;

    private interface StateAction {

        void run() throws ManagerLoadException;

    }

}
