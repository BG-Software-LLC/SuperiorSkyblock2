package com.bgsoftware.superiorskyblock.commands.command;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.gui.GUIInventory;
import com.bgsoftware.superiorskyblock.utils.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdDisband implements ICommand {

    private GUIInventory confirmPage;

    public CmdDisband(){
        Inventory confirmPage = Bukkit.createInventory(null, InventoryType.HOPPER, ChatColor.BOLD + "      Confirm Disband");
        confirmPage.setItem(1, new ItemBuilder(Materials.getGlass(DyeColor.LIME)).withName("&aConfirm").withLore("&8Are you sure?").build());
        confirmPage.setItem(3, new ItemBuilder(Materials.getGlass(DyeColor.RED)).withName("&4Cancel").build());
        this.confirmPage = GUIInventory.from(GUIInventory.CONFIRM_PAGE_IDENTIFIER, confirmPage);
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("disband");
    }

    @Override
    public String getPermission() {
        return "superior.island.disband";
    }

    @Override
    public String getUsage() {
        return "island disband";
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
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(sender);
        Island island = superiorPlayer.getIsland();

        if(island == null){
            Locale.INVALID_ISLAND.send(superiorPlayer);
            return;
        }

        if(!superiorPlayer.hasPermission(IslandPermission.DISBAND_ISLAND)){
            Locale.NO_DISBAND_PERMISSION.send(superiorPlayer, island.getRequiredRole(IslandPermission.DISBAND_ISLAND));
            return;
        }

        if (!superiorPlayer.hasDisbands()) {
            Locale.NO_MORE_DISBANDS.send(superiorPlayer);
            return;
        }

        confirmPage.openInventory(superiorPlayer, false);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
