package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.SIsland;
import com.bgsoftware.superiorskyblock.utils.jnbt.CompoundTag;
import com.bgsoftware.superiorskyblock.utils.jnbt.NBTInputStream;
import com.bgsoftware.superiorskyblock.utils.jnbt.NBTOutputStream;
import com.bgsoftware.superiorskyblock.utils.jnbt.Tag;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ResultOfMethodCallIgnored")
public final class DataHandler {

    public SuperiorSkyblockPlugin plugin;

    public DataHandler(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;
        loadDatabase();
    }

    public void saveDatabase(boolean async){
        if(async && Bukkit.isPrimaryThread()){
            new Thread(() -> saveDatabase(false)).start();
            return;
        }

        List<Island> islands = new ArrayList<>();
        plugin.getGrid().getAllIslands().forEach(uuid -> islands.add(plugin.getGrid().getIsland(SSuperiorPlayer.of(uuid))));
        List<SuperiorPlayer> players = plugin.getPlayers().getAllPlayers();
        File file;

        /*
         * Delete all old island files
         */

        file = new File(plugin.getDataFolder(), "data/islands");

        if(file.exists()){
            //noinspection ConstantConditions
            for(File _file : file.listFiles()) {
                System.out.println(); //Idk why, but without it files are not getting deleted
                _file.delete();
            }
        }

        /*
         * Save all islands from cache
         */

        for(Island island : islands){
            file = new File(plugin.getDataFolder(), "data/islands/" + island.getOwner().getUniqueId());

            try {
                if(!file.exists()){
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }
            }catch(Exception ex){
                ex.printStackTrace();
            }

            try(NBTOutputStream stream = new NBTOutputStream(new FileOutputStream(file))){
                stream.writeTag(((SIsland) island).getAsTag());
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }

        /*
         * Save all players from cache
         */

        for(SuperiorPlayer superiorPlayer : players){
            file = new File(plugin.getDataFolder(), "data/players/" + superiorPlayer.getUniqueId());

            try {
                if(!file.exists()){
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }
            }catch(Exception ex){
                ex.printStackTrace();
            }

            try(NBTOutputStream stream = new NBTOutputStream(new FileOutputStream(file))){
                stream.writeTag(((SSuperiorPlayer) superiorPlayer).getAsTag());
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }

        /*
         * Save grid settings
         */

        file = new File(plugin.getDataFolder(), "data/grid");

        try{
            if(!file.exists()){
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }

        try(NBTOutputStream stream = new NBTOutputStream(new FileOutputStream(file))){
            stream.writeTag(plugin.getGrid().getAsTag());
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    @SuppressWarnings({"ConstantConditions", "WeakerAccess"})
    public void loadDatabase(){
        File dataDir = new File(plugin.getDataFolder(), "data/islands");
        Tag tag;

        if(!dataDir.exists()){
            dataDir.mkdirs();
        }else{
            for(File file : dataDir.listFiles()){
                try {
                    try(NBTInputStream stream = new NBTInputStream(new FileInputStream(file))){
                        tag = stream.readTag();
                        plugin.getGrid().createIsland((CompoundTag) tag);
                    }
                }catch(Exception ex){
                    ex.printStackTrace();
                    File copyFile = new File(plugin.getDataFolder(), "data/islands-backup/" + file.getName());
                    copyFile.getParentFile().mkdirs();
                    file.renameTo(copyFile);
                }
            }
        }

        dataDir = new File(plugin.getDataFolder(), "data/players");

        if(!dataDir.exists()){
            dataDir.mkdirs();
        }else{
            for(File file : dataDir.listFiles()){
                try {
                    try(NBTInputStream stream = new NBTInputStream(new FileInputStream(file))){
                        tag = stream.readTag();
                        plugin.getPlayers().loadPlayer((CompoundTag) tag);
                    }
                }catch(Exception ex){
                    ex.printStackTrace();
                    File copyFile = new File(plugin.getDataFolder(), "data/players-backup/" + file.getName());
                    copyFile.getParentFile().mkdirs();
                    file.renameTo(copyFile);
                }
            }
        }

        File gridFile = new File(plugin.getDataFolder(), "data/grid");

        if(gridFile.exists()){
            try{
                try(NBTInputStream stream = new NBTInputStream(new FileInputStream(gridFile))){
                    tag = stream.readTag();
                    plugin.getGrid().loadGrid((CompoundTag) tag);
                }
            }catch(Exception ex){
                ex.printStackTrace();
                File copyFile = new File(plugin.getDataFolder(), "data/grid-backup");
                copyFile.getParentFile().mkdirs();
                gridFile.renameTo(copyFile);
            }
        }

    }

}
