package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.menu.Menu;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.EnumHelper;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.io.MenuParserImpl;
import com.bgsoftware.superiorskyblock.core.key.types.MaterialKey;
import com.bgsoftware.superiorskyblock.core.menu.AbstractPagedMenu;
import com.bgsoftware.superiorskyblock.core.menu.MenuIdentifiers;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.CountsPagedObjectButton;
import com.bgsoftware.superiorskyblock.core.menu.view.AbstractPagedMenuView;
import com.bgsoftware.superiorskyblock.core.menu.view.args.IslandViewArgs;
import org.bukkit.Material;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class MenuCounts extends AbstractPagedMenu<MenuCounts.View, IslandViewArgs, MenuCounts.BlockCount> {

    private static final Comparator<MenuCounts.BlockCount> BLOCK_COUNT_COMPARATOR = (o1, o2) -> {
        Material firstMaterial = getMaterialFromKey(o1.getBlockKey());
        Material secondMaterial = getMaterialFromKey(o2.getBlockKey());
        int compare = plugin.getNMSAlgorithms().compareMaterials(firstMaterial, secondMaterial);
        return compare != 0 ? compare : o1.getBlockKey().compareTo(o2.getBlockKey());
    };
    private static final Function<Map.Entry<Key, BigInteger>, MenuCounts.BlockCount> BLOCK_COUNT_MAPPER =
            entry -> new MenuCounts.BlockCount(entry.getKey(), entry.getValue());

    private MenuCounts(MenuParseResult<View> parseResult) {
        super(MenuIdentifiers.MENU_COUNTS, parseResult, false);
    }

    @Override
    protected View createViewInternal(SuperiorPlayer superiorPlayer, IslandViewArgs args,
                                      @Nullable MenuView<?, ?> previousMenuView) {
        return new View(superiorPlayer, previousMenuView, this, args);
    }

    public void refreshViews(Island island) {
        refreshViews(view -> view.island.equals(island));
    }

    @Nullable
    public static MenuCounts createInstance() {
        MenuParseResult<View> menuParseResult = MenuParserImpl.getInstance().loadMenu("counts.yml",
                null, new CountsPagedObjectButton.Builder());
        return menuParseResult == null ? null : new MenuCounts(menuParseResult);
    }

    public static class View extends AbstractPagedMenuView<View, IslandViewArgs, MenuCounts.BlockCount> {

        private final Island island;

        View(SuperiorPlayer inventoryViewer, @Nullable MenuView<?, ?> previousMenuView,
             Menu<View, IslandViewArgs> menu, IslandViewArgs args) {
            super(inventoryViewer, previousMenuView, menu);
            this.island = args.getIsland();
        }

        @Override
        protected List<MenuCounts.BlockCount> requestObjects() {
            return new SequentialListBuilder<MenuCounts.BlockCount>()
                    .sorted(BLOCK_COUNT_COMPARATOR)
                    .build(island.getBlockCountsAsBigInteger().entrySet(), BLOCK_COUNT_MAPPER);
        }

    }

    private static Material getMaterialFromKey(Key key) {
        if (key instanceof MaterialKey)
            return ((MaterialKey) key).getMaterial();

        return EnumHelper.getEnum(Material.class, key.getGlobalKey(), "BEDROCK");
    }

    public static class BlockCount {

        private final Key blockKey;
        private final BigInteger amount;

        public BlockCount(Key blockKey, BigInteger amount) {
            this.blockKey = blockKey;
            this.amount = amount;
        }

        public Key getBlockKey() {
            return blockKey;
        }

        public BigInteger getAmount() {
            return amount;
        }

    }

}
