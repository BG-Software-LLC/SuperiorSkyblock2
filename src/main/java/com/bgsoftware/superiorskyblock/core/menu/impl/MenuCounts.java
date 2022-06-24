package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.io.MenuParser;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.core.menu.PagedSuperiorMenu;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.menu.CountsPagedObjectButton;
import com.bgsoftware.superiorskyblock.core.menu.pattern.impl.PagedMenuPattern;
import org.bukkit.Material;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class MenuCounts extends PagedSuperiorMenu<MenuCounts, MenuCounts.BlockCount> {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final Comparator<BlockCount> BLOCK_COUNT_COMPARATOR = (o1, o2) -> {
        Material firstMaterial = getSafeMaterial(o1.getBlockKey().getGlobalKey());
        Material secondMaterial = getSafeMaterial(o2.getBlockKey().getGlobalKey());
        int compare = plugin.getNMSAlgorithms().compareMaterials(firstMaterial, secondMaterial);
        return compare != 0 ? compare : o1.getBlockKey().compareTo(o2.getBlockKey());
    };
    private static final Function<Map.Entry<Key, BigInteger>, BlockCount> BLOCK_COUNT_MAPPER =
            entry -> new BlockCount(entry.getKey(), entry.getValue());

    private static PagedMenuPattern<MenuCounts, BlockCount> menuPattern;

    private final Island island;

    private MenuCounts(SuperiorPlayer superiorPlayer, Island island) {
        super(menuPattern, superiorPlayer);
        this.island = island;
    }

    @Override
    public void cloneAndOpen(ISuperiorMenu previousMenu) {
        openInventory(inventoryViewer, previousMenu, island);
    }

    @Override
    protected List<BlockCount> requestObjects() {
        return new SequentialListBuilder<BlockCount>()
                .sorted(BLOCK_COUNT_COMPARATOR)
                .build(island.getBlockCountsAsBigInteger().entrySet(), BLOCK_COUNT_MAPPER);
    }

    public static void init() {
        menuPattern = null;

        PagedMenuPattern.Builder<MenuCounts, BlockCount> patternBuilder = new PagedMenuPattern.Builder<>();

        MenuParseResult menuLoadResult = MenuParser.loadMenu(patternBuilder,
                "counts.yml", null);

        if (menuLoadResult == null)
            return;

        MenuPatternSlots menuPatternSlots = menuLoadResult.getPatternSlots();
        CommentedConfiguration cfg = menuLoadResult.getConfig();

        menuPattern = patternBuilder
                .setPreviousPageSlots(getSlots(cfg, "previous-page", menuPatternSlots))
                .setCurrentPageSlots(getSlots(cfg, "current-page", menuPatternSlots))
                .setNextPageSlots(getSlots(cfg, "next-page", menuPatternSlots))
                .setPagedObjectSlots(getSlots(cfg, "slots", menuPatternSlots), new CountsPagedObjectButton.Builder())
                .build();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu, Island island) {
        new MenuCounts(superiorPlayer, island).open(previousMenu);
    }

    public static void refreshMenus(Island island) {
        refreshMenus(MenuCounts.class, superiorMenu -> superiorMenu.island.equals(island));
    }

    private static Material getSafeMaterial(String value) {
        try {
            return Material.valueOf(value);
        } catch (Exception ex) {
            return Material.BEDROCK;
        }
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
