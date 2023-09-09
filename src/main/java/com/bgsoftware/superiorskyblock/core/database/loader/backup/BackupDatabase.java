package com.bgsoftware.superiorskyblock.core.database.loader.backup;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.database.loader.DatabaseLoader;
import com.bgsoftware.superiorskyblock.core.errors.ManagerLoadException;
import com.bgsoftware.superiorskyblock.core.io.ZipFiles;
import com.bgsoftware.superiorskyblock.core.logging.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BackupDatabase implements DatabaseLoader {

    private static final SimpleDateFormat BACKUP_FILE_NAME_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final int MAX_BACKUP_FILES_PER_DAY = 1000;

    private final SuperiorSkyblockPlugin plugin;

    public BackupDatabase(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void setState(State state) throws ManagerLoadException {
        if (state != State.PRE_LOAD_DATA || !plugin.getSettings().getDatabase().isBackup())
            return;

        File datastoreFolder = new File(plugin.getDataFolder(), "datastore");

        if (!datastoreFolder.exists())
            return;

        String currentTime = BACKUP_FILE_NAME_DATE_FORMAT.format(new Date());

        File backupFile = getBackupFile(currentTime, 1);

        if (backupFile == null)
            throw new ManagerLoadException("Could not create a backup file as file already exists", ManagerLoadException.ErrorLevel.SERVER_SHUTDOWN);

        Log.info("Creating a backup file...");

        backupFile.getParentFile().mkdirs();

        try {
            ZipFiles.zipFolder(datastoreFolder, backupFile);
        } catch (IOException error) {
            backupFile.delete();
            throw new ManagerLoadException(error, ManagerLoadException.ErrorLevel.SERVER_SHUTDOWN);
        }

        Log.info("Backup done!");
    }

    @Nullable
    private File getBackupFile(String time, int attempt) {
        File file = new File(plugin.getDataFolder(), String.format("backup/%s%s.zip", time,
                attempt > 1 ? "-" + attempt : ""));

        if (file.exists())
            return attempt >= MAX_BACKUP_FILES_PER_DAY ? null : getBackupFile(time, attempt + 1);

        return file;
    }

}
