package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.key.Key;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuValues;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.items.TemplateItem;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public final class ValuesButton extends SuperiorMenuButton<MenuValues> {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final BigInteger MAX_STACK = BigInteger.valueOf(64);

    private final Key block;

    private ValuesButton(TemplateItem buttonItem, SoundWrapper clickSound, List<String> commands,
                         String requiredPermission, SoundWrapper lackPermissionSound, Key block) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
        this.block = block;
    }

    @Nullable
    @Override
    public ItemStack getButtonItem(MenuValues superiorMenu) {
        if (buttonItem == null)
            return null;

        SuperiorPlayer inventoryViewer = superiorMenu.getInventoryViewer();
        Island island = superiorMenu.getTargetIsland();

        BigDecimal amount = new BigDecimal(block.getGlobalKey().contains("SPAWNER") ?
                island.getExactBlockCountAsBigInteger(block) : island.getBlockCountAsBigInteger(block));

        BigDecimal blockWorth = plugin.getBlockValues().getBlockWorth(block);
        BigDecimal blockLevel = plugin.getBlockValues().getBlockLevel(block);

        ItemStack itemStack = buttonItem.getBuilder()
                .replaceAll("{0}", amount + "")
                .replaceAll("{1}", StringUtils.format(blockWorth.multiply(amount)))
                .replaceAll("{2}", StringUtils.format(blockLevel.multiply(amount)))
                .replaceAll("{3}", StringUtils.fancyFormat(blockWorth.multiply(amount), inventoryViewer.getUserLocale()))
                .replaceAll("{4}", StringUtils.fancyFormat(blockLevel.multiply(amount), inventoryViewer.getUserLocale()))
                .build();

        itemStack.setAmount(BigInteger.ONE.max(MAX_STACK.min(amount.toBigInteger())).intValue());

        return itemStack;
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuValues superiorMenu,
                              InventoryClickEvent clickEvent) {
        // Do nothing.
    }

    public static class Builder extends AbstractBuilder<Builder, ValuesButton, MenuValues> {

        private final Key block;

        public Builder(Key block) {
            this.block = block;
        }

        @Override
        public ValuesButton build() {
            return new ValuesButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, block);
        }

    }

}
