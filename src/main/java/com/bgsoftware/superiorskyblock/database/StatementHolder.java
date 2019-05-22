package com.bgsoftware.superiorskyblock.database;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.utils.threads.SuperiorThread;
import org.bukkit.Bukkit;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StatementHolder {

    private PreparedStatement statement;

    public StatementHolder(Query query) throws SQLException {
        statement = query.getStatement(SuperiorSkyblockPlugin.getPlugin().getDataHandler().getConnection());
    }

    public void set(DataType type, int index, Object object) throws SQLException {
        type.set(statement, index, object);
    }

    public void execute() {
        if(!Bukkit.isPrimaryThread()){
            Bukkit.getScheduler().runTask(SuperiorSkyblockPlugin.getPlugin(), this::execute);
            return;
        }

        new SuperiorThread(() -> {
            try {
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();
    }

}
