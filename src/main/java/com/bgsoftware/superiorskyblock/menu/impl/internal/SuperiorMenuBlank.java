package com.bgsoftware.superiorskyblock.menu.impl.internal;

import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.menu.button.impl.DummyButton;
import com.bgsoftware.superiorskyblock.menu.pattern.impl.RegularMenuPattern;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Arrays;

public final class SuperiorMenuBlank extends SuperiorMenu<SuperiorMenuBlank> {

    private static RegularMenuPattern<SuperiorMenuBlank> menuPattern;

    private SuperiorMenuBlank(SuperiorPlayer superiorPlayer) {
        super(menuPattern, superiorPlayer);
    }

    @Override
    public boolean preButtonClick(SuperiorMenuButton<SuperiorMenuBlank> menuButton, InventoryClickEvent clickEvent) {
        return false;
    }

    @Override
    public void cloneAndOpen(ISuperiorMenu previousMenu) {
        open(previousMenu);
    }

    public static void init() {
        menuPattern = new RegularMenuPattern.Builder<SuperiorMenuBlank>()
                .setTitle("" + ChatColor.RED + ChatColor.BOLD + "ERROR")
                .setRowsSize(3)
                .setButton(13, new DummyButton.Builder<SuperiorMenuBlank>()
                        .setButtonItem(new ItemBuilder(Material.BEDROCK).withName("&cUnloaded Menu")
                                .withLore(Arrays.asList("&7There was an issue with loading the menu.",
                                        "&7Contact administrator to fix the issue.")))
                        .setClickSound(new SoundWrapper(Sound.valueOf("BLOCK_ANVIL_PLACE"), 0.2f, 0.2f)))
                .build();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu) {
        new SuperiorMenuBlank(superiorPlayer).open(previousMenu);
    }

}
