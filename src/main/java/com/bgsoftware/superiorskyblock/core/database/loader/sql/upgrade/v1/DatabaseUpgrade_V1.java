package com.bgsoftware.superiorskyblock.core.database.loader.sql.upgrade.v1;

import com.bgsoftware.superiorskyblock.core.database.sql.SQLHelper;

public class DatabaseUpgrade_V1 implements Runnable {

    public static final DatabaseUpgrade_V1 INSTANCE = new DatabaseUpgrade_V1();

    private DatabaseUpgrade_V1() {

    }

    @Override
    public void run() {
        SQLHelper.addColumn("islands", "entity_counts", "LONGTEXT");
    }

}
