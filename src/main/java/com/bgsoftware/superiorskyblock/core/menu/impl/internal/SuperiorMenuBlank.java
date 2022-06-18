package com.bgsoftware.superiorskyblock.core.menu.impl.internal;

import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.DummyButton;
import com.bgsoftware.superiorskyblock.core.menu.pattern.impl.RegularMenuPattern;
import com.bgsoftware.superiorskyblock.core.menu.SuperiorMenu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Arrays;

public class SuperiorMenuBlank extends SuperiorMenu<SuperiorMenuBlank> {

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
        Sound sound;

        try {
            sound = Sound.valueOf("BLOCK_ANVIL_PLACE");
        } catch (Throwable error) {
            sound = Sound.ANVIL_LAND;
        }

        menuPattern = new RegularMenuPattern.Builder<SuperiorMenuBlank>()
                .setTitle("" + ChatColor.RED + ChatColor.BOLD + "ERROR")
                .setRowsSize(3)
                .setButton(13, new DummyButton.Builder<SuperiorMenuBlank>()
                        .setButtonItem(new TemplateItem(new ItemBuilder(Material.BEDROCK).withName("&cUnloaded Menu")
                                .withLore(Arrays.asList("&7There was an issue with loading the menu.",
                                        "&7Contact administrator to fix the issue."))))
                        .setClickSound(new GameSound(sound, 0.2f, 0.2f)))
                .build();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu) {
        new SuperiorMenuBlank(superiorPlayer).open(previousMenu);
    }

}
