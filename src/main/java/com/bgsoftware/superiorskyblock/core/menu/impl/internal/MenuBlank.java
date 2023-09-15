package com.bgsoftware.superiorskyblock.core.menu.impl.internal;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSoundImpl;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.AbstractMenu;
import com.bgsoftware.superiorskyblock.core.menu.MenuIdentifiers;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.DummyButton;
import com.bgsoftware.superiorskyblock.core.menu.layout.RegularMenuLayoutImpl;
import com.bgsoftware.superiorskyblock.core.menu.view.BaseMenuView;
import com.bgsoftware.superiorskyblock.core.menu.view.args.EmptyViewArgs;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;

import java.util.Arrays;

public class MenuBlank extends AbstractMenu<BaseMenuView, EmptyViewArgs> {

    private MenuBlank(MenuParseResult<BaseMenuView> parseResult) {
        super(MenuIdentifiers.MENU_BLANK, parseResult);
    }

    @Override
    protected BaseMenuView createViewInternal(SuperiorPlayer superiorPlayer, EmptyViewArgs unused,
                                              @Nullable MenuView<?, ?> previousMenuView) {
        return new BaseMenuView(superiorPlayer, previousMenuView, this);
    }

    public static MenuBlank createInstance() {
        Sound sound;

        try {
            sound = Sound.valueOf("BLOCK_ANVIL_PLACE");
        } catch (Throwable error) {
            sound = Sound.ANVIL_LAND;
        }

        RegularMenuLayoutImpl.Builder<BaseMenuView> patternBuilder = RegularMenuLayoutImpl.newBuilder();

        patternBuilder.setTitle("" + ChatColor.RED + ChatColor.BOLD + "ERROR");
        patternBuilder.setRowsCount(3);

        DummyButton.Builder<BaseMenuView> button = new DummyButton.Builder<>();
        button.setButtonItem(new TemplateItem(new ItemBuilder(Material.BEDROCK)
                .withName("&cUnloaded Menu")
                .withLore(Arrays.asList(
                        "&7There was an issue with loading the menu.",
                        "&7Contact administrator to fix the issue.")
                )));
        button.setClickSound(new GameSoundImpl(sound, 0.2f, 0.2f));
        patternBuilder.setButton(13, button.build());

        return new MenuBlank(new MenuParseResult<>(patternBuilder));
    }

}
