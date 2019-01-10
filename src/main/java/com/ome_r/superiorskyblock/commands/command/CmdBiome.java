package com.ome_r.superiorskyblock.commands.command;

import com.ome_r.superiorskyblock.Locale;
import com.ome_r.superiorskyblock.SuperiorSkyblock;
import com.ome_r.superiorskyblock.commands.ICommand;
import com.ome_r.superiorskyblock.island.Island;
import com.ome_r.superiorskyblock.island.IslandPermission;
import com.ome_r.superiorskyblock.wrappers.WrappedPlayer;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CmdBiome implements ICommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("setbiome", "biome");
    }

    @Override
    public String getPermission() {
        return "superior.island.biome";
    }

    @Override
    public String getUsage() {
        return "island biome";
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public int getMaxArgs() {
        return 1;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        WrappedPlayer wrappedPlayer = WrappedPlayer.of(sender);
        Island island = wrappedPlayer.getIsland();

        if(island == null){
            Locale.INVALID_ISLAND.send(wrappedPlayer);
            return;
        }

        if(!wrappedPlayer.hasPermission(IslandPermission.SET_BIOME)){
            Locale.NO_SET_BIOME_PERMISSION.send(wrappedPlayer, island.getRequiredRole(IslandPermission.SET_BIOME));
            return;
        }

        plugin.getPanel().openBiomesPanel(wrappedPlayer);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        WrappedPlayer wrappedPlayer = WrappedPlayer.of(sender);
        Island island = wrappedPlayer.getIsland();

        if(args.length == 2 && island != null && wrappedPlayer.hasPermission(IslandPermission.SET_BIOME)){
            List<String> list = new ArrayList<>();

            for(Biome biome : Biome.values()){
                if(biome.name().toLowerCase().startsWith(args[1].toLowerCase()))
                    list.add(biome.name().toLowerCase());
            }

            return list;
        }

        return new ArrayList<>();
    }

    private String getBiomeValues(){
        StringBuilder stringBuilder = new StringBuilder();

        for(Biome biome : Biome.values())
            stringBuilder.append(", ").append(biome.name().toLowerCase());

        return stringBuilder.substring(2);
    }

}
