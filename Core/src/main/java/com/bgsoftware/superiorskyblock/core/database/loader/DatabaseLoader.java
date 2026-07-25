package com.bgsoftware.superiorskyblock.core.database.loader;

import com.bgsoftware.superiorskyblock.core.errors.ManagerLoadException;

public interface DatabaseLoader {

    void setState(State state) throws ManagerLoadException;

    enum State {

        INITIALIZE,
        POST_INITIALIZE,
        PRE_LOAD_DATA,
        POST_LOAD_DATA,
        SHUTDOWN

    }

}
